# Changelog
This file is for listing all the changes to this project

## v1.3.0 Release | Feb 16, 2024
### Adding
- Mining Skill Card Now can activate the ultimine ability upon leveling to mastered. "With the option to turn it off"
- Now, the challenges panel has its color synced to the Skills Record screen.
### Changed
- The Skills Record info is clarified when the player tries to register points toward the challenges through non-eligible blocks.
- Changing the Skills Record Info translation from "Invalid Block" to "Ineligible Block"
- Changing from storing the Ineligible Blocks in Chunk data to World Saved Data
### Fixed
- [UA-19] Challenges doesn't get cleared after reaching the mastered tier in the Mining Skill Card.

## v1.2.4 Release | Feb 6, 2024
### Fixed
- [UA-17] The Skills Record doesn't register any point toward mined blocks if it's in th Curios slot.

## v1.2.3 Release | Feb 5, 2024
### Fixed
- [UA-16] Chunks not loading properly.

## v1.2.2 Release | Jan 21, 2024
### Fixed
- [UA-13] Skills Record inventory randomly getting cleared.
- [UA-14] Skills Record data gets deleted at Mainhand.
- [UA-15] Keybindings localization isn't working, Skills Record Inventory display is misplaced.

## v1.2.1 Release | Jan 18, 2024
### Fixed
- [UA-12] Incompatibility with DefaultSettings.

## v1.2.0b Release | Jan 7, 2024
### Fixed
- [UA-11] Compatibility with the new version (v2001.1.4) of the FTB Ultimine.

## v1.2.0a Release | Dec 28, 2023
### Fixed
- Crash issue related to mining skill card occurred when playstyle switched into legacy.
- Not copying player ultimine ability after exiting The End.

## v1.2.0 Release | Dec 27, 2023
### Adding
- Adding Playstyle Mode option `If you prefer the old style of the mod, Now you can change it back as before.`
- Adding Villager Trade and Price option for the Mining Skill Card `There will be an option for which villager level should the card appear and change the price.`

## v1.1.2 Release | Dec 21, 2023
### Added
- New Custom Mining Skill Card API `You can create your Custom Mining Skill Card through configs.` Check out the Wiki!


## v1.1.1 Release | Dec 2, 2023
### Added
- Curios and Trinkets Integration `Now... you can insert the Skills Record in the curios/trinkets menu.`
### Fixed
- The ChunkManager from caching unloaded chunks.


## v1.1.0-Hotfix Release | Nov 12, 2023
### Fixed
- [UA9] Fixed the Tools Validator methods to recognize tools that doesn't have the tag.


## v1.1.0 Release | Nov 11, 2023
### Added
- Adding Arabic Translation
- Progression Display `You can now pin any challenges you want on the screen.`
- Is Placed By Entity Condition Option `You can Enable/Disable the Is Placed By Entity Condition.`
- Adjustable Mining Skill Card's Potion Points `You can change the potion points values in each tier.`
- Adjustable Number of Challenges for each Tier Config `You can change the values on how many challenges should be given in each tier.`
  *But remember that you must have the exact number of challenges in the Datapack. Otherwise, it will make the game crash!*
### Fixed
- [Forge] Saving player ability after respawn

## v0.1.1a Beta | Oct 13, 2023
### Fixed
- [Forge] issue within OnDatapackSyncEvent method


## v0.1.1 Beta | Oct 11, 2023
- Fixing issue related to packet


## v0.1.0 Beta | Oct 9, 2023
### It's an early release
`Don't hesitate to submit a report if you have faced an issue.`
- Port to 1.20.1