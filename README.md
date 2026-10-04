<h1 align="center">FTB Ultimine Addition</h1>

<p align="center">
  <img src="https://i.imgur.com/4S4rtpt.png" alt="FTB Ultimine Addition" width="160">
</p>

<p align="center">
  <b>Restrict the use of Ultimine to specific conditions: players unlock the excavation skill by completing challenges.</b>
</p>

<p align="center">
  <a href="https://ixdarklord.github.io/UltimineAddition/">Website</a> ·
  <a href="https://www.curseforge.com/minecraft/mc-mods/ultimine-addition">CurseForge</a> ·
  <a href="https://github.com/ixDarkLorD/UltimineAddition/issues">Issues</a> ·
  <a href="https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge">FTB Ultimine</a>
</p>

<hr>

## About

FTB Ultimine Addition is an add-on for [FTB Ultimine](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge). Instead of
having Ultimine from the start, players earn it:

- **Mining Skill Cards**: each card belongs to a tool (pickaxe, axe, shovel, hoe) and holds mining challenges. Completing
  them raises the card through its tiers, from Unlearned to Mastered.
- **Skills Record**: a book that stores your cards, tracks their challenges and turns ink into progress.
- **Miner Certificate**: unlocks Ultimine, with every shape, for good.
- **Mine-Go Juice**: potions brewed from your progress that allow Ultimine with the juice's tool while the effect lasts.

Challenges are datapack-driven, so modpacks can add or change them, and other mods can add their own card types.

### New in 26.1.2

- **Shape Certificates**: cards reaching Novice, Apprentice, Adept or Mastered let you claim a certificate that teaches
  one Ultimine shape for that tool.
- **Ultimine undo**: press <kbd>Ctrl</kbd> + <kbd>Z</kbd> after an Ultimine to preview putting the blocks back, and again to
  confirm. The dropped items and experience are taken back.
- A redesigned card viewer (a pan-and-zoom map of the card's tiers and challenges), challenge rerolls, streaks and lucky
  finds, and a notice HUD above the hotbar.
- In-game config screens (`/ultimine_addition config`), with TOML config files.

See the [changelog](https://github.com/ixDarkLorD/UltimineAddition/blob/26.1.2/CHANGELOG.md) for everything.

## Downloads

FTB Ultimine Addition is on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/ultimine-addition).

The mods it requires:

| Mod | CurseForge | Modrinth |
|---|---|---|
| **FTB Ultimine** | [ftb-ultimine-forge](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge) | |
| **CoolCatLib: Core** | [coolcatlib](https://www.curseforge.com/minecraft/mc-mods/coolcatlib) | [CoolCatLib: Core](https://modrinth.com/mod/ASkaoGC8) |
| **CoolCatLib: Canvas** | [coolcatlib-canvas](https://www.curseforge.com/minecraft/mc-mods/coolcatlib-canvas) | [CoolCatLib: Canvas](https://modrinth.com/mod/NtytwOvv) |

## Versions

Each Minecraft version lives on its own branch. Discontinued versions get no more updates or fixes.

| Minecraft | Loaders | Branch | Status | Requires |
|---|---|---|---|---|
| 26.1.2 | NeoForge, Fabric | [`26.1.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/26.1.2) | Supported | FTB Ultimine, CoolCatLib: Core, CoolCatLib: Canvas |
| 1.21.1 | NeoForge, Fabric | [`1.21.1`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.21.1) | Supported | FTB Ultimine, CoolCatLib: Core, CoolCatLib: Canvas |
| 1.21 | NeoForge, Fabric | [`1.21`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.21) | Discontinued | FTB Ultimine, CoolCatLib |
| 1.20.1 | Forge, Fabric | [`1.20.1`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.20.1) | Supported | FTB Ultimine, CoolCatLib: Core, CoolCatLib: Canvas |
| 1.19.2 | Forge, Fabric | [`1.19.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.19.2) | Discontinued | FTB Ultimine, CoolCatLib |
| 1.18.2 | Forge, Fabric | [`1.18.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.18.2) | Discontinued | FTB Ultimine, CoolCatLib |

FTB Ultimine brings FTB Library with it. Before 26.1.2, the Fabric versions also need Forge Config API Port.

Optional integrations: JEI, and Curios or Trinkets for a Skills Record slot.

## Building

Check out the branch for your Minecraft version, then run:

```bash
./gradlew build
```

The jars end up in `fabric/build/libs` and `neoforge/build/libs` (or `forge/build/libs`). The build scripts and dependency
versions come from [ModResources](https://github.com/ixDarkLorD/ModResources).

## Reporting issues

Found a bug or have an idea? Open an [issue](https://github.com/ixDarkLorD/UltimineAddition/issues) with the bug report
or feature request template.

## License

See [LICENSE](LICENSE).
