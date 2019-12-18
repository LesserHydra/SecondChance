# Change Log
All notable changes to this project will be documented in this file.
This project supposedly adheres to [Semantic Versioning](http://semver.org/).

## [1.0.1] - 2019-12-18
This update adds a peek command and squashes a bunch of bugs
### Added
- Added peek command
### Fixed
- Fixed private particles showing in all worlds
- Fixed console spam in Spigot 1.14
- Fixed all known hitbox spawning/despawning issues
### Changed
- Will use the players death location if safe, instead of always using the last known safe location
### Technical
- Removed compatibility modules (for ease of future maintenance)

## [0.8] - 2016-06-09
This update adds options for time/death limits on deathpoints, prelimary administration commands, Bukkit 1.8 support, and fixes a couple bugs.
### Added
- Added 'time till forget' option
- Added 'deaths till forget' option
- Added list, break, and delete subcommands, all with tab-completion
- Added support for all 1.8 versions of bukkit
### Changed
- Replaced 'max deathpoints' with a more predictable 'deaths till forget' option
### Fixed
- Fixed deathpoints being broken by owner/'thief' arrows if 'break on leftclick' is enabled
### Technical
- Reworked gradle build logic, adding in a system for compatibility modules
- Abstracted Bukkit 1.9 dependant API functionality into compatibility modules (three for 1.8, one for 1.9+)
- Simplified versioning system to append "SNAPSHOT" instead of abbreviated commit hash to snapshot builds

## [0.7] - 2016-05-20
This update adds permissions and a world blacklist/whitelist.
### Added
- Added permissions for deathpoint creation and accessing unowned deathpoints
- Added world blacklist which can double as a whitelist
### Fixed
- Fixed hitboxes not despawning on word save (final hitbox bug?)
### Technical
- Reworked the hierarchy of "handlers"
- Simplified save handling code

## [0.6.1] - 2016-05-10
Quick bugfix update
### Fixed
- Fixed \<y\> and \<z\> both resolving to \<x\> in messages

## [0.6] - 2016-05-01
This update adds messages and sounds.
### Added
- Added option to make certain particle effects only show to the owner of a deathpoint
- Added optional and highly configurable messages shown on deathpoint creation and forget
- Added sounds
### Changed
- Made secondary particles only show to owner by default

## [0.5] - 2016-04-28
This update fixes compatibility with Bukkit 1.9 and improves the check for safe locations.
### Added
- Added a safety mechanism for when a hitbox armorstand persists to the world (#11)
### Changed
- Location safety check now runs on a timer instead of using movement event (#5)
### Fixed
- Fixed location not being considered "safe" when riding a horse (#6)
- Fixed location being considered "safe" when over a ledge (#7)
### Technical
- Began building off of Bukkit 1.9 instead of 1.9.2 (#10)
- Added tests for location safety check

## [0.4] - 2016-04-26
This update adds a reload command, fixes a few bugs, and teaches the plugin to play nicely.
### Added
- Added reload command
### Fixed
- Fixed keepInventory not being respected (Issue #1)
- Fixed NPE caused by reload (Issue #3)
- Fixed minor inaccuracy in experience calculation
- Stopped storing drops removed by other plugins
### Technical
- Began building off of base Bukkit API, instead of full Spigot jar
- Began adding unit-tests
- Changed death event priority to HIGHEST from MONITOR

## [0.3] - 2016-04-22
This update adds a configuration file with quite a few options.
### Added
- Configuration file
- Option to allow breaking a deathpoint by punching it
- Option to allow anyone access to all deathpoints, regardless of ownership
- Control over what is held in and dropped from a deathpoint
- Control over maximum number of deathpoints per player
- Control over particles

## [0.2.1] - 2016-04-22
Major bugfix
### Fixed
- Armorstand hitboxes not despawning on chunk unload

## [0.2] - 2016-04-21
First official release
### Added
- Deathpoints are now grouped by world
- Prepared for eventual configuration implementation
### Changed
- Changed save file format
### Fixed
- Contents not dropping in unloaded chunks
- Others?
### Technical
- Moved to Java 8
- Refactored all the things

## [0.1] - 2016-04-05
First unofficial "release"
### Added
- Support for 1.9
### Technical
- Rewrote just about everything

[1.0.1]: https://github.com/lesserhydra/SecondChance/compare/0.8...1.0.1
[0.8]: https://github.com/lesserhydra/SecondChance/compare/0.7...0.8
[0.7]: https://github.com/lesserhydra/SecondChance/compare/0.6.1...0.7
[0.6.1]: https://github.com/lesserhydra/SecondChance/compare/0.6...0.6.1
[0.6]: https://github.com/lesserhydra/SecondChance/compare/0.5...0.6
[0.5]: https://github.com/lesserhydra/SecondChance/compare/0.4...0.5
[0.4]: https://github.com/lesserhydra/SecondChance/compare/0.3...0.4
[0.3]: https://github.com/lesserhydra/SecondChance/compare/0.2.1...0.3
[0.2.1]: https://github.com/lesserhydra/SecondChance/compare/0.2...0.2.1
[0.2]: https://github.com/lesserhydra/SecondChance/compare/0.1...0.2
[0.1]: https://github.com/lesserhydra/SecondChance/compare/833d4eb...0.1
