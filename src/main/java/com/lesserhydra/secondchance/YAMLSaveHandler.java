package com.lesserhydra.secondchance;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * A SaveHandler implementation using Bukkit's YAMLConfiguration.
 */
class YAMLSaveHandler implements SaveHandler {
	
	private static final String SAVE_SECTION = "deathpoints";
	
	private final File saveFolder;
	
	
	/**
	 * Constructs a YAMLSaveHandler with a given save folder.
	 * @param saveFolder The folder to save data to
	 */
	public YAMLSaveHandler(File saveFolder) {
		this.saveFolder = saveFolder;
	}
	
	@Override
	public Deque<Deathpoint> load(World world) {
		File file = new File(saveFolder + File.separator + world.getName() + ".yml");
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		YamlConfiguration save = YamlConfiguration.loadConfiguration(file);
		Deque<Deathpoint> results = new LinkedList<>();
		save.getList(SAVE_SECTION, Arrays.asList()).stream()
				.filter(obj -> (obj instanceof Deathpoint))
				.map(point -> (Deathpoint) point)
				.forEachOrdered(results::add);
		return results;
	}
	
	@Override
	public void save(World world, Collection<Deathpoint> deathpoints) {
		File file = new File(saveFolder + File.separator + world.getName() + ".yml");
		YamlConfiguration save = new YamlConfiguration();
		save.set(SAVE_SECTION, deathpoints);
		
		//TODO: Handle gracefully?
		try {
			save.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
