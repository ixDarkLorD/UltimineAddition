package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.data.recipe.forge.RecipeSerializers;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ServicePlatformImpl {
    public static void registerConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT.SPEC, UltimineAddition.MOD_ID + "/client-config.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON.SPEC, UltimineAddition.MOD_ID + "/common-config.toml");
    }

    public static Supplier<? extends RecipeSerializer<?>> getItemStorageDataSerializer() {
        return RecipeSerializers.ItemStorageDataRecipeSerializer::new;
    }

    public static Supplier<? extends RecipeSerializer<?>> getMCRecipeSerializer() {
        return RecipeSerializers.MCRecipeSerializer::new;
    }
}
