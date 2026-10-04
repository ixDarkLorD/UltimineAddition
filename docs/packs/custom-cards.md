# Custom Cards

Besides the four built-in tools, a pack can add Mining Skill Cards for other tools: a hammer, a sickle, a mod's own multi-tool. Each custom card gets its own item, its own Mine-Go Juice and its own challenges.

## Adding a card type

Put one JSON file per card in the config folder:

```
config/ultimine_addition/custom_cards/<name>.json
```

```json title="config/ultimine_addition/custom_cards/hammer.json"
{
  "active": true,
  "card_id": "hammer",
  "required_tools": [
    "mymod:iron_hammer",
    "mymod:diamond_hammer"
  ],
  "potion_color": [192, 192, 192]
}
```

| Field | Meaning |
|---|---|
| `active` | `false` skips the file without deleting it. |
| `card_id` | The card type's id. It must be unique, and it is what challenges name in `for_card_type`. |
| `required_tools` | The tools the card works with. It can't be empty. |
| `potion_color` | The color of this card's Mine-Go Juice, as `[red, green, blue]` (0 to 255 each). Optional; white when left out. |

The files are read when the game starts, because each card type registers its own item and potion. **Restart the game after adding or changing one**, and give the server and every client the same files.

## What else a custom card needs

- **Challenges.** A card with no challenges can't progress. Add [challenge files](challenges.md) whose `for_card_type` is the card's id, for every tier.
- **A look.** On 26.1.2 the card item needs an item definition (`items/<id>.json`) and textures in a resource pack. Without them it shows as a missing model.
- **Names.** Add translations for the card and its juice in the same resource pack.
