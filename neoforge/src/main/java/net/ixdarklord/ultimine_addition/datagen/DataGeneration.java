package net.ixdarklord.ultimine_addition.datagen;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.advancement.AdvancementGenerator;
import net.ixdarklord.ultimine_addition.datagen.challenge.ChallengeGenerator;
import net.ixdarklord.ultimine_addition.datagen.language.LanguageGenerator;
import net.ixdarklord.ultimine_addition.datagen.model.ItemModelGenerator;
import net.ixdarklord.ultimine_addition.datagen.particle.ParticleGenerator;
import net.ixdarklord.ultimine_addition.datagen.recipe.RecipeGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.BlockTagGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.ItemTagGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = FTBUltimineAddition.MOD_ID)
public class DataGeneration {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagGenerator blockTagProvider =
                generator.addProvider(event.includeServer(), new BlockTagGenerator(output, lookupProvider, existingFileHelper));

        generator.addProvider(event.includeServer(), new ItemTagGenerator(output, lookupProvider, blockTagProvider.contentsGetter()));
        generator.addProvider(event.includeServer(), new AdvancementGenerator(output, lookupProvider));
        generator.addProvider(event.includeServer(), new ChallengeGenerator(output, lookupProvider));
        generator.addProvider(event.includeServer(), new RecipeGenerator(output, lookupProvider));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new LanguageGenerator(output, "en_us"));
        generator.addProvider(event.includeClient(), new ParticleGenerator(output));
    }
}
