package com.lesserhydra.secondchance.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.lesserhydra.secondchance.SecondChance;

public class ReloadSubcommand implements Subcommand {
	
	@Override
	public void execute(CommandSender sender, Command command, String label, String[] args) {
		SecondChance.instance().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded SecondChance");
	}
	
}
