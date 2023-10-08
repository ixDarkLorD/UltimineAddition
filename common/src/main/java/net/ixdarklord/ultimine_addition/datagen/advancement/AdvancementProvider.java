package net.ixdarklord.ultimine_addition.datagen.advancement;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AdvancementProvider extends net.minecraft.data.advancements.AdvancementProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator.PathProvider pathProvider;
    public AdvancementProvider(DataGenerator dataGenerator) {
        super(dataGenerator);
        this.pathProvider = dataGenerator.createPathProvider(DataGenerator.Target.DATA_PACK, "advancements");
    }

    @Override
    public void run(CachedOutput cachedOutput) {
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path = this.pathProvider.json(advancement.getId());
                try {
                    DataProvider.saveStable(cachedOutput, advancement.deconstruct().serializeToJson(), path);
                } catch (IOException var6) {
                    LOGGER.error("Couldn't save advancement {}", path, var6);
                }

            }
        };
        this.registerAdvancements(consumer);
    }

    protected void registerAdvancements(Consumer<Advancement> consumer) {}
}
