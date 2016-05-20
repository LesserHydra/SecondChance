package com.lesserhydra.secondchance;

import java.util.Collection;
import java.util.Deque;
import org.bukkit.World;

/**
 * Represents an object that handles SecondChance save data.
 */
interface SaveHandler {
	
	/**
	 * Loads the saved deathpoints for the given world.
	 * The resulting deque is sorted by time (oldest first), and is safe to modify.
	 * @param world The world to load for
	 * @return The saved deathpoints
	 */
	public Deque<Deathpoint> load(World world);
	
	/**
	 * Saves the given deathpoints for the given world.
	 * @param world The world to save for
	 * @param deathpoints A collection of deathpoints to save
	 */
	public void save(World world, Collection<Deathpoint> deathpoints);
	
}
