package com.lesserhydra.secondchance.configuration;

import com.lesserhydra.secondchance.ParticleEffect;
import com.lesserhydra.secondchance.SoundEffect;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class ConfigOptions {
	
	public final boolean holdItems;
	public final boolean holdExp;
	public final boolean dropItemsOnForget;
	public final boolean dropExpOnForget;
	public final boolean breakOnHit;
	public final boolean isProtected;
	public final long locationCheckDelay;
	public final long particleDelay;
	
	public final int deathsTillForget;
	public final int ticksTillForget;
	public final long timeCheckDelay;
	
	public final DeathpointMessage deathMessage;
	public final DeathpointMessage forgetMessage;
	
	public final ParticleEffect particlePrimary;
	public final ParticleEffect particleSecondary;
	
	public final long ambientSoundDelay;
	public final SoundEffect ambientSound;
	public final SoundEffect creationSound;
	public final SoundEffect openSound;
	public final SoundEffect closeSound;
	public final SoundEffect breakSound;
	public final SoundEffect forgetSound;
	
	private final boolean useWorldWhitelist;
	private final Set<String> worldBlacklist = new HashSet<>();
	
	public ConfigOptions(FileConfiguration config) {
		this.holdItems = config.getBoolean("Hold Items", true);
		this.holdExp = config.getBoolean("Hold Experience", true);
		this.dropItemsOnForget = config.getBoolean("Drop Items When Forgotten", true);
		this.dropExpOnForget = config.getBoolean("Drop Experience When Forgotten", false);
		this.breakOnHit = config.getBoolean("Break Deathpoint On Leftclick", false);
		this.isProtected = config.getBoolean("Owner Based", true);
		this.locationCheckDelay = config.getLong("Safe Location Timer Delay", 40);
		this.particleDelay = config.getLong("Particle Timer Delay", 20);
		
		this.deathsTillForget = config.getInt("Number of Deaths Before Deathpoint Forgotten", 1);
		this.ticksTillForget = config.getInt("Number of Ticks Before Deathpoint Forgotten", -1);
		this.timeCheckDelay = config.getLong("Time Check Delay", 400);
		
		this.deathMessage = new DeathpointMessage(config.getString("Death Message", "&dA memory forms in the back of your mind."));
		this.forgetMessage = new DeathpointMessage(config.getString("Forget Message", "&cYou feel something slipping away..."));
		
		this.particlePrimary = getParticleEffect("Primary Particles", new ParticleEffect("PORTAL", 50, 0.2, 0.5, false), config);
		this.particleSecondary = getParticleEffect("Secondary Particles", new ParticleEffect("END_ROD", 15, 10, 0.1, true), config);
		
		this.ambientSoundDelay = config.getLong("Ambient Sound Delay", 50);
		this.ambientSound = getSoundEffect("Play Ambient Sound", new SoundEffect(true, "item.elytra.flying", 0.1f, 2.0f, false), config);
		this.creationSound = getSoundEffect("Play Sound on Deathpoint Created", new SoundEffect(true, "entity.zombie_villager.converted", 1.0f, 2.0f, true), config);
		this.forgetSound = getSoundEffect("Play Sound on Deathpoint Forgotten", new SoundEffect(false, "entity.lightning.thunder", 0.75f, 2.0f, true), config);
		this.openSound = getSoundEffect("Play Sound on Deathpoint Opened", new SoundEffect(false, "ui.button.click", 1.0f, 1.0f, false), config);
		this.closeSound = getSoundEffect("Play Sound on Deathpoint Closed", new SoundEffect(true, "entity.item.pickup", 1.0f, 0.5f, false), config);
		this.breakSound = getSoundEffect("Play Sound on Deathpoint Broken", new SoundEffect(true, "entity.item.pickup", 1.0f, 0.5f, false), config);
		
		this.useWorldWhitelist = config.getBoolean("Use World Whitelist", false);
		this.worldBlacklist.addAll(config.getStringList("World Blacklist/Whitelist"));
	}
	
	public boolean isWorldDisabled(World world) {
		return useWorldWhitelist ^ worldBlacklist.contains(world.getName());
	}
	
	private static ParticleEffect getParticleEffect(String path, ParticleEffect def, FileConfiguration config) {
		String type = config.getString(path + ".Type", def.getName());
		int count = config.getInt(path + ".Count", def.getAmount());
		double spread = config.getDouble(path + ".Spread", def.getSpread());
		double speed = config.getDouble(path + ".Speed", def.getSpeed());
		boolean ownerOnly = config.getBoolean(path + ".Owner Only", def.isOwnerOnly());
		return new ParticleEffect(type, count, spread, speed, ownerOnly);
	}
	
	private static SoundEffect getSoundEffect(String path, SoundEffect def, FileConfiguration config) {
		boolean enabled = config.getBoolean(path + ".Enabled", def.isEnabled());
		String sound = config.getString(path + ".Sound", def.getSound());
		float volume = (float) config.getDouble(path + ".Volume", def.getVolume());
		float pitch = (float) config.getDouble(path + ".Pitch", def.getPitch());
		boolean direct = config.getBoolean(path + ".Direct", def.isDirect());
		return new SoundEffect(enabled, sound, volume, pitch, direct);
	}

}
