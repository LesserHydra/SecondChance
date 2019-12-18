package com.lesserhydra.testing;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public abstract class FakeWorld implements World {
	
	private String name;
	private UUID uuid;
	
	private Map<Vector, Chunk> chunkMap = new HashMap<>();
	private Set<Vector> chunkLoadMap = new HashSet<>();
	private Map<Vector, Block> blockMap = new HashMap<>();
	
	public static World mockBukkitWorld(String worldName) {
		FakeWorld mockWorld = mock(FakeWorld.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS).stubOnly());
		mockWorld.name = worldName;
		mockWorld.uuid = UUID.randomUUID();
		return mockWorld;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public UUID getUID() {
		return uuid;
	}
	
	@Override
	public Block getBlockAt(int x, int y, int z) {
		Vector blockLoc = new Vector(x, y, z);
		Block block = blockMap.get(blockLoc);
		if (block != null) return block;
		
		block = FakeBlock.mockBukkitBlock(this, x, y, z);
		blockMap.put(blockLoc.clone(), block);
		return block;
	}
	
	@Override
	public Block getBlockAt(Location loc) {
		return getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	@Override
	public Chunk getChunkAt(int x, int z) {
		Vector chunkLoc = new Vector(x, 0, z);
		Chunk mockChunk = chunkMap.get(chunkLoc);
		if (mockChunk != null) return mockChunk;
		
		mockChunk = mock(Chunk.class);
		when(mockChunk.getX()).thenReturn(x);
		when(mockChunk.getZ()).thenReturn(z);
		when(mockChunk.isLoaded()).thenAnswer(invocation -> isChunkLoaded(x, z));
		when(mockChunk.load()).thenAnswer(invocation -> { loadChunk(x, z); return true; });
		when(mockChunk.unload()).thenAnswer(invocation -> unloadChunk(x, z));
		when(mockChunk.getWorld()).thenReturn(this);
		when(mockChunk.getEntities()).thenReturn(new Entity[0]); //TODO: Temporary?
		chunkMap.put(chunkLoc.clone(), mockChunk);
		return mockChunk;
	}
	
	@Override
	public Chunk getChunkAt(Location loc) {
		return getChunkAt(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
	}
	
	@Override
	public boolean isChunkLoaded(int x, int z) {
		return chunkLoadMap.contains(new Vector(x, 0, z));
	}
	
	@Override
	public void loadChunk(int x, int z) {
		chunkLoadMap.add(new Vector(x, 0, z));
	}
	
	@Override
	public boolean unloadChunk(int x, int z) {
		chunkLoadMap.remove(new Vector(x, 0, z));
		return true;
	}
	
	@Override
	public Entity spawnEntity(Location loc, EntityType type) {
		Entity mockEntity = mock(type.getEntityClass(), withSettings().defaultAnswer(RETURNS_MOCKS));
		when(mockEntity.getLocation()).then(i -> loc.clone());
		when(mockEntity.getWorld()).thenReturn(this);
		
		return mockEntity;
	}
	
}
