package com.lesserhydra.secondchance;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.lesserhydra.testing.FakeWorld.mockBukkitWorld;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.lesserhydra.secondchance.configuration.ConfigOptions;
import com.lesserhydra.testing.Capsule;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WorldHandler.class, Bukkit.class, JavaPlugin.class})
@PowerMockIgnore("javax.management.*")
public class DeathpointHandlerTest {
	
	private SecondChance mockPlugin;
	private Map<String, SaveHandler> mockedSaves = new HashMap<>();
	
	private DeathpointHandler deathpointHandler;
	
	@Before public void init() throws Exception {
		PowerMockito.mockStatic(Bukkit.class, JavaPlugin.class);
		
		//Scheduler and server
		BukkitScheduler mockScheduler = mock(BukkitScheduler.class);
		when(mockScheduler.runTaskTimer(any(Plugin.class), any(Runnable.class), anyLong(), anyLong())).thenReturn(mock(BukkitTask.class));
		Server mockServer = mock(Server.class);
		when(mockServer.getPluginManager()).thenReturn(new SimplePluginManager(mockServer, new SimpleCommandMap(mockServer)));
		BDDMockito.given(Bukkit.getScheduler()).willReturn(mockScheduler);
		BDDMockito.given(Bukkit.getServer()).willReturn(mockServer);
		//Inventory creation
		when(mockServer.createInventory(any(InventoryHolder.class), anyInt(), anyString())).then(DeathpointHandlerTest::createMockInventory);
		
		//Main plugin
		mockPlugin = mock(SecondChance.class);
		BDDMockito.given(JavaPlugin.getPlugin(eq(SecondChance.class))).willReturn(mockPlugin);
		
		PowerMockito.whenNew(SaveHandler.class).withArguments(any(File.class), any(World.class)).then(this::getMockSaveHandler);
		
		//Instantiate deathpoint handler
		deathpointHandler = new DeathpointHandler(mockPlugin);
	}
	
	//Test hitbox creation/destruction
	@Test public void hitboxHandling() {
		World mockWorld1 = mockBukkitWorld("world1");
		World mockWorld2 = mockBukkitWorld("world2");
		BDDMockito.given(Bukkit.getWorlds()).willReturn(Arrays.asList(mockWorld1, mockWorld2));
		
		Player mockPlayer = mock(Player.class);
		when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
		Deathpoint point1 = new Deathpoint(mockPlayer, new Location(mockWorld1, 0, 0, 0), null, 0);
		Deathpoint point2 = new Deathpoint(mockPlayer, new Location(mockWorld1, -1, 0, -1), null, 0);
		Deathpoint point3 = new Deathpoint(mockPlayer, new Location(mockWorld2, 0, 0, 0), null, 0);
		SaveHandler saveWorld1 = getMockSaveHandler(mockWorld1);
		SaveHandler saveWorld2 = getMockSaveHandler(mockWorld2);
		saveWorld1.put(point1);
		saveWorld1.put(point2);
		saveWorld2.put(point3);
		
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
	
	//Test compatibility with other plugins that modify drops on death
	@Test public void itemHandling() {
		/*----------Given----------*/
		//Items
		ItemStack[] items = {new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.APPLE), new ItemStack(Material.TORCH),
				new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.WRITTEN_BOOK)};
		
		World mockWorld = mockBukkitWorld("world");
		when(mockWorld.getGameRuleValue(eq("keepInventory"))).thenReturn("false");
		BDDMockito.given(Bukkit.getWorlds()).willReturn(Arrays.asList(mockWorld));
		deathpointHandler.init(new ConfigOptions(new YamlConfiguration()));
		
		//Player
		Player mockPlayer = mock(Player.class);
		when(mockPlayer.getName()).thenReturn("TestPlayer1");
		when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
		when(mockPlayer.getWorld()).thenReturn(mockWorld);
		when(mockPlayer.hasPermission(eq(SecondChance.enabledPermission))).thenReturn(true);
		when(mockPlayer.getMetadata("lastSafePosition")).thenReturn(Arrays.asList(new FixedMetadataValue(mockPlugin, new Location(mockWorld, 10, 60, -10))));
		PlayerInventory mockInventory = mock(PlayerInventory.class);
		when(mockPlayer.getInventory()).thenReturn(mockInventory);
		
		//Book is kept from dropping by another plugin
		when(mockInventory.getContents()).thenReturn(Arrays.copyOf(new ItemStack[]{items[0], items[1], items[4], items[2]}, 41));
		//Gold is added by another plugin
		List<ItemStack> drops = new ArrayList<>(Arrays.asList(items[0], items[1], items[2], items[3]));
		
		/*----------When----------*/
		//Send event
		PlayerDeathEvent event = new PlayerDeathEvent(mockPlayer, drops, 10, "");
		event.setKeepInventory(false);
		deathpointHandler.onPlayerDeath(event);
		
		/*----------Then----------*/
		SaveHandler saveHandler = getMockSaveHandler(mockWorld);
		
		//Gold (only) should still drop
		assertEquals(1, event.getDrops().size());
		assertEquals(items[3], event.getDrops().get(0));
		
		//Should be one deathpoint
		assertEquals(1, saveHandler.stream().count());
		
		//Book and gold should not be stored
		Deathpoint finalDeathpoint = saveHandler.stream().findFirst().get();
		List<ItemStack> resultingContents = Arrays.asList(finalDeathpoint.getInventory().getContents());
		assertTrue(resultingContents.containsAll(Arrays.asList(items[0], items[1], items[2])));
		assertFalse(resultingContents.contains(items[3]));
		assertFalse(resultingContents.contains(items[4]));
	}
	
	private SaveHandler getMockSaveHandler(InvocationOnMock invoke) {
		return getMockSaveHandler(invoke.getArgumentAt(1, World.class));
	}
	
	private SaveHandler getMockSaveHandler(World world) {
		String worldName = world.getName();
		SaveHandler result = mockedSaves.get(worldName);
		if (result != null) return result;
		
		result = mock(SaveHandler.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
		doNothing().when(result).load();
		doNothing().when(result).save();
		Whitebox.setInternalState(result, YamlConfiguration.class, new YamlConfiguration());
		
		mockedSaves.put(worldName, result);
		return result;
	}
	
	private static Inventory createMockInventory(InvocationOnMock invoke) {
		Capsule<ItemStack[]> contents = new Capsule<>();
		Inventory inv = mock(Inventory.class);
		doAnswer(i -> contents.set(i.getArgumentAt(0, ItemStack[].class))).when(inv).setContents(any(ItemStack[].class));
		when(inv.getContents()).then(i -> contents.get());
		return inv;
	}
	
}
