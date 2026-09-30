# Changelog
This file is for listing all the changes to this project
<hr>

## v2001.2.0.0 Release - Unreleased (Minecraft 1.20.1 backport)
Backport of the 26.1.2 release: the same features on Minecraft 1.20.1, for Forge 47 and Fabric (FTB Ultimine 2001.1.8, FTB Library 2001.2.13). Replaces the 2001.1.5.x line; worlds, items and configs from it carry over (see "Updating from 2001.1.5.x").
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
- The configs now use CoolCatLib's config system, as TOML files: `ultimine_addition-client.toml` and `ultimine_addition-server.toml`. Edit them in-game with `/ultimine_addition config` (or from the Forge mod list / Mod Menu): searchable screens with sliders, a color picker, undo/redo, reset buttons and settings that grey out when the setting they depend on is off. Operators can change server settings live, and every player gets the new values right away; edits to the files are picked up without a restart. Settings from the old `.toml` configs are carried over once (see "Updating from 2001.1.5.x" below).
- The Skills Record's configuration button now opens a small settings window over the book (animations, text shadow, challenges panel alignment) instead of the side panel. It's drawn like the book: its frame, a striped title bar like the notice's, a dark inner screen, a matching striped footer for the buttons, and grey controls.
- Empty Mining Skill Cards are crafted now, early-game friendly: five papers around a token of each tool's work (copper, iron or coal for the pickaxe, dirt for the shovel, a log for the axe and seeds for the hoe) make one card. The Card Blueprint (and its advancements) and the Toolsmith's card trade are gone; the Shape Selector takes a compass where it took a Card Blueprint. Card Blueprints already in a world disappear.
- Dyed Skills Records: the Skills Record is now crafted as a clipboard (an iron clip, paper around an empty Mining Skill Card, and a board of planks); plain planks make the plain, white, record, and a dye in the middle of the board makes that color's record, and any Skills Record can be recolored with a dye as often as you like, keeping everything inside (JEI shows the recoloring for each dye). Each color is its own edition with a recolored item and a Minecraft-inspired motif (Snowfall, Harvest, Allium, Skyward, Beehive, Slime, Cherry Grove, Stonecutter, Rainfall, Warm Ocean, Amethyst, Deep Lapis, Mushroom Fields, Firefly Forest, Nether Ember, Starry Night): a tinted book, labels colored to stay readable on it, its motif moving in the card viewer's background in its own way (a spinning snowflake, a glowing pumpkin, a swaying allium, a drifting cloud, a bee flying figure eights, a bouncing slime, a falling blossom, a mining pickaxe, a splashing raindrop, a swimming fish, a pulsing crystal, a spinning gem, a breathing mushroom, a swaying tree, a flickering flame, a twinkling star), and its own living background that pans with the map (snowfall, falling leaves, allium petals, drifting clouds, pollen and a bee, bouncing slimes, cherry petals, sifting gravel, rain, bubbles, amethyst glints, rippling light, spores, fireflies, rising embers, a starry sky with shooting stars). The HUD panels follow the record you carry. A plain Skills Record is the white one (Snowfall), with a new silver board texture. All 16 are in the creative tab.
- The playstyle mode is now a server setting (`general.playstyle_mode`) and applies at once: the server reloads its recipes, items switch behaviour right away and clients rebuild the creative tab. Players on a server play by the server's mode and get their own back when they leave.

