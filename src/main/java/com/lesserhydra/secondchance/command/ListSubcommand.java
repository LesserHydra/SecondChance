package com.lesserhydra.secondchance.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ListSubcommand implements Subcommand {
	
	private final MainCommand mainCommand;
	
	ListSubcommand(MainCommand mainCommand) {
		this.mainCommand = mainCommand;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 3) {
			sender.sendMessage(ChatColor.RED + "Wrong number of arguments.");
			sendUsage(sender, label);
			return;
		}
		
		Arguments parsed = parseArguments(args);
		if (parsed == null) {
			sender.sendMessage(ChatColor.RED + "Error in parsing arguments."); //TODO: More descriptive
			sendUsage(sender, label);
			return;
		}
		
		mainCommand.getListed().list(sender, parsed.owner, parsed.world, parsed.radius);
	}
	
	@Override
	public List<String> autoCompleteArg(String[] args) {
		Arguments parsed = new Arguments();
		
		if (args.length == 1) {
			Stream<String> worldNames = Bukkit.getWorlds().stream().map(World::getName);
			Stream<String> playerNames = Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName);
			return Stream.concat(worldNames, playerNames)
					.filter(name -> name.regionMatches(true, 0, args[0], 0, args[0].length()))
					.collect(Collectors.toList());
		}
		else if (args.length == 2) {
			if (parsed.parseOwner(args[0])) return Bukkit.getWorlds().stream()
					.map(World::getName)
					.filter(name -> name.regionMatches(true, 0, args[0], 0, args[0].length()))
					.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	private void sendUsage(CommandSender sender, String label) {
		sender.sendMessage(ChatColor.GRAY + "Usage: /" + label + " list [owner] [world] [radius]");
	}
	
	private Arguments parseArguments(String[] args) {
		Arguments result = new Arguments();
		List<Function<String, Boolean>> parsers = Arrays.asList(result::parseOwner, result::parseWorld, result::parseRadius);
		int lastSuccess = -1;
		for (String arg : args) {
			boolean success = false;
			for (int j = lastSuccess + 1; j < 3; j++) {
				if (!parsers.get(j).apply(arg)) continue;
				lastSuccess = j;
				success = true;
				break;
			}
			if (!success) return null;
		}
		return result;
	}
	
	private static class Arguments {
		World world = null;
		OfflinePlayer owner = null;
		int radius = -1;
		
		boolean parseOwner(String arg) {
			owner = Arrays.stream(Bukkit.getOfflinePlayers())
					.filter(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(arg))
					.findAny().orElse(null);
			return owner != null;
		}
		
		boolean parseWorld(String arg) {
			world = Bukkit.getWorld(arg);
			return world != null;
		}
		
		boolean parseRadius(String arg) {
			radius = (arg.matches("\\d+") ? Integer.parseInt(arg) : -1);
			return radius >= 0;
		}
	}
	
}
