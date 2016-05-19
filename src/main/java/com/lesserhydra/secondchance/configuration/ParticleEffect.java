package com.lesserhydra.secondchance.configuration;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleEffect {
	
	private final Particle type;
	private final int amount;
	private final double spread;
	private final double speed;
	private final boolean ownerOnly;
	
	public ParticleEffect(Particle type, int amount, double spread, double speed, boolean ownerOnly) {
		this.type = type;
		this.amount = amount;
		this.spread = spread;
		this.speed = speed;
		this.ownerOnly = ownerOnly;
	}
	
	public void run(Location location, Player owner) {
		if (ownerOnly) runForPlayer(location, owner);
		else runInWorld(location);
	}
	
	private void runInWorld(Location location) {
		location.getWorld().spawnParticle(type, location, amount, spread, spread, spread, speed);
	}
	
	private void runForPlayer(Location location, Player owner) {
		if (owner == null) return;
		owner.spawnParticle(type, location, amount, spread, spread, spread, speed);
	}

	public Particle getType() {
		return type;
	}

	public int getAmount() {
		return amount;
	}

	public double getSpread() {
		return spread;
	}

	public double getSpeed() {
		return speed;
	}

	public boolean isOwnerOnly() {
		return ownerOnly;
	}

}
