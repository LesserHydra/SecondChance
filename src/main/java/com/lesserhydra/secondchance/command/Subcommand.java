package com.lesserhydra.secondchance.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface Subcommand {
	
	public void execute(CommandSender sender, Command command, String label, String[] args);
	
	public default List<String> autoCompleteArg(String[] args) {
		return Collections.emptyList();
	}
	
}
