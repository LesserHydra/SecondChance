package com.lesserhydra.testing;

import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.powermock.reflect.Whitebox;

public abstract class FakeWorld implements World {
	
	private String name;
	
	private Map<Vector, Chunk> chunkMap = new HashMap<>();
	private Map<Vector, Block> blockMap = new HashMap<>();
	
	public static World mockBukkitWorld(String worldName) {
		World mockWorld = mock(FakeWorld.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS).stubOnly());
		Whitebox.setInternalState(mockWorld, String.class, worldName, FakeWorld.class);
		return mockWorld;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Block getBlockAt(int x, int y, int z) {
		Vector blockLoc = new Vector(x, y, z);
		Block block = blockMap.get(blockLoc);
		if (block != null) return block;
		
		block = FakeBlock.mockBukkitBlock(this, x, y, z);
		//when(mockBlock.getLocation()).thenReturn(new Location(this, blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ()));
		blockMap.put(blockLoc.clone(), block);
		return block;
	}
	
	@Override
	public Block getBlockAt(Location loc) {
		return getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	@Override
	public Chunk getChunkAt(Location loc) {
		Vector chunkLoc = new Vector(Math.floorDiv(loc.getBlockX(), 16), 0, Math.floorDiv(loc.getBlockZ(), 16));
		Chunk mockChunk = chunkMap.get(chunkLoc);
		if (mockChunk != null) return mockChunk;
		
		mockChunk = mock(Chunk.class);
		when(mockChunk.isLoaded()).thenReturn(true);
		when(mockChunk.getWorld()).thenReturn(this);
		when(mockChunk.getEntities()).thenReturn(new Entity[0]); //TODO: Temporary?
		chunkMap.put(chunkLoc.clone(), mockChunk);
		return mockChunk;
	}
	
	@Override
	public Entity spawnEntity(Location loc, EntityType type) {
		Entity mockEntity = mock(type.getEntityClass(), withSettings().defaultAnswer(RETURNS_MOCKS));
		when(mockEntity.getLocation()).then(i -> loc.clone());
		when(mockEntity.getWorld()).thenReturn(this);
		
		return mockEntity;
	}
	
}
