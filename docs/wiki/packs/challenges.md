# Challenges

Challenges are data pack files, so a modpack can add its own, change the built-in ones or remove them. The mod ships with around ninety, spread over the four tools.

## Where they go

```
data/<namespace>/challenges/<any folders>/<name>.json
```

The built-in ones are under `data/ultimine_addition/challenges/` in `axe`, `hoe`, `pickaxe` and `shovel` folders. A file at the same path in your data pack replaces the built-in one.

## The format

```json title="data/mypack/challenges/axe/gathering_bamboo.json"
{
  "challenge_type": {
    "id": "break_block",
    "consume_block": false
  },
  "for_card_type": "axe",
  "for_card_tier": 3,
  "required_amount": {
    "min": 8,
    "max": 16
  },
  "targeted_blocks": [
    "minecraft:bamboo"
  ]
}
```

| Field | Meaning |
|---|---|
| `challenge_type.id` | The action: `break_block`, `strip_block` (axe), `flatten_block` (shovel) or `tilling_block` (hoe). |
| `challenge_type.consume_block` | `true` makes it a consume challenge: it only progresses with the Skills Record's consume mode on. |
| `for_card_type` | The card it belongs to: `pickaxe`, `axe`, `shovel`, `hoe`, or a [custom card](custom-cards.md)'s id. |
| `for_card_tier` | The tier that can roll it. See the table below. |
| `required_amount` | How many times the action is needed. Each card rolls its own number between `min` and `max`. |
| `targeted_blocks` | The blocks that count. |

`for_card_tier` uses the tier's number:

| Number | Tier |
|---|---|
| 0 | Unlearned |
| 1 | Novice |
| 2 | Apprentice |
| 3 | Adept |

Mastered cards have no challenges.

## How cards use them

When a card enters a tier it rolls that tier's challenges at random from the ones defined for its type and tier. How many it rolls is the `mining_skill_cards.challenges_amount` [server setting](configuration.md#mining-skill-cards), so define at least that many per type and tier.

After a data pack reload, cards are checked on a timer (`general.challenge_validator`) and challenges that no longer exist are replaced.

## Finding a challenge's id

With the client's `debug.skills_record_edit_mode` setting on, operators can copy a challenge's id from the Skills Record and edit its progress there.
