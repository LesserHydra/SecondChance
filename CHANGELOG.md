# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/), or so I think.

## [0.5] - 2016-04-28
This update fixes compatibility with Bukkit 1.9 and improves the check for safe locations
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
### Techinical
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
- Rewrote just about everything everything

[0.5]: https://github.com/Roboboy64/SecondChance/compare/0.4...0.5
[0.4]: https://github.com/Roboboy64/SecondChance/compare/0.3...0.4
[0.3]: https://github.com/Roboboy64/SecondChance/compare/0.2.1...0.3
[0.2.1]: https://github.com/Roboboy64/SecondChance/compare/0.2...0.2.1
[0.2]: https://github.com/Roboboy64/SecondChance/compare/0.1...0.2
[0.1]: https://github.com/Roboboy64/SecondChance/compare/833d4eb...0.1
