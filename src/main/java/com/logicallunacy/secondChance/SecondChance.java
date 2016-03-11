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

	/*//Command handling
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			if (args.length > 0)
			{
				String subCommand = args[0];
				if (subCommand.equalsIgnoreCase("loadInv"))
				{
					DeathPoint point = m_deathPoints.get(player.getName());
					if (point != null) point.showContents(player);
					else player.sendMessage(ChatColor.RED + "You don't have a death point in memory!");
				}
				else showCommandHelp(sender);
			}
			else showCommandHelp(sender);
		}
		else
		{
			//Sender is not a player
			sender.sendMessage(ChatColor.RED + "Only players may use this command!");
		}
		
		
		return true;
	}*/

	/*private void showCommandHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.GRAY + "---------------SecondChance---------------");
		sender.sendMessage(ChatColor.GRAY + "This command is for testing purposes only, for now.");
		sender.sendMessage(ChatColor.GRAY + "/sc loadInv - Load your saved inventory");
	}*/

}
