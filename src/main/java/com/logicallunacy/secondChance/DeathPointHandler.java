package com.logicallunacy.secondChance;

import java.io.IOException;
import java.util.HashMap;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DeathPointHandler implements Listener
{
	private SecondChance plugin;
	
	private BukkitTask particleTask;
	private HashMap<String, DeathPoint> m_deathPoints = new HashMap<>();
	
	public DeathPointHandler(SecondChance tm) {
		plugin = tm;
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
	public void onPlayerQuit(PlayerQuitEvent e) throws IOException {
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
	public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e) {
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
		if (deathPoint.isInventory(e.getInventory())) deathPoint.close();
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
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(final PlayerRespawnEvent e) {
		playerSpawnEffect(e.getPlayer());
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				playerSpawnEffect(e.getPlayer());
			}
		}, 1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPortal(PlayerPortalEvent e) {
		playerSpawnEffect(e.getPlayer());
	}

	private void playerSpawnEffect(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 9, true, false), true);
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
		}}.runTaskTimer(plugin, 15L, 15L);
	}

	public void deinit() {
		for(DeathPoint deathPoint: m_deathPoints.values()) {
			deathPoint.despawnHitbox();
			deathPoint.save();
		}
		
		particleTask.cancel();
		m_deathPoints.clear();
	}
	
}

