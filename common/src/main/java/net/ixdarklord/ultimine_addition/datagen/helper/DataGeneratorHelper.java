package net.ixdarklord.ultimine_addition.datagen.helper;

import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class DataGeneratorHelper {
    public static Path createDataPath(Path path, String folderName, ResourceLocation location) {
        return path.resolve("data/%s/%s/%s.json".formatted(location.getNamespace(), folderName, location.getPath()));
    }
    public static Path createAssetsPath(Path path, String folderName, ResourceLocation location) {
        return path.resolve("assets/%s/%s/%s.json".formatted(location.getNamespace(), folderName, location.getPath()));
    }
}
