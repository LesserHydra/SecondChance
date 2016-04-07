package com.roboboy.bukkitutil;

import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {
	
	public static boolean isValid(ItemStack item) {
		return (item != null && item.getType() != Material.AIR);
	}
	
	public static boolean anyValid(ItemStack... items) {
		return Arrays.stream(items).anyMatch(ItemStackUtils::isValid);
	}
	
}
