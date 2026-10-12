---
icon: material/hook
description: Run your own functions and scripts on progress
---

# Hooks

A pack can react when a player makes progress: run a function, give a reward of its own, unlock a quest. There are two ways in, and both fire at the same five moments.

| Moment | When |
|---|---|
| `challenge_completed` | A card's challenge is finished. |
| `tier_reached` | A card moves up a tier. |
| `daily_completed` | The player finishes the daily (or weekly) challenge. |
| `perk_learned` | The player picks a perk. |
| `prestige` | A Mastered card starts over. |

## Function tags

Every moment runs the functions in a tag, as the player and at the player's position:

```
ultimine_addition:on_challenge_completed
ultimine_addition:on_tier_reached
ultimine_addition:on_daily_completed
ultimine_addition:on_perk_learned
ultimine_addition:on_prestige
```

Add your function to the tag in your data pack:

```json title="data/ultimine_addition/tags/function/on_tier_reached.json"
{
  "values": [
    "mypack:tier_up"
  ]
}
```

```mcfunction title="data/mypack/function/tier_up.mcfunction"
give @s minecraft:emerald 2
tellraw @s {"text":"A tier higher!","color":"green"}
```

On 1.20.1 the folders are `tags/functions` and `functions`.

A function doesn't know which card or tier it was. For that, use a [reward](../../docs/data-packs.md#reward) file, which can name the card type, the tier or the challenge, or use KubeJS.

## KubeJS

With KubeJS installed (NeoForge on 26.1.2 and 1.21.1, Forge on 1.20.1), the same moments are server script events:

```js title="kubejs/server_scripts/ultimine.js"
UltimineAdditionEvents.tierReached(event => {
  if (event.cardType == 'pickaxe' && event.tier == 3) {
    event.player.give('minecraft:diamond')
  }
})
```

| Event | Moment |
|---|---|
| `UltimineAdditionEvents.challengeCompleted` | A challenge is finished. |
| `UltimineAdditionEvents.tierReached` | A card moves up a tier. |
| `UltimineAdditionEvents.dailyCompleted` | The daily challenge is finished. |
| `UltimineAdditionEvents.perkLearned` | A perk is picked. |
| `UltimineAdditionEvents.prestige` | A Mastered card starts over. |

Each event has:

| Field | Meaning |
|---|---|
| `player` | The player. |
| `cardType` | The card's type: `pickaxe`, `axe`, `shovel`, `hoe` or a data pack type's id. |
| `tier` | The tier's number (`0` Unlearned to `4` Mastered). |
| `challenge` | The challenge's id, where there is one. |
| `detail` | The perk's id for `perkLearned`. |

## Stages

A challenge can ask for a stage before it counts: see `stages` under [conditions](../../docs/data-packs.md#challenge). A stage is an entity tag on the player, so a function or script gives it with `/tag <player> add <stage>`:

```mcfunction
tag @s add miner
```

That makes hooks and stages work together: a function on `on_tier_reached` can hand out the stage the next challenges ask for.
