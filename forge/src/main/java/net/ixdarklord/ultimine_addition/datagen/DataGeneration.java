package net.ixdarklord.ultimine_addition.datagen;

import net.ixdarklord.coolcatlib.api.datagen.ForgeLanguageWrapper;
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
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = FTBUltimineAddition.MOD_ID, bus = Bus.MOD)
public class DataGeneration {
   @SubscribeEvent
   public static void onDataGeneration(GatherDataEvent event) {
      DataGenerator generator = event.getGenerator();
      PackOutput output = generator.getPackOutput();
      ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
      CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
      generator.addProvider(event.includeServer(), new AdvancementGenerator(output, lookupProvider));
      BlockTagGenerator blockTagGenerator = generator.addProvider(event.includeServer(), new BlockTagGenerator(output, lookupProvider, existingFileHelper));
      generator.addProvider(event.includeServer(), new ItemTagGenerator(output, lookupProvider, blockTagGenerator.contentsGetter(), existingFileHelper));
      generator.addProvider(event.includeServer(), new ChallengeGenerator(output, lookupProvider));
      generator.addProvider(event.includeServer(), new RecipeGenerator(output));
      generator.addProvider(event.includeClient(), new ForgeLanguageWrapper(output, FTBUltimineAddition.MOD_ID, "en_us", new LanguageGenerator()));
      generator.addProvider(event.includeClient(), new ItemModelGenerator(output, existingFileHelper));
      generator.addProvider(event.includeClient(), new ParticleGenerator(output));
   }
}
