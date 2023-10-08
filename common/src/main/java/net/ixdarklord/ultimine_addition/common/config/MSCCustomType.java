package net.ixdarklord.ultimine_addition.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MSCCustomType {
    public static List<MSCCustomType> TYPES;
    private String id;
    private String displayItem;
    private List<String> requiredTools;

    public String getId() {
        return id;
    }

    public String getDisplayItem() {
        return displayItem;
    }

    public List<String> getRequiredTools() {
        return requiredTools;
    }

    public static void registerConfig() {
        try {
            TYPES = Serializer.readFrom(String.format("%s/%s/custom_types", Platform.getConfigFolder(), Constants.MOD_ID));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Item> utilizeRequiredTools() {
        List<Item> list = new ArrayList<>();
        if (requiredTools == null) return new ArrayList<>();
        for (String value : requiredTools) {
            if (value.startsWith("#")) {
                List<Item> blocks = new ArrayList<>();
                Registry.ITEM.getTag(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(value.replaceAll("#", "")))).ifPresent(holders ->
                        blocks.addAll(holders.stream().map(Holder::value).toList()));
                if (!blocks.isEmpty()) list.addAll(blocks);
            } else {
                Item item = Registry.ITEM.get(new ResourceLocation(value));
                if (item != Items.AIR) list.add(item);
            }
        }
        return list;
    }

    public static class Serializer {
        private static List<MSCCustomType> readFrom(String directoryPath) throws IOException {
            List<MSCCustomType> items = new ArrayList<>();
            Gson gson = new GsonBuilder().create();
            Set<String> uniqueIds = new HashSet<>();

            File directory = new File(directoryPath);

            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + directoryPath);
            }

            if (directory.isDirectory()) {
                File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));

                if (jsonFiles != null) {
                    for (File jsonFile : jsonFiles) {
                        try (FileReader reader = new FileReader(jsonFile)) {
                            MSCCustomType type = gson.fromJson(reader, MSCCustomType.class);

                            if (uniqueIds.contains(type.getId())) {
                                throw new RuntimeException("Duplicate ID found: " + type.getId());
                            }

                            if (type.getId().isEmpty() || type.getDisplayItem().isEmpty() || type.getDisplayItem().equals("minecraft:air")) {
                                throw new RuntimeException("Invalid data for type: " + type.getId());
                            }

                            if (type.getRequiredTools().isEmpty()) {
                                throw new RuntimeException("Required tools are empty for type: " + type.getId());
                            }

                            uniqueIds.add(type.getId());
                            items.add(type);
                        }
                    }
                }
            }
            return items;
        }
    }
}

