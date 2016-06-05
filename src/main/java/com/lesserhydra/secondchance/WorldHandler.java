package com.lesserhydra.secondchance;

import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import com.lesserhydra.secondchance.configuration.ConfigOptions;

class WorldHandler {
	
	private final SecondChance plugin;
	private final ConfigOptions options;
	private final World world;
	
	private Deque<Deathpoint> worldDeathpoints;
	private BukkitTask particleTask;
	private BukkitTask ambientSoundTask;
	private BukkitTask timeCheckTask;
	
	
	public WorldHandler(SecondChance plugin, ConfigOptions options, World world) {
		this.plugin = plugin;
		this.options = options;
		this.world = world;
	}
	
	public void init() {
		//Remove residual hitboxes in world
		world.getEntities().stream()
				.filter(e -> e.getType() == EntityType.ARMOR_STAND)
				.map(e -> (ArmorStand) e)
				.filter(Deathpoint::armorstandIsHitbox)
				.forEach(Entity::remove);
		
		//Initiate all deathpoints in world
		this.worldDeathpoints = plugin.getSaveHandler().load(world);
		worldDeathpoints.forEach(Deathpoint::spawnHitbox);
		
		//Add initial "safe" positions to all online players in world
		world.getPlayers().stream()
				.filter(player -> !player.hasMetadata("lastSafePosition"))
				.forEach(player -> player.setMetadata("lastSafePosition", new FixedMetadataValue(plugin, player.getLocation().add(0, 1, 0))));
		
		//Start particle timer for world
		particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> worldDeathpoints.forEach(this::runParticles), 0, options.particleDelay);
		
		//Start ambient sound timer for world
		if (options.ambientSoundDelay > 0 && options.ambientSound.isEnabled()) {
			ambientSoundTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> worldDeathpoints.forEach(this::runAmbientSound), 0, options.ambientSoundDelay);
		}
		
		//Start time check timer for world
		if (options.timeCheckDelay > 0 && options.ticksTillForget >= 0) {
			timeCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTicksTillForget, 0, options.timeCheckDelay);
		}
	}
	
	public void deinit() {
		//Cancel tasks
		particleTask.cancel();
		if (ambientSoundTask != null) ambientSoundTask.cancel();
		if (timeCheckTask != null) timeCheckTask.cancel();
		//Despawn hitboxes
		worldDeathpoints.stream()
				.forEach(Deathpoint::despawnHitbox);
		//Save
		plugin.getSaveHandler().save(world, worldDeathpoints);
		//Clear members
		worldDeathpoints = null;
	}
	
	public void addDeathpoint(Deathpoint deathpoint) {
		deathpoint.spawnHitbox();
		worldDeathpoints.add(deathpoint);
	}
	
	public void destroyDeathpoint(Deathpoint deathpoint) {
		deathpoint.destroy();
		worldDeathpoints.remove(deathpoint);
	}
	
	public void onChunkLoad(Chunk chunk) {
		//Remove residual hitboxes
		Arrays.stream(chunk.getEntities())
				.filter(e -> e.getType() == EntityType.ARMOR_STAND)
				.map(e -> (ArmorStand) e)
				.filter(Deathpoint::armorstandIsHitbox)
				.peek(e -> plugin.getLogger().warning("Found residual armorstand."))
				.forEach(Entity::remove);
		
		//Spawn deathpoint hitboxes
		worldDeathpoints.stream()
				.filter((point) -> chunk.equals(point.getLocation().getChunk()))
				.forEach(Deathpoint::spawnHitbox);
	}
	
	public void onChunkUnload(Chunk chunk) {
		worldDeathpoints.stream()
				.filter((point) -> chunk.equals(point.getLocation().getChunk()))
				.forEach(Deathpoint::despawnHitbox);
	}
	
	public void onWorldSave() {
		//Save
		plugin.getSaveHandler().save(world, worldDeathpoints);
		
		//Despawn hitboxes
		worldDeathpoints.stream()
				.forEachOrdered(Deathpoint::despawnHitbox);
		
		//Schedual hitbox respawn
		final UUID worldUUID = world.getUID();
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (Bukkit.getWorld(worldUUID) == null) return;
			worldDeathpoints.stream()
					.forEachOrdered(Deathpoint::spawnHitbox);
		}, 1);
	}
	
	public Stream<Deathpoint> deathpoints() {
		return worldDeathpoints.stream();
	}
	
	public void updateDeathsTillForget(Player player) {
		for (Iterator<Deathpoint> it = worldDeathpoints.iterator(); it.hasNext();) {
			Deathpoint deathpoint = it.next();
			if (!deathpoint.getOwnerUniqueId().equals(player.getUniqueId())) continue;
			if (deathpoint.updateDeathsTillForget()) forgetDeathpoint(deathpoint, it);
		}
	}
	
	private void updateTicksTillForget() {
		for (Iterator<Deathpoint> it = worldDeathpoints.iterator(); it.hasNext();) {
			Deathpoint deathpoint = it.next();
			if (deathpoint.updateTicksTillForget(options.timeCheckDelay)) forgetDeathpoint(deathpoint, it);
		}
	}
	
	private void forgetDeathpoint(Deathpoint deathpoint, Iterator<Deathpoint> it) {
		//Play sound and message for owner, if online
		Player owner = Bukkit.getPlayer(deathpoint.getOwnerUniqueId());
		if (owner != null) {
			options.forgetSound.run(deathpoint.getLocation(), owner);
			options.forgetMessage.sendMessage(owner, deathpoint);
		}
		
		//Forget deathpoint
		if (options.dropItemsOnForget) deathpoint.dropItems();
		if (options.dropExpOnForget) deathpoint.dropExperience();
		deathpoint.destroy();
		it.remove();
	}
	
	private void runParticles(Deathpoint deathpoint) {
		Location location = deathpoint.getLocation();
		if (!location.getChunk().isLoaded()) return;
		
		Player owner = Bukkit.getPlayer(deathpoint.getOwnerUniqueId());
		options.particlePrimary.run(location, owner);
		options.particleSecondary.run(location, owner);
	}
	
	private void runAmbientSound(Deathpoint deathpoint) {
		Location location = deathpoint.getLocation();
		if (!location.getChunk().isLoaded()) return;
		
		Player owner = Bukkit.getPlayer(deathpoint.getOwnerUniqueId());
		options.ambientSound.run(location, owner);
	}
	
}
