package com.lesserhydra.secondchance;

import org.bukkit.Particle;

class ConfigOptions {
	
	//Items are held on death
	public final boolean holdItems = true;
	//Exp is held on death
	public final boolean holdExp = true;
	//Items are dropped when point not retrieved; otherwise items are lost
	public final boolean dropItemsOnForget = true;
	//Exp is dropped when point not retrieved; otherwise exp is lost
	public final boolean dropExpOnForget = false;
	
	//Maximum number of deathpoints per player, before the oldest are lost (-1 disables)
	public final int maxPerPlayer = 1;
	
	//Delay for particle timer
	public final long particleDelay = 20;
	
	//Primary particles (Show location to click, by default)
	public final Particle particlePrimary = Particle.PORTAL;
	public final int particlePrimaryCount = 50;
	public final double particlePrimarySpread = 0.2;
	public final double particlePrimarySpeed = 0.5;
	
	//Secondary particles (Show proximity, by default)
	public final Particle particleSecondary = Particle.END_ROD;
	public final int particleSecondaryCount = 15;
	public final double particleSecondarySpread = 10;
	public final double particleSecondarySpeed = 0.1;
	
	//Only a deathpoint's owner can access it
	public final boolean isProtected = true;
	
}
