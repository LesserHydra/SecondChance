package com.lesserhydra.secondchance;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
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

public class WorldHandler {
	
	private final SecondChance plugin;
	private final ConfigOptions options;
	private final World world;
	
	private final SaveHandler saveHandler;
	private final Deque<Deathpoint> worldDeathpoints = new LinkedList<>();
	
	private BukkitTask particleTask;
	private BukkitTask ambientSoundTask;
	
	
	public WorldHandler(SecondChance plugin, ConfigOptions options, World world) {
		this.plugin = plugin;
		this.options = options;
		this.world = world;
		
		this.saveHandler = new SaveHandler(plugin.getSaveFolder(), world);
	}
	
	public void init() {
		//Remove residual hitboxes in world
		world.getEntities().stream()
				.filter(e -> e.getType() == EntityType.ARMOR_STAND)
				.map(e -> (ArmorStand) e)
				.filter(Deathpoint::armorstandIsHitbox)
				.forEach(Entity::remove);
		
		//Load save file
		saveHandler.load();
		
		//Initiate all deathpoints in world
		saveHandler.stream()
				.sorted((p1, p2) -> p1.getCreationInstant().compareTo(p2.getCreationInstant()))
				.forEachOrdered(worldDeathpoints::add);
		worldDeathpoints.forEach(Deathpoint::spawnHitbox);
		
		//Add initial "safe" positions to all online players in world
		world.getPlayers().stream()
				.filter(player -> !player.hasMetadata("lastSafePosition"))
				.forEach(player -> player.setMetadata("lastSafePosition", new FixedMetadataValue(plugin, player.getLocation().add(0, 1, 0))));
		
		//Start particle timer for world
		particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> worldDeathpoints.forEach(this::runParticles), 0, options.particleDelay);
		
		//Start ambient sound timer for world
		ambientSoundTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> worldDeathpoints.forEach(this::runAmbientSound), 0, options.ambientSoundDelay);
	}
	
	public void deinit() {
		//Cancel tasks
		particleTask.cancel();
		ambientSoundTask.cancel();
		//Despawn hitboxes
		worldDeathpoints.stream()
				.forEach(Deathpoint::despawnHitbox);
		//Save
		saveHandler.save();
		//Clear members
		worldDeathpoints.clear();
	}
	
	public void addDeathpoint(Deathpoint deathpoint) {
		deathpoint.spawnHitbox();
		worldDeathpoints.add(deathpoint);
		saveHandler.put(deathpoint);
	}
	
	public void destroyDeathpoint(Deathpoint deathpoint) {
		deathpoint.destroy();
		worldDeathpoints.remove(deathpoint);
		saveHandler.remove(deathpoint);
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
		saveHandler.putAll(worldDeathpoints);
		saveHandler.save();
		
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
