package com.lesserhydra.secondchance;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class SaveHandler {

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
		List<Deathpoint> deathpointList = stream()
				.filter(point -> !deathpoint.equals(point))
				.collect(Collectors.toList());
		deathpointList.add(deathpoint);
		save.set("deathpoints", deathpointList);
	}
	
	public void putAll(Collection<Deathpoint> deathpoints) {
		List<Deathpoint> deathpointList = stream()
				.collect(Collectors.toList());
		deathpointList.removeAll(deathpoints);
		deathpointList.addAll(deathpoints);
		save.set("deathpoints", deathpointList);
	}
	
	public void remove(Deathpoint deathpoint) {
		List<?> deathpointList = save.getList("deathpoints", Arrays.asList());
		deathpointList.remove(deathpoint);
		save.set("deathpoints", deathpointList);
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
	
	public Stream<Deathpoint> stream() {
		return save.getList("deathpoints", Arrays.asList()).stream()
				.filter(obj -> (obj instanceof Deathpoint))
				.map(point -> (Deathpoint) point);
	}
	
	private final File file;
	private FileConfiguration save;
	
}
