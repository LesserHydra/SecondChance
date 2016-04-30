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
	//Delay for safe location finding timer
	public final long locationCheckDelay;
	//Delay for particle timer
	public final long particleDelay;
	
	//Message that plays on death
	public final DeathpointMessage deathMessage;
	//Message that plays on forget
	public final DeathpointMessage forgetMessage;
	
	//Primary particles (Show location to click, by default)
	public final ParticleEffect particlePrimary;
	//Secondary particles (Show proximity, by default)
	public final ParticleEffect particleSecondary;
	
	
	public ConfigOptions(FileConfiguration config) {
		this.holdItems = config.getBoolean("Hold Items", true);
		this.holdExp = config.getBoolean("Hold Experience", true);
		this.dropItemsOnForget = config.getBoolean("Drop Items When Forgotten", true);
		this.dropExpOnForget = config.getBoolean("Drop Experience When Forgotten", false);
		this.breakOnHit = config.getBoolean("Break Deathpoint On Leftclick", false);
		this.maxPerPlayer = config.getInt("Player Deathpoint Maximum", 1);
		this.isProtected = config.getBoolean("Owner Based", true);
		this.locationCheckDelay = config.getLong("Safe Location Timer Delay", 40);
		this.particleDelay = config.getLong("Particle Timer Delay", 20);
		
		this.deathMessage = new DeathpointMessage(config.getString("Death Message", ""));
		this.forgetMessage = new DeathpointMessage(config.getString("Forget Message", ""));
		
		Particle particlePrimaryType = getEnum("Primary Particles.Type", Particle.PORTAL, config);
		int particlePrimaryCount = config.getInt("Primary Particles.Count", 50);
		double particlePrimarySpread = config.getDouble("Primary Particles.Spread", 0.2);
		double particlePrimarySpeed = config.getDouble("Primary Particles.Speed", 0.5);
		boolean particlePrimaryOwner = config.getBoolean("Primary Particles.Owner Only", false);
		this.particlePrimary = new ParticleEffect(particlePrimaryType, particlePrimaryCount, particlePrimarySpread,
				particlePrimarySpeed, particlePrimaryOwner);
		
		Particle particleSecondaryType = getEnum("Secondary Particles.Type", Particle.END_ROD, config);
		int particleSecondaryCount = config.getInt("Secondary Particles.Count", 15);
		double particleSecondarySpread = config.getDouble("Secondary Particles.Spread", 10);
		double particleSecondarySpeed = config.getDouble("Secondary Particles.Speed", 0.1);
		boolean particleSecondaryOwner = config.getBoolean("Secondary Particles.Owner Only", true);
		this.particleSecondary = new ParticleEffect(particleSecondaryType, particleSecondaryCount, particleSecondarySpread,
				particleSecondarySpeed, particleSecondaryOwner);
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
