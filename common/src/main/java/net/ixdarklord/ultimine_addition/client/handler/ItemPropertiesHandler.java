package net.ixdarklord.ultimine_addition.client.handler;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public class ItemPropertiesHandler {
    public static void register() {
        ItemPropertiesRegistry.register(BuiltInRegistries.ITEM.get(Registration.MINER_CERTIFICATE.getId()), ResourceLocation.fromNamespaceAndPath(FTBUltimineAddition.MOD_ID, "opened"), (stack, level, living, id) -> MinerCertificateItem.isAccomplished(stack) ? 1.0F : 0.0F);
        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
            Item item = BuiltInRegistries.ITEM.get(type.getRegistryId());
            ItemPropertiesRegistry.register(item, ResourceLocation.fromNamespaceAndPath(FTBUltimineAddition.MOD_ID, "is_custom_renderer"), (stack, level, living, id) -> ConfigHandler.CLIENT.MSC_RENDERER.get() ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, ResourceLocation.fromNamespaceAndPath(FTBUltimineAddition.MOD_ID, "tier_1"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Novice) ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, ResourceLocation.fromNamespaceAndPath(FTBUltimineAddition.MOD_ID, "tier_2"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Apprentice) ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, ResourceLocation.fromNamespaceAndPath(FTBUltimineAddition.MOD_ID, "tier_3"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Adept) ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, ResourceLocation.fromNamespaceAndPath(FTBUltimineAddition.MOD_ID, "tier_maxed"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Mastered) ? 1.0F : 0.0F);
        }
    }
}
