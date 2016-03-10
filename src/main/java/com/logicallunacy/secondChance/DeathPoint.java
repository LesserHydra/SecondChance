package com.logicallunacy.secondChance;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.darkblade12.particleeffect.ParticleEffect;

public class DeathPoint
{
	ParticleEffect m_effect = ParticleEffect.PORTAL;
	
	SecondChance m_plugin;
	
	Player m_player;
	Location m_location;
	ArmorStand m_armorStand;
	
	//Contents
	Inventory m_contents;
	int m_experience;
	//int m_level;
	//float m_exp;
	
	
	public DeathPoint(SecondChance plugin, Player player)
	{
		m_plugin = plugin;
		m_player = player;
		m_location = null;
		m_contents = Bukkit.getServer().createInventory(null, 45, "Lost Inventory");//player.getName() + "'s Lost Inventory");
		m_experience = 0;
	}
	
	public void createNew()
	{
		destroy();
		
		m_location = findSafeLocation(m_player.getLocation());
		
		PlayerInventory playerInv = m_player.getInventory();
		
		ItemStack[] inventory = playerInv .getContents();
		ItemStack[] armor = playerInv.getArmorContents();
		ItemStack[] contentsArray = new ItemStack[45];
		System.arraycopy(inventory, 9, contentsArray, 0, 27); //Main inventory
		System.arraycopy(inventory, 0, contentsArray, 27, 9); //Hotbar
		System.arraycopy(armor, 0, contentsArray, 41, 4); //Armor
		
		m_contents.setContents(contentsArray);
		
		m_experience = convertLevelsToPoints(m_player.getLevel(), m_player.getExp());
		
		//Create armor stand
		spawnHitbox();
		
		//Clear player's inventory
		playerInv.clear();
		playerInv.setArmorContents(new ItemStack[4]);
		
		//TODO: Need some safety if the save fails
		try {save();}
		catch (IOException e) {e.printStackTrace();}
		
		if (m_experience == 0 && isEmpty())
			destroy();
	}
	
	public void spawnHitbox()
	{
		if (m_location != null)
		{
			if (m_location.getChunk().isLoaded())
			{
				Location standLoc = m_location.clone().add(0, -0.75, 0);
				m_armorStand = (ArmorStand) m_location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
				m_armorStand.setGravity(false);
				m_armorStand.setVisible(false);
			}
		}
	}
	
	public void despawnHitbox()
	{
		if (m_armorStand != null) {
			m_armorStand.remove();
			m_armorStand = null;
		}
	}

	private Location findSafeLocation(Location playerLoc)
	{
		World w = playerLoc.getWorld();
		int x = playerLoc.getBlockX();
		int y = playerLoc.getBlockY();
		int z = playerLoc.getBlockZ();
		
		if (y < 0)
			y = 0;
		
		Location bestLoc = new Location(w, x, y, z);
		int bestLocScore = scoreLocation(bestLoc);
		
		while (y < 255)
		{
			//Middle
			Location loc = new Location(w, x, y, z);
			int locScore = scoreLocation(loc);
			
			if (locScore > bestLocScore)
			{
				bestLoc = loc;
				bestLocScore = locScore;
			}
			
			if (bestLocScore == 10 || loc.getBlock().getType() == Material.BEDROCK)
				break;
			
			//Negative x
			loc = new Location(w, x-1, y, z);
			locScore = scoreLocation(loc);
			
			if (locScore > bestLocScore)
			{
				bestLoc = loc;
				bestLocScore = locScore;
			}
			
			//Positive x
			loc = new Location(w, x+1, y, z);
			locScore = scoreLocation(loc);
			
			if (locScore > bestLocScore)
			{
				bestLoc = loc;
				bestLocScore = locScore;
			}
			
			//Negative z
			loc = new Location(w, x, y, z-1);
			locScore = scoreLocation(loc);
			
			if (locScore > bestLocScore)
			{
				bestLoc = loc;
				bestLocScore = locScore;
			}
			
			//Positive z
			loc = new Location(w, x, y, z+1);
			locScore = scoreLocation(loc);
			
			if (locScore > bestLocScore)
			{
				bestLoc = loc;
				bestLocScore = locScore;
			}
			
			if (bestLocScore == 10)
				break;
			
			y++;
		}
		
		Location upLoc = bestLoc.clone().add(0.5, 1, 0.5);
		if (scoreLocation(upLoc) == bestLocScore)
			return upLoc;
		else
			return bestLoc.add(0.5, 0, 0.5);
	}

