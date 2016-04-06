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
import org.bukkit.scheduler.BukkitRunnable;
import com.roboboy.bukkitutil.ExpUtil;

class DeathPoint {
	
	private static final int INV_SIZE = 45; //Must be a multiple of 9, and at least 45
	
	private final SecondChance plugin;
	private final Player player;
	
	private Location location;
	private ArmorStand hitbox;
	private Inventory contents;
	private int experience;
	
	public DeathPoint(SecondChance plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		this.location = null;
		this.contents = Bukkit.getServer().createInventory(null, INV_SIZE, "Lost Inventory");//player.getName() + "'s Lost Inventory");
		this.experience = 0;
	}
	
	public void save() {
		String playerName = player.getName();
		UUID playerUUID = player.getUniqueId();
		
		plugin.getLogger().info("Saving " + playerName + "'s DeathPoint");
		try {
			File file = new File(plugin.saveFolder + File.separator + playerUUID.toString() + ".yml");
			file.createNewFile();
			YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);
			
			saveFile.set("playerName", playerName);
			saveFile.set("location", location);
			saveFile.set("experience", Integer.valueOf(experience));
			saveFile.set("contents", contents.getContents());
			
			saveFile.save(file);
		} catch (IOException exception) {
			plugin.getLogger().info("FAILED!");
			player.sendMessage(ChatColor.RED + "Deathpoint failed to save; dropping on the ground.");
			player.sendMessage(ChatColor.RED + "Please notify a server administrator, as this should never happen.");
			destroy();
			exception.printStackTrace();
		}
	}
	
	public void load() {
		String playerName = player.getName();
		UUID playerUUID = player.getUniqueId();
		
		plugin.getLogger().info("Attempting to load " + playerName + "'s saved death point...");
		
		File file = new File(plugin.saveFolder + File.separator + playerUUID.toString() + ".yml");
		if (file.exists()) {
			YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);
			
			//Load
			location = (Location) saveFile.get("location");
			experience = saveFile.getInt("experience");
			contents.setContents(saveFile.getList("contents").toArray(new ItemStack[INV_SIZE]));
		}
		else plugin.getLogger().warning("Failed. File not found.");
	}
	
	public void createNew(Location playerLocation) {
		destroy();
		
		location = findLocation();
		
		PlayerInventory playerInv = player.getInventory();
		ItemStack[] inventory = playerInv.getContents();
		ItemStack[] contentsArray = new ItemStack[INV_SIZE];
		System.arraycopy(inventory, 9, contentsArray, 0, 27); //Main inventory
		System.arraycopy(inventory, 0, contentsArray, 27, 9); //Hotbar
		System.arraycopy(inventory, 36, contentsArray, INV_SIZE - 4, 4); //Armor
		System.arraycopy(inventory, 40, contentsArray, 36, 1); //Off hand
		
		contents.setContents(contentsArray);
		experience = ExpUtil.calculateXpFromLevel(player.getLevel()) + ExpUtil.calculateXpFromProgress(player.getLevel(), player.getExp());
		
		//Create hitbox
		spawnHitbox();
		
		//Clear player's inventory
		playerInv.clear();
		
		//Save death point
		save();
		
		if (experience == 0 && isEmpty()) destroy();
	}
	
	public void spawnHitbox() {
		if (location == null) return;
		if (hitbox != null) return;
		if (!location.getChunk().isLoaded()) return;
		
		Location standLoc = location.clone().add(0, -0.75, 0);
		hitbox = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
		hitbox.setGravity(false);
		hitbox.setVisible(false);
	}
	
	public void despawnHitbox() {
		if (hitbox == null) return;
		hitbox.remove();
		hitbox = null;
	}
	
	public Chunk getChunk() {
		if (location == null) return null;
		return location.getChunk();
	}

	public boolean isHitbox(Entity rightClicked) {
		return rightClicked.equals(hitbox);
	}

	public void playerClicked() {
		dropExperience(experience);
		if (isEmpty()) destroy();
		else showContents(player);
	}
	
	public void destroy() {
		if (location == null) return;
		for (ItemStack item : contents.getContents()) {
			if (item == null || item.getType() == Material.AIR) continue;
			location.getWorld().dropItemNaturally(location, item);
		}
		despawnHitbox();
		contents.clear();
		location = null;
	}

	public boolean isInventory(Inventory inventory) {
		return inventory.equals(contents);
	}
	
	public void showContents(Player player) {
		player.openInventory(contents);
	}
	
	public void particles() {
		if (location == null) return;
		location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 0.2, 0.2, 0.2, 0.5);
		location.getWorld().spawnParticle(Particle.END_ROD, location, 15, 10, 10, 10, 0.1);
	}
	
	private Location findLocation() {
		for (MetadataValue value: player.getMetadata("lastSafePosition")) {
			if (value.getOwningPlugin() != plugin) continue;
			Location loc = ((Location) value.value()).getBlock().getLocation();
			loc.add(0.5, 0, 0.5);
			return loc;
		}
		
		throw new IllegalStateException("No valid position found for deathpoint.");
	}
	
	private boolean isEmpty() {
		for (ItemStack item: contents.getContents()) {
			if (item != null && item.getType() != Material.AIR) return false;
		}
		return true;
	}
	
	private void dropExperience(int xpPoints) {
		int toDrop = ExpUtil.calculateXpForNextLevel(player.getLevel());
		xpPoints -= toDrop;
		
		//Drop xp
		Location playerLoc = player.getLocation();
		ExperienceOrb orb = playerLoc.getWorld().spawn(playerLoc, ExperienceOrb.class);
		if (xpPoints < 0) toDrop += xpPoints;
		orb.setExperience(toDrop);
		
		final int remaining = xpPoints;
		if (remaining <= 0) return;
		new BukkitRunnable() { @Override public void run() {
			dropExperience(remaining);
		}}.runTaskLater(plugin, 2L);
	}
	
}
