package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.config.Config;
import net.ixdarklord.coolcatcore.api.config.ConfigValue;
import net.ixdarklord.coolcatcore.api.event.v2.common.ServerLifecycleEvents;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Migration: brings the settings of the earlier 1.20.1 releases (2001.1.5.x, Forge / Forge Config API Port TOML files) over
 * to the CoolCatLib configs, once:
 * <ul>
 *     <li>{@code config/ultimine_addition/client-config.toml} into the client config, when {@code ultimine_addition-client.toml}
 *     doesn't exist yet.</li>
 *     <li>{@code config/ultimine_addition/common-config.toml} (the playstyle mode) and
 *     {@code config/ultimine_addition/server-config.toml} (Forge Config API Port on Fabric kept the server config in the
 *     global config folder) into the server config, when {@code ultimine_addition-server.toml} doesn't exist yet.
 *     Without an old server config there, a modpack's {@code defaultconfigs/ultimine_addition/server-config.toml} (the
 *     template the old one would have been made from) is imported instead and left as it is (it belongs to the pack).</li>
 *     <li>{@code <world>/serverconfig/ultimine_addition/server-config.toml} (where Forge kept it, per world) when that
 *     world starts, unless the global old server config was imported: that one was in use, so it wins and the world's
 *     copy is only renamed to {@code .bak}.</li>
 * </ul>
 * Only values changed from the old defaults are taken, so untouched settings get the new defaults (fewer challenges
 * per tier, debug loggers off). Imported files are renamed to {@code .bak}. Settings that no longer exist (the progress
 * bar mode, the card renderer, the villager trade level and price, the Skills Record's background color) are dropped.
 */
public final class LegacyConfigImporter {
    private static final String OLD_FOLDER = FTBUltimineAddition.MOD_ID;
    private static final String CLIENT_FILE = "client-config.toml";
    private static final String COMMON_FILE = "common-config.toml";
    private static final String SERVER_FILE = "server-config.toml";

    // Old "Section.key" -> new path, with the old default (skipped) and how the old value converts (null: by the new
    // value's type).
    private record Mapping(String oldKey, String newPath, String oldDefault, @Nullable Function<String, Object> convert) {}

    private static final List<Mapping> CLIENT = List.of(
            new Mapping("Settings.sr_edit_mode", "debug.skills_record_edit_mode", "false", null),
            new Mapping("Settings.Visuals.shape_selector_filter", "visuals.shape_selector_filter", "ALL", null),
            new Mapping("Settings.Visuals.text_screen_shadow", "skills_record.text_shadow", "true", null),
            new Mapping("Settings.Visuals.animations_mode", "skills_record.animations", "true", null),
            new Mapping("Settings.Visuals.challenges_panel_alignment", "skills_record.challenges_panel_alignment", "LEFT", null));

    // The playstyle mode is the server config's general.playstyle_mode now; the unfinished ONE_TIER_ONLY mode is gone.
    private static final List<Mapping> COMMON = List.of(
            new Mapping("Playstyle.playstyle_mode", "general.playstyle_mode", "MODERN", value -> value.equals("ONE_TIER_ONLY") ? "MODERN" : value));

    private static final List<Mapping> SERVER = new ArrayList<>(List.of(
            // FTB Ultimine 2001 names its shapes without a namespace ("small_tunnel"); the config keeps ids now.
            new Mapping("General.blacklisted_shapes", "general.blacklisted_shapes", "[]", LegacyConfigImporter::shapeIds),
            new Mapping("General.is_placed_by_entity_condition", "general.is_placed_by_entity_condition", "true", null),
            new Mapping("General.challenge_validator", "general.challenge_validator", "2", null),
            new Mapping("Gameplay.paper_consummation_rate", "skills_record.paper_consumption_rate", "0.35", null),
            new Mapping("Gameplay.card_mastered_effect", "mining_skill_cards.mastered_effect", "true", null),
            new Mapping("Gameplay.tier_based_max_blocks", "mining_skill_cards.tier_based_max_blocks", "true", null),
            new Mapping("Debugging.ineligible_blocks_logger", "debugging.ineligible_blocks_logger", "true", null),
            new Mapping("Debugging.challenge_manager_logger", "debugging.challenge_manager_logger", "true", null),
            new Mapping("Debugging.challenge_actions_logger", "debugging.challenge_actions_logger", "true", null)));

    static {
        // "min, max" became two values.
        SERVER.add(new Mapping("Gameplay.legacy_required_amount#0", "legacy.required_amount_min", "64", null));
        SERVER.add(new Mapping("Gameplay.legacy_required_amount#1", "legacy.required_amount_max", "128", null));
        // "Novice=3, Apprentice=2, ..." became a group with a value per tier.
        tiers("card_challenges_amount", "challenges_amount", Map.of("Unlearned", "1", "Novice", "2", "Apprentice", "3", "Adept", "4"));
        tiers("card_potion_points", "potion_points", Map.of("Novice", "3", "Apprentice", "2", "Adept", "1"));
        tiers("card_potion_durations", "potion_durations", Map.of("Novice", "300", "Apprentice", "600", "Adept", "1200"));
        tiers("card_max_blocks", "max_blocks", Map.of("Novice", "8", "Apprentice", "16", "Adept", "32"));
    }

    private static Object shapeIds(String raw) {
        return readStringList(raw).stream().map(name -> name.contains(":") ? name : "ftbultimine:" + name).toList();
    }

    private static void tiers(String oldKey, String newGroup, Map<String, String> oldDefaults) {
        oldDefaults.forEach((tier, value) -> SERVER.add(new Mapping("Gameplay." + oldKey + "@" + tier,
                "mining_skill_cards." + newGroup + "." + tier.toLowerCase(Locale.ROOT), value, null)));
    }

    private static boolean serverConfigExisted = true;

    private LegacyConfigImporter() {}

    // Before the server config is registered (it's created on registration).
    static void beforeServerConfig() {
        serverConfigExisted = Files.exists(Platform.getConfigFolder().resolve(FTBUltimineAddition.MOD_ID + "-server.toml"));
    }

    // After it's registered and loaded: the playstyle mode from the old common config and the old global server config
    // (or the modpack's default for it), then the old per-world server config of each world as it starts.
    static void afterServerConfig() {
        Path folder = Platform.getConfigFolder().resolve(OLD_FOLDER);
        if (!serverConfigExisted) {
            if (Files.exists(folder.resolve(COMMON_FILE))) importFile(folder.resolve(COMMON_FILE), COMMON, UAServerConfig.CONFIG, true);
            Path defaults = Platform.getGameFolder().resolve("defaultconfigs").resolve(OLD_FOLDER).resolve(SERVER_FILE);
            if (Files.exists(folder.resolve(SERVER_FILE))) importFile(folder.resolve(SERVER_FILE), SERVER, UAServerConfig.CONFIG, true);
            else if (Files.exists(defaults)) importFile(defaults, SERVER, UAServerConfig.CONFIG, false);
        }
        ServerLifecycleEvents.STARTING.register(LegacyConfigImporter::importWorld);
    }

    private static void importWorld(MinecraftServer server) {
        Path old = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(OLD_FOLDER).resolve(SERVER_FILE).normalize();
        if (Files.notExists(old)) return;
        // The global old server config was imported (it's kept as .bak): it was the one in use, so it wins.
        if (Files.exists(Platform.getConfigFolder().resolve(OLD_FOLDER).resolve(SERVER_FILE + ".bak"))) {
            try {
                Files.move(old, old.resolveSibling(SERVER_FILE + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                FTBUltimineAddition.LOGGER.info("Not importing {}: the global old server config was imported instead (kept as .bak)", old);
            } catch (IOException e) {
                FTBUltimineAddition.LOGGER.error("Couldn't rename the old config {}", old, e);
            }
            return;
        }
        importFile(old, SERVER, UAServerConfig.CONFIG, true);
    }

    // Client only; the client config is created on registration too, so this runs around it.
    static void registerClient(Runnable register) {
        boolean existed = Files.exists(Platform.getConfigFolder().resolve(FTBUltimineAddition.MOD_ID + "-client.toml"));
        register.run();
        Path old = Platform.getConfigFolder().resolve(OLD_FOLDER).resolve(CLIENT_FILE);
        if (!existed && Files.exists(old)) importFile(old, CLIENT, UAClientConfig.CONFIG, true);
    }

    private static void importFile(Path file, List<Mapping> mappings, Config config, boolean keepAsBackup) {
        Map<String, String> old;
        try {
            old = readToml(Files.readAllLines(file));
        } catch (IOException e) {
            FTBUltimineAddition.LOGGER.error("Couldn't read the old config {}", file, e);
            return;
        }
        int imported = 0;
        for (Mapping mapping : mappings) {
            String raw = oldValue(old, mapping.oldKey());
            if (raw == null || raw.equals(mapping.oldDefault())) continue;
            ConfigValue<?> value = config.find(mapping.newPath()).orElse(null);
            if (value == null) {
                FTBUltimineAddition.LOGGER.warn("Old config setting {} has no place in {} ({})", mapping.oldKey(), config.id(), mapping.newPath());
                continue;
            }
            try {
                Object converted = convert(mapping.convert() != null ? mapping.convert().apply(raw) : raw, value.getDefault());
                if (converted == null) continue;
                set(value, converted);
                imported++;
            } catch (RuntimeException e) {
                FTBUltimineAddition.LOGGER.warn("Skipped old config setting {} = {}: {}", mapping.oldKey(), raw, e.getMessage());
            }
        }
        config.save();
        if (!keepAsBackup) {
            FTBUltimineAddition.LOGGER.info("Imported {} setting(s) from {} into {}", imported, file, config.id());
            return;
        }
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            FTBUltimineAddition.LOGGER.error("Couldn't rename the imported old config {}", file, e);
        }
        FTBUltimineAddition.LOGGER.info("Imported {} setting(s) from the old config {} into {} (kept as .bak)", imported, file, config.id());
    }

    @SuppressWarnings("unchecked")
    private static <T> void set(ConfigValue<T> value, Object converted) {
        value.set((T) converted);
    }

    // The old value for a key: "key#i" is the i-th item of a "a, b" string, "key@Tier" the tier's number in a
    // "Tier=n, ..." string.
    private static @Nullable String oldValue(Map<String, String> old, String key) {
        int index = key.indexOf('#'), tier = key.indexOf('@');
        String raw = old.get(index >= 0 ? key.substring(0, index) : tier >= 0 ? key.substring(0, tier) : key);
        if (raw == null) return null;
        if (index >= 0) {
            String[] parts = unquote(raw).split(",");
            int i = Integer.parseInt(key.substring(index + 1));
            return i < parts.length ? parts[i].trim() : null;
        }
        if (tier >= 0) {
            String name = key.substring(tier + 1);
            for (String part : unquote(raw).split(",")) {
                String[] pair = part.trim().split("=");
                if (pair.length == 2 && pair[0].trim().equals(name)) return pair[1].trim();
            }
            return null;
        }
        return unquote(raw);
    }

    // The old text to the new value's type.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @Nullable Object convert(Object value, Object example) {
        if (!(value instanceof String raw)) return value;
        if (example instanceof Boolean) return Boolean.parseBoolean(raw);
        if (example instanceof Integer) return Integer.parseInt(unquote(raw));
        if (example instanceof Double) return Double.parseDouble(unquote(raw));
        if (example instanceof List) return readStringList(raw);
        if (example instanceof Enum<?> e) return Enum.valueOf((Class) e.getDeclaringClass(), unquote(raw).toUpperCase(Locale.ROOT));
        if (example instanceof String) return unquote(raw);
        return null;
    }

    // --- The small part of TOML the old (NightConfig-written) files use: [sections], key = value, # comments,
    // quoted strings and arrays of strings (possibly over several lines). Keys come back as "Section.Sub.key". ---

    private static Map<String, String> readToml(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        String section = "";
        StringBuilder pending = null;
        String pendingKey = null;
        for (String line : lines) {
            String text = stripComment(line).trim();
            if (pending != null) {
                pending.append(' ').append(text);
                if (balanced(pending)) {
                    values.put(pendingKey, pending.toString().trim());
                    pending = null;
                }
                continue;
            }
            if (text.isEmpty()) continue;
            if (text.startsWith("[") && text.endsWith("]") && !text.contains("=")) {
                section = text.substring(1, text.length() - 1).trim().replace("\"", "");
                continue;
            }
            int equals = text.indexOf('=');
            if (equals < 0) continue;
            String key = text.substring(0, equals).trim().replace("\"", "");
            String value = text.substring(equals + 1).trim();
            String fullKey = section.isEmpty() ? key : section + "." + key;
            if (value.startsWith("[") && !balanced(new StringBuilder(value))) {
                pending = new StringBuilder(value);
                pendingKey = fullKey;
            } else {
                values.put(fullKey, value);
            }
        }
        return values;
    }

    private static boolean balanced(CharSequence text) {
        int depth = 0;
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) quoted = !quoted;
            else if (!quoted && c == '[') depth++;
            else if (!quoted && c == ']') depth--;
        }
        return depth <= 0;
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) quoted = !quoted;
            else if (c == '#' && !quoted) return line.substring(0, i);
        }
        return line;
    }

    private static String unquote(String value) {
        String text = value.trim();
        if (text.length() >= 2 && (text.startsWith("\"") && text.endsWith("\"") || text.startsWith("'") && text.endsWith("'"))) {
            text = text.substring(1, text.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return text;
    }

    private static List<String> readStringList(String value) {
        String text = value.trim();
        if (text.startsWith("[")) text = text.substring(1);
        if (text.endsWith("]")) text = text.substring(0, text.length() - 1);
        List<String> list = new ArrayList<>();
        for (String part : text.split(",")) {
            if (!part.isBlank()) list.add(unquote(part));
        }
        return list;
    }
}
