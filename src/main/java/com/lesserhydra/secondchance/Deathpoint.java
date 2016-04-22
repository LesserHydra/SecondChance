package com.lesserhydra.secondchance;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import com.lesserhydra.bukkitutil.ItemStackUtils;

public class Deathpoint implements InventoryHolder, ConfigurationSerializable {
	
	private static final int INV_SIZE = 45; //Must be a multiple of 9, and at least 45
	
	private final UUID uniqueId;
	private final UUID ownerUniqueId;
	private final Location location;
	private final Instant creationInstant;
	
	private Inventory inventory;
	private int experience;
	
	private ArmorStand hitbox;
	private boolean invalid = false;
	

	public Deathpoint(Player owner, Location location, ItemStack[] items, int experience) {
		this.uniqueId = UUID.randomUUID();
		this.creationInstant = Instant.now();
		this.ownerUniqueId = owner.getUniqueId();

		this.location = location.clone();
		//this.location.setPitch(0);
		//this.location.setYaw(0);
		
		this.inventory = createInventory(items);
		this.experience = experience;
	}
	
	public Deathpoint(Map<String, Object> map) {
		this.uniqueId = UUID.fromString((String) map.get("uniqueId"));
		this.creationInstant = Instant.parse((String) map.get("instant"));
		this.ownerUniqueId = UUID.fromString((String) map.get("ownerUniqueId"));
		this.location = (Location) map.get("location");

		this.experience = (Integer) map.get("experience");
		this.inventory = Bukkit.getServer().createInventory(this, INV_SIZE, "Lost Inventory");
		ItemStack[] contents = ((List<?>) map.get("contents")).toArray(new ItemStack[INV_SIZE]);
		inventory.setContents(contents);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> result = new HashMap<>();
		result.put("uniqueId", uniqueId.toString());
		result.put("instant", creationInstant.toString());
		result.put("ownerUniqueId", ownerUniqueId.toString());
		result.put("location", location);
		
		ItemStack[] contents = inventory.getContents();
		int i = contents.length;
		while (i-- > 0) {
			if (contents[i] != null) break;
		}
		result.put("contents", Arrays.copyOf(contents, i+1));
		result.put("experience", experience);

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
		hitbox.setMetadata("deathpoint", new FixedMetadataValue(SecondChance.getPlugin(SecondChance.class), this));
	}
	
	public void despawnHitbox() {
		if (hitbox == null) return;
		hitbox.remove();
		hitbox = null;
	}
	
	public void destroy() {
		if (invalid) return;
		Arrays.stream(inventory.getContents())
			.filter(ItemStackUtils::isValid)
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
	
	public final UUID getUniqueId() {
		return uniqueId;
	}
	
	public final Instant getCreationInstant() {
		return creationInstant;
	}
	
	public final Location getLocation() {
		return location.clone();
	}
	
	public final World getWorld() {
		return location.getWorld();
	}
	
	public final UUID getOwnerUniqueId() {
		return ownerUniqueId;
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}
	
	@Override
	public int hashCode() {
		return (101 * uniqueId.hashCode())
				^ (109 * ownerUniqueId.hashCode())
				^ (107 * location.hashCode())
				^ (103 * creationInstant.hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Deathpoint)) return false;
		Deathpoint other = (Deathpoint) obj;
		return uniqueId.equals(other.getUniqueId())
				&& ownerUniqueId.equals(other.getOwnerUniqueId())
				&& location.equals(other.getLocation())
				&& creationInstant.equals(other.getCreationInstant());
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
