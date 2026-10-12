---
icon: material/cog-outline
description: The mod's settings
---

# Configuration

The mod has two config files, both TOML, in the `config` folder:

| File | Scope |
|---|---|
| `ultimine_addition-server.toml` | Gameplay rules. The server's values are synced to every player on join and whenever they change. |
| `ultimine_addition-client.toml` | Each player's own display settings. |

## Editing in game

| Command | Opens |
|---|---|
| `/ultimine_addition config` | Both configs |
| `/ultimine_addition clientconfig` | The client settings |
| `/ultimine_addition serverconfig` | The server settings |

The editor screens come from **Glazed Menu** or **Configured**. Without either, the command tells you how to change the config instead. The Skills Record's own settings button always works, with or without them.

## Server settings

### General

| Setting | What it does |
|---|---|
| `general.playstyle_mode` | How players unlock Ultimine: `MODERN` or `LEGACY`. See [Playstyle modes](../getting-started.md#playstyle-modes). Applies at once. |
| `general.blacklisted_shapes` | Ultimine shapes nobody can use, by shape ID (e.g. `ftbultimine:shapeless`). |
| `general.ultimine_sound` | A chime after a big Ultimine. |
| `general.drops_to_inventory` | An Ultimine's drops go straight into the player's inventory. Off by default. |
| `general.is_placed_by_entity_condition` | Blocks placed by players or other entities don't count towards challenges. |
| `general.challenge_validator` | How often, in seconds, cards are checked for challenges that no longer exist. |

### Mining Skill Cards

| Setting | What it does |
|---|---|
| `mining_skill_cards.challenges_amount` | Number of challenges a card gets in each tier. |
| `mining_skill_cards.max_blocks` | Ultimine block limit for each tier, used by Mine-Go Juice and Shape Certificates. |
| `mining_skill_cards.potion_durations` | Seconds a Mine-Go Juice lasts, by the tier of the card it was brewed with. |
| `mining_skill_cards.mastered_effect` | Carrying a Mastered card grants Ultimine for its tool. |
| `mining_skill_cards.prestige` | A Mastered card can start over for a lasting bonus. See [Perks and Prestige](../perks-and-prestige.md). |
| `mining_skill_cards.bind_to_player` | A card belongs to the first player who makes progress on it. Off by default. |

### Progression

| Setting | What it does |
|---|---|
| `progression.shape_certificates` | Turns Shape Certificates on or off. |
| `progression.novice_certificate_shapes`, `apprentice_certificate_shapes`, `adept_certificate_shapes` | The shape IDs each tier's certificate offers. |
| `progression.extra_shapes_certificate` | Which certificate picks up shapes no list names (such as shapes from other mods), or none. |
| `progression.streak_window` | Seconds allowed between two challenge blocks before a streak resets. |
| `progression.streak_bonus_interval` | Every Nth block of a streak counts one extra point. `0` disables streaks. |
| `progression.lucky_find_chance` | Chance (0 to 1) for a challenge block to count double. |
| `progression.rerolls_per_tier` | How many unstarted challenges a card can swap per tier. `0` disables rerolls. |
| `progression.reroll_ink_cost` | Ink taken from the pen for each reroll. |
| `progression.tier_up_taste`, `tier_up_taste_duration` | A short free Mine-Go Juice when a card tiers up, and how many seconds it lasts. Off by default. |
| `progression.card_perks` | Cards offer a perk at each tier. See [Perks and Prestige](../perks-and-prestige.md). |
| `progression.tool_affinity` | Tools count the blocks Ultimined with them and earn a small bonus. |
| `progression.juice_idle_pause` | With a number of seconds, a drunk Mine-Go Juice only runs down for that long after each Ultimine. `0` is off. |
| `progression.custom_shapes_per_player` | How many shapes a player may keep from the [Shape Designer](../shape-designer.md). `0` turns it off. |
| `progression.custom_shapes_max_blocks` | How many blocks a shape of a player's own may have. |
| `progression.timed_challenge` | The [daily challenge](../daily-challenge.md): `daily`, `weekly` or `off`. |
| `progression.timed_challenge_team` | A teammate's work on the daily challenge counts for the team's online members. |
| `progression.timed_challenge_experience`, `timed_challenge_juice` | The daily challenge's reward: experience, and seconds of Mine-Go Juice. |

### Skills Record

| Setting | What it does |
|---|---|
| `skills_record.paper_consumption_rate` | Chance (0 to 1) for each challenge point to use up a paper. |

### Undo

| Setting | What it does |
|---|---|
| `undo.enabled` | Lets players undo their last Ultimines. |
| `undo.history` | How many Ultimines per player can be undone, newest first. |
| `undo.window` | Seconds after an Ultimine during which it can be undone. |
| `undo.animation`, `animation_blocks_per_tick` | Blocks grow back one after another, and how many start growing each tick. |

### Legacy mode

| Setting | What it does |
|---|---|
| `legacy.required_amount_min`, `required_amount_max` | The range of ores a Completion Envelope can ask for. |

## Client settings

| Setting | What it does |
|---|---|
| `skills_record.animations` | Animations in the Skills Record. |
| `skills_record.text_shadow` | Drop shadow on the card viewer's text. |
| `skills_record.challenges_panel_alignment` | Where pinned challenges are shown on the HUD. |
| `visuals.shape_selector_filter` | Shapes the Shape Selector lists: all, or only the ones that aren't blacklisted. |
| `undo.confirm_missing_items` | Asks before an undo with missing items. |

## Older config files

Before the mod moved to CoolCatLib's config system the files were JSON5. They are converted once, with the same keys, and the originals are kept as `.json5.bak`.
