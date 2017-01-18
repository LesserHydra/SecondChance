package com.lesserhydra.secondchance.compat;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Compatibility interface for SecondChance
 */
public interface Compat {
	
	/**
	 * Returns the version of the implementation.
	 * @return The version of the implementation
	 */
	String getVersion();
	
	/**
	 * Combines a player's entire inventory contents into an array.
	 * This includes the storage, hotbar, armor, and off-hand.
	 * @param inventory Player's inventory
	 * @return Array containing entire contents
	 */
	ItemStack[] inventoryContents(PlayerInventory inventory);
	
	/**
	 * Checks if an armorstand was spawned for hitbox use. This is meant to be used as a safety net in cleaning up
	 * armorstands left over by previous bugs/crashes/whatever. Do not use to identify hitboxes normally!
	 * @param entity Armorstand to check
	 * @return Whether armorstand was used as a hitbox
	 */
	boolean armorstandIsHitbox(ArmorStand entity);
	
	/**
	 * Spawns in an armorstand to use as a hitbox. The returned armorstand can be identified by {@link #armorstandIsHitbox}.
	 * @param location Location to spawn hitbox at
	 * @return The resulting armorstand
	 */
	ArmorStand spawnHitbox(Location location);
	
	/**
	 * Constructs a version specific {@link SoundEffect}.
	 * @param enabled Is enabled
	 * @param sound Sound string to send to client
	 * @param volume Volume
	 * @param pitch Pitch
	 * @param direct Whether sound is direct
	 * @return The resulting {@link SoundEffect}
	 */
	SoundEffect makeSoundEffect(boolean enabled, String sound, float volume, float pitch, boolean direct);
	
	/**
	 * Constructs a version specific {@link ParticleEffect}.
	 * @param particleName Name of particle to use
	 * @param amount Number of particles
	 * @param spread Radius of particle spread
	 * @param speed Extra data
	 * @param ownerOnly Whether only the deathpoint owner should see the effect
	 * @return The resulting {@link ParticleEffect}
	 */
	ParticleEffect makeParticleEffect(String particleName, int amount, double spread, double speed, boolean ownerOnly);
	
}
