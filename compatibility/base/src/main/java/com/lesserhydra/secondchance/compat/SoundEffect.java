package com.lesserhydra.secondchance.compat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents a sound effect
 */
public interface SoundEffect {
	
	public void run(Location deathpointLocation, Player owner);
	
	public boolean isEnabled();
	
	public String getSound();
	
	public float getVolume();
	
	public float getPitch();
	
	public boolean isDirect();
	
}
