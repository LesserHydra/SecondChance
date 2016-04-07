package com.logicallunacy.secondChance;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class SaveHandler {

	private File file;
	private FileConfiguration save;
	
	
	public void load(String filePath) throws IOException {
		this.file = new File(filePath);
		file.createNewFile();
		this.save = YamlConfiguration.loadConfiguration(file);
	}
	
	public void put(Deathpoint deathpoint) {
		save.set(deathpoint.getUniqueId().toString(), deathpoint);
	}
	
	public void putAll(Collection<Deathpoint> deathpoints) {
		deathpoints.forEach(deathpoint -> save.set(deathpoint.getUniqueId().toString(), deathpoint));
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
	
}
