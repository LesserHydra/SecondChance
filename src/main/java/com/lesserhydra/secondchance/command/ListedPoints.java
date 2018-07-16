package com.lesserhydra.secondchance.command;

import com.lesserhydra.secondchance.Deathpoint;
import com.lesserhydra.secondchance.SecondChance;
import com.lesserhydra.secondchance.WorldHandler;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

class ListedPoints {
	
	private static final long RESET_DELAY = 6000; //Five minutes
	private final Map<String, ArrayList<Deathpoint>> playerLists = new HashMap<>();
	private final Map<String, BukkitTask> resetTasks = new HashMap<>();
	
	
	public void list(CommandSender sender, @Nullable OfflinePlayer owner, @Nullable World world, int radius) {
		if (radius >= 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Error: Can only check for distance if run by a player.");
				return;
			}
			
			Player playerSender = (Player) sender;
			if (world != null && !playerSender.getWorld().equals(world)) {
				sender.sendMessage(ChatColor.RED + "Error: Cannot check for distance in another world.");
				return;
			}
			
			list(playerSender, owner, radius);
			return;
		}
		
		list(sender, world, owner);
	}
	
	private void list(CommandSender sender, @Nullable World world, @Nullable OfflinePlayer owner) {
		String message = ChatColor.BLUE + "Showing deathpoints";
		if (owner != null) message += " for " + owner.getName();
		if (world != null) message += " in " + world.getName();
		message += ":";
		sender.sendMessage(message);
		
		ArrayList<Deathpoint> matching = SecondChance.instance().worldHandlers()
				.filter(worldHandler -> world == null || worldHandler.getWorld().equals(world))
				.flatMap(WorldHandler::deathpoints)
				.filter(deathpoint -> owner == null || deathpoint.getOwnerUniqueId().equals(owner.getUniqueId()))
				.collect(Collectors.toCollection(ArrayList::new));
		
		showListToPlayer(sender, matching);
	}
	
	private void list(Player sender, OfflinePlayer owner, int radius) {
		String message = ChatColor.BLUE + "Showing deathpoints";
		if (owner != null) message += " for " + owner.getName();
		message += " within " + radius + "m:";
		sender.sendMessage(message);
		
		int radSqr = radius * radius;
		ArrayList<Deathpoint> matching = SecondChance.instance().worldHandlers()
				.filter(worldHandler -> worldHandler.getWorld().equals(sender.getWorld()))
				.flatMap(WorldHandler::deathpoints)
				.filter(deathpoint -> owner == null || deathpoint.getOwnerUniqueId().equals(owner.getUniqueId()))
				.filter(deathpoint -> deathpoint.getLocation().distanceSquared(sender.getLocation()) <= radSqr)
				.collect(Collectors.toCollection(ArrayList::new));
		
		showListToPlayer(sender, matching);
	}
	
	private void showListToPlayer(CommandSender sender, ArrayList<Deathpoint> list) {
		//(Re)Start reset timer
		BukkitTask resetTask = resetTasks.get(sender.getName());
		if (resetTask != null) resetTask.cancel();
		resetTask = Bukkit.getScheduler().runTaskLater(SecondChance.instance(), () -> {
			resetTasks.remove(sender.getName());
			playerLists.remove(sender.getName());
		}, RESET_DELAY);
		resetTasks.put(sender.getName(), resetTask);
		
		//Remember indices
		playerLists.put(sender.getName(), list);
		
		//Show to player
		for (int i = 0; i < list.size(); i++) {
			Deathpoint point = list.get(i);
			Location loc = point.getLocation();
			OfflinePlayer owner = Bukkit.getOfflinePlayer(point.getOwnerUniqueId());
			sender.sendMessage(ChatColor.AQUA.toString() + i + ": " + owner.getName() + " @" + loc.getWorld().getName()
					+ " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
		}
		
		//Send reset info message
		if (list.isEmpty()) sender.sendMessage(ChatColor.RED + "None found.");
		else sender.sendMessage(ChatColor.GREEN + "Indices are valid for the next " + RESET_DELAY/1200 + " minutes.");
	}
	
	@Nullable
	Deathpoint getDeathpoint(CommandSender sender, int index) {
		ArrayList<Deathpoint> list = playerLists.get(sender.getName());
		if (list == null) return null;
		if (index < 0 || index >= list.size()) return null;
		return list.get(index);
	}
	
}
