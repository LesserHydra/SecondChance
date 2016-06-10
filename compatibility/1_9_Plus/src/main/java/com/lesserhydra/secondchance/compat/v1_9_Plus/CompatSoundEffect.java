package com.lesserhydra.secondchance.compat.v1_9_Plus;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.lesserhydra.secondchance.compat.BaseSoundEffect;

public class CompatSoundEffect extends BaseSoundEffect {

	public CompatSoundEffect(boolean enabled, String sound, float volume, float pitch, boolean direct) {
		super(enabled, sound, volume, pitch, direct);
	}
	
	@Override
	protected void playIndirect(Location deathpointLocation) {
		deathpointLocation.getWorld().playSound(deathpointLocation, sound, volume, pitch);
	}
	
	@Override
	protected void playDirect(Player player) {
		if (player == null) return;
		player.playSound(player.getLocation(), sound, volume, pitch);
	}
	
}
