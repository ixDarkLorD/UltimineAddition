# Custom Cards

Besides the four built-in tools, a data pack can add Mining Skill Cards for other tools: a hammer, a sickle, shears, a mod's own multi-tool. Each card type gets its own card, challenges, Shape Certificates and Mine-Go Juice.

!!! info "Since"
    FTB Ultimine Addition **26.1.2-5**. It replaces the card files in `config/ultimine_addition/custom_cards`, which are no longer read.

## Adding a card type

```
data/<namespace>/mining_skill_cards/<name>.json
```

```json title="data/mypack/mining_skill_cards/hammer.json"
{
  "name": "Hammer",
  "tools": ["mymod:iron_hammer", "#mymod:hammers"],
  "icon": "mymod:iron_hammer",
  "juice_color": "#C0C0C0",
  "juice_name": "Mine-Go Juice: Smash Hit"
}
```

The type's id is `<namespace>:<name>`, here `mypack:hammer`. Card types load with the data pack, so `/reload` picks up changes, and the server sends them to every player.

| Field | Meaning |
|---|---|
| `name` | The tool's name, as players read it: "Mining Skill Card: Hammer", "Required Skill for: Hammer". |
| `tools` | The tools the card works with: item ids, or item tags starting with `#`. At least one. |
| `icon` | The item drawn on the card. Optional: without it, the first item in `tools` is used. |
| `juice_color` | The color of this card's Mine-Go Juice, as `"#RRGGBB"`. Optional; white when left out. |
| `juice_name` | The juice's name. Optional: without it, "Mine-Go Juice: " and the tool's name. |

A file with a mistake is skipped, and the server log says what is wrong with it.

## The card

Every data pack card is the same item, `ultimine_addition:mining_skill_card_generic`, with its type on the stack. It looks like the built-in cards: the card of its tier, with the `icon` item drawn on its plate.

- **Crafting**: an empty Mining Skill Card and one of the type's tools, anywhere in the grid.
- **Giving one**: `/give @s ultimine_addition:mining_skill_card_generic[ultimine_addition:card_type="mypack:hammer"]`
- It is in no creative tab, and so not in JEI's item list.
- A card whose type no longer exists (its data pack was removed) does nothing until the type is back.

## Its challenges

A card with no challenges can't progress. Add [challenge files](challenges.md) whose `for_card_type` is the type's id, for every tier, at least as many as the tier rolls (`mining_skill_cards.challenges_amount`):

```json title="data/mypack/challenges/hammer/smashing_stone.json"
{
  "challenge_type": { "id": "break_block", "consume_block": false },
  "for_card_type": "mypack:hammer",
  "for_card_tier": 0,
  "required_amount": { "min": 16, "max": 32 },
  "targeted_blocks": ["minecraft:stone", "minecraft:cobblestone"]
}
```

A challenge only counts while the player holds one of the type's tools.

## Its Mine-Go Juice

Brewed like the built-in ones: a Knowledge Potion and the card (Novice to Adept, with potion points left) in a brewing stand. The result is `ultimine_addition:mine_go_juice_generic`, a drink in the type's color that carries the type and the card's tier.

- All data pack types share **one effect**, so a player has one data pack juice at a time: drinking another type's replaces it. Built-in juices are separate and stack with it.
- It is a drink only: there are no splash or lingering versions and no tipped arrows.
- Like the card, it is in no creative tab.

## Its Shape Certificates

They work as for the built-in tools: each tier the card reaches offers a shape, and the certificate teaches it for this type's tools.

!!! note "Ultimine and the right tool"
    Ultimine only starts on a block the held tool is the right tool for. Shears, for instance, are the right tool for cobwebs but not for leaves, so a shears card Ultimines cobwebs.
