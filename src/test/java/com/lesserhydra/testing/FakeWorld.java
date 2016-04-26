package com.lesserhydra.testing;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.powermock.reflect.Whitebox;

public abstract class FakeWorld implements World {
	
	private String name;
	
	private Map<Location, Chunk> chunkMap = new HashMap<>();
	private Map<Location, Block> blockMap = new HashMap<>();
	
	public static World mockBukkitWorld(String worldName) {
		World mockWorld = mock(FakeWorld.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
		Whitebox.setInternalState(mockWorld, String.class, worldName, FakeWorld.class);
		return mockWorld;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Block getBlockAt(Location loc) {
		Location blockLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		Block mockBlock = blockMap.get(blockLoc);
		if (mockBlock != null) return mockBlock;
		
		mockBlock = mock(Block.class);
		when(mockBlock.getLocation()).thenReturn(blockLoc.clone());
		blockMap.put(blockLoc.clone(), mockBlock);
		return mockBlock;
	}
	
	@Override
	public Chunk getChunkAt(Location loc) {
		Location chunkLoc = new Location(loc.getWorld(), Math.floorDiv(loc.getBlockX(), 16), 0, Math.floorDiv(loc.getBlockZ(), 16));
		Chunk mockChunk = chunkMap.get(chunkLoc);
		if (mockChunk != null) return mockChunk;
		
		mockChunk = mock(Chunk.class);
		when(mockChunk.isLoaded()).thenReturn(true);
		when(mockChunk.getWorld()).thenReturn(this);
		chunkMap.put(chunkLoc.clone(), mockChunk);
		return mockChunk;
	}
	
	@Override
	public Entity spawnEntity(Location loc, EntityType type) {
		Entity mockEntity = mock(type.getEntityClass());
		when(mockEntity.getLocation()).then(i -> loc.clone());
		when(mockEntity.getWorld()).thenReturn(this);
		return mockEntity;
	}
	
}
