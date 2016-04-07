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
	
	public void put(DeathPoint deathpoint) {
		save.set(deathpoint.getUUID().toString(), deathpoint);
	}
	
	public void putAll(Collection<DeathPoint> deathpoints) {
		deathpoints.forEach(deathpoint -> save.set(deathpoint.getUUID().toString(), deathpoint));
	}
	
	public void remove(DeathPoint deathpoint) {
		save.set(deathpoint.getUUID().toString(), null);
	}
	
	public void save() throws IOException {
		save.save(file);
	}
	
	public Collection<DeathPoint> getAll() {
		return save.getValues(false).values().stream()
				.filter(obj -> (obj instanceof DeathPoint))
				.map(point -> (DeathPoint) point)
				.collect(Collectors.toList());
	}
	
}
