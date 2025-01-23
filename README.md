# Spice of Life Discovery

Spice of Life Discovery is a spin on the Spice of Life: Carrot Edition idea, but more geared toward finding more unique
foods as you play. SOL: Discovery is best enjoyed with a mountain of new modded foods. Consider using Farmer's Delight
with a bunch of addons!

## Configuration

### Default

By default, SOL:D starts the player at 6 HP (3 hearts) and grants an additional 2 HP (1 heart) for each 5 new food items
eaten. That formula is completely configurable in `.minecraft/config/soldisco.json5`. Here's what it looks
like by default:



The formula is parsed by exp4j. You can find which functions are available [here](https://www.objecthunter.net/exp4j/#Built-in_functions).

### Suggestions

Here are a few interesting or weird ideas to get you started.

This formula will require an increasing amount of new food items for each additional heart:

```json5
{
    "formula": "20 + 2 * floor(sqrt(foodsEaten))",
    "minHp": 20.0
}
```

This formula punishes you for each new food you eat:

```json5
{
    "formula": "40 - foodsEaten",
    "minHp": 1.0
}
```

Start at 3 hearts and gain a heart for every five new foods you eat (this is the default):

```json5
{
    "formula": "6 + 2 * floor(foodsEaten / 5)",
    "minHp": 6.0
}
```

## Commands

The `/foodhistory` command lets you manage the foods you've eaten.

- `foodhistory get [<player>]` gets the full food history of a player
- `foodhistory add <food> [<players>]` adds a food to the food history of `players`
- `foodhistory remove <food> [<players>]` removes a food from the food history of `players`
- `foodhistory query <food> [<players>]` check if `players` have a food

## Credits

- [Siphalor's Spice of Fabric mod](https://github.com/Siphalor/spiceoffabric), for inspiration and a code reference.
- [Twemoji](https://github.com/twitter/twemoji), for the magnifying glass in the mod icon.
