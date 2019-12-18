package com.lesserhydra.secondchance;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;


public class Util {
	
	@Nullable
	public static Location entityLocationIsSafe(Entity entity) {
		//Use vehicle location, if exists
		Entity vehicle = entity.getVehicle();
		if (vehicle != null) return entityLocationIsSafe(vehicle);
		
		//Must be on the ground
		if (!entity.isOnGround()) return null;
		//Feet block must be nonsolid and nonliquid
		Block feetBlock = entity.getLocation().getBlock();
		if (feetBlock.getType().isOccluding() || feetBlock.isLiquid()) return null;
		//Head block must be nonsolid and nonliquid
		Block headBlock = feetBlock.getRelative(BlockFace.UP);
		if (headBlock.getType().isOccluding() || headBlock.isLiquid()) return null;
		
		//Ground block must be solid
		Block groundBlock = feetBlock.getRelative(BlockFace.DOWN);
		if (!groundBlock.getType().isSolid()) return null;
		
		return entity.getLocation();
	}
	
}
