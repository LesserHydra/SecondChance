package com.logicallunacy.secondChance;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;

class DeathPoint {
	
	private static final int INV_SIZE = 45; //Must be a multiple of 9, and at least 45
	
	private SecondChance m_plugin;
	
	private Player m_player;
	private Location m_location;
	private ArmorStand m_armorStand;
	
	//Contents
	private Inventory m_contents;
	private int m_experience;
	
	
	public DeathPoint(SecondChance plugin, Player player) {
		m_plugin = plugin;
		m_player = player;
		m_location = null;
		m_contents = Bukkit.getServer().createInventory(null, INV_SIZE, "Lost Inventory");//player.getName() + "'s Lost Inventory");
		m_experience = 0;
	}
	
	public void save() {
		String playerName = m_player.getName();
		UUID playerUUID = m_player.getUniqueId();
		
		m_plugin.getLogger().info("Saving " + playerName + "'s DeathPoint");
		try {
			File file = new File(m_plugin.m_saveFolder + File.separator + playerUUID.toString() + ".yml");
			file.createNewFile();
			YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);
			
			saveFile.set("playerName", playerName);
			saveFile.set("location", m_location);
			saveFile.set("experience", Integer.valueOf(m_experience));
			saveFile.set("contents", m_contents.getContents());
			
			saveFile.save(file);
		} catch (IOException exception) {
			m_plugin.getLogger().info("FAILED!");
			m_player.sendMessage(ChatColor.RED + "Deathpoint failed to save; dropping on the ground.");
			m_player.sendMessage(ChatColor.RED + "Please notify a server administrator, as this should never happen.");
			destroy();
			exception.printStackTrace();
		}
	}
	
	public void load() {
		String playerName = m_player.getName();
		UUID playerUUID = m_player.getUniqueId();
		
		m_plugin.getLogger().info("Attempting to load " + playerName + "'s saved death point...");
		
		File file = new File(m_plugin.m_saveFolder + File.separator + playerUUID.toString() + ".yml");
		if (file.exists()) {
			YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);
			
			//Load
			m_location = (Location) saveFile.get("location");
			m_experience = saveFile.getInt("experience");
			m_contents.setContents(saveFile.getList("contents").toArray(new ItemStack[INV_SIZE]));
		}
		else m_plugin.getLogger().warning("Failed. File not found.");
	}
	
	public void createNew(Location playerLocation) {
		destroy();
		
		m_location = findLocation();
		
		PlayerInventory playerInv = m_player.getInventory();
		ItemStack[] inventory = playerInv.getContents();
		ItemStack[] contentsArray = new ItemStack[INV_SIZE];
		System.arraycopy(inventory, 9, contentsArray, 0, 27); //Main inventory
		System.arraycopy(inventory, 0, contentsArray, 27, 9); //Hotbar
		System.arraycopy(inventory, 36, contentsArray, INV_SIZE - 4, 4); //Armor
		System.arraycopy(inventory, 40, contentsArray, 36, 1); //Off hand
		
		m_contents.setContents(contentsArray);
		m_experience = Util.calculateXpFromLevel(m_player.getLevel()) + Util.calculateXpFromProgress(m_player.getLevel(), m_player.getExp());
		
		//Create hitbox
		spawnHitbox();
		
		//Clear player's inventory
		playerInv.clear();
		
		//Save death point
		save();
		
		if (m_experience == 0 && isEmpty()) destroy();
	}
	
	public void spawnHitbox() {
		if (m_location == null) return;
		if (m_armorStand != null) return;
		if (!m_location.getChunk().isLoaded()) return;
		
		Location standLoc = m_location.clone().add(0, -0.75, 0);
		m_armorStand = (ArmorStand) m_location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
		m_armorStand.setGravity(false);
		m_armorStand.setVisible(false);
	}
	
	public void despawnHitbox() {
		if (m_armorStand == null) return;
		m_armorStand.remove();
		m_armorStand = null;
	}
	
	public Chunk getChunk() {
		if (m_location == null) return null;
		return m_location.getChunk();
	}

	public boolean isHitbox(Entity rightClicked) {
		return rightClicked.equals(m_armorStand);
	}

	public void playerClicked() {
		dropExperience(m_experience);
		if (isEmpty()) destroy();
		else showContents(m_player);
	}
	
	public void destroy() {
		for (ItemStack item : m_contents.getContents()) {
			if (item == null || item.getType() == Material.AIR) continue;
			m_location.getWorld().dropItemNaturally(m_location, item);
		}
		despawnHitbox();
		m_contents.clear();
		m_location = null;
	}

	public boolean isInventory(Inventory inventory) {
		return inventory.equals(m_contents);
	}
	
	public void showContents(Player player) {
		player.openInventory(m_contents);
	}
	
	public void particles() {
		if (m_location == null) return;
		m_location.getWorld().spawnParticle(Particle.PORTAL, m_location, 50, 0.2, 0.2, 0.2, 0.5);
		m_location.getWorld().spawnParticle(Particle.END_ROD, m_location, 15, 10, 10, 10, 0.1);
	}
	
	private Location findLocation() {
		for (MetadataValue value: m_player.getMetadata("lastNonsolidGroundPosition")) {
			if (value.getOwningPlugin() != m_plugin) continue;
			Location loc = ((Location) value.value()).getBlock().getLocation();
			loc.add(0.5, 0, 0.5);
			return loc;
		}
		
		throw new IllegalStateException("No valid position found for deathpoint.");
	}
	
	private boolean isEmpty() {
		for (ItemStack item: m_contents.getContents()) {
			if (item != null && item.getType() != Material.AIR) return false;
		}
		return true;
	}
	
	private void dropExperience(int xpPoints) {
		int toDrop = Util.calculateXpForNextLevel(m_player.getLevel());
		xpPoints -= toDrop;
		
		//Drop xp
		Location playerLoc = m_player.getLocation();
		ExperienceOrb orb = playerLoc.getWorld().spawn(playerLoc, ExperienceOrb.class);
		if (xpPoints < 0) orb.setExperience(toDrop + xpPoints);
		else orb.setExperience(toDrop);
		
		final int remaining = xpPoints;
		if (remaining <= 0) return;
		m_plugin.getServer().getScheduler().scheduleSyncDelayedTask(m_plugin, new Runnable() {
			public void run()
			{dropExperience(remaining);}
		}, 2);
	}
	
}
