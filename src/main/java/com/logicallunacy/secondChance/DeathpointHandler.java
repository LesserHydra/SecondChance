package com.logicallunacy.secondChance;

import java.util.HashMap;
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
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

class DeathpointHandler implements Listener {
	
	private final SecondChance plugin;
	private final HashMap<String, DeathPoint> m_deathPoints = new HashMap<>();
	private BukkitTask particleTask;
	
	public DeathpointHandler(SecondChance tm) {
		plugin = tm;
	}
	
	public void init() {
		for (Player player: plugin.getServer().getOnlinePlayers()) {
			DeathPoint deathPoint = new DeathPoint(plugin, player);
			deathPoint.load();
			m_deathPoints.put(player.getName(), deathPoint);
		}
		
		particleTask = new BukkitRunnable() { @Override public void run() {
			for (DeathPoint deathPoint: m_deathPoints.values()) {
				deathPoint.particles();
			}
		}}.runTaskTimer(plugin, 0L, 20L);
	}
	
	public void deinit() {
		for (DeathPoint deathPoint: m_deathPoints.values()) {
			deathPoint.despawnHitbox();
			deathPoint.save();
		}
		
		particleTask.cancel();
		m_deathPoints.clear();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		DeathPoint deathPoint = new DeathPoint(plugin, player);
		deathPoint.load();
		deathPoint.spawnHitbox();
		m_deathPoints.put(player.getName(), deathPoint);
		
		playerSpawnEffect(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e) {
		DeathPoint deathPoint = m_deathPoints.get(e.getPlayer().getName());
		deathPoint.despawnHitbox();
		deathPoint.save();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player player = e.getEntity().getPlayer();
		if (player == null || e.getKeepInventory()) return;
		
		DeathPoint deathPoint = m_deathPoints.get(player.getName());
		deathPoint.createNew(player.getLocation());
		e.setDroppedExp(0);
		e.setKeepInventory(true);
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
	public void onChunkUnload(ChunkUnloadEvent e) {
		for (DeathPoint deathPoint: m_deathPoints.values()) {
			if (e.getChunk().equals(deathPoint.getChunk())) deathPoint.despawnHitbox();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent e) {
		for (DeathPoint deathPoint: m_deathPoints.values()) {
			if (e.getChunk().equals(deathPoint.getChunk())) deathPoint.spawnHitbox();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onArmorStandDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof ArmorStand)) return;
		
		for (DeathPoint deathPoint: m_deathPoints.values()) {
			if (!deathPoint.isHitbox(e.getEntity())) continue;
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerClickArmorStand(PlayerInteractAtEntityEvent e) {
		if (!(e.getRightClicked() instanceof ArmorStand)) return;
		
		Player player = e.getPlayer();
		DeathPoint deathPoint = m_deathPoints.get(player.getName());
		
		if (!deathPoint.isHitbox(e.getRightClicked())) return;
		deathPoint.playerClicked();
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();
		DeathPoint deathPoint = m_deathPoints.get(player.getName());
		if (deathPoint.isInventory(e.getInventory())) deathPoint.destroy();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(final PlayerRespawnEvent e) {
		new BukkitRunnable() { @Override public void run() {
			playerSpawnEffect(e.getPlayer());
		}}.runTaskLater(plugin, 0L);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPortal(PlayerPortalEvent e) {
		playerSpawnEffect(e.getPlayer());
	}
	
	private void playerSpawnEffect(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 9, true, false), true);
	}
	
}