### 📦 Updating from 2001.1.5.x
Everything from the earlier 1.20.1 releases carries over on its own:
- Mining Skill Cards keep their data under the same item NBT key (`ultimine_addition:mining_skill_card_data`: `UUID`, `Tier`, `PotionPoints`, `Challenges`): the tier and UUID stay on the item, and the challenges and potion points move into the world save the first time a player carries the card (inventory, a Curios/Trinkets slot) or it is in one of their Skills Records. The old `DisplayItem` (from the removed card renderer) is ignored. Cards that shared a UUID (copies of one card) end up sharing their progress.
- Skills Records keep the same NBT key (`ultimine_addition:skills_record_data`): contents, selected card and consume mode move into the world save (`data/ultimine_addition.skills_records.dat` of the overworld) the first time the record is loaded, and the item keeps only its UUID; a record whose UUID is already taken gets a new one. Cards inside a record are migrated like carried ones.
- A player's Miner Certificate unlock (Forge: the `ultimine_addition:properties` capability, saved as `ForgeCaps."ultimine_addition:properties"."ultimine_addition:ftb_ultimine_ability"`; Fabric: `ultimine_addition:ftb_ultimine_ability` in the player's data) moves to the new ability data (a CoolCatLib attachment) when the player is loaded.
- A tool's Shape Selector shape (`ultimine_addition:selected_shape_data`) is still read: the old FTB Ultimine shape names (`small_tunnel`) are taken along with the new ids (`ftbultimine:small_tunnel`).
- Player-placed block tracking (`ultimine_addition.ineligible_blocks`, per dimension), Miner Certificates (`ultimine_addition:miner_certificate_data`) and ink storage (`ultimine_addition:item_storage_data`) keep their format.
- Card Blueprints (removed) disappear from worlds; on Forge they're dropped without the missing registry entries warning.
- Configs: on the first start, `config/ultimine_addition/client-config.toml` and `common-config.toml` are imported into `ultimine_addition-client.toml` / `ultimine_addition-server.toml` (only when those don't exist yet), and the old server config into `ultimine_addition-server.toml`: on Fabric (Forge Config API Port) the global `config/ultimine_addition/server-config.toml`, imported on the first start (without one, a modpack's `defaultconfigs/ultimine_addition/server-config.toml` is imported and left in place); on Forge each world's `serverconfig/ultimine_addition/server-config.toml`, imported when that world starts, unless the global one was imported (it wins, and the world's copy is only renamed to `.bak`). The server config is shared by all worlds now. Only settings changed from their old defaults are taken; the old files are kept as `.bak`. Renamed settings: `sr_edit_mode` → `debug.skills_record_edit_mode`; `text_screen_shadow` → `skills_record.text_shadow`; `animations_mode` → `skills_record.animations`; `challenges_panel_alignment` → `skills_record.challenges_panel_alignment`; `shape_selector_filter` → `visuals.shape_selector_filter`; `playstyle_mode` → `general.playstyle_mode` (`one_tier_only` becomes `modern`); `blacklisted_shapes` → `general.blacklisted_shapes` (old shape names get FTB Ultimine's namespace: `small_tunnel` → `ftbultimine:small_tunnel`); `paper_consummation_rate` → `skills_record.paper_consumption_rate`; `legacy_required_amount` ("min, max") → `legacy.required_amount_min` / `legacy.required_amount_max`; `card_challenges_amount`, `card_potion_points`, `card_potion_durations` and `card_max_blocks` ("Tier=n, ...") → one value per tier under `mining_skill_cards.challenges_amount` / `potion_points` / `potion_durations` / `max_blocks`; `card_mastered_effect` and `tier_based_max_blocks` → `mining_skill_cards.*`; the other `General` and `Debugging` settings keep their names under `general` / `debugging`. `progress_bar_mode`, `msc_renderer`, `background_color`, `card_trade_level` and `card_trade_price` no longer exist and are dropped.

### ⚙️ Refactoring
- Architectury API is no longer required by this mod (FTB Ultimine 2001 still brings it): events, registration, networking, menus and client registries now go through CoolCatLib's cross-loader layer. Forge Config API Port is no longer needed on Fabric.
- Now requires both **CoolCatLib: Core** and **CoolCatLib: Canvas** 2001.2.0.0 or newer (CoolCatLib was split in two; the old `coolcatlib` mod is no longer used), and Forge 47.4.0 or newer.
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
- The card viewer's background is now a soft, slowly drifting gradient in the Skills Record's background color, with faint Mining Skill Cards of different sizes scattered across it in an uneven grid, slowly scrolling to the right (their frame, written lines, picture box and badge).
- The server config's debugging loggers (`debugging.*`) are now off by default.
- The placeholder items in the Skills Record's empty card, pen and paper slots are now greyed and see-through.
- "No card inserted" and "select a card" are now guide panels in the card viewer's style, each with a small looping animation of what to do: a card dropping into a card slot, or a cursor right-clicking a card in its slot, which gets selected (with the tutorial toast's mouse showing the right click).
- Opening a challenge's details slides the tree up as it fades, then the details rise into place as they fade in; closing them slides the details down and away the same way, and the tree drops back into place.
- The edge fades of the card viewer shrink into their side as you reach the end of the tree on that side.
- The challenge details' footer buttons use smaller labels and are centered in the footer, and button labels too long for their button (e.g. in other languages) shrink to fit. The guide panels' text shrinks the same way to stay inside its plate.
- The selected card slot and its marker get an animated gradient outline, one shape running up both sides of the marker and around the slot, in brighter shades of the Skills Record's background color (or darker ones when the color is already bright).
- The challenge details and shape choice panels (scrollbar and progress bar track too), and the card viewer's tiers, badges, challenges and slots, take on the Skills Record's background color. A challenge's box turns gold while in progress (a gold frame, with a gold gradient cycling around it when animated) and green once done; its count is colored by its state, and a done challenge gets a check mark (one needing Consume Mode a red "!").
- The challenge details show the challenge's target block and name with its tier small beside it, and its footer buttons have icons (the reroll one a pixel glyph from the mod's icon font).
- The card viewer's background effect uses fewer particles when zoomed out, so zooming out stays smooth.
- The card viewer's Fit and Expand/Collapse buttons fade in and out instead of popping, and are drawn like the Skills Record's configuration button, beside it on the book's title bar (on the expanded window's title bar while expanded).
- The challenge boxes' block slot is a bit bigger, with clean edges at any zoom, and turns gold with the box while the challenge is in progress.
- The edition's background effect and its emblems (now scattered across the background in different sizes, each moving on its own) pan and zoom with the card viewer's map.
- The Edit Challenge dialog is laid out again: everything is centred, its value field shows its number, the quick buttons fit their labels, and the change is shown centred above Done and Cancel. The challenge ID line is gone; hover the challenge's name to see its ID and click to copy it.
- The open Skills Record follows its settings as they change (in its settings window or the config file) instead of only when reopened.
- A missing pen or paper, or a pen without enough ink, is marked with a red overlay and an animated red outline on its slot, and the card viewer is dimmed behind the banner that explains it. The banner slides up into the viewer when it appears and back down when it goes.
- Fixed a Mining Skill Card's tier change not reaching the player on a server: the card kept showing its old tier until it was moved.
- `/ultimine_addition skills_record inspect [targets]` and `/ultimine_addition_client skills_record inspect` (operators) print the carried Skills Records and cards as the server and the client see them, to check they're in sync.
- Ultimine undo puts glass panes, iron bars, fences and walls back connected as they were (they came back with gaps where neighbours broken earlier in the same Ultimine had been), and restores doors, beds and tall plants whole: the other half, removed together with the half that was mined, now comes back too (and shows in the preview and growth). Torches, lanterns, rails, buttons and anything else that popped off the mined blocks come back as well, paid for with the items they dropped, so nothing gets duplicated.
- Cleaner undo panel: its body is now one soft outline with faint diagonal lines, without the corner marks.
- Less grind: challenges per tier are now 1/2/2/3 (was 1/2/3/4), required amounts are roughly halved, and rare targets are much lower (e.g. diamond ores 8-16, emerald ores 4-8, ancient debris 4-8).
- The Shape Selector marks shapes that haven't been learned from a Shape Certificate yet.
- Without the Miner Certificate or a Mine-Go Juice, the Ultimine shape list only offers the shapes learned for the tool in hand; with none it shows "No Shape Learned" instead of a shape the player can't use.
- Fixed `/ultimine_addition ultimine_shape blacklist add|remove` failing with an error.
- The Ultimine notice panel fits its text (long lines wrap), no longer has a line under the title, and its item icon now fades out with the panel. Its body now hangs from the title bar's bottom edge instead of sticking out past the bar's pointed ends.
- The creative tab only lists the plain Mining Skill Cards (no pre-tiered copies).
- New consume effect: fragments of the consumed block and glowing motes float up out of the spot, then get vacuumed into the player completing the challenge, with new sounds (a soft whoosh, and a chime when they reach the player).
- Shape Certificates show their tool's icon, like the Mining Skill Cards, with an outlined slot.
- Every Shape Certificate now teaches one shape. When a card reaches Novice, Apprentice, Adept or Mastered, clicking its tier box in the card viewer lets you pick one shape: that tier's list plus any shape from an earlier tier you passed over (Mastered: anything left). Shapes you don't pick stay on offer for later tiers and for other cards of the same tool; shapes already learned for the tool aren't offered.
- The Skills Record's background color is now a free color picker in the config screen, and the GUI's label color can be picked too.
- Redesigned the challenge edit screen: challenge name, number, tier and ID (click to copy), its target blocks, a value field with −/+ (Shift: 10), Reset/Half/Complete buttons, and a progress bar you can click or drag to set the value, previewing the change.
- The current tier's outline no longer takes the Skills Record's background color.
- Mine-Go Juice potions describe what they do: Ultimine with all shapes for their tool while the effect lasts.
- Shapes added by other mods (FTB Ultimine plugins) are handled: any that no certificate lists join a certificate tier (`progression.extra_shapes_certificate`, Adept by default, or none to keep them for the Miner Certificate and Mine-Go Juice). The server log lists where they went.
- Removed the Mining Skill Card custom renderer (the work-in-progress `mining_skill_card_renderer` client option) and the card's display item it drew.
- Removed the unfinished `one_tier_only` playstyle mode; `modern` and `legacy` remain (a config still set to it falls back to `modern`).
- Removed the Forge Config API Port dependency and the `card_trade_level`/`card_trade_price` options.
- Version numbers now follow the Minecraft version: `1.20.1-<build>` (was `2001.x.x.x`).

