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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
	private ConfigOptions options;
	
	
	public DeathpointHandler(SecondChance plugin) {
		this.plugin = plugin;
	}
	
	public void init(ConfigOptions options) {
		this.options = options;
		Bukkit.getWorlds().forEach(this::initWorld);
		Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(this::setSafePosition), 
				options.locationCheckDelay, options.locationCheckDelay);
	}
	
	public void deinit() {
		//Stop particle timers
		Bukkit.getScheduler().cancelTasks(plugin);
		//Despawn all hitboxes
		deathpoints.values().stream()
				.flatMap(Collection::stream)
				.forEach(Deathpoint::despawnHitbox);
		//Clear members
		deathpoints.clear();
		options = null;
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
	
	//TODO: Too high?
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (player == null) return;
		
		//KeepInventory seems to override all event settings
		if (player.getWorld().getGameRuleValue("keepInventory").equals("true")) return;
		
		//Destroy old deathpoint(s)
		destroyOldDeathpoints(player);
		
		//Get location
		Location location = getSafePosition(player);
		
		//Get items, if applicable
		ItemStack[] itemsToHold = null;
		if (options.holdItems && !event.getKeepInventory()) {
			//Store all inventory items that have been dropped, and remove from drops
			itemsToHold = player.getInventory().getContents();
			for (int i = 0; i < itemsToHold.length; i++) {
				boolean wasRemoved = event.getDrops().remove(itemsToHold[i]);
				if (!wasRemoved) itemsToHold[i] = null;
			}
			//If no items were found, return null
			if (Arrays.stream(itemsToHold).noneMatch(ItemStackUtils::isValid)) itemsToHold = null;
		}
		
		//Get exp, if applicable
		int exp = 0;
		if (options.holdExp && !event.getKeepLevel()) {
			exp = ExpUtil.calculateXpFromLevel(player.getLevel())
					+ ExpUtil.calculateXpFromProgress(player.getLevel(), player.getExp());
			event.setDroppedExp(0);
		}
		
		//Create if not empty
		if (itemsToHold == null && exp == 0) return;
		Deathpoint newPoint = new Deathpoint(player, location, itemsToHold, exp);
		options.deathMessage.sendMessage(player, newPoint);
		newPoint.spawnHitbox();
		deathpoints.get(location.getWorld().getName()).add(newPoint);
		plugin.getSaveHandler(location.getWorld()).put(newPoint);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkUnload(ChunkUnloadEvent event) {
		deathpoints.get(event.getWorld().getName()).stream()
			.filter((point) -> event.getChunk().equals(point.getLocation().getChunk()))
			.forEach(Deathpoint::despawnHitbox);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		//Remove residual hitboxes
		Arrays.stream(event.getChunk().getEntities())
				.filter(e -> e.getType() == EntityType.ARMOR_STAND)
				.map(e -> (ArmorStand) e)
				.filter(Deathpoint::armorstandIsHitbox)
				.forEach(Entity::remove);
		
		//Spawn deathpoint hitboxes
		deathpoints.get(event.getWorld().getName()).stream()
				.filter((point) -> event.getChunk().equals(point.getLocation().getChunk()))
				.forEach(Deathpoint::spawnHitbox);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onArmorStandDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.ARMOR_STAND) return;
		
		Optional<MetadataValue> found = event.getEntity().getMetadata("deathpoint").stream()
				.filter(meta -> meta.getOwningPlugin() == plugin)
				.findAny();
		if (!found.isPresent()) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onArmorStandPunched(EntityDamageByEntityEvent event) {
		if (!options.breakOnHit) return;
		if (event.getEntityType() != EntityType.ARMOR_STAND) return;
		if (event.getDamager().getType() != EntityType.PLAYER) return;
		
		Optional<Deathpoint> found = event.getEntity().getMetadata("deathpoint").stream()
				.filter(meta -> meta.getOwningPlugin() == plugin)
				.map(meta -> (Deathpoint) meta.value())
				.findAny();
		if (!found.isPresent()) return;
		Deathpoint deathpoint = found.get();
		Player punched = (Player) event.getDamager();
		
		if (options.isProtected && !punched.getUniqueId().equals(deathpoint.getOwnerUniqueId())) return;
		deathpoint.dropItems();
		deathpoint.dropExperience();
		deathpoint.destroy();
		remove(deathpoint);	
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerClickArmorStand(PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof ArmorStand)) return;
		
		Optional<Deathpoint> found = event.getRightClicked().getMetadata("deathpoint").stream()
				.filter(meta -> meta.getOwningPlugin() == plugin)
				.map(meta -> (Deathpoint) meta.value())
				.findAny();
		if (!found.isPresent()) return;
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		Deathpoint deathpoint = (Deathpoint) found.get();
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
		
		if (deathpoint.isInvalid()) return;
		deathpoint.dropItems();
		deathpoint.destroy();
		remove(deathpoint);
	}
	
	private void initWorld(World world) {
		//Remove residual hitboxes in world
		world.getEntities().stream()
				.filter(e -> e.getType() == EntityType.ARMOR_STAND)
				.map(e -> (ArmorStand) e)
				.filter(Deathpoint::armorstandIsHitbox)
				.forEach(Entity::remove);
		
		//Initiate all deathpoints in world
		Deque<Deathpoint> worldDeathpoints = new LinkedList<>();
		plugin.getSaveHandler(world).stream()
				.sorted((p1, p2) -> p1.getCreationInstant().compareTo(p2.getCreationInstant()))
				.forEachOrdered(worldDeathpoints::add);
		worldDeathpoints.forEach(Deathpoint::spawnHitbox);
		deathpoints.put(world.getName(), worldDeathpoints);
		
		//Add initial "safe" positions to all online players in world
		world.getPlayers().stream()
				.forEach(player -> player.setMetadata("lastSafePosition", new FixedMetadataValue(plugin, player.getLocation().add(0, 1, 0))));
		
		//Start particle timer for world
		Bukkit.getScheduler().runTaskTimer(plugin, () -> worldDeathpoints.forEach(this::runParticles), 0, options.particleDelay);
	}
	
	private void runParticles(Deathpoint deathpoint) {
		Location location = deathpoint.getLocation();
		if (!location.getChunk().isLoaded()) return;
		
		Player owner = Bukkit.getPlayer(deathpoint.getOwnerUniqueId());
		options.particlePrimary.run(location, owner);
		options.particleSecondary.run(location, owner);
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
			options.forgetMessage.sendMessage(player, deathpoint);
		}
	}
	
	private void remove(Deathpoint deathpoint) {
		deathpoints.get(deathpoint.getWorld().getName()).remove(deathpoint);
		plugin.getSaveHandler(deathpoint.getWorld()).remove(deathpoint);
	}
	
	private void setSafePosition(Player player) {
		Location safeLoc = Util.entityLocationIsSafe(player);
		if (safeLoc == null) return;
		player.setMetadata("lastSafePosition", new FixedMetadataValue(plugin, safeLoc));
	}
	
	private Location getSafePosition(Player player) {
		Location loc = (Location) player.getMetadata("lastSafePosition").stream()
				.filter(value -> value.getOwningPlugin() == plugin)
				.findFirst().get().value();
		return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 1, loc.getBlockZ() + 0.5);
	}
	
}

