package com.logicallunacy.secondChance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.roboboy.bukkitutil.ExpUtil;
import com.roboboy.bukkitutil.ItemStackUtils;

class DeathpointHandler implements Listener {
	
	private final Deque<Deathpoint> deathpoints = new LinkedList<>();
	private final SecondChance plugin;
	private BukkitTask particleTask;
	
	
	public DeathpointHandler(SecondChance plugin) {
		this.plugin = plugin;
	}
	
	public void init() {
		plugin.saveHandler.getAll().stream()
				.sorted((p1, p2) -> p1.getCreationInstant().compareTo(p2.getCreationInstant()))
				.forEachOrdered(deathpoints::add);
		deathpoints.forEach(deathPoint -> deathPoint.spawnHitbox());
		
		particleTask = new BukkitRunnable() { @Override public void run() {
			deathpoints.forEach(point -> point.runParticles());
		}}.runTaskTimer(plugin, 0L, 20L);
	}
	
	public void deinit() {
		particleTask.cancel();

		plugin.saveHandler.putAll(deathpoints);
		saveDeathpoints();
		deathpoints.forEach(deathPoint -> deathPoint.despawnHitbox());
		deathpoints.clear();
	}
	
	public void panic() {
		deathpoints.forEach(point -> point.destroy());
		deathpoints.clear();
	}
	
	public void saveDeathpoints() {
		try {
			plugin.saveHandler.save();
		} catch (IOException e) {
			//TODO: Can anything else be done?
			plugin.getLogger().severe("Could not save deathpoints!");
			e.printStackTrace();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldSave(WorldSaveEvent event) {
		deathpoints.stream()
				.filter(point -> point.getLocation().getWorld().equals(event.getWorld()))
				.forEach(plugin.saveHandler::put);
		saveDeathpoints();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setMetadata("lastSafePosition", new FixedMetadataValue(plugin, player.getLocation().add(0, 1, 0)));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity().getPlayer();
		if (player == null) return;
		
		//Destroy old deathpoint(s)
		//TODO: Config option
		deathpoints.stream()
				.filter(point -> point.getOwnerUniqueId().equals(player.getUniqueId()))
				.forEach(Deathpoint::destroy);
		
		//Get location
		Location location = findLocation(player);
		
		//Get items, if applicable
		ItemStack[] itemsToHold = null;
		if (!event.getKeepInventory()) {
			itemsToHold = player.getInventory().getContents();
			event.getDrops().removeAll(Arrays.asList(itemsToHold));
			if (!Arrays.stream(itemsToHold).anyMatch(ItemStackUtils::isValid)) itemsToHold = null;
		}
		
		//Get exp, if applicable
		int exp = 0;
		if (!event.getKeepLevel()) { //FIXME: Still goes when gamerule keepInventory is true?
			exp = ExpUtil.calculateXpFromLevel(player.getLevel())
					+ ExpUtil.calculateXpFromProgress(player.getLevel(), player.getExp());
			event.setDroppedExp(0);
		}
		
		//Create if not empty
		if (itemsToHold == null && exp == 0) return;
		Deathpoint newPoint = new Deathpoint(player, location, itemsToHold, exp);
		newPoint.spawnHitbox();
		deathpoints.add(newPoint);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void setLastSafePosition(PlayerMoveEvent event) {
		if (!((Entity)event.getPlayer()).isOnGround()) return;
		
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		//Check bottom block
		if (loc.getBlock().getType().isSolid()) return;
		if (loc.getBlock().isLiquid()) return;
		//Check top block
		loc.add(0, 1, 0);
		if (loc.getBlock().getType().isSolid()) return;
		if (loc.getBlock().isLiquid()) return;
		
		player.setMetadata("lastSafePosition", new FixedMetadataValue(plugin, loc));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkUnload(ChunkUnloadEvent event) {
		deathpoints.stream()
			.filter((point) -> event.getChunk().equals(point.getLocation().getChunk()))
			.forEach((point) -> point.despawnHitbox());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		deathpoints.stream()
				.filter((point) -> event.getChunk().equals(point.getLocation().getChunk()))
				.forEach((point) -> point.spawnHitbox());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onArmorStandDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof ArmorStand)) return;
		
		Optional<MetadataValue> found = event.getEntity().getMetadata("deathpoint")
				.stream().filter(meta -> meta.getOwningPlugin() == plugin).findAny();
		if (!found.isPresent()) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerClickArmorStand(PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof ArmorStand)) return;
		
		Optional<MetadataValue> found = event.getRightClicked().getMetadata("deathpoint")
				.stream().filter(meta -> meta.getOwningPlugin() == plugin).findAny();
		if (!found.isPresent()) return;
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		Deathpoint deathpoint = (Deathpoint) found.get().value();
		if (player.getUniqueId().equals(deathpoint.getOwnerUniqueId())) {
			deathpoint.dropExperience();
			if (deathpoint.isEmpty()) {
				deathpoint.destroy();
				remove(deathpoint);
			}
			else player.openInventory(deathpoint.getInventory());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if (!(holder instanceof Deathpoint)) return;
		Deathpoint deathpoint = (Deathpoint) holder;
		
		if (deathpoint.isValid()) {
			deathpoint.destroy();
			remove(deathpoint);
		}
		
	}
	
	public void remove(Deathpoint deathpoint) {
		deathpoints.remove(deathpoint);
		plugin.saveHandler.remove(deathpoint);
	}
	
	private Location findLocation(Player player) {
		Location loc = (Location) player.getMetadata("lastSafePosition").stream()
				.filter(value -> value.getOwningPlugin() == plugin)
				.findFirst().get().value();
		return loc.getBlock().getLocation().add(0.5, 0, 0.5);
	}
	
	/*@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(final PlayerRespawnEvent e) {
		new BukkitRunnable() { @Override public void run() {
			playerSpawnEffect(e.getPlayer());
		}}.runTaskLater(plugin, 0L);
	}*/
	
	//TODO: New plugin
	/*private void playerSpawnEffect(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 9, true, false), true);
	}*/
	
}

