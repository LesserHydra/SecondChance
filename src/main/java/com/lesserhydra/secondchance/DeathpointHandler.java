package com.lesserhydra.secondchance;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import com.lesserhydra.bukkitutil.ExpUtil;
import com.lesserhydra.bukkitutil.ItemStackUtils;

class DeathpointHandler implements Listener {
	
	private final Map<UUID, WorldHandler> worlds = new HashMap<>();
	private final SecondChance plugin;
	
	private ConfigOptions options;
	private BukkitTask safeLocationTask;
	
	
	public DeathpointHandler(SecondChance plugin) {
		this.plugin = plugin;
	}
	
	public void init(ConfigOptions options) {
		this.options = options;
		Bukkit.getWorlds().forEach(this::initWorld);
		safeLocationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(this::setSafePosition), 
				options.locationCheckDelay, options.locationCheckDelay);
	}
	
	public void deinit() {
		//Stop safe location task
		safeLocationTask.cancel();
		//Deinit all remaining worlds
		worlds.values().stream()
				.forEach(WorldHandler::deinit);
		//Clear members
		worlds.clear();
		options = null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldLoad(WorldLoadEvent event) {
		initWorld(event.getWorld());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldUnload(WorldUnloadEvent event) {
		WorldHandler handler = worlds.remove(event.getWorld().getUID());
		if (handler == null) return;
		
		handler.deinit();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldSave(WorldSaveEvent event) {
		WorldHandler handler = worlds.get(event.getWorld().getUID());
		if (handler == null) return;
		
		handler.onWorldSave();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		WorldHandler handler = worlds.get(event.getWorld().getUID());
		if (handler == null) return;
		
		handler.onChunkLoad(event.getChunk());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkUnload(ChunkUnloadEvent event) {
		WorldHandler handler = worlds.get(event.getWorld().getUID());
		if (handler == null) return;
		
		handler.onChunkUnload(event.getChunk());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setMetadata("lastSafePosition", new FixedMetadataValue(plugin, player.getLocation().add(0, 1, 0)));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (player == null) return;
		
		//KeepInventory seems to override all event settings (SPIGOT-2222)
		if (player.getWorld().getGameRuleValue("keepInventory").equals("true")) return;
		
		//Destroy old deathpoint(s)
		destroyOldDeathpoints(player);
		
		//Get location
		Location location = getSafePosition(player);
		WorldHandler handler = worlds.get(location.getWorld().getUID());
		if (handler == null) return;
		
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
		Deathpoint deathpoint = new Deathpoint(player, location, itemsToHold, exp);
		options.creationSound.run(location, player);
		options.deathMessage.sendMessage(player, deathpoint);
		handler.addDeathpoint(deathpoint);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onArmorStandDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.ARMOR_STAND) return;
		
		Deathpoint deathpoint = findDeathpointFromHitbox((ArmorStand) event.getEntity());
		if (deathpoint == null) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onArmorStandPunched(EntityDamageByEntityEvent event) {
		if (!options.breakOnHit) return;
		if (event.getEntityType() != EntityType.ARMOR_STAND) return;
		if (event.getDamager().getType() != EntityType.PLAYER) return;
		
		Deathpoint deathpoint = findDeathpointFromHitbox((ArmorStand) event.getEntity());
		if (deathpoint == null) return;
		Player punched = (Player) event.getDamager();
		
		if (options.isProtected && !punched.getUniqueId().equals(deathpoint.getOwnerUniqueId())) return;
		options.breakSound.run(deathpoint.getLocation(), punched);
		deathpoint.dropItems();
		deathpoint.dropExperience();
		worlds.get(deathpoint.getWorld().getUID()).destroyDeathpoint(deathpoint);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerClickArmorStand(PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof ArmorStand)) return;
		
		Deathpoint deathpoint = findDeathpointFromHitbox((ArmorStand) event.getRightClicked());
		if (deathpoint == null) return;
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		if (options.isProtected && !player.getUniqueId().equals(deathpoint.getOwnerUniqueId())) return;
		
		deathpoint.dropExperience();
		if (deathpoint.isEmpty()) {
			options.closeSound.run(deathpoint.getLocation(), player);
			worlds.get(deathpoint.getWorld().getUID()).destroyDeathpoint(deathpoint);
		}
		else {
			options.openSound.run(deathpoint.getLocation(), player);
			player.openInventory(deathpoint.getInventory());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if (!(holder instanceof Deathpoint)) return;
		Deathpoint deathpoint = (Deathpoint) holder;
		
		if (deathpoint.isInvalid()) return;
		if (event.getPlayer() instanceof Player) options.closeSound.run(deathpoint.getLocation(), (Player) event.getPlayer());
		deathpoint.dropItems();
		worlds.get(deathpoint.getWorld().getUID()).destroyDeathpoint(deathpoint);
	}
	
	private void initWorld(World world) {
		WorldHandler newWorldHandler = new WorldHandler(plugin, options, world);
		newWorldHandler.init();
		worlds.put(world.getUID(), newWorldHandler);
	}
	
	private void destroyOldDeathpoints(Player player) {
		if (options.maxPerPlayer <= 0) return;
		
		Deque<Deathpoint> playerDeathpoints = worlds.values().stream()
				.flatMap(WorldHandler::deathpoints)
				.filter(point -> point.getOwnerUniqueId().equals(player.getUniqueId()))
				.collect(Collectors.toCollection(LinkedList::new));
		
		while (playerDeathpoints.size() >= options.maxPerPlayer) {
			Deathpoint deathpoint = playerDeathpoints.remove();
			options.forgetSound.run(deathpoint.getLocation(), player);
			options.forgetMessage.sendMessage(player, deathpoint);
			if (options.dropItemsOnForget) deathpoint.dropItems();
			if (options.dropExpOnForget) deathpoint.dropExperience();
			worlds.get(deathpoint.getWorld().getUID()).destroyDeathpoint(deathpoint);
		}
	}
	
	private Deathpoint findDeathpointFromHitbox(LivingEntity hitbox) {
		Optional<Deathpoint> result = hitbox.getMetadata("deathpoint").stream()
				.filter(meta -> meta.getOwningPlugin() == plugin)
				.map(meta -> (Deathpoint) meta.value())
				.findAny();
		return result.orElse(null);
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

