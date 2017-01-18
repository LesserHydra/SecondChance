package com.lesserhydra.secondchance.compat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents a sound effect
 */
public interface SoundEffect {
	
	void run(Location deathpointLocation, Player owner);
	
	boolean isEnabled();
	
	String getSound();
	
	float getVolume();
	
	float getPitch();
	
	boolean isDirect();
	
}
