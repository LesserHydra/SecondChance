package com.lesserhydra.secondchance;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondChance extends JavaPlugin {
	
	private static Plugin plugin;
	
	private final File saveFolder = new File(getDataFolder() + File.separator + "saves");
	private final DeathpointHandler deathpointHandler = new DeathpointHandler(this);
	
	
	@Override
	public void onEnable() {
		plugin = this;
		
		//Create config & save folder if nonexistant
		if (!getDataFolder().exists()) getDataFolder().mkdir();
		if (!saveFolder.exists()) saveFolder.mkdir();
		saveDefaultConfig();
		
		//Register Deathpoint serializability
		ConfigurationSerialization.registerClass(Deathpoint.class, "Deathpoint");
		//Initiate deathpoint handler
		deathpointHandler.init(new ConfigOptions(getConfig()));
		//Register listener events
		getServer().getPluginManager().registerEvents(deathpointHandler, this);
	}
	
	@Override
	public void onDisable() {
		//Deinitiate everything
		deathpointHandler.deinit();
		//Unregister Deathpoint serializability
		ConfigurationSerialization.unregisterClass(Deathpoint.class);
		
		plugin = null;
	}
	
	//Reload command
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("SecondChance")) return false;
		if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) return false;
		
		//Deinit everything
		deathpointHandler.deinit();
		
		//Recheck folders and config
		if (!getDataFolder().exists()) getDataFolder().mkdir();
		if (!saveFolder.exists()) saveFolder.mkdir();
		saveDefaultConfig();
		
		//Reinit everything
		reloadConfig();
		deathpointHandler.init(new ConfigOptions(getConfig()));
		
		//Finished
		sender.sendMessage(ChatColor.GREEN + "Reloaded SecondChance");
		return true;
	}
	
	public File getSaveFolder() {
		return saveFolder;
	}
	
	public static Logger logger() {
		if (plugin == null) throw new IllegalStateException("Plugin is not enabled!");
		return plugin.getLogger();
	}

}
