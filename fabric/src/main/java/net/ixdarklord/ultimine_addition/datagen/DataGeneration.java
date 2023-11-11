package net.ixdarklord.ultimine_addition.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.ixdarklord.ultimine_addition.datagen.advancement.AdvancementGenerator;
import net.ixdarklord.ultimine_addition.datagen.challenge.ChallengeGenerator;
import net.ixdarklord.ultimine_addition.datagen.model.ItemModelGenerator;
import net.ixdarklord.ultimine_addition.datagen.particle.ParticleGenerator;
import net.ixdarklord.ultimine_addition.datagen.recipe.RecipeGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.BlockTagGenerator;
import net.ixdarklord.ultimine_addition.datagen.tag.ItemTagGenerator;

@SuppressWarnings("unused")
public class DataGeneration implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        generator.addProvider(ItemTagGenerator::new);
        generator.addProvider(BlockTagGenerator::new);
        generator.addProvider(AdvancementGenerator::new);
        generator.addProvider(ChallengeGenerator::new);
        generator.addProvider(RecipeGenerator::new);
        generator.addProvider(ItemModelGenerator::new);
        generator.addProvider(ParticleGenerator::new);
//        generator.addProvider(fabricDataGenerator -> new LanguageGenerator(fabricDataGenerator, "en_us"));
    }
}
