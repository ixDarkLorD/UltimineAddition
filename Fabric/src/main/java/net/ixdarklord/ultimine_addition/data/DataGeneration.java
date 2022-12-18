package net.ixdarklord.ultimine_addition.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.ixdarklord.ultimine_addition.data.gen.RecipeProvider;

public class DataGeneration implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator DataGenerator) {
        DataGenerator.addProvider(RecipeProvider::new);
    }
}
