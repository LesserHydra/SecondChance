package com.lesserhydra.secondchance;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public class Util {
	
	public static Location entityLocationIsSafe(Entity entity) {
		//Use vehicle location, if exists
		if (entity.isInsideVehicle()) return entityLocationIsSafe(entity.getVehicle());
		
		//Must be on the ground
		if (!entity.isOnGround()) return null;
		//Feet block must be nonsolid and nonliquid
		Block feetBlock = entity.getLocation().getBlock();
		if (feetBlock.getType().isSolid() || feetBlock.isLiquid()) return null;
		//Head block must be nonsolid and nonliquid
		Block headBlock = feetBlock.getRelative(BlockFace.UP);
		if (headBlock.getType().isSolid() || headBlock.isLiquid()) return null;
		
		//Ground block must be solid
		Block groundBlock = feetBlock.getRelative(BlockFace.DOWN);
		if (!groundBlock.getType().isSolid()) return null;
		
		return entity.getLocation();
	}
	
}