	private int scoreLocation(Location loc)
	{
		Material mat = loc.getBlock().getType();
		int score = -5;
		
		switch (mat)
		{
		case AIR:				score = 10;
			break;
		case STATIONARY_WATER:	score = 5;
			break;
		case WATER:				score = 6;
			break;
		case STATIONARY_LAVA:	score = -10;
			break;
		case LAVA:				score = -9;
			break;
		default:
			if (!mat.isSolid())
				score = 10;
			else if (mat.isTransparent())
				score = 3;
			else if (!mat.isOccluding())
				score = 1;
			break;
		}
		
		if (loc.getBlock().getBiome() == Biome.SKY && loc.getY() < 32)
			score--;
		
		return score;
	}

	public void showContents(Player player) {player.openInventory(m_contents);}
	
	private void destroy()
	{
		despawnHitbox();
		if (m_location != null)
		{
			for (ItemStack item : m_contents.getContents())
			{
		    	if (item != null)
		    	{
		        	m_location.getWorld().dropItemNaturally(m_location, item);
		        	item = null;
		    	}
			}
			m_location = null;
		}
		else if (!isEmpty())
		{
			//TODO: Stuff?
			m_player.sendMessage("ERROR!");
		}
		
	}
	
	public void save() throws IOException
	{
		String playerName = m_player.getName();
		UUID playerUUID = m_player.getUniqueId();
		
		m_plugin.getLogger().info("Saving " + playerName + "'s DeathPoint");
		
		File file = new File(m_plugin.m_saveFolder + File.separator + playerUUID.toString() + ".yml");
		file.createNewFile();
		YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);
		
		saveFile.set("playerName", playerName);
		saveFile.set("location", m_location);
		//saveFile.set("armorStandUUID", m_armorStand.getUniqueId().toString());
		saveFile.set("experience", Integer.valueOf(m_experience));
		saveFile.set("contents", m_contents.getContents());
		
		saveFile.save(file);
	}
	
	public void load()
	{
		String playerName = m_player.getName();
		UUID playerUUID = m_player.getUniqueId();
		
		m_plugin.getLogger().info("Attempting to load " + playerName + "'s saved death point...");
		
		File file = new File(m_plugin.m_saveFolder + File.separator + playerUUID.toString() + ".yml");
		if (file.exists())
		{
			YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(file);
			
			//Load
			m_location = (Location) saveFile.get("location");
			//m_armorStand = getArmorStand(saveFile.getString("armorStandUUID"));
			m_experience = saveFile.getInt("experience");
			m_contents.setContents(saveFile.getList("contents").toArray(new ItemStack[0]));
		}
		else
			m_plugin.getLogger().warning("Failed. File not found.");
	}

	public void particles()
	{
		if (m_location == null) return;
		//particle display portal 0.2 0.2 0.2 0.5 200 100
		m_effect.display(0.2f, 0.2f, 0.2f, 0.5f, 100, m_location, 100.0d);
	}
	
	public boolean isEmpty()
	{
		for (ItemStack item: m_contents.getContents()) {
			if (item != null) return false;
		}
		
		return true;
	}

	public void finish()
	{
		final int exp = m_experience;
		
		m_plugin.getServer().getScheduler().scheduleSyncDelayedTask(m_plugin, new Runnable()
		{
			public void run()
			{dropExperience(exp);}
		}, 2);
	}
	
	private void dropExperience(int xpPoints)
	{
		if (xpPoints > 0)
		{
			int playerLevel = m_player.getLevel();
			int toDrop = 0;
			
			if (playerLevel < 17)
			{
				toDrop = 2*playerLevel + 7;
			}
			else if (playerLevel < 32)
			{
				toDrop = 5*playerLevel - 38;
			}
			else
			{
				toDrop = 9*playerLevel - 158;
			}
			
			xpPoints-= toDrop;
			
			
			//Drop xp
			Location playerLoc = m_player.getLocation();
			ExperienceOrb orb = playerLoc.getWorld().spawn(playerLoc, ExperienceOrb.class);
			if (xpPoints < 0)
				orb.setExperience(toDrop + xpPoints);
			else
				orb.setExperience(toDrop);
			
			final int remaining = xpPoints;
			m_plugin.getServer().getScheduler().scheduleSyncDelayedTask(m_plugin, new Runnable()
			{
				public void run()
				{dropExperience(remaining);}
			}, 2);
		}
		else destroy();
	}
	
	private int convertLevelsToPoints(int level, double progress)
	{
		double fromLevel = 0;
		double fromProgress = 0;
		
		if (level < 17)
		{
			fromLevel = level*level + 6*level;
			fromProgress = progress*(2*level + 7);
		}
		else if (level < 32)
		{
			fromLevel = 2.5*level*level - 40.5*level + 360;
			fromProgress = progress*(5*level - 38);
		}
		else
		{
			fromLevel = 4.5*level*level - 162.5*level + 2220;
			fromProgress = progress*(9*level - 158);
		}
		
		return (int)(fromLevel + fromProgress);
	}
}
