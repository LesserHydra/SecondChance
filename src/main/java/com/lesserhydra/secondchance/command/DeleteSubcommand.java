package com.lesserhydra.secondchance.command;

import com.lesserhydra.secondchance.Deathpoint;
import com.lesserhydra.secondchance.SecondChance;
import com.lesserhydra.secondchance.WorldHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DeleteSubcommand implements Subcommand {
	
	private final MainCommand mainCommand;
	
	public DeleteSubcommand(MainCommand mainCommand) {
		this.mainCommand = mainCommand;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 1 || !args[0].matches("\\d+")) {
			sender.sendMessage(ChatColor.RED + "Error: Wrong number of arguments.");
			sendUsage(sender, label);
			return;
		}
		
		int index = Integer.parseInt(args[0]);
		Deathpoint deathpoint = mainCommand.getListed().getDeathpoint(sender, index);
		if (deathpoint == null) {
			sender.sendMessage(ChatColor.RED + "Error: Unknown index. Have you run the 'list' subcommand yet?");
			return;
		}
		if (deathpoint.isInvalid()) {
			sender.sendMessage(ChatColor.RED + "Error: Deathpoint no longer exists.");
			return;
		}
		
		WorldHandler worldHandler = SecondChance.instance().worldHandlers()
				.filter(handler -> handler.getWorld().equals(deathpoint.getWorld()))
				.findAny().orElse(null);
		
		if (worldHandler == null) {
			sender.sendMessage(ChatColor.RED + "Error: The world this deathpoint exists in is no longer enabled.");
			return;
		}
		
		worldHandler.destroyDeathpoint(deathpoint);
		sender.sendMessage(ChatColor.GREEN + "Success");
	}
	
	private void sendUsage(CommandSender sender, String label) {
		sender.sendMessage(ChatColor.GRAY + "Usage: /" + label + " delete <index>");
		sender.sendMessage(ChatColor.GRAY + "Use the 'list' subcommand first, to get a list of deathpoints to work with.");
	}
	
}
