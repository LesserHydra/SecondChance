package com.logicallunacy.secondChance;

import java.io.IOException;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

public class SecondChanceListener implements Listener
{
	private SecondChance plugin;
	
	
	public SecondChanceListener(SecondChance tm) {plugin = tm;}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player player = e.getPlayer();
		DeathPoint deathPoint = new DeathPoint(plugin, player);
		deathPoint.load();
		deathPoint.spawnHitbox();
		plugin.m_deathPoints.put(player.getName(), deathPoint);
		
		playerSpawnEffect(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e) throws IOException
	{
		DeathPoint deathPoint = plugin.m_deathPoints.get(e.getPlayer().getName());
		deathPoint.despawnHitbox();
		deathPoint.save();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		Player player = e.getEntity().getPlayer();
		if (player != null && !e.getKeepInventory())
		{
			DeathPoint deathPoint = plugin.m_deathPoints.get(player.getName());
			
			deathPoint.createNew();
			
			e.setKeepInventory(true);
			e.setDroppedExp(0);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkUnload(ChunkUnloadEvent e)
	{
		for (DeathPoint deathPoint: plugin.m_deathPoints.values())
		{
			if (deathPoint.m_location != null && deathPoint.m_location.getChunk().equals(e.getChunk()))
				deathPoint.despawnHitbox();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent e)
	{
		for (DeathPoint deathPoint: plugin.m_deathPoints.values())
		{
			if (deathPoint.m_location != null && deathPoint.m_location.getChunk().equals(e.getChunk()))
				deathPoint.spawnHitbox();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e)
	{
		if (!(e.getRightClicked() instanceof ArmorStand)) return;
		
		Player player = e.getPlayer();
		DeathPoint deathPoint = plugin.m_deathPoints.get(player.getName());
		
		if (e.getRightClicked().equals(deathPoint.m_armorStand))
		{
			for (Block block: player.getLineOfSight((HashSet<Material>)null, 5))
			{
				if (block.getLocation().equals(deathPoint.m_location.getBlock().getLocation()))
				{
					if (!deathPoint.isEmpty())
						deathPoint.showContents(player);
					else
						deathPoint.finish();
					break;
				}
			}
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent e)
	{
		Player player = (Player) e.getPlayer();
		DeathPoint deathPoint = plugin.m_deathPoints.get(player.getName());
		if (e.getInventory().equals(deathPoint.m_contents))
			if (deathPoint.isEmpty()) deathPoint.finish();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onArmorStandDamage(EntityDamageEvent e)
	{
		if (!(e.getEntity() instanceof ArmorStand)) return;
		
		for (DeathPoint deathPoint: plugin.m_deathPoints.values()) {
			if (e.getEntity().equals(deathPoint.m_armorStand)) {
				e.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(final PlayerRespawnEvent e)
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run() {
				playerSpawnEffect(e.getPlayer());
			}
		}, 10);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPortal(PlayerPortalEvent e)
	{
		playerSpawnEffect(e.getPlayer());
	}

	private void playerSpawnEffect(Player player)
	{
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 9, true, false), true);
	}
}

