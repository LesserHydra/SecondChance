package com.lesserhydra.secondchance;

import java.util.EnumSet;
import java.util.Optional;
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
	//Break when valid player left clicks
	public final boolean breakOnHit;
	//Maximum number of deathpoints per player, before the oldest are lost (-1 disables)
	public final int maxPerPlayer;
	//Only a deathpoint's owner can access it
	public final boolean isProtected;
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
	
	
	public ConfigOptions(FileConfiguration config) {
		this.holdItems = config.getBoolean("Hold Items", true);
		this.holdExp = config.getBoolean("Hold Experience", true);
		this.dropItemsOnForget = config.getBoolean("Drop Items When Forgotten", true);
		this.dropExpOnForget = config.getBoolean("Drop Experience When Forgotten", false);
		this.breakOnHit = config.getBoolean("Break Deathpoint On Leftclick", true);
		this.maxPerPlayer = config.getInt("Player Deathpoint Maximum", 1);
		this.isProtected = config.getBoolean("Owner Based", true);
		this.particleDelay = config.getLong("Particle Timer Delay", 20);
		
		this.particlePrimary = getEnum("Primary Particles.Type", Particle.PORTAL, config);
		this.particlePrimaryCount = config.getInt("Primary Particles.Count", 50);
		this.particlePrimarySpread = config.getDouble("Primary Particles.Spread", 0.2);
		this.particlePrimarySpeed = config.getDouble("Primary Particles.Speed", 0.5);
		
		this.particleSecondary = getEnum("Secondary Particles.Type", Particle.END_ROD, config);
		this.particleSecondaryCount = config.getInt("Secondary Particles.Count", 15);
		this.particleSecondarySpread = config.getDouble("Secondary Particles.Spread", 10);
		this.particleSecondarySpeed = config.getDouble("Secondary Particles.Speed", 0.1);
	}
	
	private static <T extends Enum<T>> T getEnum(String path, T def, FileConfiguration config) {
		String configString = config.getString(path, null);
		if (configString == null) return def;
		
		Optional<T> match = EnumSet.allOf(def.getDeclaringClass()).stream()
				.filter(type -> configString.equalsIgnoreCase(type.name()))
				.findAny();
		
		if (match.isPresent()) return match.get();
		SecondChance.logger().warning("There is no " + def.getDeclaringClass().getName() + " with the name \"" + configString + "\".");
		SecondChance.logger().warning("Defaulting to " + def.name());
		return def;
	}

}
