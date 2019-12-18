package com.lesserhydra.secondchance;

import static com.lesserhydra.testing.FakeWorld.mockBukkitWorld;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

public class UtilTest {
	
	private World mockWorld;
	
	@Before public void init() {
		mockWorld = mockBukkitWorld("world");
	}
	
	@Test public void safeLocation_OpenAir() {
		//In air
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.AIR);
		feetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.STONE);
		
		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(true);
		when(player.getLocation()).thenReturn(location);
		
		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNotNull(safeLocation);
		assertEquals(location.getBlock(), safeLocation.getBlock());
	}
	
	@Test public void safeLocation_InSign() {
		//In sign
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.OAK_SIGN);
		feetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.STONE);

		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(true);
		when(player.getLocation()).thenReturn(location);

		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNotNull(safeLocation);
		assertEquals(location.getBlock(), safeLocation.getBlock());
	}
	
	@Test public void safeLocation_MountedInOpen() {
		//Riding a mount in the open
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.AIR);
		feetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN, 2).setType(Material.STONE);
		
		Entity mount = mock(Entity.class);
		when(mount.isOnGround()).thenReturn(true);
		when(mount.getLocation()).thenReturn(location.clone().add(0, -1, 0));
		
		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(false);
		when(player.getLocation()).thenReturn(location);
		when(player.isInsideVehicle()).thenReturn(true);
		when(player.getVehicle()).thenReturn(mount);
		
		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNotNull(safeLocation);
		assertEquals(location.getBlock().getRelative(BlockFace.DOWN), safeLocation.getBlock());
	}
	
	@Test public void safeLocation_MountedInGrass() {
		//Riding a mount in the open
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.AIR);
		feetBlock.getRelative(BlockFace.UP).setType(Material.TALL_GRASS);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.TALL_GRASS);
		feetBlock.getRelative(BlockFace.DOWN, 2).setType(Material.STONE);
		
		Entity mount = mock(Entity.class);
		when(mount.isOnGround()).thenReturn(true);
		when(mount.getLocation()).thenReturn(location.clone().add(0, -1, 0));
		
		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(false);
		when(player.getLocation()).thenReturn(location);
		when(player.isInsideVehicle()).thenReturn(true);
		when(player.getVehicle()).thenReturn(mount);
		
		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNotNull(safeLocation);
		assertEquals(location.getBlock().getRelative(BlockFace.DOWN), safeLocation.getBlock());
	}
	
	@Test public void safeLocation_MountedInWater() {
		//Riding a mount in the open
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.AIR);
		feetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.WATER);
		feetBlock.getRelative(BlockFace.DOWN, 2).setType(Material.STONE);
		
		Entity mount = mock(Entity.class);
		when(mount.isOnGround()).thenReturn(true);
		when(mount.getLocation()).thenReturn(location.clone().add(0, -1, 0));
		
		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(false);
		when(player.getLocation()).thenReturn(location);
		when(player.isInsideVehicle()).thenReturn(true);
		when(player.getVehicle()).thenReturn(mount);
		
		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNull(safeLocation);
	}
	
	@Test public void safeLocation_NotOnGround() {
		//Jumping, perhaps
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70.2, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.AIR);
		feetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.STONE);

		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(false);
		when(player.getLocation()).thenReturn(location);

		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNull(safeLocation);
	}
	
	@Test public void safeLocation_InLava() {
		//Lava at head level
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.LAVA);
		feetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.STONE);

		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(true);
		when(player.getLocation()).thenReturn(location);

		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNull(safeLocation);
	}
	
	@Test public void safeLocation_NoGround() {
		//On ground, no block below
		/*----------Given----------*/
		Location location = new Location(mockWorld, 0, 70, 0);
		Block feetBlock = location.getBlock();
		feetBlock.setType(Material.AIR);
		feetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
		feetBlock.getRelative(BlockFace.DOWN).setType(Material.AIR);
		
		Player player = mock(Player.class);
		when(((Entity)player).isOnGround()).thenReturn(true);
		when(player.getLocation()).thenReturn(location);
		
		/*----------When----------*/
		Location safeLocation = Util.entityLocationIsSafe(player);
		
		/*----------Then----------*/
		assertNull(safeLocation);
	}
	
}
