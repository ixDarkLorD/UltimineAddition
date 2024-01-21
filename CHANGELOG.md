# Changelog
This file is for listing all the changes to this project

## v1.2.2 Release | Jan 21, 2024
### Fixed
- [UA-13] Skills Record inventory randomly getting cleared.
- [UA-14] Skills Record data gets deleted at Mainhand.
- [UA-15] Keybindings localization isn't working, Skills Record Inventory display is misplaced.

## v1.2.1 Release | Jan 18, 2024
### Fixed
- [UA-12] Incompatibility with DefaultSettings.

## v1.2.0a Release | Dec 28, 2023
### Fixed
- Crash issue related to mining skill card occurred when playstyle switched into legacy.
- Not copying player ultimine ability after exiting The End.

## v1.2.0 Release | Dec 27, 2023
### Adding
- New Custom Mining Skill Card API `You can create your Custom Mining Skill Card through configs.` Check out the Wiki!
- Adding Playstyle Mode option `If you prefer the old style of the mod, Now you can change it back as before.`
- Adding Villager Trade and Price option for the Mining Skill Card `There will be an option for which villager level should the card appear and change the price.`

## v1.1.1 Release | Dec 2, 2023
### Added
- Curios and Trinkets Integration `Now... you can insert the Skills Record in the curios/trinkets menu.`

### Fixed
- The ChunkManager from caching unloaded chunks.

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

## v1.0.0a Release | Oct 13, 2023
### Fixed
- [Forge] issue within OnDatapackSyncEvent method

## v1.0.0 Release | Oct 13, 2023
### Added
- New Way to Obtain The Ultimine Ability
- New Items introduced. Skills Record, Potions, etc
- 80+ Challenges
- New Challenges System with a Custom Challenge API
- New Advancements
- New Commands

## v0.1.0a Beta | Jan 20, 2023
### Fixed
- Losing the obtained skill after respawning

## v0.1.0 Beta | Dec 28, 2022
- Port to 1.18.2