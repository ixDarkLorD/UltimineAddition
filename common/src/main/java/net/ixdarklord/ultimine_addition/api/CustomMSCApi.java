package net.ixdarklord.ultimine_addition.api;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomMSCApi {
    private final static Logger LOGGER = LogManager.getLogger(UltimineAddition.MOD_NAME + "/API/CustomMSC");
    public static final String dir = String.format("%s/%s/custom_cards", Platform.getConfigFolder(), UltimineAddition.MOD_ID);
    public static List<MiningSkillCardItem.Type> CUSTOM_TYPES = new ArrayList<>();

    public static void init() {
        try {
            CUSTOM_TYPES = readFrom();
            MiningSkillCardItem.Type.refreshTypes();
        } catch (IOException err) {
            LOGGER.error(err);
        }
    }

    private static List<MiningSkillCardItem.Type> readFrom() throws IOException {
        List<MiningSkillCardItem.Type> items = new ArrayList<>();
        Set<String> uniqueIds = new HashSet<>();

        File directory = new File(dir);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: .config/" + dir);
        }

        if (directory.isDirectory()) {
            File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));

            if (jsonFiles != null) {
                LOGGER.info("Loading Custom Mining Skill Cards...");
                Stopwatch stopwatch = Stopwatch.createStarted();

                for (File jsonFile : jsonFiles) {
                    try (FileReader reader = new FileReader(jsonFile)) {
                        MiningSkillCardItem.Type type = MiningSkillCardItem.Type.CARD_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result()
                                .orElseThrow(() -> new RuntimeException("Failed to load custom card: " + jsonFile.getAbsolutePath()));

                        if (!type.isActive()) {
                            LOGGER.warn("[Disabled] Skipping custom card: \"{}\"...", type.getId());
                            continue;
                        }

                        if (type.getId().isEmpty() || type.getDefaultDisplayItem() == null || type.getDefaultDisplayItem() == Items.AIR) {
                            throw new RuntimeException("Invalid data for card type: " + type.getId());
                        }

                        if (uniqueIds.contains(type.getId())) {
                            throw new RuntimeException("Duplicate ID found: " + type.getId());
                        }

                        if (type.getRequiredTools().isEmpty()) {
                            throw new RuntimeException("Required tools are empty for card type: " + type.getId());
                        }

                        uniqueIds.add(type.getId());
                        items.add(type);
                        LOGGER.info("[Enabled] Custom card \"{}\" is loaded.", type.getId());
                    }
                }
                if (!items.isEmpty())
                    LOGGER.info("Loaded {} Custom Mining Skill Cards took {}.", items.size(), stopwatch);
            }
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static void printJson() {
        var type = new MiningSkillCardItem.Type(true, "test", List.of(), Items.BARRIER);
        MiningSkillCardItem.Type.CARD_CODEC.encodeStart(JsonOps.INSTANCE, type).result().ifPresent(jsonElement ->
                System.out.println(jsonElement.getAsJsonObject().toString()));
    }
}

