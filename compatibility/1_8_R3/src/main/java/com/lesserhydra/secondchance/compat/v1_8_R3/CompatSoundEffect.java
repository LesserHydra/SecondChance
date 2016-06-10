package com.lesserhydra.secondchance.compat.v1_8_R3;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import com.lesserhydra.secondchance.compat.BaseSoundEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.WorldServer;

public class CompatSoundEffect extends BaseSoundEffect {

	public CompatSoundEffect(boolean enabled, String sound, float volume, float pitch, boolean direct) {
		super(enabled, sound, volume, pitch, direct);
	}
	
	@Override
	protected void playIndirect(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        WorldServer world = ((CraftWorld)location.getWorld()).getHandle();
        
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(sound, x, y, z, volume, pitch);
        world.getMinecraftServer().getPlayerList().sendPacketNearby(null, x, y, z, volume > 1.0F ? 16.0F * volume : 16.0D, world.dimension, packet);
	}
	
	@Override
	protected void playDirect(Player player) {
		if (player == null) return;
		player.playSound(player.getLocation(), sound, volume, pitch);
	}
	
}
