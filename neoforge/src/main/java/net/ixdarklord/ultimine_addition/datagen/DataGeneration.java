package net.ixdarklord.ultimine_addition.datagen;

import net.ixdarklord.ultimine_addition.datagen.model.ItemModelDataProvider;
import net.ixdarklord.coolcatcore.api.datagen.NeoForgeLanguageWrapper;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.advancement.AdvancementGenerator;
import net.ixdarklord.ultimine_addition.datagen.challenge.ChallengeGenerator;
import net.ixdarklord.ultimine_addition.datagen.language.LanguageGenerator;
import net.ixdarklord.ultimine_addition.datagen.particle.ParticleGenerator;
import net.ixdarklord.ultimine_addition.datagen.recipe.RecipeGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.BlockTagGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.ItemTagGenerator;
import net.minecraft.core.HolderLookup;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = FTBUltimineAddition.MOD_ID)
public class DataGeneration {
    // The client data run generates everything (assets and data).
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(BlockTagGenerator::new);
        event.createProvider(ItemTagGenerator::new);
        event.createProvider(AdvancementGenerator::new);
        event.createProvider(ChallengeGenerator::new);
        event.createProvider(RecipeGenerator.Runner::new);
        event.createProvider(ItemModelDataProvider::new);
        event.createProvider(output -> new NeoForgeLanguageWrapper(output, FTBUltimineAddition.MOD_ID, "en_us", new LanguageGenerator()));
        event.createProvider(ParticleGenerator::new);
    }
}
