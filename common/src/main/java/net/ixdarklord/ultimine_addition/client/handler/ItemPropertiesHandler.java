package net.ixdarklord.ultimine_addition.client.handler;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public class ItemPropertiesHandler {
    public static void register() {
        ItemPropertiesRegistry.register(Registration.MINER_CERTIFICATE.get(), new ResourceLocation(UltimineAddition.MOD_ID, "opened"), (stack, level, living, id) -> MinerCertificateItem.isAccomplished(stack) ? 1.0F : 0.0F);

        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
            Item item = Registration.ITEMS.getRegistrar().get(UltimineAddition.getLocation(String.format("mining_skill_card_%s", type.getId())));
            if (item == null) continue;
            ItemPropertiesRegistry.register(item, new ResourceLocation(UltimineAddition.MOD_ID, "is_custom_renderer"), (stack, level, living, id) -> ConfigHandler.CLIENT.MSC_RENDERER.get() ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, new ResourceLocation(UltimineAddition.MOD_ID, "tier_1"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Novice) ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, new ResourceLocation(UltimineAddition.MOD_ID, "tier_2"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Apprentice) ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, new ResourceLocation(UltimineAddition.MOD_ID, "tier_3"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Adept) ? 1.0F : 0.0F);
            ItemPropertiesRegistry.register(item, new ResourceLocation(UltimineAddition.MOD_ID, "tier_maxed"), (stack, level, living, id) -> MiningSkillCardItem.isTierEqual(stack, MiningSkillCardItem.Tier.Mastered) ? 1.0F : 0.0F);
        }
    }
}
