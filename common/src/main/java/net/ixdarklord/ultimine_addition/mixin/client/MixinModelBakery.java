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

@Mixin(value = ModelBakery.class, priority = 1600)
public abstract class MixinModelBakery {
    @Final
    @Shadow
    private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Final
    @Shadow
    private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Shadow
    public abstract UnbakedModel getModel(ResourceLocation modelLocation);

    @Inject(method = "loadTopLevel", at = @At("HEAD"))
    private void loadTopLevel(ModelResourceLocation location, CallbackInfo ci) {
        ModelHandler.register();
        ModelHandler.CUSTOM_MODELS.forEach((resourceLocation) -> {
            UnbakedModel unbakedModel = this.getModel(resourceLocation);
            this.unbakedCache.put(resourceLocation, unbakedModel);
            this.topLevelModels.put(resourceLocation, unbakedModel);
        });
    }
}
