package com.logicallunacy.secondChance;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondChance extends JavaPlugin {
	
	private static Plugin plugin;
	
	private final DeathpointHandler deathpointHandler = DeathpointHandler.getInstance();
	public final SaveHandler saveHandler = new SaveHandler();
	
	@Override
	public void onEnable() {
		plugin = this;
		
		//Create data folders if nonexistant
		if (!getDataFolder().exists()) {
			getLogger().info("Creating data folders");
			getDataFolder().mkdir();
		}
		
		ConfigurationSerialization.registerClass(Deathpoint.class, "Deathpoint");
		
		try {saveHandler.load(getDataFolder() + File.separator + "save.yml");}
		catch (IOException e) {
			getLogger().severe("Failed to load save file! Disabling.");
			e.printStackTrace();
			setEnabled(false);
			return;
		}
		
		deathpointHandler.init(this);
		
		//Register listener events
		getServer().getPluginManager().registerEvents(deathpointHandler, this);
	}
	
	@Override
	public void onDisable() {
		deathpointHandler.deinit();
		
		try {
			saveHandler.save();
		} catch (IOException e) {
			getLogger().severe("Could not save deathpoints! Droppinp on the ground as a last-ditch.");
			deathpointHandler.panic();
			e.printStackTrace();
		}
		
		ConfigurationSerialization.unregisterClass(Deathpoint.class);
		
		plugin = null;
	}
	
	public static Logger logger() {
		if (plugin == null) throw new IllegalStateException("Plugin is not enabled!");
		return plugin.getLogger();
	}

}
