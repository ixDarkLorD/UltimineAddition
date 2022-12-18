package net.ixdarklord.ultimine_addition.data;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.gen.RecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(true, new RecipeProvider(generator));
    }
}
