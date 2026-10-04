# Translation Keys

The mod's texts are in `assets/ultimine_addition/lang/en_us.json`. A resource pack can override any of them, and add the keys data pack content uses.

## Data pack content

| Key | For |
|---|---|
| Whatever a card type's `name` holds | The tool's name, when `name` is a translation key. |
| Whatever a card type's `juice_name` holds | The juice's name, when `juice_name` is a translation key. |
| `ultimine_addition.card_type.<namespace>.<name>` | The tool's name of a card type with no `name`. |
| `ftbultimine.shape.<namespace>.<name>` | A data pack shape with no `name` (26.1.2 and 1.21.1). |
| `ftbultimine.shape.<namespace>:<name>` | A data pack shape in FTB Ultimine's own shape display on 1.20.1. |
| `challenge.<namespace>.<path>.name` | A challenge's title in the Skills Record, like `challenge.mypack.hammer.smashing_stone.name`. Without one it is shown as "Challenge 1", "Challenge 2"... |

In a path, `/` becomes `.`: the card type `mypack:tools/hammer` uses `ultimine_addition.card_type.mypack.tools.hammer`.

## Wrappers the mod fills in

These carry a `%s` the mod fills with a name from above. Override them to change the wording around it.

| Key | English | `%s` |
|---|---|---|
| `item.ultimine_addition.mining_skill_card_generic` | Mining Skill Card: %s | The tool's name |
| `effect.ultimine_addition.mine_go_juice_generic.of` | Mine-Go Juice: %s | The tool's name, when the type has no `juice_name` |
| `info.ultimine_addition.required_skill` | Required Skill for: %s | The tool's name |
| `tooltip.ultimine_addition.mine_go_juice.info` | Unlocks Ultimine with all shapes for %s tools while it lasts. | The tool's name |

## Built-in names

| Key | English |
|---|---|
| `info.ultimine_addition.required_skill.pickaxe`, `.axe`, `.shovel`, `.hoe` | The built-in tools' names |
| `item.ultimine_addition.mining_skill_card_<tool>` | The built-in cards |
| `effect.ultimine_addition.mine_go_juice_<tool>` | The built-in juices |
| `item.ultimine_addition.completion_envelope` | Completion Envelope |
| `item.ultimine_addition.miner_certificate` | Miner Certificate |

## Config screens

Config names and tooltips are `config.ultimine_addition.<config>.<path>`, with `.tooltip` for the tooltip and `.title` for a screen's title, where `<config>` is `client` or `server`. For example `config.ultimine_addition.server.undo.window` and `config.ultimine_addition.server.undo.window.tooltip`.
