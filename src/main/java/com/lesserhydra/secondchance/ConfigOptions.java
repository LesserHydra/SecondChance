package com.lesserhydra.secondchance;

import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;

class ConfigOptions {
	
	//Items are held on death
	public final boolean holdItems;
	//Exp is held on death
	public final boolean holdExp;
	//Items are dropped when point not retrieved; otherwise items are lost
	public final boolean dropItemsOnForget;
	//Exp is dropped when point not retrieved; otherwise exp is lost
	public final boolean dropExpOnForget;
	
	//Maximum number of deathpoints per player, before the oldest are lost (-1 disables)
	public final int maxPerPlayer;
	
	//Delay for particle timer
	public final long particleDelay;
	
	//Primary particles (Show location to click, by default)
	public final Particle particlePrimary;
	public final int particlePrimaryCount;
	public final double particlePrimarySpread;
	public final double particlePrimarySpeed;
	
	//Secondary particles (Show proximity, by default)
	public final Particle particleSecondary;
	public final int particleSecondaryCount;
	public final double particleSecondarySpread;
	public final double particleSecondarySpeed;
	
	//Only a deathpoint's owner can access it
	public final boolean isProtected;
	
	public ConfigOptions(FileConfiguration config) {
		this.holdItems = config.getBoolean("holdItems", true);
		
		this.holdExp = config.getBoolean("holdExp", true);
		this.dropItemsOnForget = config.getBoolean("dropItemsOnForget", true);
		this.dropExpOnForget = config.getBoolean("dropExpOnForget", false);
		
		this.maxPerPlayer = config.getInt("maxPerPlayer", 1);
		
		this.particleDelay = config.getLong("particleDelay", 20);
		
		this.particlePrimary = Particle.valueOf(config.getString("particlePrimary", "PORTAL"));
		this.particlePrimaryCount = config.getInt("particlePrimaryCount", 50);
		this.particlePrimarySpread = config.getDouble("particlePrimarySpread", 0.2);
		this.particlePrimarySpeed = config.getDouble("particlePrimarySpeed", 0.5);
		
		this.particleSecondary = Particle.valueOf(config.getString("particleSecondary", "END_ROD"));
		this.particleSecondaryCount = config.getInt("particleSecondaryCount", 15);
		this.particleSecondarySpread = config.getDouble("particleSecondarySpread", 10);
		this.particleSecondarySpeed = config.getDouble("particleSecondarySpeed", 0.1);
		
		this.isProtected = config.getBoolean("isProtected", true);;
	}
	
}
