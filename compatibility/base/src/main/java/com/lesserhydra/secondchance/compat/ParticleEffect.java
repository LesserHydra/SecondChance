package com.lesserhydra.secondchance.compat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents a particle effect
 */
public interface ParticleEffect {
	
	void run(Location deathpointLocation, Player owner);

	String getName();

	int getAmount();

	double getSpread();

	double getSpeed();

	boolean isOwnerOnly();

}
