package com.logicallunacy.secondChance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.roboboy.bukkitutil.ItemStackUtils;

public class DeathPoint implements InventoryHolder, ConfigurationSerializable {
	
	private static final int INV_SIZE = 45; //Must be a multiple of 9, and at least 45
	
	private final UUID id;
	private final UUID playerId;
	private final Location location;
	
	private final Inventory inventory;
	private int experience;
	
	private ArmorStand hitbox;
	private boolean invalid = false;
	

	public DeathPoint(UUID id, UUID playerId, Location location, ItemStack[] items, int experience) {
		this.id = id;
		this.playerId = playerId;
		this.location = location;
		this.inventory = createInventory(items);
		this.experience = experience;
		
		SecondChance.logger().info("New deathpoint created at (" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ")");
	}
	
	public DeathPoint(Map<String, Object> map) {
		this.id = UUID.fromString((String) map.get("UUID"));
		this.playerId = UUID.fromString((String) map.get("playerUUID"));
		this.location = (Location) map.get("location");
		this.experience = (Integer) map.get("experience");
		this.inventory = Bukkit.getServer().createInventory(this, INV_SIZE, "Lost Inventory");
		ItemStack[] contents = ((List<?>) map.get("contents")).toArray(new ItemStack[INV_SIZE]);
		inventory.setContents(contents);
		
		SecondChance.logger().info("Deathpoint loaded at (" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ")");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> result = new HashMap<>();
		result.put("UUID", id.toString());
		result.put("playerUUID", playerId.toString());
		result.put("location", location);
		result.put("experience", experience);
		
		ItemStack[] contents = inventory.getContents();
		int i = contents.length;
		while (i-- > 0) {
			if (contents[i] != null) break;
		}
		result.put("contents", Arrays.copyOf(contents, i+1));

		return result;
	}

	public void spawnHitbox() {
		if (hitbox != null) return;
		if (!location.getChunk().isLoaded()) return;
		
		Location standLoc = location.clone().add(0, -0.75, 0);
		hitbox = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
		hitbox.setGravity(false);
		hitbox.setVisible(false);
		hitbox.setInvulnerable(true);
	}
	
	public void despawnHitbox() {
		if (hitbox == null) return;
		hitbox.remove();
		hitbox = null;
	}
	
	public void destroy() {
		if (invalid) return;
		Arrays.stream(inventory.getContents())
			.filter(Objects::nonNull)
			.filter((item) -> item.getType() != Material.AIR)
			.forEach((item) -> location.getWorld().dropItemNaturally(location, item));
		inventory.clear();
		despawnHitbox();
		invalid = true;
	}
	
	public void dropExperience() {
		ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
		orb.setExperience(experience);
		experience = 0;
		/*int toDrop = ExpUtil.calculateXpForNextLevel(player.getLevel());
		xpPoints -= toDrop;
		
		//Drop xp
		ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
		if (xpPoints < 0) toDrop += xpPoints;
		orb.setExperience(toDrop);
		
		final int remaining = xpPoints;
		if (remaining <= 0) return;
		new BukkitRunnable() { @Override public void run() {
			dropExperience(remaining);
		}}.runTaskLater(plugin, 2L);*/
	}
	
	public void runParticles() {
		if (location == null) return;
		location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 0.2, 0.2, 0.2, 0.5);
		location.getWorld().spawnParticle(Particle.END_ROD, location, 15, 10, 10, 10, 0.1);
	}
	
	public boolean isEmpty() {
		return !Arrays.stream(inventory.getContents())
				.anyMatch(ItemStackUtils::isValid);
	}
	
	public boolean isValid() {
		return !invalid;
	}
	
	public boolean isHitbox(Entity entity) {
		return entity == hitbox;
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}
	
	public Location getLocation() {
		return location.clone();
	}
	
	public UUID getOwnerUUID() {
		return playerId;
	}
	
	public UUID getUUID() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return 101 * id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DeathPoint)) return false;
		return id.equals(((DeathPoint) obj).getUUID());
	}
	
	private Inventory createInventory(ItemStack[] items) {
		Inventory result = Bukkit.getServer().createInventory(this, INV_SIZE, "Lost Inventory");
		if (items == null) return result;
		
		ItemStack[] contentsArray = new ItemStack[INV_SIZE];
		System.arraycopy(items, 9, contentsArray, 0, 27); //Main inventory
		System.arraycopy(items, 0, contentsArray, 27, 9); //Hotbar
		System.arraycopy(items, 36, contentsArray, INV_SIZE - 4, 4); //Armor
		System.arraycopy(items, 40, contentsArray, 36, 1); //Off hand
		
		result.setContents(contentsArray);
		return result;
	}

}
