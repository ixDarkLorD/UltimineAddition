package net.ixdarklord.ultimine_addition.datagen;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.datagen.advancement.AdvancementGenerator;
import net.ixdarklord.ultimine_addition.datagen.challenge.ChallengeGenerator;
import net.ixdarklord.ultimine_addition.datagen.model.ItemModelGenerator;
import net.ixdarklord.ultimine_addition.datagen.language.LanguageGenerator;
import net.ixdarklord.ultimine_addition.datagen.particle.ParticleGenerator;
import net.ixdarklord.ultimine_addition.datagen.recipe.RecipeGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.BlockTagGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.ItemTagGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new AdvancementGenerator(generator));
        generator.addProvider(event.includeServer(), new BlockTagGenerator(generator, existingFileHelper));
        generator.addProvider(event.includeServer(), new ItemTagGenerator(generator, existingFileHelper));
        generator.addProvider(event.includeServer(), new ChallengeGenerator(generator));
        generator.addProvider(event.includeServer(), new RecipeGenerator(generator));
        generator.addProvider(event.includeServer(), new LanguageGenerator(generator, "en_us"));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(event.includeClient(), new ParticleGenerator(generator));
    }
}
