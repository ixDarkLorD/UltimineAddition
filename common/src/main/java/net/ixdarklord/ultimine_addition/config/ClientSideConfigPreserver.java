package net.ixdarklord.ultimine_addition.config;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public final class ClientSideConfigPreserver {
    private static final Map<List<String>, ConfigValueWrapper<?>> PRESERVED_VALUES = new ConcurrentHashMap<>();
    private static boolean hasPreservedValues = false;

    public static void preserveOriginalValues(Map<List<String>, ConfigValueWrapper<?>> configValues) {
        if (!Platform.getEnv().equals(EnvType.CLIENT)) {
            FTBUltimineAddition.LOGGER.warn("Config preservation attempted on server side!");
            return;
        }

        if (!hasPreservedValues) {
            PRESERVED_VALUES.putAll(configValues);
            hasPreservedValues = true;
            FTBUltimineAddition.LOGGER.info("Preserved client-side common config values ({} entries)",
                    PRESERVED_VALUES.size());
        }
    }

    public static void restoreOriginalValues() {
        if (Platform.getEnv() != EnvType.CLIENT) {
            FTBUltimineAddition.LOGGER.warn("Config restoration attempted on server side!");
            return;
        }

        if (!hasPreservedValues || PRESERVED_VALUES.isEmpty()) {
            FTBUltimineAddition.LOGGER.debug("No preserved config values to restore");
            return;
        }

        FTBUltimineAddition.LOGGER.info("Restoring client-side common config values...");

        PRESERVED_VALUES.forEach((path, wrapper) -> {
            ConfigHandler.applySyncedValues(path, wrapper, true, false);
            FTBUltimineAddition.LOGGER.debug("Restoring config: [{}]", String.join(".", path));
        });

        clearPreservedValues();
        FTBUltimineAddition.LOGGER.info("Config restoration completed");
    }

    public static void clearPreservedValues() {
        PRESERVED_VALUES.clear();
        hasPreservedValues = false;
        FTBUltimineAddition.LOGGER.debug("Cleared preserved config values");
    }

    public static boolean hasPreservedValues() {
        return hasPreservedValues && !PRESERVED_VALUES.isEmpty();
    }
}