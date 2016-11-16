package com.lesserhydra.secondchance.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

interface Subcommand {
	
	void execute(CommandSender sender, Command command, String label, String[] args);
	
	default List<String> autoCompleteArg(String[] args) {
		return Collections.emptyList();
	}
	
}
