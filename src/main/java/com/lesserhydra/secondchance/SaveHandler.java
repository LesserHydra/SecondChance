package com.lesserhydra.secondchance;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class SaveHandler {

	private File file;
	private FileConfiguration save;
	
	
	//TODO: Handle worlds separately (File per world/dimension?)
	//		If the server switches worlds, the previous world's deathpoints should be uneffected.
	//		However, if a player has a deathpoint in the Nether and dies in the overworld, the
	//		Nether deathpoint should be destroyed. How to differentiate between worlds and
	//		dimensions? Or perhaps, deathpoints in loaded worlds should be destroyed?
	public void load(String filePath) throws IOException {
		this.file = new File(filePath);
		file.createNewFile();
		this.save = YamlConfiguration.loadConfiguration(file);
	}
	
	public void put(Deathpoint deathpoint) {
		save.set(getKeyFromDeathpoint(deathpoint), deathpoint);
	}
	
	public void putAll(Collection<Deathpoint> deathpoints) {
		deathpoints.forEach(this::put);
	}
	
	public void remove(Deathpoint deathpoint) {
		save.set(deathpoint.getUniqueId().toString(), null);
	}
	
	public void save() throws IOException {
		save.save(file);
	}
	
	public Collection<Deathpoint> getAll() {
		return save.getValues(false).values().stream()
				.filter(obj -> (obj instanceof Deathpoint))
				.map(point -> (Deathpoint) point)
				.collect(Collectors.toList());
	}
	
	private static String getKeyFromDeathpoint(Deathpoint point) {
		return point.getCreationInstant() + "-" + point.getUniqueId().toString();
	}
	
}
