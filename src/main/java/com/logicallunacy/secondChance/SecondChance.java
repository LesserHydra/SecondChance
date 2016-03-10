package com.logicallunacy.secondChance;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondChance extends JavaPlugin
{
	SecondChanceListener m_eventListener = new SecondChanceListener(this);
	
	//IO Stuff
	String	m_dataFolderDir	= getDataFolder().toString();
	File	m_dataFolder	= new File(m_dataFolderDir);
	File	m_saveFolder	= new File(m_dataFolderDir + File.separator + "saves");
	
	//DeathPoints
	HashMap<String, DeathPoint> m_deathPoints = new HashMap<>();

	//Plugin startup
	@Override
	public void onEnable()
	{
		if (!m_dataFolder.exists())
		{
			getLogger().info("Creating data folders");
			m_dataFolder.mkdir();
			m_saveFolder.mkdir();
		}
		
		for (Player player: getServer().getOnlinePlayers())
		{
			DeathPoint deathPoint = new DeathPoint(this, player);
			deathPoint.load();
			m_deathPoints.put(player.getName(), deathPoint);
		}
		
		//Register listener events
		getServer().getPluginManager().registerEvents(m_eventListener, this);
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run()
			{
				deathPointParticles();
			}
		}, 15, 15);
	}
	
	//Plugin startup
	@Override
	public void onDisable()
	{
		for(DeathPoint deathPoint: m_deathPoints.values())
		{
			deathPoint.despawnHitbox();
			try
			{
				deathPoint.save();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void deathPointParticles()
	{
		for (DeathPoint deathPoint: m_deathPoints.values())
		{
			deathPoint.particles();
		}
	}

	//Command handling
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			if (args.length > 0)
			{
				String subCommand = args[0];
				if (subCommand.equalsIgnoreCase("saveInv"))
				{
					player.sendMessage(ChatColor.RED + "No longer implemented.");
					
					File file = new File(m_saveFolder + File.separator + "debug_state.yml");
					try
					{
						file.createNewFile();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);

					for (String pName: m_deathPoints.keySet())
					{
						saveFile.set(pName + ".location", m_deathPoints.get(pName).m_location);
						saveFile.set(pName + ".armorStand", m_deathPoints.get(pName).m_armorStand.getUniqueId().toString());
					}

					try
					{
						saveFile.save(file);
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/*
					DeathPoint deathPoint = new DeathPoint(this, player);
					deathPoint.createNew();
					m_deathPoints.put(player.getName(), deathPoint);
					*/
					
					/*
					player.sendMessage(ChatColor.GRAY + "Attempting to save " + player.getName() + "'s inventory...");
					
					try
					{
						saveInventory(player);
						sender.sendMessage(ChatColor.GREEN + "Done!");
					} catch (IOException e)
					{
						sender.sendMessage(ChatColor.RED + "Failed! See console.");
						e.printStackTrace();
					}
					*/
				}
				else if (subCommand.equalsIgnoreCase("loadInv"))
				{
					if (m_deathPoints.containsKey(player.getName()))
						m_deathPoints.get(player.getName()).showContents(player);
					else
						player.sendMessage(ChatColor.RED + "You don't have a death point in memory!");
					
					/*
					try
					{
						loadInventory(player);
						player.sendMessage(ChatColor.GREEN + "Done!");
					} catch (IOException e)
					{
						player.sendMessage(ChatColor.RED + "Failed! See console.");
						e.printStackTrace();
					}
					*/
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
	}

	private void showCommandHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.GRAY + "---------------SecondChance---------------");
		sender.sendMessage(ChatColor.GRAY + "This command is for testing purposes only, for now.");
		sender.sendMessage(ChatColor.GRAY + "/sc saveInv - Save your current inventory");
		sender.sendMessage(ChatColor.GRAY + "/sc loadInv - Load your saved inventory");
	}
	
	/*
	private void saveInventory(Player player) throws IOException
	{
		String playerName = player.getName();
		UUID playerUUID = player.getUniqueId();
		
		getLogger().info("Saving " + playerName + "'s inventory");
		
		ItemStack[] inventory = player.getInventory().getContents();
		ItemStack[] armor = player.getInventory().getArmorContents();
		int level = player.getLevel();
		float exp = player.getExp();

		File file = new File(m_saveFolder + File.separator + playerUUID.toString() + ".yml");
		file.createNewFile();
		YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);

		saveFile.set("playerName", playerName);
		saveFile.set("level", Integer.valueOf(level));
		saveFile.set("experience", Float.valueOf(exp));
		saveFile.set("armor", armor);
		saveFile.set("inventory", inventory);

		saveFile.save(file);
	}
	
	private void loadInventory(Player player) throws IOException
	{
		String playerName = player.getName();
		UUID playerUUID = player.getUniqueId();
		
		getLogger().info("Attempting to load " + playerName + "'s saved inventory");
		
		File file = new File(m_saveFolder + File.separator + playerUUID.toString() + ".yml");
		if (file.exists())
		{
			YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);
			
			//Load
			ItemStack[] inventory = saveFile.getList("inventory").toArray(new ItemStack[0]);
			ItemStack[] armor = saveFile.getList("armor").toArray(new ItemStack[0]);
			//int level = saveFile.getInt("level");
			//float exp = (float) saveFile.get("exp");
			
			//Create chest inventory
			Inventory loadedInventory = getServer().createInventory(null, 45, playerName + "'s Inventory");
			ItemStack[] fullInventory = new ItemStack[45];
			//Main inventory
			System.arraycopy(inventory, 9, fullInventory, 0, 27);
			//Hotbar
			System.arraycopy(inventory, 0, fullInventory, 27, 9);
			//Armor
			System.arraycopy(armor, 0, fullInventory, 41, 4);
			loadedInventory.setContents(fullInventory);
			
			player.openInventory(loadedInventory);
		}
		else
			getLogger().info("No file found.");
	}
	*/
}
