package com.lesserhydra.secondchance;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.World;

/**
 * A SaveHandler implementation that utilizes an internal map for testing purposes.
 */
class MapSaveHandler implements SaveHandler {
	
	private Map<String, Deque<Deathpoint>> deathpoints = new HashMap<>();
	
	@Override
	public Deque<Deathpoint> load(World world) {
		Deque<Deathpoint> found = deathpoints.get(world.getName());
		if (found == null) return new LinkedList<>();
		return new LinkedList<>(found);
	}
	
	@Override
	public void save(World world, Collection<Deathpoint> deathpoints) {
		this.deathpoints.put(world.getName(), new LinkedList<>(deathpoints));
	}
	
}
