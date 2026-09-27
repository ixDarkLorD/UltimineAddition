package net.ixdarklord.ultimine_addition.client.renderer.item;

import com.mojang.serialization.MapCodec;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public final class UAItemModels {
    public static final Identifier MINING_SKILL_CARD = FTBUltimineAddition.id("mining_skill_card");
    public static final Identifier CERTIFICATE_OPENED = FTBUltimineAddition.id("certificate_opened");

    public static void registerModels(BiConsumer<Identifier, MapCodec<? extends ItemModel.Unbaked>> registry) {
        registry.accept(MINING_SKILL_CARD, MiningSkillCardItemModel.Unbaked.MAP_CODEC);
    }

    public static void registerConditionalProperties(BiConsumer<Identifier, MapCodec<? extends ConditionalItemModelProperty>> registry) {
        registry.accept(CERTIFICATE_OPENED, CertificateOpenedProperty.MAP_CODEC);
    }

    private UAItemModels() {}
}
