# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

FTB Ultimine Addition (`modid = ultimine_addition`, package `net.ixdarklord.ultimine_addition`): an add-on for FTB Ultimine. Players have to unlock Ultimine by completing challenges (Mining Skill Cards, Skills Record, Miner Certificate, Mine-Go Juice potions). It is a multi-loader mod (Fabric + NeoForge) built with Architectury.

**Ported from Minecraft 1.21.1 to 26.1.2** on the `26.1.2` branch (created from `1.21.1`, commit `266a641`). It builds and runs on both loaders; in-game client visuals still need checking by hand (see "Rendering" below).

Sibling checkouts (one folder per MC version, each on its version branch of the same GitHub repo):
- `../FTBUltimineAddition-1.21.1`: the 1.21.1 source, the reference for how things behaved before the port.
- `../CoolCatLib-26.1.2`: the author's own library (`net.ixdarklord.coolcatlib`, on its `26.1.2` branch), a hard dependency. If this mod needs a CoolCatLib API that changed or is missing, fix it there and republish. Don't add a workaround here. CoolCatLib API changes made during its 26.1.2 port that affect this mod:
  - `ComponentItem.isShiftButtonNotPressed` takes a `Consumer<Component>`, not a `List`, to match the new `Item.appendHoverText(stack, context, TooltipDisplay, Consumer<Component>, flag)`.
  - `ComponentItem.appendToName()` still works, but it is now applied from each loader's client tooltip event, because `appendHoverText` can no longer edit the name line.
  - `RenderUtils.TRANSLUCENT_TRANSPARENCY` was removed (`RenderStateShard` no longer exists). `TextScreen` was removed.
  - `AbstractMultiPanelWidget` was rewritten as a stack of `widgets/panel/Panel`s (with `ViewportPanel` for pan/zoom and `ScrollPanel`). In 26.1 a stratum draws all text after all fills, so a panel stacked over another starts a new stratum (`graphics.nextStratum()`).
  - `RenderUtils.textColor(int)` restores the pre-26.1 rule that a text color without alpha bits is opaque. 26.1 silently skips text whose color has alpha 0, so raw RGB literals (`0xFFFFFF`, `TextColor.getValue()`) passed to `GuiGraphicsExtractor.text` must be wrapped (`ARGB.opaque` / `RenderUtils.textColor`).

For looking up 26.1.2 vanilla APIs, decompile Minecraft with `./gradlew :common:genSourcesWithVineflower`, then unzip the resulting `*-sources.jar` from `.gradle/loom-cache/minecraftMaven/`. Key renames from 1.21.1:
- `ResourceLocation` → `Identifier`, `net.minecraft.Util` → `net.minecraft.util.Util`, `FastColor` → `ARGB`, `ResourceKey.location()` → `identifier()`.
- `GuiGraphics` → `GuiGraphicsExtractor`, and `render*` → `extract*` (e.g. `Renderable.extractRenderState`, `AbstractWidget.extractContents`).
- In GUI drawing: `drawString` → `text`, `blit`/`blitSprite` now take a `RenderPipeline` first (`RenderPipelines.GUI_TEXTURED`), `setColor` is gone (pass an ARGB tint instead), and `pose()` is a 2D `Matrix3x2fStack` with no z.
- GUI input handlers take `MouseButtonEvent`/`KeyEvent`/`CharacterEvent`.
- `Screen.hasShiftDown()` → `Minecraft.getInstance().hasShiftDown()`.
- `Registry.get(id)` returns `Optional<Holder.Reference>` (use `getValue`).
- `Ingredient` is a holder set (`items()`), so it can no longer match stack components.

Reference docs for the port: Fabric docs (docs.fabricmc.net), NeoForge docs and the version-by-version porting primers (docs.neoforged.net), and Architectury docs (docs.architectury.dev). The jump crosses every change from 1.21.2 through 26.1, so go through the primers one version at a time.

## Build system

The Gradle logic is **not in this repo**. Each `build.gradle` / `settings.gradle` applies remote scripts from `https://raw.githubusercontent.com/ixDarkLorD/ModResources/main/gradle/<vN>/…`, and dependency versions come from the shared catalog `net.ixdarklord:sharedcatalogs:${dependencies_catalog_version}` (hosted in the same ModResources repo under `maven/`). Read those scripts to see what a task does. They set up the run configs, the resource-template expansion of `fabric.mod.json`/`neoforge.mods.toml`/`pack.mcmeta` with `${…}` properties, the mixin-config renaming, and the shadow/jar layout.

### 1.21.1 → 26.1.2 build differences (already migrated; keep in mind when comparing with the 1.21.1 checkout)

