---
icon: material/package-variant-closed
description: The data pack formats
---

# Data Pack Reference

Everything the mod reads from data packs. All of it reloads with `/reload`, and the server sends what clients need to every player on join and after a reload.

## Folders

| Folder | Holds | Guide |
|---|---|---|
| `data/<namespace>/challenges/` | Mining Skill Card challenges | [Challenges](../wiki/packs/challenges.md) |
| `data/<namespace>/ultimine_shapes/` | Ultimine shapes | [Ultimine Shapes](../wiki/packs/shapes.md) |
| `data/<namespace>/mining_skill_cards/` | Card types for other tools | [Custom Cards](../wiki/packs/custom-cards.md) |
| `data/<namespace>/ultimine_rewards/` | Rewards for challenges, tiers and the daily challenge | [Reward](#reward) |

A file's id is its namespace and its path under the folder, without `.json`: `data/mypack/ultimine_shapes/wide_cut.json` is `mypack:wide_cut`. Subfolders become part of the path.

On 1.20.1 and 1.21.1 the folders are the same.

## Load order

1. **Card types** are read first: challenges name them.
2. **Challenges** are read next. A card type left with no challenges for one of its tiers is dropped here.
3. **Shapes** are read on their own and appended to FTB Ultimine's shape list, in id order, after the shapes mods register.

A file that fails to parse is skipped with an error in the log; the rest still load.

## Challenge

```json
{
  "challenge_type": { "id": "break_block", "consume_block": false },
  "for_card_type": "pickaxe",
  "for_card_tier": 0,
  "required_amount": { "min": 32, "max": 64 },
  "targeted_blocks": ["minecraft:stone"]
}
```

| Field | Type | Notes |
|---|---|---|
| `challenge_type.id` | string | `break_block`, `strip_block`, `flatten_block`, `tilling_block`. |
| `challenge_type.consume_block` | boolean | A consume challenge only counts with the Skills Record's consume mode on. |
| `for_card_type` | string | `pickaxe`, `axe`, `shovel`, `hoe`, or a data pack card type's id (`mypack:hammer`). |
| `for_card_tier` | integer | `0` Unlearned, `1` Novice, `2` Apprentice, `3` Adept. |
| `required_amount.min`, `.max` | integer | Each card rolls its own number in the range. |
| `targeted_blocks` | list of block ids | The blocks that count. |

## Shape

```json
{
  "name": "Wide Cut",
  "pattern": ["#####", "##o##", "#####"],
  "blocks": [[0, 0, 1]],
  "repeat": "none"
}
```

| Field | Type | Notes |
|---|---|---|
| `name` | string, optional | Shown to players. Without it: the key `ftbultimine.shape.<namespace>.<name>`. |
| `pattern` | list of strings, optional | Rows top to bottom, facing the block. `o` the broken block, `#` a block. Up to 65 × 65. |
| `blocks` | list of `[right, up, depth]`, optional | Offsets from the broken block, each from -32 to 32. |
| `repeat` | `"none"` or `"depth"`, optional | `"depth"` repeats the blocks one step deeper until the block limit. |

A shape needs a `pattern` or `blocks`. A mod's shape with the same id wins over a data pack's.

## Card type

```json
{
  "name": "Hammer",
  "tools": ["mymod:iron_hammer", "#mymod:hammers"],
  "icon": "mymod:iron_hammer",
  "juice_color": "#C0C0C0",
  "juice_name": "Mine-Go Juice: Smash Hit"
}
```

| Field | Type | Notes |
|---|---|---|
| `name` | string, optional | Plain text or a translation key. Without it: the key `ultimine_addition.card_type.<namespace>.<name>`. |
| `tools` | list of strings | Item ids, or item tags starting with `#`. At least one. |
| `icon` | item id, optional | The item drawn on the card. Without it: the first item id in `tools`. |
| `juice_color` | `"#RRGGBB"`, optional | White when left out. |
| `juice_name` | string, optional | Plain text or a translation key. Without it: the key `ultimine_addition.card_type.<namespace>.<name>.juice`, or else "Mine-Go Juice: " and the tool's name. |

## Reward

`data/<namespace>/ultimine_rewards/<name>.json`. Hands something out as players progress. Only the server reads these files.

```json
{
  "when": "challenge",
  "challenge": "mypack:hammer/smashing_stone",
  "loot_table": "mypack:rewards/gems",
  "experience": 20,
  "commands": ["say @s smashed a lot of stone"]
}
```

| Field | Type | Notes |
|---|---|---|
| `when` | string | `challenge`: a card's challenge is completed. `tier`: a card reaches a tier. `timed`: the player finishes the daily (or weekly) challenge. |
| `challenge` | challenge id, optional | Only for this challenge. Not allowed with `"when": "tier"`. |
| `card_type` | card type id, optional | Only for cards of this type (`pickaxe`, `mypack:hammer`...). |
| `tier` | number, optional | Only at this tier: the tier the card was at for `challenge`, the tier it reached for `tier` (1 Novice, 2 Apprentice, 3 Adept, 4 Mastered). |
| `loot_table` | loot table id, optional | Rolled with the `gift` parameters; the items go to the inventory, or to the ground when it is full. |
| `experience` | number, optional | Experience points. |
| `commands` | list of strings, optional | Run by the server at the player; `@s` is the player. |

A reward needs at least one of `loot_table`, `experience` and `commands`. A filter left out matches everything, so `{ "when": "tier", "experience": 100 }` pays for every tier of every card. A file with a mistake is skipped, and the log says why.

## What is sent to clients

| Data | When |
|---|---|
| Card types (names, tools, icons, juice colors) | Join and reload, before the challenges. |
| Challenges | Join and reload. |
| Data pack shapes (ids and names) | Join and reload. Clients don't get a shape's blocks: the server works those out. |
| Shape diagrams | On request, when a player opens the shape choice in the Skills Record. |
