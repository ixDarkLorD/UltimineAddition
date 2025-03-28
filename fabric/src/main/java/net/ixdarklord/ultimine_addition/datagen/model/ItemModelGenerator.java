package net.ixdarklord.ultimine_addition.datagen.model;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Objects;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.INK_CHAMBER);
        handheldItem(ModItems.PEN);
        simpleItem(ModItems.MINER_CERTIFICATE)
                .override()
                .predicate(FTBUltimineAddition.rl("opened"), 1.0f)
                .model(getItemModelName(ModItems.MINER_CERTIFICATE, "_opened"))
                .end();
        simpleItem(ModItems.MINER_CERTIFICATE, "_opened").addStringToFileName();

        handheldItem(ModItems.SKILLS_RECORD).transforms()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .translation(1, 4, 0)
                .rotation(-10, -45, 0)
                .scale(.65F, .65F, .65F)
                .end()

                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .translation(1, 4, 0)
                .rotation(-10, -45, 0)
                .scale(.65F, .65F, .65F)
                .end()

                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .translation(0, 4, 5)
                .rotation(45, -10, 0)
                .scale(.75F, .75F, .75F)
                .end()

                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .translation(0, 4, 5)
                .rotation(45, -10, 0)
                .scale(.75F, .75F, .75F)
                .end()
                .end();

        handheldItem(ModItems.SHAPE_SELECTOR).transforms()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .translation(1, 4, 0)
                .rotation(-10, -45, 0)
                .scale(.65F, .65F, .65F)
                .end()

                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .translation(1, 4, 0)
                .rotation(-10, -45, 0)
                .scale(.65F, .65F, .65F)
                .end()

                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .translation(0, 4, 5)
                .rotation(45, -10, 0)
                .scale(.75F, .75F, .75F)
                .end()

                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .translation(0, 4, 5)
                .rotation(45, -10, 0)
                .scale(.75F, .75F, .75F)
                .end()
                .end();

        simpleItem(ModItems.CARD_BLUEPRINT);
        simpleItem(ModItems.MINING_SKILL_CARD_EMPTY);
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_unlearned")
                .override()
                .predicate(FTBUltimineAddition.rl("is_custom_renderer"), 1.0f)
                .model(FTBUltimineAddition.rl("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_1"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_2"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_3"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered").addStringToFileName();

        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_unlearned")
                .override()
                .predicate(FTBUltimineAddition.rl("is_custom_renderer"), 1.0f)
                .model(FTBUltimineAddition.rl("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_1"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_2"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_3"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_mastered").addStringToFileName();

        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_unlearned")
                .override()
                .predicate(FTBUltimineAddition.rl("is_custom_renderer"), 1.0f)
                .model(FTBUltimineAddition.rl("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_1"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_2"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_3"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered").addStringToFileName();

        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_unlearned")
                .override()
                .predicate(FTBUltimineAddition.rl("is_custom_renderer"), 1.0f)
                .model(FTBUltimineAddition.rl("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_1"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_2"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_3"))
                .end()
                .override()
                .predicate(FTBUltimineAddition.rl("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_mastered").addStringToFileName();

        specialRendererItem(FTBUltimineAddition.rl("custom_renderer/mining_skill_card")).guiLight(BlockModel.GuiLight.FRONT);
        simpleItem(FTBUltimineAddition.rl("custom_renderer/mining_skill_card"), "_unlearned").addStringToFileName();
        simpleItem(FTBUltimineAddition.rl("custom_renderer/mining_skill_card"), "_1").addStringToFileName();
        simpleItem(FTBUltimineAddition.rl("custom_renderer/mining_skill_card"), "_2").addStringToFileName();
        simpleItem(FTBUltimineAddition.rl("custom_renderer/mining_skill_card"), "_3").addStringToFileName();
        simpleItem(FTBUltimineAddition.rl("custom_renderer/mining_skill_card"), "_mastered").addStringToFileName();
    }

    private ResourceLocation getItemModelName(Item item, String suffix) {
        return FTBUltimineAddition.rl("item/" + Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(item)).getPath() + suffix);
    }
}
