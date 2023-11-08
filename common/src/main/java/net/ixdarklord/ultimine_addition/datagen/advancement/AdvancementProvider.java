package net.ixdarklord.ultimine_addition.datagen.advancement;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.ixdarklord.ultimine_addition.datagen.helper.DataGeneratorHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AdvancementProvider extends net.minecraft.data.advancements.AdvancementProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final Path path;

    public AdvancementProvider(DataGenerator generator) {
        super(generator);
        this.path = generator.getOutputFolder();
    }

    @Override
    public void run(HashCache cachedOutput) {
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path = DataGeneratorHelper.createDataPath(this.path, "advancements", advancement.getId());
                try {
                    DataProvider.save(GSON, cachedOutput, advancement.deconstruct().serializeToJson(), path);
                } catch (IOException var6) {
                    LOGGER.error("Couldn't save advancement {}", path, var6);
                }

            }
        };
        this.registerAdvancements(consumer);
    }

    protected void registerAdvancements(Consumer<Advancement> consumer) {}
}
