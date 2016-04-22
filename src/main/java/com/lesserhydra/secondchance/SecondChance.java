package com.lesserhydra.secondchance;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondChance extends JavaPlugin {
	
	private static Plugin plugin;
	
	private final File saveFolder = new File(getDataFolder() + File.separator + "saves");
	private final ConfigOptions options = new ConfigOptions();
	private final DeathpointHandler deathpointHandler = new DeathpointHandler(this, options);
	private final Map<String, SaveHandler> saveHandlers = new HashMap<>();
	
	
	@Override
	public void onEnable() {
		plugin = this;
		
		//Create config & save folder if nonexistant
		if (!getDataFolder().exists()) getDataFolder().mkdir(); //TODO: Check for failure
		if (!saveFolder.exists()) saveFolder.mkdir(); //TODO: Check for failure
		
		//Register Deathpoint serializability
		ConfigurationSerialization.registerClass(Deathpoint.class, "Deathpoint");
		//Initiate all worlds
		getServer().getWorlds().forEach(deathpointHandler::initWorld);
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