### 🔧 Differences from the 26.1.2 release
- Built for Forge 47 (1.20.1) instead of NeoForge, on Java 17, with FTB Ultimine 2001.1.8 and FTB Library 2001.2.13.
- 1.20.1 has no data components: the items' data is kept in their NBT, under the same keys the earlier 1.20.1 releases used (`ultimine_addition:mining_skill_card_data`, `ultimine_addition:skills_record_data`, ...), so their items are read as they are. A Mining Skill Card that isn't stored in the world save yet keeps its temporary challenges for a few minutes by UUID (a component value kept them on 26.1.2).
- Item models pick their model through item properties and model overrides instead of item model definitions: `ultimine_addition:tier` on Mining Skill Cards (the tier's value / 4), `ultimine_addition:certificate_opened` on the Miner Certificate and `ultimine_addition:tool` on Shape Certificates (pickaxe 0.25, axe 0.5, shovel 0.75, hoe 1) and `ultimine_addition:dye` on the Skills Record (the dye's id / 16). Custom card types from `config/ultimine_addition/custom_cards` need a `models/item/<id>.json` with `tier` overrides in a resource pack.
- A Skills Record's dye is a `Color` tag (the dye's name) in its NBT instead of the `base_color` component, and the dyed clipboard recipes are `ultimine_addition:dyed_skills_record` recipes (a shaped recipe with a `color`), as 1.20.1's crafting results can't carry NBT on Fabric. The dye tags are Forge's `forge:dyes/<color>` and Fabric API's `c:<color>_dyes`; the seeds in the empty card's recipe are `forge:seeds` on Forge and a `c:seeds` tag the mod adds on Fabric.
- FTB Ultimine 2001: its one (static) shape registry serves the client and the integrated server, so the shape list the local player sees is picked by thread. Its shapes are named with plain strings (`small_tunnel`), which the mod turns into the same ids as on 26.1.2 (`ftbultimine:small_tunnel`) for its configs, certificates and commands; shape names are translated as `ftbultimine.shape.<name>`. It has no restriction handlers, so the "is Ultimine allowed" check is an FTB Ultimine plugin instead, and the undo records the blocks broken through the player's `destroyBlock` (FTB Ultimine 2001 breaks them itself).
- The undo key is Ctrl + Z through Forge's key modifiers; Fabric has none, so there the key mapping is plain Z and Ctrl is checked separately (the controls screen shows Z).
- GUI: 1.20.1 has no GUI sprite atlas, so the mod's sprites are textures under `textures/gui/sprites/`, scaled (nine-slice, tile, stretch) as their `.mcmeta` says, drawn by CoolCatLib's sprite loader; vanilla's beacon confirm/cancel icons and the slot come from their 1.20.1 textures, and the Shape Selector's scrollbar is drawn like 1.20.1's own. Layering, tooltips and fading follow 1.20.1's rendering: later layers clear the depth buffer so they cover items drawn before, the mod's tooltips are drawn once its screens are done, and GUI items fade through the shader colour (items with cutout models can't fade). The Skills Record's tooltip image is also placed at its line in Forge's container tooltips.
- The undo ghosts and growing blocks are drawn through each loader's level render events, and their block entities with the level's other block entities.
- The consume effect uses 1.20.1 sounds (Eye of Ender launch, amethyst resonance) in place of the breeze inhale and trial spawner sounds, which 1.20.1 doesn't have.
- Brewing recipes are built once per game on 1.20.1 (Mine-Go Juice brewing is still always registered and follows the playstyle mode as on 26.1.2); a Mine-Go Juice's duration follows the config when its effect is applied, as in the earlier 1.20.1 releases.
- "Not a tool" (for the locked-Ultimine notice) means an item that isn't a digging tool, sword or shears (1.20.1 has no tool component). The targeted-block reach is Forge's reach attribute, or vanilla's reach on Fabric.
- Recipe conditions use Forge's `conditions` array and Fabric's load conditions; tags use Forge's `forge:` tags and Fabric API's 1.20.1 conventional tags (the mod adds `c:stones` and `c:cobblestones`, which those don't have yet).
- The JEI refill category keeps its 3D crafting table.
- The playstyle mode's config tooltip describes both modes (the 1.20.1 CoolCatLib config screens have no per-option descriptions).

## v2001.1.5.3 Release - Jun 20, 2026

### 🐛 Bug Fixes & Improvements

- Updated compatibility with FTB Ultimine 2001.1.8.
- Resolved issues caused by the latest FTB Ultimine update to ensure stable functionality.

## v2001.1.5.2 Release - Nov 23, 2025

### ✨ New Features

- Added a new visual effect when a block is being consumed by a challenge.
- Added a consume-mode indicator inside the Challenges Panel for clearer visibility.

## v2001.1.5.1 Release - Nov 3, 2025

### 🐛 Bug Fixes & Improvements

- The "Consume Mode" button is now functioning correctly and is properly synchronized.
- The "Consume Mode" button will now be visually dimmed when it is disabled.

## v2001.1.5.0 Release - Nov 2, 2025

### ⚙️ Refactoring

- Renamed and reorganized several classes.
- Updated version formatting from X.X.X to MCVR.X.X.X to clearly distinguish between Minecraft release versions.

### 🐛 Bug Fixes & Improvements

- [UA-43] Fixed the compatibility with the latest version of FTBUltimine.

### ✨ New Feature

- A new item (**Shape Selector**) – Backported from the latest version 1.21.1.
- Overhauled the Pin Challenge Panel for improved functionality and design.

<div style="display: flex; gap: 10px;">
<img src="https://i.imgur.com/n7942YT.png" width="300"  alt=""/>
<img src="https://i.imgur.com/kMiVBjX.png" width="300"  alt=""/>
</div>

## v1.4.0 Release - Jun 22, 2024

### Added

- Two New Buttons in Skills Record Configuration.
  <br></br>

<hr>

## v1.3.2 Release - Mar 5, 2024
### Fixed
- [UA-22] The tool-checking method of Challenges is not functioning correctly.
  <br></br>
<hr>

## v1.3.1 Release - Mar 4, 2024
### Changed
- Now, the Mine-GO Juice effect has a strict behavior! `Previously, if you had at least one of the Mine-GO Juice effects, you could use any tool with ultimine ability regardless of whether it was the perfect tool for the block.`
### Fixed
- [UA-21] Miner Certificate and Mining Skill Card recipes don't function properly.
  <br></br>
<hr>

## v1.3.0 Release - Feb 16, 2024
### Adding
- Mining Skill Card Now can activate the ultimine ability upon leveling to mastered. "With the option to turn it off"
- Now, the challenges panel has its color synced to the Skills Record screen.
### Changed
- The Skills Record info is clarified when the player tries to register points toward the challenges through non-eligible blocks.
- Changing the Skills Record Info translation from "Invalid Block" to "Ineligible Block"
- Changing from storing the Ineligible Blocks in Chunk data to World Saved Data
### Fixed
- [UA-19] Challenges doesn't get cleared after reaching the mastered tier in the Mining Skill Card.
  <br></br>
<hr>

## v1.2.4 Release - Feb 6, 2024
### Fixed
- [UA-17] The Skills Record doesn't register any point toward mined blocks if it's in th Curios slot.
  <br></br>
<hr>

## v1.2.3 Release - Feb 5, 2024
### Fixed
- [UA-16] Chunks not loading properly.
  <br></br>
<hr>

## v1.2.2 Release - Jan 21, 2024
### Fixed
- [UA-13] Skills Record inventory randomly getting cleared.
- [UA-14] Skills Record data gets deleted at Mainhand.
- [UA-15] Keybindings localization isn't working, Skills Record Inventory display is misplaced.
  <br></br>
<hr>

## v1.2.1 Release - Jan 18, 2024
### Fixed
- [UA-12] Incompatibility with DefaultSettings.
  <br></br>
<hr>

## v1.2.0b Release - Jan 7, 2024
### Fixed
- [UA-11] Compatibility with the new version (v2001.1.4) of the FTB Ultimine.
  <br></br>
<hr>

## v1.2.0a Release - Dec 28, 2023
### Fixed
- Crash issue related to mining skill card occurred when playstyle switched into legacy.
- Not copying player ultimine ability after exiting The End.
  <br></br>
<hr>

## v1.2.0 Release - Dec 27, 2023
### Adding
- Adding Playstyle Mode option `If you prefer the old style of the mod, Now you can change it back as before.`
- Adding Villager Trade and Price option for the Mining Skill Card `There will be an option for which villager level should the card appear and change the price.`
  <br></br>
<hr>

## v1.1.2 Release - Dec 21, 2023
### Added
- New Custom Mining Skill Card API `You can create your Custom Mining Skill Card through configs.` Check out the Wiki!
  <br></br>
<hr>

## v1.1.1 Release - Dec 2, 2023
### Added
- Curios and Trinkets Integration `Now... you can insert the Skills Record in the curios/trinkets menu.`
### Fixed
- The ChunkManager from caching unloaded chunks.
  <br></br>
<hr>

## v1.1.0-Hotfix Release - Nov 12, 2023
### Fixed
- [UA9] Fixed the Tools Validator methods to recognize tools that doesn't have the tag.
  <br></br>
<hr>

## v1.1.0 Release - Nov 11, 2023
### Added
- Adding Arabic Translation
- Progression Display `You can now pin any challenges you want on the screen.`
- Is Placed By Entity Condition Option `You can Enable/Disable the Is Placed By Entity Condition.`
- Adjustable Mining Skill Card's Potion Points `You can change the potion points values in each tier.`
- Adjustable Number of Challenges for each Tier Config `You can change the values on how many challenges should be given in each tier.`
  *But remember that you must have the exact number of challenges in the Datapack. Otherwise, it will make the game crash!*
### Fixed
- [Forge] Saving player ability after respawn
  <br></br>
<hr>

## v0.1.1a Beta - Oct 13, 2023
### Fixed
- [Forge] issue within OnDatapackSyncEvent method
  <br></br>
<hr>

## v0.1.1 Beta - Oct 11, 2023
- Fixing issue related to packet
  <br></br>
<hr>

## v0.1.0 Beta - Oct 9, 2023
### It's an early release
`Don't hesitate to submit a report if you have faced an issue.`
- Port to 1.20.1