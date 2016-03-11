package com.logicallunacy.secondChance;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondChance extends JavaPlugin
{
	DeathPointHandler m_eventListener = new DeathPointHandler(this);
	
	//IO Stuff
	String	m_dataFolderDir	= getDataFolder().toString();
	File	m_dataFolder	= new File(m_dataFolderDir);
	File	m_saveFolder	= new File(m_dataFolderDir + File.separator + "saves");

	//Plugin startup
	@Override
	public void onEnable() {
		if (!m_dataFolder.exists()) {
			getLogger().info("Creating data folders");
			m_dataFolder.mkdir();
			m_saveFolder.mkdir();
		}
		
		//Add online players to death point handler
		m_eventListener.init();
		
		//Register listener events
		getServer().getPluginManager().registerEvents(m_eventListener, this);
	}
	
	//Plugin startup
	@Override
	public void onDisable() {
		m_eventListener.deinit();
	}

}
