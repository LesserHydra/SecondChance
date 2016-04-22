package com.lesserhydra.secondchance;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class SaveHandler {

	private final File file;
	private FileConfiguration save;
	
	public SaveHandler(File saveDirectory, World world) {
		file = new File(saveDirectory + File.separator + world.getName() + ".yml");
	}
	
	public void load() {
		//TODO: Handle gracefully
		try {
			file.createNewFile();
			this.save = YamlConfiguration.loadConfiguration(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void put(Deathpoint deathpoint) {
		save.set(getKeyFromDeathpoint(deathpoint), deathpoint);
	}
	
	/*public void putAll(Collection<Deathpoint> deathpoints) {
		deathpoints.forEach(this::put);
	}*/
	
	public void remove(Deathpoint deathpoint) {
		save.set(getKeyFromDeathpoint(deathpoint), null);
	}
	
	public void save() {
		//TODO: Handle gracefully
		try {
			save.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Collection<Deathpoint> getAll() {
		return save.getValues(false).values().stream()
				.filter(obj -> (obj instanceof Deathpoint))
				.map(point -> (Deathpoint) point)
				.collect(Collectors.toList());
	}
	
	private static String getKeyFromDeathpoint(Deathpoint point) {
		return point.getCreationInstant().toString().replace(".", ",") + "/" + point.getUniqueId().toString();
	}
	
}
