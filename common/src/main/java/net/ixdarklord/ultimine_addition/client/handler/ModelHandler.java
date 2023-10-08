package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;

public class ModelHandler {
    public static final Collection<ModelResourceLocation> CUSTOM_MODELS = new HashSet<>();

    public static final ModelResourceLocation UNLEARNED_ID = registerCustomModel(new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID, "custom_renderer/mining_skill_card_unlearned"), "inventory"));
    public static final ModelResourceLocation TIER_1_ID = registerCustomModel(new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID, "custom_renderer/mining_skill_card_1"), "inventory"));
    public static final ModelResourceLocation TIER_2_ID = registerCustomModel(new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID, "custom_renderer/mining_skill_card_2"), "inventory"));
    public static final ModelResourceLocation TIER_3_ID = registerCustomModel(new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID, "custom_renderer/mining_skill_card_3"), "inventory"));
    public static final ModelResourceLocation MASTERED_ID = registerCustomModel(new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID, "custom_renderer/mining_skill_card_mastered"), "inventory"));
    
    public static void register() {}

    public static ModelResourceLocation registerCustomModel(ModelResourceLocation modelResourceLocation) {
        CUSTOM_MODELS.add(modelResourceLocation);
        return modelResourceLocation;
    }
}
