package com.lesserhydra.secondchance.compat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class BaseParticleEffect implements ParticleEffect {

	protected final String name;
	protected final int amount;
	protected final double spread;
	protected final double speed;
	protected final boolean ownerOnly;
	
	public BaseParticleEffect(String name, int amount, double spread, double speed, boolean ownerOnly) {
		this.name = name;
		this.amount = amount;
		this.spread = spread;
		this.speed = speed;
		this.ownerOnly = ownerOnly;
	}
	
	public void run(Location location, Player owner) {
		if (ownerOnly) runForPlayer(location, owner);
		else runInWorld(location);
	}
	
	protected abstract void runInWorld(Location location);
	
	protected abstract void runForPlayer(Location location, Player owner);
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getAmount() {
		return amount;
	}
	
	@Override
	public double getSpread() {
		return spread;
	}
	
	@Override
	public double getSpeed() {
		return speed;
	}
	
	@Override
	public boolean isOwnerOnly() {
		return ownerOnly;
	}
	
}
