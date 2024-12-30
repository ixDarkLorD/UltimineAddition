package net.ixdarklord.ultimine_addition.datagen.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementProvider extends net.minecraft.data.advancements.AdvancementProvider {
    public AdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, List<AdvancementGenerator> subProviders) {
        super(output, registries, subProviders.stream().map(AdvancementGenerator::toSubProvider).toList());
    }
    public interface AdvancementGenerator {
        void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver);
        default AdvancementSubProvider toSubProvider() {
            return this::generate;
        }
    }
}
