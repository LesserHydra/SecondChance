package com.lesserhydra.secondchance;

import static org.mockito.Mockito.*;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.lesserhydra.secondchance.compat.Compat;
import com.lesserhydra.secondchance.compat.ParticleEffect;
import com.lesserhydra.secondchance.compat.SoundEffect;

public class TestCompat implements Compat{
	
	@Override
	public String getVersion() {
		return "test";
	}
	
	@Override
	public ItemStack[] inventoryContents(PlayerInventory inventory) {
		return ArrayUtils.addAll(inventory.getContents(), inventory.getArmorContents());
	}
	
	@Override
	public boolean armorstandIsHitbox(ArmorStand entity) {
		return (entity.getHealth() == 1337D);
	}

	@Override
	public ArmorStand spawnHitbox(Location location) {
		ArmorStand result = mock(ArmorStand.class);
		when(result.getLocation()).thenReturn(location);
		when(result.getType()).thenReturn(EntityType.ARMOR_STAND);
		when(result.getHealth()).thenReturn(1337D);
		return result;
	}

	@Override
	public SoundEffect makeSoundEffect(boolean enabled, String sound, float volume, float pitch, boolean direct) {
		SoundEffect result = mock(SoundEffect.class);
		return result;
	}

	@Override
	public ParticleEffect makeParticleEffect(String particleName, int amount, double spread, double speed, boolean ownerOnly) {
		ParticleEffect result = mock(ParticleEffect.class);
		return result;
	}
	
}
