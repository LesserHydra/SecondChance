package com.lesserhydra.secondchance;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import com.lesserhydra.bukkitutil.ExpUtil;
import com.lesserhydra.bukkitutil.ItemStackUtils;

class DeathpointHandler implements Listener {
	
	private final Map<String, Deque<Deathpoint>> deathpoints = new HashMap<>();
	private final SecondChance plugin;
	private final ConfigOptions options;
	
	
	public DeathpointHandler(SecondChance plugin, ConfigOptions options) {
		this.plugin = plugin;
		this.options = options;
	}
	
	public void deinit() {
		//Stop particle timers
		Bukkit.getScheduler().cancelTasks(plugin);
		//Despawn all hitboxes
		deathpoints.values().stream()
				.flatMap(Collection::stream)
				.forEach(Deathpoint::despawnHitbox);
	}
	
	public void initWorld(World world) {
		Deque<Deathpoint> worldDeathpoints = new LinkedList<>();
		plugin.getSaveHandler(world).stream()
				.sorted((p1, p2) -> p1.getCreationInstant().compareTo(p2.getCreationInstant()))
				.forEachOrdered(worldDeathpoints::add);
		worldDeathpoints.forEach(deathPoint -> deathPoint.spawnHitbox());
		deathpoints.put(world.getName(), worldDeathpoints);
		
		Bukkit.getScheduler().runTaskTimer(plugin, () -> worldDeathpoints.forEach(this::runParticles), 0, options.particleDelay);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldInit(WorldInitEvent event) {
		initWorld(event.getWorld());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldSave(WorldSaveEvent event) {
		SaveHandler saveHandler = plugin.getSaveHandler(event.getWorld());
		saveHandler.putAll(deathpoints.get(event.getWorld().getName()));
		saveHandler.save();
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
		destroyOldDeathpoints(player);
		
		//Get location
		Location location = findLocation(player);
		
		//Get items, if applicable
		ItemStack[] itemsToHold = null;
		if (options.holdItems && !event.getKeepInventory()) {
			itemsToHold = player.getInventory().getContents();
			event.getDrops().removeAll(Arrays.asList(itemsToHold));
			if (!Arrays.stream(itemsToHold).anyMatch(ItemStackUtils::isValid)) itemsToHold = null;
		}
		
		//Get exp, if applicable
		int exp = 0;
		if (options.holdExp && !event.getKeepLevel()) { //FIXME: Still goes when gamerule keepInventory is true?
			exp = ExpUtil.calculateXpFromLevel(player.getLevel())
					+ ExpUtil.calculateXpFromProgress(player.getLevel(), player.getExp());
			event.setDroppedExp(0);
		}
		
		//Create if not empty
		if (itemsToHold == null && exp == 0) return;
		Deathpoint newPoint = new Deathpoint(player, location, itemsToHold, exp);
		newPoint.spawnHitbox();
		deathpoints.get(location.getWorld().getName()).add(newPoint);
		plugin.getSaveHandler(location.getWorld()).put(newPoint);
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
		deathpoints.get(event.getWorld().getName()).stream()
			.filter((point) -> event.getChunk().equals(point.getLocation().getChunk()))
			.forEach(Deathpoint::spawnHitbox);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		deathpoints.get(event.getWorld().getName()).stream()
				.filter((point) -> event.getChunk().equals(point.getLocation().getChunk()))
				.forEach(Deathpoint::spawnHitbox);
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
		if (options.isProtected && !player.getUniqueId().equals(deathpoint.getOwnerUniqueId())) return;
		
		deathpoint.dropExperience();
		if (deathpoint.isEmpty()) {
			deathpoint.destroy();
			remove(deathpoint);
		}
		else player.openInventory(deathpoint.getInventory());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if (!(holder instanceof Deathpoint)) return;
		Deathpoint deathpoint = (Deathpoint) holder;
		
		if (!deathpoint.isInvalid()) {
			deathpoint.dropItems();
			deathpoint.destroy();
			remove(deathpoint);
		}
	}
	
	private void runParticles(Deathpoint deathpoint) {
		Location location = deathpoint.getLocation();
		location.getWorld().spawnParticle(options.particlePrimary, location, options.particlePrimaryCount,
				options.particlePrimarySpread, options.particlePrimarySpread, options.particlePrimarySpread,
				options.particlePrimarySpeed);
		location.getWorld().spawnParticle(options.particleSecondary, location, options.particleSecondaryCount,
				options.particleSecondarySpread, options.particleSecondarySpread, options.particleSecondarySpread,
				options.particleSecondarySpeed);
	}
	
	private void destroyOldDeathpoints(Player player) {
		if (options.maxPerPlayer <= 0) return;
		
		Deque<Deathpoint> playerDeathpoints = deathpoints.values().stream()
				.flatMap(Collection::stream)
				.filter(point -> point.getOwnerUniqueId().equals(player.getUniqueId()))
				.collect(Collectors.toCollection(LinkedList::new));
		
		while (playerDeathpoints.size() >= options.maxPerPlayer) {
			Deathpoint deathpoint = playerDeathpoints.remove();
			if (options.dropItemsOnForget) deathpoint.dropItems();
			if (options.dropExpOnForget) deathpoint.dropExperience();
			deathpoint.destroy();
			remove(deathpoint);
		}
	}
	
	private void remove(Deathpoint deathpoint) {
		deathpoints.get(deathpoint.getWorld().getName()).remove(deathpoint);
		plugin.getSaveHandler(deathpoint.getWorld()).remove(deathpoint);
	}
	
	private Location findLocation(Player player) {
		Location loc = (Location) player.getMetadata("lastSafePosition").stream()
				.filter(value -> value.getOwningPlugin() == plugin)
				.findFirst().get().value();
		return loc.getBlock().getLocation().add(0.5, 0, 0.5);
	}
	
}

