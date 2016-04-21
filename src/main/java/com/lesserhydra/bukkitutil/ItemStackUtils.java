package com.lesserhydra.bukkitutil;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {
	
	public static boolean isValid(ItemStack item) {
		return (item != null && item.getType() != Material.AIR);
	}
	
}
