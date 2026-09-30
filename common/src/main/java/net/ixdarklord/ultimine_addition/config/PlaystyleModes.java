package net.ixdarklord.ultimine_addition.config;

import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.client.handler.PlaystyleModeClient;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.LOGGER;

// The playstyle mode is a server setting (UAServerConfig.PLAYSTYLE_MODE) and applies at once. Items always exist and
// check the mode when used; what the mode decides at load time follows it here:
// - the server reloads its data packs, like /reload, so the recipes gated by the legacy_mode condition are re-read;
// - clients rebuild the creative tab, whose contents depend on the mode.
// CoolCatLib syncs the value: a client on a server plays by the server's mode, and gets its own back when it leaves
// (both arrive as value changes, so they land here too).
public final class PlaystyleModes {
    // Where the mode lived before it became a server setting (a startup config, fixed until a restart).
    private static final String OLD_STARTUP_FILE = "ultimine_addition-startup.toml";
    private static final Pattern OLD_MODE = Pattern.compile("(?m)^\\s*playstyle_mode\\s*=\\s*\"?([a-zA-Z_]+)\"?");

    private PlaystyleModes() {}

    public static PlaystyleMode get() {
        return UAServerConfig.PLAYSTYLE_MODE.get();
    }

    public static boolean isLegacy() {
        return get() == PlaystyleMode.LEGACY;
    }

    // While the mod is constructed, once the server config is loaded.
    static void register() {
        importOldStartupConfig();
        UAServerConfig.PLAYSTYLE_MODE.addListener((oldMode, newMode) -> onChanged(newMode));
    }

    private static void onChanged(PlaystyleMode mode) {
        LOGGER.info("Playstyle mode is now {}", mode.name().toLowerCase(Locale.ROOT));
        MinecraftServer server = Platform.getServer();
        if (server != null) server.execute(() -> server.reloadResources(server.getPackRepository().getSelectedIds()));
        Platform.runOnClient(() -> PlaystyleModeClient::onModeChanged);
    }

    // The value set in the old startup config carries over once; the file is kept as .bak. (A mode that no longer
    // exists, like one_tier_only, leaves the default.)
    private static void importOldStartupConfig() {
        Path file = Platform.getConfigFolder().resolve(OLD_STARTUP_FILE);
        if (Files.notExists(file)) return;
        try {
            Matcher matcher = OLD_MODE.matcher(Files.readString(file));
            if (matcher.find()) {
                String name = matcher.group(1).toUpperCase(Locale.ROOT);
                for (PlaystyleMode mode : PlaystyleMode.values()) {
                    if (mode.name().equals(name) && mode != get()) {
                        UAServerConfig.PLAYSTYLE_MODE.set(mode);
                        UAServerConfig.CONFIG.save();
                        LOGGER.info("Moved the playstyle mode ({}) from {} to the server config", name.toLowerCase(Locale.ROOT), OLD_STARTUP_FILE);
                    }
                }
            }
            Files.move(file, file.resolveSibling(OLD_STARTUP_FILE + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Couldn't read the old playstyle mode from {}", file, e);
        }
    }
}
