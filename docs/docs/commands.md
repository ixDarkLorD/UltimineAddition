# Commands

Everything is under `/ultimine_addition`. The root needs no permission; each subcommand sets its own.

## Config

| Command | Who | Does |
|---|---|---|
| `/ultimine_addition config` | Any player | Opens the mod's configs. |
| `/ultimine_addition clientconfig` | Any player | Opens the client settings. |
| `/ultimine_addition serverconfig` | Operators | Opens the server settings. |

The editor screens come from Glazed Menu or Configured. Without either, the command says how to change the config instead.

## Ultimine ability

Operators only.

| Command | Does |
|---|---|
| `/ultimine_addition ultimine_ability <targets> set <true\|false>` | Gives or takes the Miner Certificate's ability: Ultimine with every shape and any item. |
| `/ultimine_addition ultimine_ability <targets> shapes grant <tier> [tool]` | Teaches the shapes of that tier's certificate list (`novice`, `apprentice`, `adept`...). Without a tool, for every card type; with one (`pickaxe`, `mypack:hammer`), for that tool only. |
| `/ultimine_addition ultimine_ability <targets> shapes reset` | Forgets every learned shape. |

## Shapes

Operators only.

| Command | Does |
|---|---|
| `/ultimine_addition ultimine_shape blacklist add <shape_id>` | Blocks a shape for everyone. |
| `/ultimine_addition ultimine_shape blacklist remove <shape_id>` | Allows it again. |
| `/ultimine_addition ultimine_shape blacklist clear` | Empties the blacklist. |

These edit `general.blacklisted_shapes` in the server config.

## Mining Skill Cards

Operators only. These reach a card through the inventory slot it is in.

| Command | Does |
|---|---|
| `/ultimine_addition mining_skill_card <targets> tier <tier> in_inventory <slot> <card_holder>` | Sets the card's tier. |
| `/ultimine_addition mining_skill_card <targets> challenge <challenge_id> in_inventory <slot> <card_holder> set_point <amount>` | Sets a challenge's progress. |
| `… add_point <amount>` | Adds to it. |
| `… accomplish` | Completes it. |

- `<slot>` is a vanilla slot, like `hotbar.0` or `inventory.5`.
- `<card_holder>` says where the card is in that slot. For a card inside a Skills Record it is `skills_record.<n>`, the record's card slot counted from 0. Tab completion lists the choices.
- With Curios or Trinkets installed there is an `in_curios` / `in_trinkets` form beside `in_inventory`.

Example, for the first card of the record in the first hotbar slot:

```
/ultimine_addition mining_skill_card @s tier Adept in_inventory hotbar.0 skills_record.0
```

## Skills Record

| Command | Who | Does |
|---|---|---|
| `/ultimine_addition skills_record inspect [targets]` | Operators | Prints what the server has for the player's records and cards: ink, slots, tiers, challenges. |
| `/ultimine_addition_client skills_record inspect` | Any player | Prints what the client has. Comparing the two shows a sync problem. |
