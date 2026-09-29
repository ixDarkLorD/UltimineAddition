# Changelog
This file is for listing all the changes to this project
<hr>

## v26.1.2-1 Release - Unreleased (Minecraft 26.1.2 port)
### ✨ New Features
- Redesigned the Skills Record's card viewer: the selected card's tiers and challenges are shown on a map you can pan (drag) and zoom (mouse wheel or `+`/`-`). Completed tiers, the current tier's live progress and locked tiers are all visible.
- Clicking a challenge opens its details (description, target blocks, progress, completion date) with Pin and Edit actions.
- The card viewer can be expanded into a large draggable window.
- Mining Skill Cards now keep a history of the tiers they completed and when each challenge was finished. The history follows the card between Skills Records.
- Added Shape Certificates (#42): when a Mining Skill Card reaches Novice, Apprentice or Adept for the first time, a Shape Certificate of that tier for the card's tool (e.g. "Novice Shape Certificate (Pickaxe)") can be claimed by clicking the tier's box in the Skills Record's card viewer, once per card and tier (the box shows the certificate and tells you if your inventory is full). Using one permanently unlocks its Ultimine shapes for that tool only (Small Square; Escape and Mining Tunnel; Small and Large Tunnel by default), and unlocks add up. Without the Miner Certificate, Ultimine then works with the shapes learned for the tool in hand, limited by that tool's certificate tier. Mine-Go Juice is the alternative: while it lasts, its tool can use every shape. The Miner Certificate still unlocks everything for good. The shapes per certificate are configurable, and the feature can be turned off (`progression.shape_certificates`).
- Tier-up taste: a card tiering up grants a short Mine-Go Juice effect for its tool (60 s by default), so Ultimine can be tried right away.
- Streaks: breaking challenge blocks in quick succession builds a streak, and every 5th block of a streak counts an extra point.
- Lucky Finds: each challenge block has a 5% chance to count double.
- Challenge rerolls: once per tier, an unstarted challenge can be swapped for a new one from its details panel, for 16 ink.
- New notice HUD above the hotbar, styled like the challenges panel, for Ultimine being locked (replacing the chat messages), tier-up rewards, streaks and lucky finds.
- Ultimine undo (#50): press Ctrl + Z after an Ultimine to preview putting it back: see-through ghosts of the broken blocks appear inside one connected outline, like FTB Ultimine's selection, and a small panel under the crosshair lists what it costs, how many undos you have left, how long this one stays undoable, and a timer bar before the panel fades. Press Ctrl + Z again to confirm, and the panel turns into a radial progress bar with a percentage while the blocks go back; sneak (or wait, or walk away) to cancel. While one undo is growing back you can preview and confirm the next ones: they queue up and start one after another (the progress panel shows how many are waiting). Undoing takes back the items and experience the blocks dropped (from the ground first, then from your inventory) and is refused, with a warning listing what's missing, when you don't have them. The blocks then grow back into place in the order they were broken, as one smooth wave, and the progress panel follows that wave. The last 3 operations within 5 minutes can be undone. Blocks with contents (chests, furnaces...) come back empty in their original form (their ghosts and growth draw through their block-entity renderers too); what spilled out of them stays yours and isn't part of the cost, and a shulker box paid back hands you what it held. All of it is configurable in the server config's `undo` group.
- `/ultimine_addition ultimine_ability <targets> shapes grant <novice|apprentice|adept> [tool]` (all tools when omitted) and `... shapes reset` commands.
- The configs now use CoolCatLib's config system, as TOML files: `ultimine_addition-client.toml` and `ultimine_addition-server.toml`. Existing `.json5` configs from earlier 26.1.2 builds are converted once (the old file is kept as `.json5.bak`). Edit them in-game with `/ultimine_addition config` (or from the NeoForge mod list / Mod Menu): searchable screens with sliders, a color picker, undo/redo, reset buttons and settings that grey out when the setting they depend on is off. Operators can change server settings live, and every player gets the new values right away; edits to the files are picked up without a restart. The old `.toml` configs are no longer read, so settings need to be set again.
- The Skills Record's configuration button now opens a small settings window over the book (background and label colors, animations, text shadow, challenges panel alignment) instead of the side panel. It's drawn like the book: its frame, a striped title bar like the notice's, a dark inner screen, a matching striped footer for the buttons, and grey controls.
- Empty Mining Skill Cards are crafted now, early-game friendly: five papers around a token of each tool's work (copper, iron or coal for the pickaxe, dirt for the shovel, a log for the axe and seeds for the hoe) make one card. The Card Blueprint (and its advancements) and the Toolsmith's card trade are gone; the Shape Selector takes a compass where it took a Card Blueprint. Card Blueprints already in a world disappear.
- Dyed Skills Records: the Skills Record is now crafted as a clipboard (an iron clip, paper around an empty Mining Skill Card, and a board of planks); plain planks make the plain, white, record, and a dye in the middle of the board makes that color's record, and any Skills Record can be recolored with a dye as often as you like, keeping everything inside (JEI shows the recoloring for each dye). Each color is its own edition with a recolored item and a Minecraft-inspired motif (Snowfall, Harvest, Allium, Skyward, Beehive, Slime, Cherry Grove, Stonecutter, Rainfall, Warm Ocean, Amethyst, Deep Lapis, Mushroom Fields, Firefly Forest, Nether Ember, Starry Night): a tinted book, labels colored to stay readable on it, its motif moving in the card viewer's background in its own way (a spinning snowflake, a glowing pumpkin, a swaying allium, a drifting cloud, a bee flying figure eights, a bouncing slime, a falling blossom, a mining pickaxe, a splashing raindrop, a swimming fish, a pulsing crystal, a spinning gem, a breathing mushroom, a swaying tree, a flickering flame, a twinkling star), and its own living background that pans with the map (snowfall, falling leaves, allium petals, drifting clouds, pollen and a bee, bouncing slimes, cherry petals, sifting gravel, rain, bubbles, amethyst glints, rippling light, spores, fireflies, rising embers, a starry sky with shooting stars). The HUD panels follow the record you carry. A plain Skills Record is the white one (Snowfall), with a new silver board texture. All 16 are in the creative tab.
- The playstyle mode is now a server setting (`general.playstyle_mode`) and applies at once: the server reloads its recipes, items switch behaviour right away and clients rebuild the creative tab. Players on a server play by the server's mode and get their own back when they leave. A mode set in the old `ultimine_addition-startup.toml` carries over once (the file is kept as `.bak`).

### ⚙️ Refactoring
- Architectury API is no longer required: events, registration, networking, menus and client registries now go through CoolCatLib's cross-loader layer.
- Now requires both **CoolCatLib: Core** and **CoolCatLib: Canvas** (CoolCatLib was split in two), and NeoForge 26.1.2.112 or newer.
- A player's Ultimine ability data (Miner Certificate, learned shapes) is now a CoolCatLib attachment, the same on both loaders, saved with the player, kept through death and synced to them whenever it changes. Existing saves are moved over when the player joins.

### 🐛 Bug Fixes & Improvements
- Skills Record contents are now stored in the world save and linked to the item by UUID; existing Skills Records are migrated automatically the first time they are loaded.
- Mining Skill Card challenges and potion points are now stored in the world save with the card's history; the item keeps its UUID and tier. Existing cards are migrated automatically.
- Cards without a saved UUID no longer share one.
- The server now checks permissions before applying challenge edits from the Skills Record.
- The Skills Record window is larger, giving the card viewer more room.
- The challenge details show a progress bar and the target blocks as items.
- Removed the Skills Record's Background Color and Label Color settings; the record's dye sets its look instead.
- The Skills Record's "Inventory" label sits in a box framed on its left, top and right in the gray of the inventory's border, with a backing that always contrasts with the label.
- Removed the "Progression Bar" option and its "Show Progression Bar" keybind; progress is shown in the card viewer instead.
- Removed the Skills Record scroller; the card viewer now uses its space.
- Redesigned the card viewer's look: shaped tier and challenge nodes, animated connections and background, hover and reveal animations (follows the Animations option). The expanded viewer uses the Skills Record's frame.
- Challenges of locked tiers are hidden until the tier is unlocked.
- Fixed the mouse wheel not scrolling the Shape Selector's shape list.
- The server config's debugging loggers (`debugging.*`) are now off by default.
- The placeholder items in the Skills Record's empty card, pen and paper slots are now greyed and see-through.
- The card viewer's background is now a soft, slowly drifting gradient in the Skills Record's background color, with faint Mining Skill Cards of different sizes scattered across it in an uneven grid, slowly scrolling to the right (their frame, written lines, picture box and badge).
- "No card inserted" and "select a card" are now guide panels in the card viewer's style, each with a small looping animation of what to do: a card dropping into a card slot, or a cursor right-clicking a card in its slot, which gets selected.
- Opening a challenge's details slides the tree up as it fades, then the details rise into place as they fade in; closing them fades back to the tree.
- The edge fades of the card viewer shrink into their side as you reach the end of the tree on that side.
- The challenge details' footer buttons use smaller labels and are centered in the footer, and button labels too long for their button (e.g. in other languages) shrink to fit. The guide panels' text shrinks the same way to stay inside its plate.
- The selected card slot and its marker get an animated gradient outline, one shape running up both sides of the marker and around the slot, in brighter shades of the Skills Record's background color (or darker ones when the color is already bright).
- The challenge details and shape choice panels (scrollbar and progress bar track too), and the card viewer's tiers, badges, challenges and slots, take on the Skills Record's background color. A challenge's box turns gold while in progress (a gold frame, with a gold gradient cycling around it when animated) and green once done; its count is colored by its state, and a done challenge gets a check mark (one needing Consume Mode a red "!").
- The challenge details show the challenge's target block and name with its tier small beside it, and its footer buttons have icons (the reroll one a pixel glyph from the mod's icon font).
- The card viewer's background effect uses fewer particles when zoomed out, so zooming out stays smooth.
- The card viewer's Fit and Expand/Collapse buttons fade in and out instead of popping.
- The challenge boxes' block slot is a bit bigger.
- Fixed a thin line from another item sometimes showing beside items drawn at a zoom (such as the card viewer's cycling challenge blocks).
- The edition's background effect and its emblems (now scattered across the background in different sizes, each moving on its own) pan and zoom with the card viewer's map.
- The Edit Challenge dialog is laid out again: everything is centred, its value field shows its number, the quick buttons fit their labels, and the change is shown centred above Done and Cancel. The challenge ID line is gone; hover the challenge's name to see its ID and click to copy it.
- The open Skills Record follows its settings as they change (in its settings window or the config file) instead of only when reopened.
- A missing pen or paper, or a pen without enough ink, is marked with a red overlay and an animated red outline on its slot, and the card viewer is dimmed behind the banner that explains it.
- Fixed a Mining Skill Card's tier change not reaching the player on a server: the card kept showing its old tier until it was moved.
- Fixed the JEI plugin failing on clients connected to a dedicated server (the Shape Certificate info pages found no shapes).
- `/ultimine_addition skills_record inspect [targets]` and `/ultimine_addition_client skills_record inspect` (operators) print the carried Skills Records and cards as the server and the client see them, to check they're in sync.
- Ultimine undo puts glass panes, iron bars, fences and walls back connected as they were (they came back with gaps where neighbours broken earlier in the same Ultimine had been), and restores doors, beds and tall plants whole: the other half, removed together with the half that was mined, now comes back too (and shows in the preview and growth). Torches, lanterns, rails, buttons and anything else that popped off the mined blocks come back as well, paid for with the items they dropped, so nothing gets duplicated.
- Cleaner undo panel: its body is now one soft outline with faint diagonal lines, without the corner marks.
- Less grind: challenges per tier are now 1/2/2/3 (was 1/2/3/4), required amounts are roughly halved, and rare targets are much lower (e.g. diamond ores 8-16, emerald ores 4-8, ancient debris 4-8).
- The Shape Selector marks shapes that haven't been learned from a Shape Certificate yet.
- Without the Miner Certificate or a Mine-Go Juice, the Ultimine shape list only offers the shapes learned for the tool in hand; with none it shows "No Shape Learned" instead of a shape the player can't use.
- Fixed `/ultimine_addition ultimine_shape blacklist add|remove` failing with an error.
- The Ultimine notice panel fits its text (long lines wrap), no longer has a line under the title, and its item icon now fades out with the panel. Its body now hangs from the title bar's bottom edge instead of sticking out past the bar's pointed ends.
- The creative tab only lists the plain Mining Skill Cards (no pre-tiered copies).
- New consume effect: fragments of the consumed block and glowing motes float up out of the spot, then get vacuumed into the player completing the challenge, with new sounds (a breeze-like inhale, and a chime when they reach the player).
- Shape Certificates show their tool's icon, like the Mining Skill Cards, with an outlined slot.
- Every Shape Certificate now teaches one shape. When a card reaches Novice, Apprentice, Adept or Mastered, clicking its tier box in the card viewer lets you pick one shape: that tier's list plus any shape from an earlier tier you passed over (Mastered: anything left). Shapes you don't pick stay on offer for later tiers and for other cards of the same tool; shapes already learned for the tool aren't offered.
- The Skills Record's background color is now a free color picker in the config screen, and the GUI's label color can be picked too.
- Redesigned the challenge edit screen: challenge name, number, tier and ID (click to copy), its target blocks, a value field with −/+ (Shift: 10), Reset/Half/Complete buttons, and a progress bar you can click or drag to set the value, previewing the change.
- The current tier's outline no longer takes the Skills Record's background color.
- Mine-Go Juice potions describe what they do: Ultimine with all shapes for their tool while the effect lasts.
- Shapes added by other mods (FTB Ultimine plugins) are handled: any that no certificate lists join a certificate tier (`progression.extra_shapes_certificate`, Adept by default, or none to keep them for the Miner Certificate and Mine-Go Juice). The server log lists where they went.
- Removed the Mining Skill Card custom renderer (the work-in-progress `mining_skill_card_renderer` client option) and the card's display item it drew.
- Removed the unfinished `one_tier_only` playstyle mode; `modern` and `legacy` remain (a config still set to it falls back to `modern`).
- Removed the Forge Config API Port dependency and the `card_trade_level`/`card_trade_price` options, which no longer did anything since villager trades became data-driven.

## v2101.1.2.4 Release - Jun 20, 2026
### 🐛 Bug Fixes & Improvements
- Updated compatibility with FTB Ultimine 2101.1.15.
- Resolved issues caused by the latest FTB Ultimine update to ensure stable functionality.

## v2101.1.2.3 Release - Feb 11, 2026
### 🐛 Bug Fixes & Improvements
- Updated compatibility with FTB Ultimine 2101.1.13.
- Resolved issues caused by the latest FTB Ultimine update to ensure stable functionality.

## v2101.1.2.2 Release - Nov 22, 2025
### ✨ New Features
- Added a new visual effect when a block is being consumed by a challenge.
- Added a consume-mode indicator inside the Challenges Panel for clearer visibility.

### 🐛 Bug Fixes & Improvements
- [UA-45] Fixed an issue where stripping wood blocks would cause a crash.

## v2101.1.2.1 Release - Nov 3, 2025
### 🐛 Bug Fixes & Improvements
- The "Consume Mode" button is now functioning correctly and is properly synchronized.
- The "Consume Mode" button will now be visually dimmed when it is disabled.
- Resolved an issue where the keybind for opening "Skills Record" was not functioning correctly.

## v2101.1.2.0 Release - Nov 2, 2025
### ⚙️ Refactoring
- Renamed and reorganized several classes.
- Updated version formatting from X.X.X to MCVR.X.X.X to clearly distinguish between Minecraft release versions.

### 🐛 Bug Fixes & Improvements
- [UA-44] Fixed a crash that occurred during the mod's initialization phase.

### ✨ New Feature
- Overhauled the Pin Challenge Panel for improved functionality and design.

<div style="display: flex; gap: 10px;">
<img src="https://i.imgur.com/kMiVBjX.png" width="300"  alt=""/>
</div>

## v1.1.6 Release - June 8, 2025
### 🐛 Bug Fixes & Improvements
- The mod is now compatible with the new update for **FTBUltimine** (`2101.1.3`); it will no longer crash.
- Fixed an issue that prevented players from retrieving the **Skills Record** and **Mining Skill Card** from **Refined Storage**.

## v1.1.5 Release - April 11, 2025
### ✨ New Feature
- Added a progression tooltip to the Miner Certificate in Legacy Mode.
- Introduced a new toggle button in the Shape Selector to filter the shape list between all shapes and non-blacklisted shapes.

## v1.1.4 Release - April 9, 2025
### 🐛 Bug Fixes & Improvements
- Change the JEI's supported version to a public version, not the latest one (version 19.21.0.247 or higher).

## v1.1.3 Release - April 9, 2025
### 🐛 Bug Fixes & Improvements
- Fixed an issue where the mining skill card's villager trade was not being registered correctly.
- The Miner Certificate in legacy mode now correctly counts points towards the ores challenge.

## v1.1.2 Release - March 28, 2025
### 🐛 Bug Fixes & Improvements
- The empty pen and ink chamber has been re-added to the Creative Tab. **( It was mistakenly removed; )**
- Fixed a crash issue that occurs when a player has an empty Mining Skill Card.
- The holding positions for both Skills Record and Shape Selector items have been adjusted.

## v1.1.1 Release - March 27, 2025
### 🐛 Bug Fixes & Improvements
- Fixed an issue where the active ultimine shape on an item wasn't visible in tooltips.

## v1.1.0 Release - March 27, 2025
### 🐛 Bug Fixes & Improvements
- Placing a large number of blocks at once (e.g., with Building Gadgets or Construction Wands) may cause significant lag or performance issues.

### ✨ New Feature
- A new item (**Shape Selector**) – Assign any Ultimine mining shape to a specific tool. Perfect for customizing your favorite pickaxe, shovel, or other mining tools!

<div style="display: flex; gap: 10px;">
<img src="https://i.imgur.com/n7942YT.png" width="300"  alt=""/>
</div>

## v1.0.3 Release - March 19, 2025
### ✨ New Feature
- Re-enable Curios compatibility for NeoForge
- Added a new Edit Challenge screen in the Skills Record section, accessible only to players with admin permissions.

<div style="display: flex; gap: 10px;">
<img src="https://i.imgur.com/t6eklpZ.png" width="300"  alt=""/>
<img src="https://i.imgur.com/uD5SA3d.png" width="300"  alt=""/>
</div>

### 🐛 Bug Fixes & Improvements
- Fix issue with Mine-GO Juice Brewing Recipe not functioning properly
- Overhauled the networking system for improved performance and maintainability.
- Centralized all common configurations on the server, except for the PlayStyle Mode configuration, which remains common-side.

## v1.0.2 Release - December 30, 2024
### ✨ New Feature
- A blacklist for preventing players from using shapes.
### 🐛 Bug Fixes & Improvements
- Breaking blocks using paxel doesn't count toward the challenges
- A game crash occurring when trying to craft a mining skill card

## v1.0.1 Release - September 30, 2024
### 🐛 Bug Fixes & Improvements
- Right click with tools causes a game crash!

## v1.0.0 Release - September 21, 2024
- Port to 1.21