| | 1.21.1 | 26.1.2 |
|---|---|---|
| Remote scripts | `gradle/v1/*.gradle`, `gradle/v2/settings.gradle` | `gradle/v3/*.gradle` (settings too) |
| Root plugin alias | `libs.plugins.architectury.loom` | `libs.plugins.architectury.loom.noremap` |
| Mappings | Mojang + Parchment, remapped | none. 26.1 ships unobfuscated, so no remap/refmap |
| Dependency configs | `modApi`, `modImplementation`, `modCompileOnly`, `modRuntimeOnly`, `modCompileOnlyApi`, `modLocalRuntime` | `api`, `implementation`, `compileOnly`, `runtimeOnly`, `compileOnlyApi`, `localRuntime` |
| `dependencies_catalog_version` | `1.21.1-v9` | `26.1.2-v1` |
| Java | 21 | 25 (from catalog `java`) |
| Gradle wrapper | 8.14.4 | 9.4.0 |
| Access widener header | `accessWidener v2 named` | `accessWidener v2 official` |
| Output jar | `remapJar` of shadowJar | `shadowJar` (classifier-less) built on top of the `raw` jar |

Other consequences of the unobfuscated setup:
- The common mixin config lives in the repo as `ultimine_addition.common.mixins.json`. The v1 scripts renamed `common.mixins.json` at build time, but v3 doesn't.
- `fabric.mod.json` `loom:injected_interfaces` keys use Mojang class names (`net/minecraft/world/entity/player/Player`), not intermediary `class_XXXX`.
- Access-widener entries must name classes and members that exist in 26.1.2 (`validateAccessWidener` fails the build otherwise).
- **NeoForge ignores access wideners in production** (it only reads access transformers, and nothing converts the AW anymore now that there's no `remapJar`). `neoforge/src/main/resources/META-INF/accesstransformer.cfg` mirrors the AW and is declared in `neoforge.mods.toml`; keep the two in sync. Fabric-only entries (the item-model `ID_MAPPER`s) don't need an AT.

**Shadow override (local, in `settings.gradle` + root `build.gradle`).** The shared v3 scripts pin `com.github.johnrengelman.shadow` 8.1.1, which breaks on Gradle 9 ("Could not add META-INF to ZIP … No such property: mode"). `settings.gradle` remaps that plugin to `com.gradleup.shadow:shadow-gradle-plugin:8.3.11`. The root `build.gradle` also applies `com.gradleup.shadow` to subprojects before `main.gradle`, because the old ID is only a marker in GradleUp's Shadow. CoolCatLib has the same override. Drop both once ModResources' v3 scripts move to GradleUp Shadow.

**`pack.mcmeta`.** Pack formats above 81 must declare `min_format`/`max_format`; `pack_format` alone is rejected. A mod pack serves both assets (resource format) and data (data format), so it declares `min_format: ${resource_pack_format}` and `max_format: ${data_pack_format}`.

**Dev runs.** NeoForge's dev `runServer` skips the EULA check and starts a full world. Fabric's stops at the EULA check unless `run/server/eula.txt` exists. NeoForge 26.1 split datagen into client/server runs, so `neoforge/build.gradle` switches the shared `data` run to `clientData()` (the client run generates everything); it may keep running after "finished" lines appear and can be stopped. Trinkets is `localRuntime` on Fabric because it injects interfaces into vanilla classes, so dev runs crash without it.

`mod_version` is a plain build number, like CoolCatLib's (e.g. `1`); the build scripts prefix the Minecraft version, so jars and maven artifacts are `<mc>-<build>` (`ultimine_addition-fabric-26.1.2-1.jar`), similar to FTB Ultimine's `26.1.2.5`. The 1.21.1 branch used the older `MCVR.X.X.X` scheme (`2101.x.x.x`).

### Dependencies in 26.1.2

- **CoolCatLib**: not on a public maven. Publish it locally from `../CoolCatLib-26.1.2`:
  ```bash
  ./gradlew :common:publishToMavenLocal :fabric:publishToMavenLocal :neoforge:publishToMavenLocal
  ```
  It publishes as `net.ixdarklord.coolcatlib:coolcatlib-<platform>:<mc>-<CoolCatLib mod_version>`, and this repo's `coolcatlib_version` must equal CoolCatLib's `mod_version` (currently `1`). Loom resolves dependencies while configuring the project, so if CoolCatLib is missing, *every* Gradle task in this repo fails.
- **FTB Ultimine `26.1.2.5` / FTB Library `26.1.2.8`** come from `maven.ftb.dev` (common, fabric and neoforge artifacts).
- **Catalog changes**: Trinkets is `libs.trinkets` (`eu.pb4:trinkets`). EMI and Parchment are gone. The 1.21.1 Fabric dev dependency on Adapaxels (curse.maven) was dropped.

## Commands

Run from the repo root. Use `./gradlew` in Git Bash, `.\gradlew.bat` in PowerShell. There are no tests or linters. Verify a change by compiling and running the game.

```bash
./gradlew build
```
Builds all loaders. Jars go to `fabric/build/libs` and `neoforge/build/libs` (`copy_build_jar=true` also copies them out).

```bash
./gradlew :fabric:compileJava
```
Quickest compile check for one loader (use `:common:compileJava` for common only).

```bash
./gradlew :neoforge:runClient
```
Other run tasks per loader: `runServer` (runs in `run/server`), `runData`, and `runDevClient` (a client with `--username Dev`, defined in each platform's `build.gradle`).

```bash
./gradlew :fabric:runData
```
Regenerates `src/generated/resources` for that loader. Datagen runs **per platform** (entry points in `<loader>/src/main/java/.../datagen`), and the generated output is committed. Run it on both loaders after changing recipes, tags, models, advancements, or language. Item models and `items/*.json` item definitions come from the shared `common/.../datagen/model/ItemModelDataProvider`. During 26.1 datagen, item components aren't bound, so providers must not create `ItemStack`s (use `ItemStackTemplate` / `item.getDescriptionId()`); `MCRecipe`/`ItemStorageDataRecipe` results are `ItemStackTemplate`s for that reason.

The dev run directory is `../run`, relative to each subproject, which means the repo-root `run/` folder.

## Architecture

**Module split.** `common/` holds nearly all the logic and compiles against vanilla, Architectury and FTB Ultimine. `fabric/` and `neoforge/` hold the entrypoints, datagen, slot-mod integration and loader-specific hooks. The `common` jar is shaded into each loader jar.

**Platform abstraction (`@ExpectPlatform`).** Common interfaces with a static `@ExpectPlatform` method are implemented in `<same package>.fabric` / `.neoforge` as `<Name>Impl`:
- `core.ServicePlatform`: config registration, `SlotAPI` (Trinkets on Fabric, Curios on NeoForge, for the Skills Record slot), `Players` (Ultimine capability storage, tool checks, paxel detection).
- `common.tag.PlatformTags`, `hooks.KeyBindingHooks`.
When you change one of these interfaces, update both Impl classes.

**Lifecycle.** `FabricSetup` / `@Mod NeoForgeSetup` → `CommonSetup.init()` (registration, `CustomMSCApi`, client init through `EnvExecutor`) → `CommonSetup.setup()` on common setup (payload registration etc.). The loader setup classes also bridge loader events (tool modification, datapack reload/sync, tags updated, config reload) into the mod's own events in `common/event/impl` (`BlockToolModificationEvent`, `DatapackEvents`, `ConfigLifecycleEvent`), which the common handlers in `common/event` listen to.

**Registration.** All content is registered in `core.Registration` using Architectury `DeferredRegister`s. Item instances live in `ModItems` / `ModMobEffects`, and the suppliers just return them. Custom Mining Skill Card types added by other mods (`api.CustomMSCApi`, `IUAPlugin`) register extra items, effects and potions in loops. Command argument types go through CoolCatLib's `ArgumentTypeRegistry`.

**Player ability data.** Whether a player may use Ultimine is stored differently per loader. NeoForge uses a data attachment (`NeoForgeSetup.PLAYER_ABILITY_DATA`) and Fabric uses a mixin on `Player` (`MixinPlayerData`). Both go through `ServicePlatform.Players`.

**Item data.** Item state uses data components (`common/data/item/*Data`, registered in `Registration`), with codecs and stream codecs, mostly built on CoolCatLib's `ItemDataComponent`/`DataComponent`.

**Skills Record storage.** The item only carries `SkillsRecordLink` (the record's UUID, registered under the old `skills_record_data` component id; its codec still reads the pre-SavedData layout so `SkillsRecordSavedData.resolve` can migrate old items). Contents/settings live in the server-wide `SkillsRecordSavedData` (`common/data/record`), together with each card's `CardHistory` keyed by card UUID. Get a record with `SkillsRecordData.get(stack, level)` on the server (links/migrates the stack) or `SkillsRecordData.getClient(stack)` on the client. `SkillsRecordData.save()` on the server records card history (by diffing the card against the last observation) and bumps the record's version; `SkillsRecordSync` pushes changed records to players carrying them every tick, and `SkillsRecordClientCache` holds the client copies (unknown records are requested on demand). The menu works directly on the record's live container.

**Mining Skill Card storage.** The card's item component (`MiningSkillCardData`) holds only its UUID, tier and display item, which rendering, recipes and JEI need anywhere. Its challenges and potion points (`CardProgress`) live in `SkillsRecordSavedData` next to its `CardHistory` (one `CardEntry` per card UUID; the history fields stay inline, as older saves have them). `MiningSkillCardData` keeps its API and reaches the progress through `CardStore`, which picks the side by thread: server thread → the SavedData, otherwise → `SkillsRecordClientCache`. Reading never creates entries: a card is stored when the server first saves it or a player carries it (so crafting previews stay out of the file); until then it uses a temporary local copy that becomes the stored progress. Use `writeComponent()` instead of `save()` for stacks that may not become real cards (recipe results, ingredient displays). Old items still carrying `Challenges`/`PotionPoints` are migrated when first stored. `SkillsRecordSync` pushes carried cards (`SyncCards`, snapshots with the just-completed challenges for the toasts); others are requested on demand (`RequestCard`).

**Card viewer.** `client/gui/components/cardviewer` replaces the old `TextScreen`: a `CardViewerWidget` (CoolCatLib `AbstractMultiPanelWidget`) stacking a pan/zoom `TierTreePanel` (`ViewportPanel`), status `MessagePanel`s and a modal `ChallengeDetailsPanel`. `CardTree` builds the layout from the card and its history. 

**Networking.** Payloads are in `network/payloads`. They are registered and sent through Architectury `NetworkManager` in `PayloadHandler` (FTB Library 26.1 has its own networking layer with a different handler type, so its `NetworkHelper` is no longer used).

**Challenges.** Challenges are datapack-driven: `ChallengesManager` loads them and `ChallengeData` defines them. The built-in ones come from `datagen/challenge`. `IneligibleBlocksSavedData` tracks player-placed blocks so they don't count toward challenges (a `SavedDataType` wrapping the 1.21.1 NBT layout).

**Villager trade.** Trades are data-driven in 26.1: the Mining Skill Card trade is `data/ultimine_addition/villager_trade/mining_skill_card.json`, added to the `minecraft:toolsmith/level_1` villager-trade tag (static files in `common/src/main/resources`). The `card_trade_level`/`card_trade_price` config options no longer affect it.

**Config.** Configs use the NeoForge config spec (through Forge Config API Port on Fabric), in `config/ConfigHandler` (CLIENT/COMMON/SERVER). `PlaystyleMode` (e.g. `LEGACY`) changes which items and recipes are active. Recipes are gated by a `legacy_mode` condition that is implemented separately per loader.

**FTB Ultimine coupling (the most fragile part).** Common mixins target FTB Ultimine internals: `FTBUltimine` (`handleBlockBreak`, `blockRightClick`, `playerTick`), `FTBUltiminePlayerData`, `FTBUltimineClient`, `ShapeRegistry` (plus `ShapeRegistryAccessor`). `core.FTBUltimineIntegration` holds the glue. Past releases have mostly been "compat with FTB Ultimine X", so compare against the FTB Ultimine sources (`maven.ftb.dev` publishes `-sources.jar`s) whenever a mixin target moves. FTB Ultimine keeps separate client/server `ShapeRegistry` instances with distinct `Shape` objects; the no-arg `FTBUltimineIntegration` shape helpers use the server instance, and the mixins pass their own instance. The restriction handler is registered through FTB's loader-specific events (`FTBUltimineEvents` on Fabric, `FTBUltimineEvent.RegisterRestrictionHandler` on NeoForge).

**Rendering.** Mining Skill Cards use a custom item model type, `ultimine_addition:mining_skill_card` (`client/renderer/item/MiningSkillCardItemModel`): it picks the tier model and, with the client "custom renderer" option, draws the card's display item on top through a special-model layer. Its placement constants still need tuning in-game. The Miner Certificate uses the `ultimine_addition:certificate_opened` model condition. Both are registered per loader (`UAItemModels`: NeoForge events, Fabric via the access-widened vanilla `ID_MAPPER`s). Custom card types from `config/ultimine_addition/custom_cards` need an `items/<id>.json` in a resource pack. GUI code has no z-order anymore: use `guiGraphics.nextStratum()` to draw over items/text drawn earlier.

**Integrations.** JEI (`integration/jei`, common, loaded through the `jei_mod_plugin` entrypoint on Fabric), Trinkets (Fabric, the `eu.pb4` fork) and Curios (NeoForge). The client has no `RecipeManager` anymore, so the JEI category reads recipes through JEI's internal `mezz.jei.common.Internal.getClientSyncedRecipes()` (`common` has `jei-26.1.2-common` as `compileOnly` for that); expect it to break on JEI updates.

## Conventions

- Keep `CHANGELOG.md` up to date. Entries use the format `## v<mc>-<build> Release - <date>` (1.21.1 used `## vX.X.X.X Release - <date>`) with emoji subsections (✨ New Features / 🐛 Bug Fixes & Improvements / ⚙️ Refactoring). It is bundled into the jar.
- Loader-specific code goes under a `.fabric` / `.neoforge` subpackage that mirrors the common package.
