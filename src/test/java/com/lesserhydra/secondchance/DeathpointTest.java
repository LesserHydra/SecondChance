package com.lesserhydra.secondchance;

import static com.lesserhydra.testing.FakeWorld.mockBukkitWorld;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.lesserhydra.testing.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class})
public class DeathpointTest {
	
	private World mockWorld;
	private Player player1;
	private Player player2;
	
	private List<Deathpoint> deathpoints;
	
	@Before public void init() {
		PowerMockito.mockStatic(Bukkit.class);
		
		//Mock server
		Server mockServer = mock(Server.class);
		when(mockServer.createInventory(any(InventoryHolder.class), anyInt(), anyString())).then(TestUtils::createMockInventory);
		
		//Mock item factory
		ItemFactory mockItemFactory = mock(ItemFactory.class);
		when(mockItemFactory.equals(any(), any())).thenReturn(true); //Hacky, but meta comp fails by default otherwise
		
		//Get static Bukkit methods to return mocks
		BDDMockito.given(Bukkit.getServer()).willReturn(mockServer);
		BDDMockito.given(Bukkit.getItemFactory()).willReturn(mockItemFactory);
		
		//Mock world and players
		this.mockWorld = mockBukkitWorld("world");
		BDDMockito.given(Bukkit.getWorld(eq("world"))).willReturn(mockWorld);
		this.player1 = mock(Player.class);
		this.player2 = mock(Player.class);
		when(player1.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player2.getUniqueId()).thenReturn(UUID.randomUUID());
		
		//Create deathpoints
		ItemStack[] inv1 = Arrays.copyOf(new ItemStack[] {new ItemStack(Material.POTATO_ITEM)}, 41);
		ItemStack[] inv2 = Arrays.copyOf(new ItemStack[] {new ItemStack(Material.REDSTONE_LAMP_OFF, 64), new ItemStack(Material.REDSTONE)}, 41);
		ItemStack[] inv3 = new ItemStack[41];
		Deathpoint deathpoint1 = new Deathpoint(player1, new Location(mockWorld, 0.03, 555, 10.2), inv1, 99, 3, 5000);
		Deathpoint deathpoint2 = new Deathpoint(player2, new Location(mockWorld, 50, 404, 42), inv2, 1337, 1, -1);
		Deathpoint deathpoint3 = new Deathpoint(player1, new Location(mockWorld, 0, 0, 0), inv3, 1000, -1, 1);
		deathpoints = Arrays.asList(deathpoint1, deathpoint2, deathpoint3);
	}
	
	/*
	 * Test deathpoint serializarion and deserialization
	 */
	@Test
	public void serialization() {
		/*----------Given----------*/
		//Register needed classes for serialization
		ConfigurationSerialization.registerClass(Deathpoint.class);
		ConfigurationSerialization.registerClass(Location.class);
		ConfigurationSerialization.registerClass(ItemStack.class);
		
		/*----------When----------*/
		//Save deathpoints to YAML string
		YamlConfiguration save = new YamlConfiguration();
		save.set("deathpoints", deathpoints);
		String saveString = save.saveToString();
		
		//Load from string
		YamlConfiguration load = YamlConfiguration.loadConfiguration(new StringReader(saveString));
		List<Deathpoint> loadedDeathpoints = load.getList("deathpoints").stream()
				.map(Deathpoint.class::cast)
				.collect(Collectors.toList());
		
		/*----------Then----------*/
		//Match saved & loaded deathpoints
		assertEquals(deathpoints.size(), loadedDeathpoints.size());
		for (int i = 0; i < deathpoints.size(); i++) {
			Deathpoint savePoint = deathpoints.get(i);
			Deathpoint loadPoint = loadedDeathpoints.get(i);
			
			assertEquals(savePoint, loadPoint);
			assertEquals(savePoint.hashCode(), loadPoint.hashCode());
			
			assertEquals(savePoint.getOwnerUniqueId(), loadPoint.getOwnerUniqueId());
			assertEquals(savePoint.getCreationInstant(), loadPoint.getCreationInstant());
			assertEquals(savePoint.getWorld(), loadPoint.getWorld());
			assertEquals(savePoint.getLocation(), loadPoint.getLocation());
			assertEquals(savePoint.getTicksToLive(), loadPoint.getTicksToLive());
			assertEquals(savePoint.getTimeToLive(), loadPoint.getTimeToLive());
			assertArrayEquals(savePoint.getInventory().getContents(), loadPoint.getInventory().getContents());
		}
	}

	/*
	 * Test contract with hash, equals, and clone
	 */
	@Test
	public void hashEqualsClone() {
		Deathpoint point1 = deathpoints.get(0);
		Deathpoint point2 = deathpoints.get(1);

		Deathpoint point1Later = point1.clone();
		point1Later.updateDeathsTillForget();
		point1Later.updateTicksTillForget(4990);
		
		assertTrue(point1.equals(point1Later));
		assertTrue(point1Later.equals(point1));
		assertEquals(point1.hashCode(), point1Later.hashCode());
		
		assertFalse(point1.equals(point2));
		assertFalse(point1Later.equals(point2));
		assertFalse(point2.equals(point1));
		assertFalse(point2.equals(point1Later));
		assertNotEquals(point1.hashCode(), point2.hashCode());
		assertNotEquals(point1Later.hashCode(), point2.hashCode());
	}
	
}
