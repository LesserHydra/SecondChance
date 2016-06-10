package com.lesserhydra.secondchance.command;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.lesserhydra.secondchance.SecondChance;
import com.lesserhydra.util.MapBuilder;

public class MainCommand implements TabCompleter, CommandExecutor {
	
	private final Map<String, Subcommand> subcommands = MapBuilder.init(HashMap<String, Subcommand>::new)
			.put("list", new ListSubcommand(this))
			.put("break", new BreakSubcommand(this))
			.put("delete", new DeleteSubcommand(this))
			.put("reload", new ReloadSubcommand())
			.buildImmutable();
	
	private ListedPoints listed = new ListedPoints();
	

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		//Check permission node
		if (!sender.hasPermission(SecondChance.commandPermission)) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return true;
		}
		
		//No subcommand given
		if (args.length == 0) return false;
		
		//Subcommand
		Subcommand sub = subcommands.get(args[0].toLowerCase());
		if (sub == null) return false; //Invalid
		sub.execute(sender, command, alias, getSubcommandArgs(args));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		//Check permission node
		if (!sender.hasPermission(SecondChance.commandPermission)) return Collections.emptyList();
		
		//Autocomplete the subcommand name
		if (args.length <= 1) return completeSubCmd(args.length == 0 ? "" : args[0].toLowerCase());
		
		//Autocomplete the subcommand arguments
		Subcommand sub = subcommands.get(args[0].toLowerCase());
		if (sub == null) return Collections.emptyList(); //Invalid
		return sub.autoCompleteArg(getSubcommandArgs(args));
	}
	
	ListedPoints getListed() {
		return listed;
	}

	private List<String> completeSubCmd(String begin) {
		return subcommands.keySet().stream()
				.filter(name -> name.startsWith(begin))
				.collect(Collectors.toList());
	}
	
	private String[] getSubcommandArgs(String[] mainArgs) {
		return (mainArgs.length > 1 ? Arrays.copyOfRange(mainArgs, 1, mainArgs.length) : new String[0]);
	}
	

}
