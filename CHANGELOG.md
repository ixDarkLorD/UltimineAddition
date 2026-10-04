# Changelog
This file is for listing all the changes to this project
<hr>

## v2101.2.0.4 Unreleased
### ✨ New Features
- **Mining Skill Cards from data packs**: `data/<namespace>/mining_skill_cards/<name>.json` adds a card type for other tools, with a `name`, its `tools` (item ids or `#tags`), an `icon` item for the card, and its Mine-Go Juice's `juice_color` and `juice_name`. Challenges name the type in `for_card_type` (`mypack:hammer`). The types reload with `/reload` and are sent to every player.
  - `name` and `juice_name` can be translation keys, for a resource pack's language files (plain text shows as written); without a `name` the key is `ultimine_addition.card_type.<namespace>.<name>`.
  - A type with no challenges for one of its tiers is skipped with a message in the log, and a tier with fewer challenges than a card rolls gives the card all of them (it used to crash the world).
  - Their cards are one generic item (`mining_skill_card_generic`, the type on the stack), crafted from an empty card and one of the type's tools. Their Mine-Go Juice is one generic drink (`mine_go_juice_generic`) and one effect, in the type's color, brewed from the card like the others; a player has one data pack juice at a time. Neither is in the creative tab or JEI.
- **Ultimine shapes from data packs**: `data/<namespace>/ultimine_shapes/<name>.json` adds a shape to FTB Ultimine's list. Draw it as a `pattern` of rows facing the block (`o` the block you break, `#` a block), list `blocks` as `[right, up, depth]` offsets, or both; `"repeat": "depth"` turns it into a tunnel, and `name` is what players see. The shapes reload with `/reload`, are sent to every player, can be taught by Shape Certificates through the server config's lists, and get a diagram in the Skills Record like any other.

### 🐛 Bug Fixes & Improvements
- A Mining Skill Card shows its tool as an item on its plate (the netherite tool for the built-in cards), drawn by its item decorator, instead of a picture baked into the texture: the card textures leave the plate empty.
- Mine-Go Juice now sets the Ultimine block limit of its tier again (the check for an active juice never matched).
- A challenge of a custom card type only counts with one of that type's own tools (any custom type's tool used to do).
- Redesigned the Skills Record's shape choice (picking a Shape Certificate's shape): each shape is a tile with a small diagram of the shape, its name and a stripe in the color of the tier it comes from, under a header in the tier's color. The hovered tile lifts and turns gold. The diagrams aren't drawn by hand: the server asks FTB Ultimine for each shape's blocks and sends their outline, so shapes from other mods and plugins get one too.
- The shape choice shows three shapes at a time, with their names at full size on up to two lines; the mouse wheel and the arrows beside Back scroll through the rest.
- On Fabric, Glazed Menu's update check reads the mod's own Fabric update file.

### ⚙️ Refactoring
- Removed the custom card files in `config/ultimine_addition/custom_cards` and the `CustomMSCApi` / `IUAPlugin` API: card types come from data packs now. Cards and juices of the old custom types (their own items) are gone with them.

## v2101.2.0.3 Release - Oct 4, 2026
### ✨ New Features
- The undo preview shows the blocks an undo with missing items can't put back as red ghosts, inside a red outline of their own; the ones that come back keep the usual look.
- A Mining Skill Card's potion points show as pips along the bottom of the card (one per point its tier holds, filled while it's left) instead of a durability bar. Requires CoolCatLib: Core 2100.2.0.1 (its new item decorators draw the pips).

### 🐛 Bug Fixes & Improvements
- Redrawn item textures, all sized like the Skills Record:
  - Mining Skill Cards: a portrait card with the tier as stars at the top (red, green, teal, and gold once mastered), the tool's plaque, and an empty strip for the potion points' pips.
  - Shape Certificates: a framed certificate page in the tier's color, the tool inked on it, the tier's shape mark and a gold rosette on a ribbon.
  - Miner Certificate: before it's earned it's a sealed envelope with a wax seal, now named **Completion Envelope**; once earned, a gold-framed Miner Certificate with a picture of a miner in a lush cave.
  - Pen and Ink Chamber: redrawn with rounded shading (the Ink Chamber as a glass refill of blue ink).

## v2101.2.0.2 Release - Oct 3, 2026
### ✨ New Features
- Undo works with items missing: it puts back only the blocks whose drops you still have (on the ground or in your inventory), takes the experience for those, and leaves the rest out. Before such an undo a popup shows what's missing and how many blocks will come back; its "Don't ask again" checkbox turns it off (`undo.confirm_missing_items` in the client config brings it back).

