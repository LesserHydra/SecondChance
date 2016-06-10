package com.lesserhydra.secondchance.compat.v1_9_Plus;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import com.lesserhydra.secondchance.compat.BaseParticleEffect;

public class CompatParticleEffect extends BaseParticleEffect {
	
	private final Particle type;
	
	
	public CompatParticleEffect(String name, int amount, double spread, double speed, boolean ownerOnly) {
		super(name, amount, spread, speed, ownerOnly);
		this.type = getParticleEnum(name);
	}
	
	@Override
	protected void runInWorld(Location location) {
		location.getWorld().spawnParticle(type, location, amount, spread, spread, spread, speed);
	}
	
	@Override
	protected void runForPlayer(Location location, Player owner) {
		if (owner == null) return;
		owner.spawnParticle(type, location, amount, spread, spread, spread, speed);
	}
	
	private Particle getParticleEnum(String name) {
		try {
			return Particle.valueOf(name.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			return Particle.SUSPENDED;
		}
	}

}
