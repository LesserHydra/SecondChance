# SecondChance

## Info
SecondChance is a simple deathpoint plugin for Minecraft Bukkit, inspired in part by the DarkSouls soul retrieval mechanic.

Pull requests are certainly welcome and appreciated.

Please note that this project relies on Java 8.

## Basic Mechanics
When a player dies, a floating deathpoint will appear at the last "safe" location that player moved through. It will persist until the player either accesses it or dies a second time, thus giving the player a _"second chance"_ at retrieving their items.

## Specifics
#### Creation
A deathpoint contains a player's inventory and/or experience. If the inventory is stored, extra drops as well as items kept from dropping will not be stored. As an example, say some plugin keeps a written book in a player's inventory at all times, or another drops a skull for PvP kills. Neither of these items will be stored in the resulting deathpoint, and the skull will still drop where the player died.

#### Access
A deathpoint can only be accessed or broken by its owner (by default) or other players with the thief permission (see permissions below).

#### Forgetting
A deathpoint will be forgotten when the owner dies a configurable number of times, or after a set amount of time (disabled by default).

When a deathpoint is forgotten, it will either drop or delete its contents depending on configuration. This behavior may be configured seperately for items and experience.

#### Safe positions
An entity is in a safe position if and only if it is riding a vehicle in a safe position or all the following are true:
- isOnGround()
- the blocks directly at and one above its position are not solid and not liquid
- the block directly below its position is solid

## Configuration
Most mechanics are configurable, and worlds can be blacklisted or whitelisted by name.
All sounds, particles, and messages can be modified or disabled.

[See default config file](src/main/resources/config.yml)

## Commands
Deathpoints can be listed by world, player, and/or proximity. The listed deathpoints can then be accessed by index for peeking, breaking, or deleting. Note that peeking, unlike normal accessing, leaves the deathpoint intact.

| Subcommand | Arguments                | Description                |
|------------|--------------------------|----------------------------|
| reload     | NA                       | Reload configuration files |
| list       | [owner] [world] [radius] | List deathpoints by owner, world, and/or proximity |
| peek       | \<index\>                | Open indexed deathpoint's inventory for sender |
| break      | \<index\>                | Break indexed deathpoint, dropping contents according to config |
| delete     | \<index\>                | Delete deathpoint along with its contents |

## Permissions
| Permission               | Description                             | Default |
|--------------------------|-----------------------------------------|---------|
| secondchance.enabled     | Allows spawning of deathpoints on death | true    |
| secondchance.thief       | Allows access to protected deathpoints  | false   |
| secondchance.maincommand | Allows use of admin commands            | op      |
