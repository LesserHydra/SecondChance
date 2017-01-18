package com.lesserhydra.secondchance.compat.v1_8_R2;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import com.lesserhydra.secondchance.compat.BaseParticleEffect;
import net.minecraft.server.v1_8_R2.EntityPlayer;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.WorldServer;

public class CompatParticleEffect extends BaseParticleEffect {
	
	private final EnumParticle type;
	
	
	public CompatParticleEffect(String name, int amount, double spread, double speed, boolean ownerOnly) {
		super(name, amount, spread, speed, ownerOnly);
		this.type = getParticleEnum(name);
	}
	
	@Override
	protected void runInWorld(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        WorldServer world = ((CraftWorld)location.getWorld()).getHandle();
        world.sendParticles(null, type, true, x, y, z, amount, spread, spread, spread, speed);
	}
	
	@Override
	protected void runForPlayer(Location location, Player owner) {
		if (owner == null || owner.getWorld() != location.getWorld()) return;
		
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
		
        WorldServer world = ((CraftWorld)location.getWorld()).getHandle();
		EntityPlayer player = ((CraftPlayer)owner).getHandle();
        world.sendParticles(player, type, true, x, y, z, amount, spread, spread, spread, speed);
	}
	
	private EnumParticle getParticleEnum(String name) {
		try {
			return EnumParticle.valueOf(name.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			return EnumParticle.SUSPENDED;
		}
	}

}
