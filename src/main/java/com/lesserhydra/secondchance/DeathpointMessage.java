package com.lesserhydra.secondchance;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DeathpointMessage {
	
	private final String messageString;
	
	public DeathpointMessage(String messageString) {
		this.messageString = messageString.replace('&', ChatColor.COLOR_CHAR);
	}
	
	public void sendMessage(Player player, Deathpoint deathpoint) {
		if (messageString.isEmpty()) return;
		Location location = deathpoint.getLocation();
		player.sendMessage(messageString.replace("<x>", "" + location.getBlockX())
										.replace("<y>", "" + location.getBlockY())
										.replace("<z>", "" + location.getBlockZ())
										.replace("<player>", player.getName()));
	}
	
}
