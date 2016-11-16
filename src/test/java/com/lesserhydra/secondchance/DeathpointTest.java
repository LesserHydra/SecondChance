package com.lesserhydra.secondchance;

import com.lesserhydra.testing.FakeBukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class DeathpointTest {
	
	private List<Deathpoint> deathpoints;
	
	@BeforeClass
	public static void initClass() {
		FakeBukkit.setup();
	}
	
	@Before
	public void init() {
		FakeBukkit.clear();
		
		//Mock world and players
		World mockWorld = FakeBukkit.makeWorld("world");
		Player player1 = FakeBukkit.makePlayer("Steve");
		Player player2 = FakeBukkit.makePlayer("Alex");
		
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
	 * Test deathpoint serialization and deserialization
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
		
		assertEquals(point1, point1Later);
		assertEquals(point1Later, point1);
		assertEquals(point1.hashCode(), point1Later.hashCode());
		
		assertNotEquals(point1, point2);
		assertNotEquals(point2, point1);
		assertNotEquals(point1Later, point2);
		assertNotEquals(point2, point1Later);
		//assertNotEquals(point1.hashCode(), point2.hashCode());
		//assertNotEquals(point1Later.hashCode(), point2.hashCode());
	}
	
}