### 🐛 Bug Fixes & Improvements
- The Mine-Go Juice taste on a Mining Skill Card tier-up is now off by default: turn it on with the new `progression.tier_up_taste` server setting (`tier_up_taste_duration` still sets how long it lasts).
- The Skills Record's settings button now opens the mod's own settings popup instead of CoolCatLib: Core's, so it keeps working once Core's config screens move out. Its option picker also opens a dropdown of every option.

## v2101.2.0.1 Release - Sep 30, 2026
### 🐛 Bug Fixes & Improvements
- The undo preview's ghosts now form one solid, seamless shape: they fill their blocks exactly (they were shrunk, which opened gaps that showed missing faces inside the group), are easier to see, and skip faces hidden against real blocks.

## v2101.2.0.0 Release - Sep 30, 2026 (Minecraft 1.21.1 backport)
Backport of the 26.1.2 release: the same features on Minecraft 1.21.1, for Fabric and NeoForge (FTB Ultimine 2101.1.15).
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
- The configs now use CoolCatLib's config system, as TOML files: `ultimine_addition-client.toml` and `ultimine_addition-server.toml`. Edit them in-game with `/ultimine_addition config` (or from the NeoForge mod list / Mod Menu): searchable screens with sliders, a color picker, undo/redo, reset buttons and settings that grey out when the setting they depend on is off. Operators can change server settings live, and every player gets the new values right away; edits to the files are picked up without a restart. Settings from the old `.toml` configs are carried over once (see "Updating from 2101.1.2.x" below).
- The Skills Record's configuration button now opens a small settings window over the book (animations, text shadow, challenges panel alignment) instead of the side panel. It's drawn like the book: its frame, a striped title bar like the notice's, a dark inner screen, a matching striped footer for the buttons, and grey controls.
- Empty Mining Skill Cards are crafted now, early-game friendly: five papers around a token of each tool's work (copper, iron or coal for the pickaxe, dirt for the shovel, a log for the axe and seeds for the hoe) make one card. The Card Blueprint (and its advancements) and the Toolsmith's card trade are gone; the Shape Selector takes a compass where it took a Card Blueprint. Card Blueprints already in a world disappear.
- Dyed Skills Records: the Skills Record is now crafted as a clipboard (an iron clip, paper around an empty Mining Skill Card, and a board of planks); plain planks make the plain, white, record, and a dye in the middle of the board makes that color's record, and any Skills Record can be recolored with a dye as often as you like, keeping everything inside (JEI shows the recoloring for each dye). Each color is its own edition with a recolored item and a Minecraft-inspired motif (Snowfall, Harvest, Allium, Skyward, Beehive, Slime, Cherry Grove, Stonecutter, Rainfall, Warm Ocean, Amethyst, Deep Lapis, Mushroom Fields, Firefly Forest, Nether Ember, Starry Night): a tinted book, labels colored to stay readable on it, its motif moving in the card viewer's background in its own way (a spinning snowflake, a glowing pumpkin, a swaying allium, a drifting cloud, a bee flying figure eights, a bouncing slime, a falling blossom, a mining pickaxe, a splashing raindrop, a swimming fish, a pulsing crystal, a spinning gem, a breathing mushroom, a swaying tree, a flickering flame, a twinkling star), and its own living background that pans with the map (snowfall, falling leaves, allium petals, drifting clouds, pollen and a bee, bouncing slimes, cherry petals, sifting gravel, rain, bubbles, amethyst glints, rippling light, spores, fireflies, rising embers, a starry sky with shooting stars). The HUD panels follow the record you use: pinned challenges take the color of the record they come from, and the Ultimine notices and undo panels the record in hand, else the equipped one, else the first one carried, so records of different colors never mix (an open Skills Record doesn't change them). A plain Skills Record is the white one (Snowfall), with a new silver board texture. All 16 are in the creative tab.
- The playstyle mode is now a server setting (`general.playstyle_mode`) and applies at once: the server reloads its recipes, items switch behaviour right away and clients rebuild the creative tab. Players on a server play by the server's mode and get their own back when they leave.

### 📦 Updating from 2101.1.2.x
Everything from the earlier 1.21.1 releases carries over on its own:
- Mining Skill Cards (same `mining_skill_card_data` component): tier, challenges and potion points move into the world save the first time a player carries the card or it is in one of their Skills Records. Cards that shared a UUID (copies of one card) end up sharing their progress.
- Skills Records (same `skills_record_data` component): contents, selected card and consume mode move into the world save the first time the record is loaded; a record whose UUID is missing or already taken gets a new one.
- A player's Miner Certificate unlock (Fabric: the `ultimine_addition:ultimine_ability` player data; NeoForge: the `ultimine_addition:player_ability` attachment) moves to the new ability data when they join.
- Player-placed block tracking (`ultimine_addition.ineligible_blocks`), Miner Certificates, ink storage and the Shape Selector's shape keep their format.
- Configs: on the first start, `config/ultimine_addition/client-config.toml` is imported into `ultimine_addition-client.toml`, and `common-config.toml` and `server-config.toml` into `ultimine_addition-server.toml` (only when the new files don't exist yet). Without an old `server-config.toml`, a modpack's `defaultconfigs/ultimine_addition/server-config.toml` is imported instead (and left in place). A world carrying its own `serverconfig/ultimine_addition/server-config.toml` is imported when it starts, unless the global one was imported (the global one wins). Only settings changed from their old defaults are taken; the old files are kept as `.bak`. Renamed settings: `sr_edit_mode` → `debug.skills_record_edit_mode`; `text_screen_shadow` → `skills_record.text_shadow`; `animations_mode` → `skills_record.animations`; `challenges_panel_alignment` → `skills_record.challenges_panel_alignment`; `shape_selector_filter` → `visuals.shape_selector_filter`; `playstyle_mode` → `general.playstyle_mode` (`one_tier_only` becomes `modern`); `paper_consummation_rate` → `skills_record.paper_consumption_rate`; `legacy_required_amount` ("min, max") → `legacy.required_amount_min` / `legacy.required_amount_max`; `card_challenges_amount`, `card_potion_points`, `card_potion_durations` and `card_max_blocks` ("Tier=n, ...") → one value per tier under `mining_skill_cards.challenges_amount` / `potion_points` / `potion_durations` / `max_blocks`; `card_mastered_effect` and `tier_based_max_blocks` → `mining_skill_cards.*`; the `General` and `Debugging` settings keep their names under `general` / `debugging`. `progress_bar_mode`, `msc_renderer`, `card_trade_level`, `card_trade_price` and `background_color` (a record's dye sets its look now) no longer exist and are dropped.

### ⚙️ Refactoring
- Architectury API is no longer required by this mod (FTB Ultimine 2101 still brings it): events, registration, networking, menus and client registries now go through CoolCatLib's cross-loader layer.
- Now requires both **CoolCatLib: Core** and **CoolCatLib: Canvas** (CoolCatLib was split in two), and NeoForge 21.1.200 or newer.
- A player's Ultimine ability data (Miner Certificate, learned shapes) is now a CoolCatLib attachment, the same on both loaders, saved with the player, kept through death and synced to them whenever it changes. Existing saves are moved over when the player joins.

### 🐛 Bug Fixes & Improvements
- Skills Record contents are now stored in the world save and linked to the item by UUID; existing Skills Records are migrated automatically the first time they are loaded.
- Mining Skill Card challenges and potion points are now stored in the world save with the card's history; the item keeps its UUID and tier. Existing cards are migrated automatically.
- Cards without a saved UUID no longer share one.
- The server now checks permissions before applying challenge edits from the Skills Record.
- The Skills Record window is larger, giving the card viewer more room.
- The challenge details show a progress bar and the target blocks as items.
- Removed the "Progression Bar" option and its "Show Progression Bar" keybind; progress is shown in the card viewer instead.
- Removed the Skills Record scroller; the card viewer now uses its space.
- Redesigned the card viewer's look: shaped tier and challenge nodes, animated connections and background, hover and reveal animations (follows the Animations option). The expanded viewer uses the Skills Record's frame.
- Challenges of locked tiers are hidden until the tier is unlocked.
- Fixed the mouse wheel not scrolling the Shape Selector's shape list.
- The card viewer's background is now a soft, slowly drifting gradient in the Skills Record's background color, with faint Mining Skill Cards of different sizes scattered across it in an uneven grid, slowly scrolling to the right (their frame, written lines, picture box and badge).
- The server config's debugging loggers (`debugging.*`) are now off by default.
- Removed the Skills Record's Background Color and Label Color settings; the record's dye sets its look instead.
- The Skills Record's "Inventory" label sits in a box framed on its left, top and right in the gray of the inventory's border, with a backing that always contrasts with the label.
- The placeholder items in the Skills Record's empty card, pen and paper slots are now greyed and see-through.
- "No card inserted" and "select a card" are now guide panels in the card viewer's style, each with a small looping animation of what to do: a card dropping into a card slot, or a cursor right-clicking a card in its slot, which gets selected (with the tutorial toast's mouse showing the right click).
- Opening a challenge's details slides the tree up as it fades, then the details rise into place as they fade in; closing them slides the details down and away the same way, and the tree drops back into place.
- The edge fades of the card viewer shrink into their side as you reach the end of the tree on that side.
- The challenge details' footer buttons use smaller labels and are centered in the footer, and button labels too long for their button (e.g. in other languages) shrink to fit. The guide panels' text shrinks the same way to stay inside its plate.
- The selected card slot and its marker get an animated gradient outline, one shape running up both sides of the marker and around the slot, in brighter shades of the Skills Record's background color (or darker ones when the color is already bright).
- The challenge details and shape choice panels (scrollbar and progress bar track too), and the card viewer's tiers, badges, challenges and slots, take on the Skills Record's background color. A challenge's box turns gold while in progress (a gold frame, with a gold gradient cycling around it when animated) and green once done; its count is colored by its state, and a done challenge gets a check mark (one needing Consume Mode a red "!").
- The challenge details show the challenge's name, after the same 📝 mark as its tooltip, with its tier small beside it, and its footer buttons have icons (the reroll one a pixel glyph from the mod's icon font).
- The card viewer's background effect uses fewer particles when zoomed out, so zooming out stays smooth.
- The card viewer's Fit and Expand/Collapse buttons stay in place and are greyed out when they don't apply (away from the challenges map), instead of popping in and out, and are drawn like the Skills Record's configuration button, beside it on the book's title bar (on the expanded window's title bar while expanded).
- JEI lists every Skills Record edition (one per dye) and every Shape Certificate (one per tool and shape), instead of only one of each.
- Shape Certificates are now upright hanging scrolls (paper rolled on wooden rods, a tier-colored cord and caps, and the tier's shape mark beside lines of writing), so they're easy to tell apart from the Miner Certificate, whose ribbon now carries a red wax seal.
- Redesigned the Shape Selector's screen after its item: a slate clipboard with its metal clip holding a blueprint sheet, with the tool slot, Set / Clear and the shape list drawn on the blueprint and the inventory on the board. Shape names too long for their row shrink to fit.
- Fixed the selected card slot's outline being hidden under the card.
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
- Removed the Forge Config API Port dependency and the `card_trade_level`/`card_trade_price` options.

### 🔧 Differences from the 26.1.2 release
- Item models pick their model through item properties and model overrides instead of item model definitions: `ultimine_addition:tier` on Mining Skill Cards (the tier's value / 4), `ultimine_addition:certificate_opened` on the Miner Certificate, `ultimine_addition:tool` on Shape Certificates (pickaxe 0.25, axe 0.5, shovel 0.75, hoe 1) and `ultimine_addition:color` on the Skills Record (its dye's id + 1, / 16; 0 undyed) for the dyed editions. Custom card types from `config/ultimine_addition/custom_cards` need a `models/item/<id>.json` with `tier` overrides in a resource pack.
- The undo key is Ctrl + Z through NeoForge's key modifiers; Fabric has none, so there the key mapping is plain Z and Ctrl is checked separately (the controls screen shows Z).
- FTB Ultimine 2101 has one shape registry for the client and the integrated server, so the shape list the local player sees is picked by thread.
- GUI layering, tooltips and fading follow 1.21.1's rendering: later layers clear the depth buffer so they cover items drawn before, the mod's tooltips are drawn once its screens are done, and GUI items fade through the shader colour (items with cutout models can't fade).
- The undo ghosts and growing blocks are drawn through each loader's level render events, and their block entities with the level's other block entities.
- The JEI refill category keeps its 3D crafting table.
- The Skills Record's dye recipe is a `crafting_special` recipe finding dyes by item (1.21.1 has no dye component), and its per-color recipes are written directly, as 1.21.1's recipe builders can't give a result components.
- The Edit Challenge dialog centres its number by moving the field's text box (1.21.1's text boxes can't centre their text).
- The playstyle mode's config tooltip describes both modes (the 1.21.1 CoolCatLib config screens have no per-option descriptions).

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