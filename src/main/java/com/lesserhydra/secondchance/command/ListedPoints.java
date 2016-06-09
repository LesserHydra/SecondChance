package com.lesserhydra.secondchance.command;

import com.lesserhydra.secondchance.Deathpoint;
import com.lesserhydra.secondchance.SecondChance;
import com.lesserhydra.secondchance.WorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

class ListedPoints {
	
	//TODO: Remove after period of time
    private final Map<String, ArrayList<Deathpoint>> playerLists = new HashMap<>();
	
	
	public void list(CommandSender sender, @Nullable OfflinePlayer owner, @Nullable World world, int radius) {
		if (radius >= 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Can only check for distance if run by a player.");
				return;
			}
			
			Player playerSender = (Player) sender;
			if (world != null && !playerSender.getWorld().equals(world)) {
				sender.sendMessage("Cannot check for distance in another world.");
				return;
			}
			
			list(playerSender, owner, radius);
			return;
		}
		
		list(sender, world, owner);
	}
	
	private void list(CommandSender sender, @Nullable World world, @Nullable OfflinePlayer owner) {
		String message = "Showing deathpoints";
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
		String message = "Showing deathpoints";
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
	
	@Nullable
	public Deathpoint getDeathpoint(CommandSender sender, int index) {
		ArrayList<Deathpoint> list = playerLists.get(sender.getName());
		if (list == null) return null;
		if (index < 0 || index >= list.size()) return null;
		return list.get(index);
	}
	
	private void showListToPlayer(CommandSender sender, ArrayList<Deathpoint> list) {
		playerLists.put(sender.getName(), list);
		for (int i = 0; i < list.size(); i++) {
			Deathpoint point = list.get(i);
			Location loc = point.getLocation();
			OfflinePlayer owner = Bukkit.getOfflinePlayer(point.getOwnerUniqueId());
			sender.sendMessage(i + ": " + owner.getName() + " @" + loc.getWorld().getName() + "(" + loc.getBlockX()
					+ ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
		}
	}
	
	
}
