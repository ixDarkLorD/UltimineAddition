package net.ixdarklord.ultimine_addition.datagen;

import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.advancement.AdvancementGenerator;
import net.ixdarklord.ultimine_addition.datagen.challenge.ChallengeGenerator;
import net.ixdarklord.ultimine_addition.datagen.language.LanguageGenerator;
import net.ixdarklord.ultimine_addition.datagen.model.ItemModelGenerator;
import net.ixdarklord.ultimine_addition.datagen.particle.ParticleGenerator;
import net.ixdarklord.ultimine_addition.datagen.recipe.RecipeGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.BlockTagGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.ItemTagGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = UltimineAddition.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(new AdvancementGenerator(generator));
        generator.addProvider(new BlockTagGenerator(generator, existingFileHelper));
        generator.addProvider(new ItemTagGenerator(generator, existingFileHelper));
        generator.addProvider(new ChallengeGenerator(generator));
        generator.addProvider(new RecipeGenerator(generator));
        generator.addProvider(new LanguageGenerator(generator, "en_us"));
        generator.addProvider(new ItemModelGenerator(generator, existingFileHelper));
        generator.addProvider(new ParticleGenerator(generator));
    }
}
