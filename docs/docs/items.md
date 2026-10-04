# Items and Ids

The mod's namespace is `ultimine_addition`.

## Items

| Id | Item |
|---|---|
| `mining_skill_card_empty` | Mining Skill Card: Empty |
| `mining_skill_card_pickaxe`, `_axe`, `_shovel`, `_hoe` | The built-in cards |
| `mining_skill_card_generic` | The card of every data pack card type |
| `skills_record` | Skills Record |
| `pen`, `ink_chamber` | Pen, Ink Chamber |
| `miner_certificate` | Miner Certificate (named Completion Envelope until earned) |
| `shape_certificate_novice`, `_apprentice`, `_adept` | Shape Certificates |
| `shape_selector` | Shape Selector |
| `mine_go_juice_generic` | The Mine-Go Juice of every data pack card type |

The two generic items are in no creative tab and so not in JEI's item list.

## Data on stacks

On 26.1.2 and 1.21.1 these are data components, written in `/give` as `item[component=value]`. On 1.20.1 the same data is in the item's NBT under the same ids.

| Component | On | Value |
|---|---|---|
| `ultimine_addition:mining_skill_card_data` | Cards | The card's UUID and tier. Its challenges and potion points are kept by the server, not on the item. |
| `ultimine_addition:card_type` | The generic card | The card type's id, like `"mypack:hammer"`. |
| `ultimine_addition:generic_juice` | The generic juice | `{type: "mypack:hammer", tier: 2}` |
| `ultimine_addition:skills_record_data` | Skills Record | The record's UUID. Its contents are kept by the server. |
| `ultimine_addition:shape_certificate_data` | Shape Certificates | The tool, as a card type id. |
| `ultimine_addition:shape_certificate_shape` | Shape Certificates | The shape's id. |
| `ultimine_addition:item_storage_data` | Pen | `{StorageName: "ink_chamber", Capacity: 2000, MaxCapacity: 2000}` |

Examples for 26.1.2 and 1.21.1:

```
/give @s ultimine_addition:mining_skill_card_generic[ultimine_addition:card_type="mypack:hammer"]
/give @s ultimine_addition:mine_go_juice_generic[ultimine_addition:generic_juice={type:"mypack:hammer",tier:2}]
/give @s ultimine_addition:pen[ultimine_addition:item_storage_data={StorageName:"ink_chamber",Capacity:2000,MaxCapacity:2000}]
```

## Effects and potions

| Id | What |
|---|---|
| `mine_go_juice_pickaxe`, `_axe`, `_shovel`, `_hoe` | The built-in juices' effects. |
| `mine_go_juice_generic` | The effect shared by every data pack card type's juice. Which type it is for is stored with the player. |
| `knowledge` | The Knowledge Potion, the base Mine-Go Juice is brewed from. |
| `mine_go_juice_<tool>`, `_2`, `_3` | The built-in juices as potions, by the card's tier. |

The effect's amplifier is the juice's tier minus one: 0 Novice, 1 Apprentice, 2 Adept.

## Recipe types

| Type | Recipe |
|---|---|
| `ultimine_addition:mining_card_recipe` | The built-in cards and the Miner Certificate: ingredients can ask for a card of a tier. |
| `ultimine_addition:item_storage_data` | Refilling the Pen with pigments. |
| `ultimine_addition:skills_record_dyeing` | A Skills Record and a dye. |
| `ultimine_addition:data_card` | An empty card and a tool of a data pack card type. |

Recipes tied to the playstyle mode carry the `ultimine_addition:legacy_mode` load condition.

## Item tags

| Tag | Used for |
|---|---|
| `ultimine_addition:mining_skill_card` | The empty and built-in cards. |
| `ultimine_addition:more_valuable_pigment` | Pigments that refill 50 ink. |
| `ultimine_addition:less_valuable_pigment` | Pigments that refill 10 ink. |
| `ultimine_addition:legacy_disabled_items` | Items switched off in Legacy mode. |

## Item models

On 26.1.2 the mod adds two item model pieces for resource packs:

| Id | Kind | What |
|---|---|---|
| `ultimine_addition:mining_skill_card` | Model type | Picks a model by the card's tier, listed under `classic` (`unlearned`, `novice`, `apprentice`, `adept`, `mastered`). |
| `ultimine_addition:certificate_opened` | Condition | True once the Miner Certificate is earned. |

On 1.21.1 and 1.20.1 the same choices are item properties used by model overrides: `ultimine_addition:tier`, `ultimine_addition:certificate_opened`, `ultimine_addition:tool` and the Skills Record's dye.
