package com.lesserhydra.secondchance;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
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

/**
 * A floating "container" that holds items and experience when a player dies.
 */
public class Deathpoint implements InventoryHolder, ConfigurationSerializable {
	
	private static final UUID HITBOX_ATTRIBUTE_UUID = UUID.fromString("f36fe1df-0036-475c-9f5a-52b95af83c96");
	private static final int INV_SIZE = 45; //Must be a multiple of 9, and at least 45
	
	private final UUID ownerUniqueId;
	private final Location location;
	private final Instant creationInstant;
	
	private Inventory inventory;
	private int experience;
	
	private ArmorStand hitbox;
	private boolean invalid = false;
	
	
	/**
	 * Constructs a deathpoint.
	 * @param owner Player that owns this deathpoint
	 * @param location Deathpoint exists at
	 * @param items Contents of deathpoint (must fit in inventory)
	 * @param experience Experience contained in deathpoint
	 */
	public Deathpoint(Player owner, Location location, ItemStack[] items, int experience) {
		this.creationInstant = Instant.now();
		this.ownerUniqueId = owner.getUniqueId();
		this.location = location.clone();
		
		this.inventory = createInventory(items);
		this.experience = experience;
	}
	
	/**
	 * Constructs a deathpoint by deserializing.
	 * @param map Map Formed from {@link #serialize} method
	 */
	public Deathpoint(Map<String, Object> map) {
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
	
	/**
	 * Spawns in the hitbox, if it doesn't already exist.
	 */
	public void spawnHitbox() {
		if (invalid || hitbox != null) return;
		if (!location.getChunk().isLoaded()) return;
		
		Location standLoc = location.clone().add(0, -0.75, 0);
		hitbox = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
		hitbox.setGravity(false);
		hitbox.setVisible(false);
		hitbox.setMetadata("deathpoint", new FixedMetadataValue(SecondChance.getPlugin(SecondChance.class), this));
		
		//Add attribute for identifying in case of persistance (Fallback, not relied upon for normal operation)
		hitbox.getAttribute(Attribute.GENERIC_MAX_HEALTH)
				.addModifier(new AttributeModifier(HITBOX_ATTRIBUTE_UUID, "isSecondChanceHitbox", 0, Operation.ADD_NUMBER));
	}
	
	/**
	 * Despawns the hitbox, if it exists.
	 */
	public void despawnHitbox() {
		if (hitbox == null) return;
		hitbox.remove();
		hitbox = null;
	}
	
	/**
	 * Destroys this deathpoint, despawning the hitbox, clearing contents and invalidating.
	 */
	public void destroy() {
		despawnHitbox();
		inventory.clear();
		experience = 0;
		invalid = true;
	}
	
	/**
	 * Drops items to the ground.
	 */
	public void dropItems() {
		location.getChunk().load();
		Arrays.stream(inventory.getContents())
			.filter(ItemStackUtils::isValid)
			.forEach((item) -> location.getWorld().dropItemNaturally(location, item));
		inventory.clear();
	}
	
	/**
	 * Drops experience to the ground.
	 */
	public void dropExperience() {
		if (experience == 0) return;
		ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
		orb.setExperience(experience);
		experience = 0;
	}
	
	/**
	 * Checks if no items are contained.
	 * @return True if no items are contained
	 */
	public boolean isEmpty() {
		return !Arrays.stream(inventory.getContents())
				.anyMatch(ItemStackUtils::isValid);
	}
	
	/**
	 * Checks if this deathpoint has not been destroyed.
	 * @return True if valid
	 */
	public boolean isInvalid() {
		return invalid;
	}
	
	/**
	 * Returns the instant of creation.
	 * @return The instant of creation
	 */
	public final Instant getCreationInstant() {
		return creationInstant;
	}
	
	/**
	 * Returns a copy of the location this deathpoint exists at.
	 * @return A copy of the location this deathpoint exists at
	 */
	public final Location getLocation() {
		return location.clone();
	}
	
	/**
	 * Returns the world inhabited by this deathpoint.
	 * @return The world inhabited by this deathpoint
	 */
	public final World getWorld() {
		return location.getWorld();
	}
	
	/**
	 * Returns the UUID of the player that owns this deathpoint.
	 * @return The UUID of the player that owns this deathpoint.
	 */
	public final UUID getOwnerUniqueId() {
		return ownerUniqueId;
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}
	
	@Override
	public int hashCode() {
		return (101 * ownerUniqueId.hashCode())
				^ (103 * location.hashCode())
				^ (107 * creationInstant.hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Deathpoint)) return false;
		if (obj == this) return true;
		
		Deathpoint other = (Deathpoint) obj;
		return ownerUniqueId.equals(other.getOwnerUniqueId())
				&& location.equals(other.getLocation())
				&& creationInstant.equals(other.getCreationInstant());
	}
	
	/**
	 * Checks if an armorstand was spawned for hitbox use. This is meant to be used as a safety net in cleaning up
	 * armorstands left over by previous bugs/crashes/whatever. Do not use to identify hitboxes normally!
	 * @param entity Armorstand to check
	 * @return Whether armorstand was used as a hitbox
	 */
	public static boolean armorstandIsHitbox(ArmorStand entity) {
		return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers().stream()
				.filter(mod -> HITBOX_ATTRIBUTE_UUID.equals(mod.getUniqueId()))
				.anyMatch(mod -> "isSecondChanceHitbox".equals(mod.getName()));
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
