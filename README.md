<p align="center">
  <img src="https://i.imgur.com/B95uFIb.png" alt="FTB Ultimine Addition">
</p>

<h1 align="center">FTB Ultimine Addition</h1>

<p align="center">
  <b>Ultimine isn't given. It's earned.</b><br>
  An add-on for FTB Ultimine: players unlock vein mining by clearing mining challenges, one tool and one shape at a time.
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/ultimine-addition"><img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/curseforge.svg" alt="CurseForge" height="26"></a>
  <a href="https://ixdarklord.github.io/UltimineAddition/"><img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/website.svg" alt="Website" height="26"></a>
  <a href="https://ixdarklord.github.io/UltimineAddition/wiki/"><img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/wiki.svg" alt="Wiki" height="26"></a>
  <a href="https://ixdarklord.github.io/UltimineAddition/changelog/"><img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/changelog.svg" alt="Changelog" height="26"></a>
</p>
<p align="center">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/fabric.svg" alt="Fabric" height="26">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/neoforge.svg" alt="NeoForge" height="26">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/forge.svg" alt="Forge" height="26">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/ftb-ultimine-add-on.svg" alt="FTB Ultimine add-on" height="26">
  <a href="https://discord.gg/ZapGEPqm2V"><img src="https://img.shields.io/discord/1173075408556654592?style=flat&logo=discord&logoColor=white&label=&labelColor=f48255&color=3A3F58" alt="Discord" width="110" height="26"></a>
</p>

<hr>

## ✨ About

With [FTB Ultimine](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge) alone, everyone can vein mine from the first minute. This add-on locks it, and turns it into a skill players work towards.

- 🔒 **Locked by default**: no Ultimine until a player has earned it
- 🎯 **Challenges to clear**: mining goals for the pickaxe, axe, shovel and hoe
- 📜 **Shapes to learn**: each finished tier teaches one Ultimine shape for its tool
- ✏️ **Shapes of your own**: draw them in the Shape Designer, layers deep, and share them as JSON
- ⭐ **Perks and prestige**: a perk for the tool at each tier, and Mastered cards that start over for a lasting bonus
- 📅 **A daily challenge**: one challenge for the whole server each day, done alone or as a team
- ↩️ **Ultimine undo**: put a wrongly mined vein back
- ⚙️ **Made for modpacks**: synced server settings, and challenges, card types, shapes and rewards from data packs

## ⛏️ How it works

1. **Craft a Skills Record.** A clipboard that holds your Mining Skill Cards and a Pen, and tracks your progress.
2. **Add Mining Skill Cards.** One card per tool. Each gives you mining challenges for its tool, like "break 48 Iron Ore".
3. **Mine to clear challenges.** Keep the Skills Record with you and break the blocks a challenge asks for.
4. **Tier up.** Clearing a tier's challenges moves the card up: Unlearned → Novice → Apprentice → Adept → Mastered.
5. **Unlock Ultimine.** Each finished tier grants a Shape Certificate that teaches one shape for that tool. Master all four cards to craft the Miner Certificate, which unlocks every shape for good.

Along the way, **Mine-Go Juice** brewed from your cards lets you Ultimine with a tool while its effect lasts.

The [wiki](https://ixdarklord.github.io/UltimineAddition/wiki/) has the full walkthrough.

## 🆕 What's new

On every supported version (26.1.2, 1.21.1 and 1.20.1):

- **Shape Designer**: draw an Ultimine shape of your own from the Shape Selector, up to five layers deep with a 3D preview. Keep a few, replace or delete them, and copy them as JSON to share.
- **Card perks**: each tier a card reaches offers a choice of two perks for its tool, from small comforts at Novice to bigger, stronger Ultimines at Adept.
- **Prestige**: start a Mastered card over for a lasting bonus to that tool's Ultimines.
- **Daily challenge**: one challenge for the whole server each day (or week), with its own reward. Teammates share the progress.
- **Data packs**: add Mining Skill Card types for other tools, Ultimine shapes, rewards, and challenges with conditions, chains and stages.
- **Hooks**: function tags, and KubeJS events, for every step of a player's progress.
- **Undo history** (<kbd>Ctrl</kbd> + <kbd>H</kbd>), undo of tool actions like stripping and tilling, and statistics with a leaderboard.
- **Tools from other mods** count for challenges even when their mod left them out of the tool tags.

Everything else is in the [changelog](https://ixdarklord.github.io/UltimineAddition/changelog/).

## 📦 Download

Get it on **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/ultimine-addition)**, along with the mods it requires:

| Mod | CurseForge | Modrinth |
|---|---|---|
| **FTB Ultimine** | [ftb-ultimine-forge](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge) | – |
| **CoolCatLib** | [coolcatlib](https://www.curseforge.com/minecraft/mc-mods/coolcatlib) | [CoolCatLib](https://modrinth.com/mod/ASkaoGC8) |

FTB Ultimine brings FTB Library with it. Before 26.1.2, the Fabric versions also need Forge Config API Port.

CoolCatLib used to be two mods, CoolCatLib: Core and CoolCatLib: Canvas. Since 26.1.2-7, 2101.2.0.6 and 2001.2.0.6 this mod needs the merged **CoolCatLib** instead: remove both old jars when you update. Older releases still need Core and Canvas.

**Optional:** JEI, and Curios or Trinkets for a Skills Record slot.

## 🧭 Versions

Each Minecraft version lives on its own branch.

| Minecraft | Loaders | Branch | Status |
|---|---|---|:-:|
| **26.1.2** | NeoForge, Fabric | [`26.1.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/26.1.2) | ✅ Supported |
| **1.21.1** | NeoForge, Fabric | [`1.21.1`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.21.1) | ✅ Supported |
| **1.20.1** | Forge, Fabric | [`1.20.1`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.20.1) | ✅ Supported |
| 1.21 | NeoForge, Fabric | [`1.21`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.21) | Discontinued |
| 1.19.2 | Forge, Fabric | [`1.19.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.19.2) | Discontinued |
| 1.18.2 | Forge, Fabric | [`1.18.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.18.2) | Discontinued |

Supported versions require FTB Ultimine and CoolCatLib. Discontinued ones get no more updates or fixes.

## 🧩 For pack makers and mod authors

Data packs can add or change challenges, Mining Skill Card types for other tools, Ultimine shapes and rewards, and function tags or KubeJS scripts can react to a player's progress. Server settings sync to every player and apply live.

- [Configuration](https://ixdarklord.github.io/UltimineAddition/wiki/packs/configuration/)
- [Challenges](https://ixdarklord.github.io/UltimineAddition/wiki/packs/challenges/)
- [Ultimine shapes](https://ixdarklord.github.io/UltimineAddition/wiki/packs/shapes/)
- [Custom cards](https://ixdarklord.github.io/UltimineAddition/wiki/packs/custom-cards/)
- [Hooks](https://ixdarklord.github.io/UltimineAddition/wiki/packs/hooks/)
- [Developer docs](https://ixdarklord.github.io/UltimineAddition/docs/)

## 🛠️ Building

Check out the branch for your Minecraft version, then run:

```bash
./gradlew build
```

The jars end up in `fabric/build/libs` and `neoforge/build/libs` (or `forge/build/libs`). The build scripts and dependency versions come from [ModResources](https://github.com/ixDarkLorD/ModResources).

## 🐞 Issues and ideas

Found a bug or have an idea? Open an [issue](https://github.com/ixDarkLorD/UltimineAddition/issues) with the bug report or feature request template, or drop by the [Discord](https://discord.gg/ZapGEPqm2V).

## 📄 License

See [LICENSE](LICENSE).
