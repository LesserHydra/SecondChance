package com.lesserhydra.testing;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public abstract class FakeBlock implements Block {
	
	private World world;
	private int x;
	private int y;
	private int z;
	private Material type;
	
	public static Block mockBukkitBlock(World world, int x, int y, int z) {
		FakeBlock fakeBlock = mock(FakeBlock.class, withSettings().defaultAnswer(CALLS_REAL_METHODS).stubOnly());
		fakeBlock.world = world;
		fakeBlock.x = x;
		fakeBlock.y = y;
		fakeBlock.z = z;
		fakeBlock.type = Material.AIR;
		return fakeBlock;
	}
	
	@Override
	public Material getType() {
		return type;
	}
	
	@Override
	public void setType(Material type) {
		this.type = type;
	}
	
	@Override
	public Location getLocation() {
		return new Location(world, x, y, z);
	}
	
	@Override
	public Block getRelative(BlockFace face) {
		return world.getBlockAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
	}
	
	@Override
	public Block getRelative(BlockFace face, int distance) {
		return world.getBlockAt(x + distance*face.getModX(), y + distance*face.getModY(), z + distance*face.getModZ());
	}
	
	@Override
	public boolean isLiquid() {
		return (type == Material.WATER || type == Material.STATIONARY_WATER
				|| type == Material.LAVA || type == Material.STATIONARY_LAVA);
	}
	
}
