package com.lesserhydra.secondchance;

import com.lesserhydra.secondchance.configuration.ConfigOptions;
import com.lesserhydra.testing.FakeBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeathpointHandlerTest {
	
	private SecondChance mockPlugin;
	private SaveHandler fakeSaveHandler;
	private DeathpointHandler deathpointHandler;
	
	@BeforeClass public static void beforeAll() {
		FakeBukkit.setup();
	}
	
	@Before public void init() {
		FakeBukkit.clear();
		
		//Main plugin
		mockPlugin = mock(SecondChance.class);
		Whitebox.setInternalState(SecondChance.class, SecondChance.class, mockPlugin);
		Whitebox.setInternalState(mockPlugin, "isEnabled", true);
		
		fakeSaveHandler = new MapSaveHandler();
		when(mockPlugin.getSaveHandler()).thenReturn(fakeSaveHandler);
		
		deathpointHandler = new DeathpointHandler(mockPlugin);
	}
	
	@Test public void taskCanceling() {
		World world = FakeBukkit.makeWorld("world");
		
		deathpointHandler.init(new ConfigOptions(new YamlConfiguration()));
		assertFalse(Bukkit.getScheduler().getPendingTasks().isEmpty());
		
		deathpointHandler.deinit();
		assertTrue(Bukkit.getScheduler().getPendingTasks().isEmpty());
	}
	
	//World is enabled, make deathpoint
	@Test public void worldEnabled() {
		World world = FakeBukkit.makeWorld("world");
		when(world.getGameRuleValue(eq("keepInventory"))).thenReturn("false");
		deathpointHandler.init(new ConfigOptions(new YamlConfiguration()));
		
		Player player = FakeBukkit.makePlayer("Billy Bob", new Location(world, 0, 0, 0));
		when(player.hasPermission(eq(SecondChance.enabledPermission))).thenReturn(true);
		when(player.getMetadata("lastSafePosition"))
				.thenReturn(Collections.singletonList(new FixedMetadataValue(mockPlugin, new Location(world, 0, 0, 0))));
		when(player.getLevel()).thenReturn(100);
		
		/*----------When----------*/
		//Send event
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.emptyList(), 0, "Killed by demon penguins with flamethrowers");
		deathpointHandler.onPlayerDeath(event);
		deathpointHandler.deinit();
		
		/*----------Then----------*/
		Deque<Deathpoint> resultingDeathpoints = fakeSaveHandler.load(world);
		
		//Should be one deathpoint
		assertEquals(1, resultingDeathpoints.size());
	}
	
	//World is disabled, no deathpoint
	@Test public void worldDisabled() {
		World disabledWorld = FakeBukkit.makeWorld("creative_world");
		when(disabledWorld.getGameRuleValue(eq("keepInventory"))).thenReturn("false");
		
		ConfigOptions options = new ConfigOptions(new YamlConfiguration());
		Whitebox.setInternalState(options, Collections.singleton("creative_world"));
		deathpointHandler.init(options);
		
		Player player = FakeBukkit.makePlayer("Billy Bob", new Location(disabledWorld, 0, 0, 0));
		when(player.hasPermission(eq(SecondChance.enabledPermission))).thenReturn(true);
		when(player.getMetadata("lastSafePosition"))
				.thenReturn(Collections.singletonList(new FixedMetadataValue(mockPlugin, new Location(disabledWorld, 0, 0, 0))));
		when(player.getLevel()).thenReturn(100);
		
		/*----------When----------*/
		//Send event
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.emptyList(), 0, "Killed by werewolf monkeys");
		deathpointHandler.onPlayerDeath(event);
		deathpointHandler.deinit();
		
		/*----------Then----------*/
		Deque<Deathpoint> resultingDeathpoints = fakeSaveHandler.load(disabledWorld);
		
		//Should be no deathpoints
		assertEquals(0, resultingDeathpoints.size());
	}
	
	//Test compatibility with other plugins that modify drops on death
	@Test public void itemHandling() {
		/*----------Given----------*/
		//Items
		final ItemStack SWORD = new ItemStack(Material.DIAMOND_SWORD);
		final ItemStack APPLE = new ItemStack(Material.APPLE);
		final ItemStack TORCH = new ItemStack(Material.TORCH);
		final ItemStack GUIDE_BOOK = new ItemStack(Material.WRITTEN_BOOK);
		final ItemStack HEAD_DROP = new ItemStack(Material.SKULL_ITEM);
		
		World mockWorld = FakeBukkit.makeWorld("world");
		when(mockWorld.getGameRuleValue(eq("keepInventory"))).thenReturn("false");
		
		deathpointHandler.init(new ConfigOptions(new YamlConfiguration()));
		
		//Player
		Player mockPlayer = FakeBukkit.makePlayer("TestPlayer1", new Location(mockWorld, 0, 0, 0));
		when(mockPlayer.hasPermission(eq(SecondChance.enabledPermission))).thenReturn(true);
		when(mockPlayer.getMetadata("lastSafePosition")).thenReturn(Collections.singletonList(new FixedMetadataValue(mockPlugin, new Location(mockWorld, 10, 60, -10))));
		
		//Book is kept from dropping by another plugin
		mockPlayer.getInventory().setContents(new ItemStack[]{SWORD, APPLE, GUIDE_BOOK, TORCH});
		//Head is added by another plugin (as a reward for killer, or something)
		List<ItemStack> drops = new ArrayList<>(Arrays.asList(SWORD, APPLE, TORCH, HEAD_DROP));
		
		/*----------When----------*/
		//Send event
		PlayerDeathEvent event = new PlayerDeathEvent(mockPlayer, drops, 10, "");
		event.setKeepInventory(false);
		deathpointHandler.onPlayerDeath(event);
		deathpointHandler.deinit();
		
		/*----------Then----------*/
		Deque<Deathpoint> resultingDeathpoints = fakeSaveHandler.load(mockWorld);
		
		//Head (only) should still drop
		assertEquals(1, event.getDrops().size());
		assertEquals(HEAD_DROP, event.getDrops().get(0));
		
		//Should be one deathpoint
		assertEquals(1, resultingDeathpoints.size());
		
		//Book and head should not be stored
		Deathpoint finalDeathpoint = resultingDeathpoints.peek();
		List<ItemStack> resultingContents = Arrays.asList(finalDeathpoint.getInventory().getContents());
		
		assertTrue(resultingContents.containsAll(Arrays.asList(SWORD, APPLE, TORCH)));
		assertFalse(resultingContents.contains(HEAD_DROP));
		assertFalse(resultingContents.contains(GUIDE_BOOK));
	}
		
	//Test hitbox creation/destruction
	@Test public void hitboxHandling() {
		World mockWorld1 = FakeBukkit.makeWorld("world1");
		World mockWorld2 = FakeBukkit.makeWorld("world2");
		
		Player mockPlayer = FakeBukkit.makePlayer("player");
		Deathpoint point1 = new Deathpoint(mockPlayer, new Location(mockWorld1, 0, 0, 0), null, 0, -1, -1L);
		Deathpoint point2 = new Deathpoint(mockPlayer, new Location(mockWorld1, -1, 0, -1), null, 0, -1, -1L);
		Deathpoint point3 = new Deathpoint(mockPlayer, new Location(mockWorld2, 0, 0, 0), null, 0, -1, -1L);
		
		fakeSaveHandler.save(mockWorld1, Arrays.asList(point1, point2));
		fakeSaveHandler.save(mockWorld2, Collections.singletonList(point3));
		
		//When plugin is loaded (and chunks are loaded)
		deathpointHandler.init(new ConfigOptions(new YamlConfiguration()));
		assertNotNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point3, ArmorStand.class));
		
		//When first is unloaded
		deathpointHandler.onChunkUnload(new ChunkUnloadEvent(point1.getLocation().getChunk()));
		assertNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point3, ArmorStand.class));
		
		//When second is unloaded
		deathpointHandler.onChunkUnload(new ChunkUnloadEvent(point2.getLocation().getChunk()));
		assertNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point3, ArmorStand.class));
		
		//When third is unloaded
		deathpointHandler.onChunkUnload(new ChunkUnloadEvent(point3.getLocation().getChunk()));
		assertNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNull(Whitebox.getInternalState(point3, ArmorStand.class));
		
		//When third is loaded
		deathpointHandler.onChunkLoad(new ChunkLoadEvent(point3.getLocation().getChunk(), false));
		assertNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point3, ArmorStand.class));
		
		//When second is loaded
		deathpointHandler.onChunkLoad(new ChunkLoadEvent(point2.getLocation().getChunk(), false));
		assertNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point3, ArmorStand.class));
		
		//When first is loaded
		deathpointHandler.onChunkLoad(new ChunkLoadEvent(point1.getLocation().getChunk(), false));
		assertNotNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNotNull(Whitebox.getInternalState(point3, ArmorStand.class));
		
		//When plugin is disabled
		deathpointHandler.deinit();
		assertNull(Whitebox.getInternalState(point1, ArmorStand.class));
		assertNull(Whitebox.getInternalState(point2, ArmorStand.class));
		assertNull(Whitebox.getInternalState(point3, ArmorStand.class));
	}
	
}
