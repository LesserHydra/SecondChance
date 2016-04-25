package com.lesserhydra.secondchance;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.lesserhydra.testing.Capsule;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, JavaPlugin.class})
@PowerMockIgnore("javax.management.*")
public class DeathpointHandlerTest {
	
	SecondChance mockPlugin;
	DeathpointHandler deathpointHandler;
	World mockWorld1;
	
	Player mockPlayer;
	
	Map<World, SaveHandler> mockedSaveHandlers;
	
	@Before public void init() {
		PowerMockito.mockStatic(Bukkit.class, JavaPlugin.class);
		
		//Scheduler and server
		BukkitScheduler mockScheduler = mock(BukkitScheduler.class);
		Server mockServer = mock(Server.class);
		BDDMockito.given(Bukkit.getScheduler()).willReturn(mockScheduler);
		BDDMockito.given(Bukkit.getServer()).willReturn(mockServer);
		
		//Inventory creation
		when(mockServer.createInventory(any(InventoryHolder.class), anyInt(), anyString())).then(invoke -> {
			Capsule<ItemStack[]> contents = new Capsule<>();
			Inventory inv = mock(Inventory.class);
			doAnswer(i -> contents.set(i.getArgumentAt(0, ItemStack[].class))).when(inv).setContents(any(ItemStack[].class));
			when(inv.getContents()).then(i -> contents.get());
			return inv;
		});
		
		//Main plugin
		mockPlugin = mock(SecondChance.class);
		when(mockPlugin.getSaveHandler(any(World.class))).then(invoke -> mockedSaveHandlers.get(invoke.getArgumentAt(0, World.class)));
		BDDMockito.given(JavaPlugin.getPlugin(eq(SecondChance.class))).willReturn(mockPlugin);
		
		//World
		mockWorld1 = mock(World.class);
		when(mockWorld1.getName()).thenReturn("world1");
		
		//ArmorStand spawning
		when(mockWorld1.spawnEntity(any(Location.class), eq(EntityType.ARMOR_STAND))).then(invoke -> {
			//Location loc = invoke.getArgumentAt(0, Location.class);
			ArmorStand mockArmorStand = mock(ArmorStand.class);
			return mockArmorStand;
		});
		
		//Block handling
		Map<Location, Block> blockMap = new HashMap<>();
		when(mockWorld1.getBlockAt(any(Location.class))).then(invoke -> {
			Location loc = invoke.getArgumentAt(0, Location.class);
			Location blockLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			Block mockBlock = blockMap.get(blockLoc);
			if (mockBlock != null) return mockBlock;
			
			mockBlock = mock(Block.class);
			when(mockBlock.getLocation()).thenReturn(blockLoc.clone());
			blockMap.put(blockLoc.clone(), mockBlock);
			return mockBlock;
		});
		
		//Chunk handling
		Map<Location, Chunk> chunkMap = new HashMap<>();
		when(mockWorld1.getChunkAt(any(Location.class))).then(invoke -> {
			Location loc = invoke.getArgumentAt(0, Location.class);
			Location chunkLoc = new Location(loc.getWorld(), Math.floorDiv(loc.getBlockX(), 16), 0, Math.floorDiv(loc.getBlockZ(), 16));
			Chunk mockChunk = chunkMap.get(chunkLoc);
			if (mockChunk != null) return mockChunk;
			
			mockChunk = mock(Chunk.class);
			when(mockChunk.isLoaded()).thenReturn(true);
			when(mockChunk.getWorld()).thenReturn(mockWorld1);
			chunkMap.put(chunkLoc.clone(), mockChunk);
			return mockChunk;
		});
		
		//Player
		mockPlayer = mock(Player.class);
		when(mockPlayer.getUniqueId()).thenReturn(UUID.fromString("c1348e68-9704-4f8b-a41d-e79bea4dbb79"));
		when(mockPlayer.getLocation()).thenReturn(new Location(mockWorld1, 0, 70, 0));
		when(mockPlayer.getWorld()).thenReturn(mockWorld1);
		
		//Save handlers
		mockedSaveHandlers = new HashMap<>();
		SaveHandler mockSaveHandler1 = mock(SaveHandler.class);
		when(mockSaveHandler1.stream()).thenReturn(Stream.of());
		mockedSaveHandlers.put(mockWorld1, mockSaveHandler1);
		
		//Instantiate deathpoint handler
		deathpointHandler = new DeathpointHandler(mockPlugin, new ConfigOptions(new YamlConfiguration()));
	}
	
