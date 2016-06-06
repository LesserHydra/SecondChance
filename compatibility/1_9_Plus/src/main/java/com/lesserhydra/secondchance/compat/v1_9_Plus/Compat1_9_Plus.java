package com.lesserhydra.secondchance.compat.v1_9_Plus;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.lesserhydra.secondchance.compat.Compat;
import com.lesserhydra.secondchance.compat.ParticleEffect;
import com.lesserhydra.secondchance.compat.SoundEffect;

public class Compat1_9_Plus implements Compat {

	private static final UUID HITBOX_ATTRIBUTE_UUID = UUID.fromString("f36fe1df-0036-475c-9f5a-52b95af83c96");
	private static final String HITBOX_ATTRIBUTE_STRING = "isSecondChanceHitbox";
	
	
	@Override
	public String getVersion() {
		return "1.9+";
	}
	
	@Override
	public ItemStack[] inventoryContents(PlayerInventory inventory) {
		return inventory.getContents();
	}
	
	@Override
	public boolean armorstandIsHitbox(ArmorStand entity) {
		return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers().stream()
				.filter(mod -> HITBOX_ATTRIBUTE_UUID.equals(mod.getUniqueId()))
				.anyMatch(mod -> HITBOX_ATTRIBUTE_STRING.equals(mod.getName()));
	}
	
	@Override
	public ArmorStand spawnHitbox(Location location) {
		Location standLoc = location.clone().add(0, -0.75, 0);
		ArmorStand hitbox = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
		hitbox.setGravity(false);
		hitbox.setVisible(false);
		
		//Add attribute for identifying in case of persistance (Fallback, not relied upon for normal operation)
		hitbox.getAttribute(Attribute.GENERIC_MAX_HEALTH)
				.addModifier(new AttributeModifier(HITBOX_ATTRIBUTE_UUID, HITBOX_ATTRIBUTE_STRING, 0, Operation.ADD_NUMBER));
		
		return hitbox;
	}
	
	@Override
	public SoundEffect makeSoundEffect(boolean enabled, String sound, float volume, float pitch, boolean direct) {
		return new CompatSoundEffect(enabled, sound, volume, pitch, direct);
	}
	
	@Override
	public ParticleEffect makeParticleEffect(String particleName, int amount, double spread, double speed, boolean ownerOnly) {
		return new CompatParticleEffect(particleName, amount, spread, speed, ownerOnly);
	}
	
}
