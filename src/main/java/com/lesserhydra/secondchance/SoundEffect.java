package com.lesserhydra.secondchance;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SoundEffect {
	
	private final boolean enabled;
	private final String sound;
	private final float volume;
	private final float pitch;
	private final boolean direct;
	
	public SoundEffect(boolean enabled, String sound, float volume, float pitch, boolean direct) {
		this.enabled = enabled;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.direct = direct;
	}
	
	public void run(Location deathpointLocation, Player player) {
		if (!enabled) return;
		if (direct) playDirect(player);
		else playIndirect(deathpointLocation);
	}
	
	private void playIndirect(Location deathpointLocation) {
		deathpointLocation.getWorld().playSound(deathpointLocation, sound, volume, pitch);
	}
	
	private void playDirect(Player player) {
		player.playSound(player.getLocation(), sound, volume, pitch);
	}

	
	public boolean isEnabled() {
		return enabled;
	}

	
	public String getSound() {
		return sound;
	}

	
	public float getVolume() {
		return volume;
	}

	
	public float getPitch() {
		return pitch;
	}

	
	public boolean isDirect() {
		return direct;
	}
	
}
