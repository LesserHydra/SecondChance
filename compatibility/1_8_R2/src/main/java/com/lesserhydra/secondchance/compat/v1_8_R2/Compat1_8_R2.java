package com.lesserhydra.secondchance.compat.v1_8_R2;

import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.lesserhydra.secondchance.compat.Compat;
import com.lesserhydra.secondchance.compat.ParticleEffect;
import com.lesserhydra.secondchance.compat.SoundEffect;
import net.minecraft.server.v1_8_R2.AttributeModifier;
import net.minecraft.server.v1_8_R2.EntityArmorStand;
import net.minecraft.server.v1_8_R2.GenericAttributes;

public class Compat1_8_R2 implements Compat {

	private static final UUID HITBOX_ATTRIBUTE_UUID = UUID.fromString("f36fe1df-0036-475c-9f5a-52b95af83c96");
	private static final String HITBOX_ATTRIBUTE_STRING = "isSecondChanceHitbox";
	
	
	@Override
	public String getVersion() {
		return "1.8_R2";
	}
	
	@Override
	public ItemStack[] inventoryContents(PlayerInventory inventory) {
		return ArrayUtils.addAll(inventory.getContents(), inventory.getArmorContents());
	}
	
	@Override
	public boolean armorstandIsHitbox(ArmorStand entity) {
		AttributeModifier mod = ((CraftLivingEntity) entity).getHandle()
				.getAttributeInstance(GenericAttributes.maxHealth)
				.a(HITBOX_ATTRIBUTE_UUID); //OBF: AttributeModifiable line 65: getByUUID
		return (mod != null && HITBOX_ATTRIBUTE_STRING.equals(mod.b())); //OBF: AttributeModifier line 34: getName
	}

	@Override
	public ArmorStand spawnHitbox(Location location) {
		Location standLoc = location.clone().add(0, -0.75, 0);
		ArmorStand hitbox = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
		hitbox.setGravity(false);
		hitbox.setVisible(false);
		
		//Add attribute for identifying in case of persistance (Fallback, not relied upon for normal operation)
		EntityArmorStand nmsStand = ((CraftArmorStand) hitbox).getHandle();
		nmsStand.getAttributeInstance(GenericAttributes.maxHealth)
				.b(new AttributeModifier(HITBOX_ATTRIBUTE_UUID, HITBOX_ATTRIBUTE_STRING, 0, 0)); //OBF: AttributeModifiable line 73: addModifier
		
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
