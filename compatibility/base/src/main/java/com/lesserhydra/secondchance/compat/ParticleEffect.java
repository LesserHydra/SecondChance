package com.lesserhydra.secondchance.compat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents a particle effect
 */
public interface ParticleEffect {
	
	public void run(Location deathpointLocation, Player owner);

	public String getName();

	public int getAmount();

	public double getSpread();

	public double getSpeed();

	public boolean isOwnerOnly();

}
