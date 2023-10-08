package net.ixdarklord.ultimine_addition.mixin.client;

import net.ixdarklord.ultimine_addition.client.handler.ModelHandler;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static net.ixdarklord.ultimine_addition.client.handler.ModelHandler.CUSTOM_MODELS;

@Mixin(value = ModelBakery.class, priority = 1600)
public abstract class ModelBakeryMixin {
    @Shadow public abstract UnbakedModel getModel(ResourceLocation resourceLocation);
    @Final @Shadow private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Final @Shadow private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Inject(method = "loadTopLevel", at = @At(value = "HEAD"))
    private void loadTopLevel(ModelResourceLocation modelResourceLocation, CallbackInfo ci) {
        ModelHandler.register();
        CUSTOM_MODELS.forEach(resourceLocation -> {
            UnbakedModel unbakedModel = getModel(resourceLocation);
            unbakedCache.put(resourceLocation, unbakedModel);
            topLevelModels.put(resourceLocation, unbakedModel);
        });
    }
}
