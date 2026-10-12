---
icon: material/star-four-points
description: Perks for your tools, and starting a card over
---

# Perks and Prestige

*Minecraft 26.1.2 only for now.*

A Mining Skill Card gives more than shapes on its way up: a perk at each tier, and once Mastered, the choice to start over for a lasting bonus.

## Perks

When a card reaches Novice, Apprentice or Adept, its tier box in the Skills Record offers a perk after the Shape Certificate is claimed. Click the box and pick one of the two on offer. The one you leave may come back at a later tier or on another card of the same tool.

| Perk | What it does |
|---|---|
| **Reach** | A quarter more blocks in every Ultimine with this tool. |
| **Magnet** | An Ultimine's drops go straight into your inventory. |
| **Fortune** | Lucky finds come twice as often in this tool's challenges. |
| **Vigor** | A few seconds of Haste after every Ultimine with this tool. |

A perk belongs to you and to that tool, for good. It does not sit on the card, so losing the card doesn't lose the perk. `/ultimine_addition stats` lists the perks you have.

## Prestige

A Mastered card shows a star on its Mastered box. Click it twice to start the card over as Unlearned. This can't be undone.

- Each prestige adds 5% to the blocks an Ultimine with that card's tool can take, for good. A card can do it five times.
- The card's challenges start again from the first tier, and its perks and Shape Certificates can be claimed again on the way back up. That is how you get the fourth perk.
- While the card climbs back, you lose what carrying a Mastered card gives.

## Tool affinity

A tool counts the blocks Ultimined with it; its tooltip shows the number. Every 500 blocks add 1% to the blocks that tool's Ultimines can take, up to 10%. The count stays on the tool itself.

## For servers and packs

All of these are in the [server settings](packs/configuration.md#server-settings): `progression.card_perks`, `progression.tool_affinity`, `mining_skill_cards.prestige`. Two more are off by default:

- `mining_skill_cards.bind_to_player`: a card belongs to the first player who makes progress on it.
- `progression.juice_idle_pause`: with a number of seconds, a drunk Mine-Go Juice only runs down for that long after each Ultimine.
