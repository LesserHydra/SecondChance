#SecondChance

##Info
---
SecondChance is a simple death-point plugin for Minecraft Spigot/Bukkit, inspired in part by the DarkSouls soul retrieval mechanic. It is currently in beta status, but nonetheless _should_ be safe for use.

Pull requests are certainly welcome and appreciated.

##Basic Mechanics
---
When a player dies, a floating death-point will appear at the last "safe" location that player moved through. It will persist until the player either accesses it or dies a second time, thus giving the player a _"second chance"_ at retrieving their items.

A death-point contains all items dropped by the player on death, as well as the player's experience points. Accessing it will allow removal of items, and will grant stored exp. On destruction, the death-point will drop all contents _except for exp_ into the world.

##Notes
---
* The death-point particle effects are not final (open to suggestions), and will be configurable by full release.
* The save format is also likely to change, although that shouldn't be an issue. The plugin should automatically convert any old files that contain valid death-points.
