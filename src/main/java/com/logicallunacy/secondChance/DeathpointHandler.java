package com.logicallunacy.secondChance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.roboboy.bukkitutil.ExpUtil;
import com.roboboy.bukkitutil.ItemStackUtils;

class DeathpointHandler implements Listener {
	
	private static final DeathpointHandler instance = new DeathpointHandler();
	
	private final Deque<DeathPoint> deathpoints = new LinkedList<>();
	private SecondChance plugin;
	private BukkitTask particleTask;
	
	private DeathpointHandler() {}
	
	public static DeathpointHandler getInstance() {
		return instance;
	}
	
	public void init(SecondChance plugin) {
		this.plugin = plugin;
		deathpoints.addAll(plugin.saveHandler.getAll());
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
		
		//Destroy old deathpoint
		Optional<DeathPoint> old = deathpoints.stream()
				.filter(point -> point.getOwnerUUID().equals(player.getUniqueId()))
				.findFirst();
		if (old.isPresent()) old.get().destroy();
		
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
		if (!event.getKeepLevel()) {
			exp = ExpUtil.calculateXpFromLevel(player.getLevel())
					+ ExpUtil.calculateXpFromProgress(player.getLevel(), player.getExp());
			event.setDroppedExp(0);
		}
		
		//Create if not empty
		if (itemsToHold == null && exp == 0) return;
		DeathPoint newPoint = new DeathPoint(UUID.randomUUID(), player.getUniqueId(), location, itemsToHold, exp);
		newPoint.spawnHitbox();
		deathpoints.add(newPoint);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event) {
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
		
		Optional<DeathPoint> deathPoint = deathpoints.stream()
				.filter(point -> point.isHitbox(event.getEntity()))
				.findAny();
		if (!deathPoint.isPresent()) return;
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerClickArmorStand(PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof ArmorStand)) return;
		
		Optional<DeathPoint> found = deathpoints.stream()
				.filter((point) -> point.isHitbox(event.getRightClicked()))
				.findAny();
		if (!found.isPresent()) return;
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		DeathPoint deathpoint = found.get();
		if (player.getUniqueId().equals(deathpoint.getOwnerUUID())) {
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
		if (!(holder instanceof DeathPoint)) return;
		DeathPoint deathpoint = (DeathPoint) holder;
		
		if (deathpoint.isValid()) {
			deathpoint.destroy();
			remove(deathpoint);
		}
		
	}
	
	public void remove(DeathPoint deathpoint) {
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

