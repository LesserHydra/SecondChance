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
	
	//Sound on creation
	public final boolean creationSoundEnabled;
	public final String creationSound;
	public final float creationSoundPitch;
	public final float creationSoundVolume;
	
	//Sound on open
	public final boolean openSoundEnabled;
	public final String openSound;
	public final float openSoundPitch;
	public final float openSoundVolume;
	
	//Sound on break
	public final boolean breakSoundEnabled;
	public final String breakSound;
	public final float breakSoundPitch;
	public final float breakSoundVolume;
	
	//Sound on forget
	public final boolean forgetSoundEnabled;
	public final String forgetSound;
	public final float forgetSoundPitch;
	public final float forgetSoundVolume;
	
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
		
		this.creationSoundEnabled = config.getBoolean("Play Sound on Deathpoint Created.Enabled", true);
		this.creationSound = config.getString("Play Sound on Deathpoint Created.Sound", "ui.button.click");
		this.creationSoundVolume = (float) config.getDouble("Play Sound on Deathpoint Created.Volume", 1);
		this.creationSoundPitch = (float) config.getDouble("Play Sound on Deathpoint Created.Pitch", 1);
		
		this.openSoundEnabled = config.getBoolean("Play Sound on Deathpoint Opened.Enabled", true);
		this.openSound = config.getString("Play Sound on Deathpoint Opened.Sound", "ui.button.click");
		this.openSoundVolume = (float) config.getDouble("Play Sound on Deathpoint Opened.Volume", 1);
		this.openSoundPitch = (float) config.getDouble("Play Sound on Deathpoint Opened.Pitch", 1);
		
		this.breakSoundEnabled = config.getBoolean("Play Sound on Deathpoint Broken.Enabled", true);
		this.breakSound = config.getString("Play Sound on Deathpoint Broken.Sound", "ui.button.click");
		this.breakSoundVolume = (float) config.getDouble("Play Sound on Deathpoint Broken.Volume", 1);
		this.breakSoundPitch = (float) config.getDouble("Play Sound on Deathpoint Broken.Pitch", 1);
		
		this.forgetSoundEnabled = config.getBoolean("Play Sound on Deathpoint Forgotten.Enabled", true);
		this.forgetSound = config.getString("Play Sound on Deathpoint Forgotten.Sound", "ui.button.click");
		this.forgetSoundVolume = (float) config.getDouble("Play Sound on Deathpoint Forgotten.Volume", 1);
		this.forgetSoundPitch = (float) config.getDouble("Play Sound on Deathpoint Forgotten.Pitch", 1);
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