	//Tests hitbox creation/destruction
	@Test public void hitboxes() {
		Deathpoint point1 = new Deathpoint(mockPlayer, new Location(mockWorld1, 0, 100, 0), null, 50);
		Deathpoint point2 = new Deathpoint(mockPlayer, new Location(mockWorld1, -100, 90, 0), null, 45);
		SaveHandler preloadedSave = mock(SaveHandler.class);
		when(preloadedSave.stream()).thenReturn(Stream.of(point1, point2));
		mockedSaveHandlers.put(mockWorld1, preloadedSave);
		
		//Plugin is loaded, both in loaded chunks
		deathpointHandler.initWorld(mockWorld1);
		assertNotNull(Whitebox.getInternalState(point1, Entity.class));
		assertNotNull(Whitebox.getInternalState(point2, Entity.class));
		
		//First is unloaded
		deathpointHandler.onChunkUnload(new ChunkUnloadEvent(point1.getLocation().getChunk()));
		assertNull(Whitebox.getInternalState(point1, Entity.class));
		assertNotNull(Whitebox.getInternalState(point2, Entity.class));
		
		//Second is unloaded
		deathpointHandler.onChunkUnload(new ChunkUnloadEvent(point2.getLocation().getChunk()));
		assertNull(Whitebox.getInternalState(point1, Entity.class));
		assertNull(Whitebox.getInternalState(point2, Entity.class));
		
		//First is loaded
		deathpointHandler.onChunkLoad(new ChunkLoadEvent(point1.getLocation().getChunk(), false));
		assertNotNull(Whitebox.getInternalState(point1, Entity.class));
		assertNull(Whitebox.getInternalState(point2, Entity.class));
		
		//Second is loaded
		deathpointHandler.onChunkLoad(new ChunkLoadEvent(point2.getLocation().getChunk(), false));
		assertNotNull(Whitebox.getInternalState(point1, Entity.class));
		assertNotNull(Whitebox.getInternalState(point2, Entity.class));
		
		//Plugin is disabled
		deathpointHandler.deinit();
		assertNull(Whitebox.getInternalState(point1, Entity.class));
		assertNull(Whitebox.getInternalState(point2, Entity.class));
	}
	
	//Test compatibility with other plugins that modify drops on death
	@Test public void itemHandling() {
		ItemStack[] items = {new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.APPLE, 23),
				new ItemStack(Material.TORCH, 59), new ItemStack(Material.GOLD_NUGGET, 5), new ItemStack(Material.WRITTEN_BOOK)};
		
		deathpointHandler.initWorld(mockWorld1);
		
		//Mock player
		when(mockPlayer.getMetadata("lastSafePosition")).thenReturn(Arrays.asList(new FixedMetadataValue(mockPlugin, new Location(mockWorld1, 10, 60, -10))));
		PlayerInventory mockInventory = mock(PlayerInventory.class);
		when(mockPlayer.getInventory()).thenReturn(mockInventory);
		
		//Book is kept from dropping by another plugin
		when(mockInventory.getContents()).thenReturn(Arrays.copyOf(new ItemStack[]{items[0], items[1], items[4], items[2]}, 41));
		//Gold is added by another plugin
		List<ItemStack> drops = new ArrayList<>(Arrays.asList(items[0], items[1], items[2], items[3]));
		
		//Send event
		PlayerDeathEvent event = new PlayerDeathEvent(mockPlayer, drops, 10, "");
		event.setKeepInventory(false);
		deathpointHandler.onPlayerDeath(event);
		
		//Gold should still drop
		assertEquals(1, event.getDrops().size());
		assertEquals(Material.GOLD_NUGGET, event.getDrops().get(0).getType());
		
		//Should be one deathpoint
		Map<String, Deque<Deathpoint>> deathpointMap = Whitebox.getInternalState(deathpointHandler, "deathpoints");
		Deque<Deathpoint> deathpoints = deathpointMap.get(mockPlayer.getWorld().getName());
		assertEquals(1, deathpoints.size());
		
		//Book should not be stored
		List<ItemStack> resultingContents = Arrays.asList(deathpoints.peek().getInventory().getContents());
		assertTrue(resultingContents.containsAll(Arrays.asList(items[0], items[1], items[2])));
		assertFalse(resultingContents.contains(items[4]));
	}
	
}
