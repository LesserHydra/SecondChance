package com.logicallunacy.secondChance;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondChance extends JavaPlugin {
	
	//IO Stuff
	public final File m_saveFolder = new File(getDataFolder() + File.separator + "saves");
	
	//Deathpoint handler
	private final DeathpointHandler deathpointHandler = new DeathpointHandler(this);
	
	@Override
	public void onEnable() {
		//Create data folders if nonexistant
		if (!getDataFolder().exists()) {
			getLogger().info("Creating data folders");
			getDataFolder().mkdir();
			if (!m_saveFolder.exists()) m_saveFolder.mkdir();
		}
		
		//Add online players to death point handler
		deathpointHandler.init();
		
		//Register listener events
		getServer().getPluginManager().registerEvents(deathpointHandler, this);
	}
	
	@Override
	public void onDisable() {
		deathpointHandler.deinit();
	}

}
