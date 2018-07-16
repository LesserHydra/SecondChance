package com.lesserhydra.secondchance;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;


/**
 * Represents a particle effect
 */
public class ParticleEffect {
  
  private final String name;
  private final int amount;
  private final double spread;
  private final double speed;
  private final boolean ownerOnly;
  private final Particle type;
  
  public ParticleEffect(String name, int amount, double spread, double speed, boolean ownerOnly) {
    this.name = name;
    this.amount = amount;
    this.spread = spread;
    this.speed = speed;
    this.ownerOnly = ownerOnly;
    this.type = getParticleEnum(name);
  }
  
  public void run(Location location, Player owner) {
    if (ownerOnly) runForPlayer(location, owner);
    else runInWorld(location);
  }
  
  public String getName() {
    return name;
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
  
  private void runInWorld(Location location) {
    location.getWorld().spawnParticle(type, location, amount, spread, spread, spread, speed);
  }
  
  private void runForPlayer(Location location, Player owner) {
    if (owner == null || owner.getWorld() != location.getWorld()) return;
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
