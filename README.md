<p align="center">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/UltimineAddition/26.1.2/common/src/main/resources/mod_logo.png" alt="FTB Ultimine Addition" width="160">
</p>

<h1 align="center">FTB Ultimine Addition</h1>

<p align="center">
  <b>Ultimine isn't given. It's earned.</b><br>
  An add-on for FTB Ultimine: players unlock vein mining by clearing mining challenges.
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/ultimine-addition"><img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/curseforge.svg" alt="CurseForge" height="26"></a>
  <a href="https://ixdarklord.github.io/UltimineAddition/"><img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/website.svg" alt="Website" height="26"></a>
  <a href="https://ixdarklord.github.io/UltimineAddition/wiki/"><img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/wiki.svg" alt="Wiki" height="26"></a>
  <a href="https://discord.gg/ZapGEPqm2V"><img src="https://img.shields.io/discord/1173075408556654592?style=flat&logo=discord&logoColor=white&label=&labelColor=f48255&color=3A3F58" alt="Discord" width="110" height="26"></a>
</p>
<p align="center">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/fabric.svg" alt="Fabric" height="26">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/neoforge.svg" alt="NeoForge" height="26">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/forge.svg" alt="Forge" height="26">
  <img src="https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/badges/ftb-ultimine-addition/ftb-ultimine-add-on.svg" alt="FTB Ultimine add-on" height="26">
</p>

<hr>

## ✨ About

With [FTB Ultimine](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge) alone, everyone can vein mine from
the first minute. This add-on locks it, and turns it into a skill players work towards: one tool and one shape at a
time.

## 🧩 Features

- **🔒 Locked by default.** No Ultimine until a player has earned it.
- **🎯 Challenges to clear.** Mining goals for the pickaxe, axe, shovel and hoe, on Mining Skill Cards kept in a Skills
  Record.
- **📜 Shapes to learn.** Each finished tier teaches one Ultimine shape for its tool; master all four cards for the
  Miner Certificate, which unlocks every shape for good.
- **✏️ Shapes of your own.** Draw them in the Shape Designer, up to five layers deep with a 3D preview, and share them
  as JSON.
- **⭐ Perks and prestige.** A perk for the tool at each tier, and Mastered cards that start over for a lasting bonus.
- **📅 A daily challenge.** One challenge for the whole server each day (or week), done alone or as a team.
- **↩️ Ultimine undo.** Put a wrongly mined vein back, with an undo history (<kbd>Ctrl</kbd> + <kbd>H</kbd>).
- **⚙️ Made for modpacks.** Synced server settings, and challenges, card types, shapes and rewards from data packs.

### ⛏️ How it works

1. **Craft a Skills Record**, a clipboard that holds your Mining Skill Cards and a Pen.
2. **Add Mining Skill Cards**, one per tool, each with mining challenges like "break 48 Iron Ore".
3. **Mine to clear challenges**, with the Skills Record on you.
4. **Tier up**: Unlearned → Novice → Apprentice → Adept → Mastered.
5. **Unlock Ultimine**: each finished tier grants a Shape Certificate that teaches one shape for that tool.

The [wiki](https://ixdarklord.github.io/UltimineAddition/wiki/) has the full walkthrough, and the
[changelog](https://ixdarklord.github.io/UltimineAddition/changelog/) what's new.

## 📦 Download

| CurseForge | Modrinth |
|---|---|
| [ultimine-addition](https://www.curseforge.com/minecraft/mc-mods/ultimine-addition) | – |

It needs [FTB Ultimine](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge) (which brings FTB Library) and
[CoolCatLib](https://modrinth.com/mod/coolcatlib). Before 26.1.2, the Fabric versions also need Forge Config API Port.
Optional: JEI, and Curios or Trinkets for a Skills Record slot.

> [!NOTE]
> Since 26.1.2-7, 2101.2.0.6 and 2001.2.0.6 it needs the merged **CoolCatLib** instead of CoolCatLib: Core and
> CoolCatLib: Canvas: remove both old jars when you update.

## 🧭 Versions

Each Minecraft version lives on its own branch. Discontinued versions get no more updates or fixes.

| Minecraft | Loaders | Branch | Status | Latest |
|---|---|---|:-:|---|
| **26.1.2** | Fabric, NeoForge | [`26.1.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/26.1.2) | ✅ Supported | 26.1.2-7 |
| **1.21.1** | Fabric, NeoForge | [`1.21.1`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.21.1) | ✅ Supported | 2101.2.0.6 |
| **1.20.1** | Fabric, Forge | [`1.20.1`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.20.1) | ✅ Supported | 2001.2.0.6 |
| 1.21 | Fabric, NeoForge | [`1.21`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.21) | Discontinued | – |
| 1.19.2 | Fabric, Forge | [`1.19.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.19.2) | Discontinued | – |
| 1.18.2 | Fabric, Forge | [`1.18.2`](https://github.com/ixDarkLorD/UltimineAddition/tree/1.18.2) | Discontinued | – |

## 🔌 For pack makers and mod authors

Data packs can add or change challenges, Mining Skill Card types for other tools, Ultimine shapes and rewards, and
function tags or KubeJS scripts can react to a player's progress. Server settings sync to every player and apply live.

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

The jars end up in each loader's `build/libs`. CoolCatLib comes from the
[ModResources Maven](https://ixdarklord.github.io/ModResources/), and the build scripts and dependency versions from
[ModResources](https://github.com/ixDarkLorD/ModResources).

## 🐞 Issues and ideas

Found a bug or have an idea? Open an [issue](https://github.com/ixDarkLorD/UltimineAddition/issues) with the bug report
or feature request template, or drop by the [Discord](https://discord.gg/ZapGEPqm2V).

## 📄 License

FTB Ultimine Addition is under the [MIT License](LICENSE).
