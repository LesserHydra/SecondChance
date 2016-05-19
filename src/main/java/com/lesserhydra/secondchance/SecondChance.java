package com.lesserhydra.secondchance;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondChance extends JavaPlugin {
	
	private static Plugin plugin;

	public static final Permission enabledPermission = new Permission("secondchance.enabled", "Allows spawning of deathpoints on death", PermissionDefault.TRUE);
	public static final Permission thiefPermission = new Permission("secondchance.thief", "Allows access to protected deathpoints", PermissionDefault.FALSE);
	public static final Permission commandPermission = new Permission("secondchance.maincommand", "Allows use of admin commands", PermissionDefault.OP);
	
	private final File saveFolder = new File(getDataFolder() + File.separator + "saves");
	private final DeathpointHandler deathpointHandler = new DeathpointHandler(this);
	private final Map<String, SaveHandler> saveHandlers = new HashMap<>();
	
	
	@Override
	public void onEnable() {
		plugin = this;
		
		//Register permissions
		getServer().getPluginManager().addPermission(enabledPermission);
		getServer().getPluginManager().addPermission(thiefPermission);
		getServer().getPluginManager().addPermission(commandPermission);
		
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
		//Deinitiate handler
		deathpointHandler.deinit();
		//Save all files
		saveHandlers.values().forEach(SaveHandler::save);
		//Unregister Deathpoint serializability
		ConfigurationSerialization.unregisterClass(Deathpoint.class);
		
		plugin = null;
	}
	
	//Reload command
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("SecondChance")) return false;
		
		//Check permission node
		if (!sender.hasPermission(commandPermission)) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return true;
		}
		
		if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) return false;
		
		//Unload everything
		deathpointHandler.deinit();
		saveHandlers.values().forEach(SaveHandler::save);
		saveHandlers.clear();
		
		//Recheck folders and config
		if (!getDataFolder().exists()) getDataFolder().mkdir();
		if (!saveFolder.exists()) saveFolder.mkdir();
		saveDefaultConfig();
		
		//Reload everything
		reloadConfig();
		deathpointHandler.init(new ConfigOptions(getConfig()));
		
		//Finished
		sender.sendMessage(ChatColor.GREEN + "Reloaded SecondChance");
		return true;
	}
	
	public SaveHandler getSaveHandler(World world) {
		SaveHandler result = saveHandlers.get(world.getName());
		if (result != null) return result;
		
		result = new SaveHandler(saveFolder, world);
		result.load();
		saveHandlers.put(world.getName(), result);
		return result;
	}
	
	public static Logger logger() {
		if (plugin == null) throw new IllegalStateException("Plugin is not enabled!");
		return plugin.getLogger();
	}

}
