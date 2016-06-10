package com.lesserhydra.secondchance.compat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class BaseSoundEffect implements SoundEffect {
	
	protected final boolean enabled;
	protected final String sound;
	protected final float volume;
	protected final float pitch;
	protected final boolean direct;
	
	public BaseSoundEffect(boolean enabled, String sound, float volume, float pitch, boolean direct) {
		this.enabled = enabled;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.direct = direct;
	}
	
	@Override
	public void run(Location location, Player player) {
		if (!enabled) return;
		if (direct) playDirect(player);
		else playIndirect(location);
	}
	
	protected abstract void playIndirect(Location location);
	
	protected abstract void playDirect(Player player);
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public String getSound() {
		return sound;
	}
	
	@Override
	public float getVolume() {
		return volume;
	}
	
	@Override
	public float getPitch() {
		return pitch;
	}
	
	@Override
	public boolean isDirect() {
		return direct;
	}
	
}
