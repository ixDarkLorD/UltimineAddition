package net.ixdarklord.ultimine_addition.datagen.model;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.jetbrains.annotations.NotNull;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.INK_CHAMBER);
        handheldItem(ModItems.PEN);
        simpleItem(ModItems.MINER_CERTIFICATE)
                .override()
                .predicate(Constants.getLocation("opened"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINER_CERTIFICATE + "_opened"))
                .end();
        simpleItem(ModItems.MINER_CERTIFICATE, "_opened").addStringToFileName();

        handheldItem(ModItems.SKILLS_RECORD);
        simpleItem(ModItems.CARD_BLUEPRINT);
        simpleItem(ModItems.MINING_SKILL_CARD_EMPTY);
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_unlearned")
                .override()
                .predicate(Constants.getLocation("is_custom_renderer"), 1.0f)
                .model(Constants.getLocation("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_1"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_1"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_2"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_2"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_3"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_3"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_maxed"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered").addStringToFileName();

        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_unlearned")
                .override()
                .predicate(Constants.getLocation("is_custom_renderer"), 1.0f)
                .model(Constants.getLocation("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_1"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_1"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_2"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_2"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_3"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_3"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_maxed"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_mastered").addStringToFileName();

        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_unlearned")
                .override()
                .predicate(Constants.getLocation("is_custom_renderer"), 1.0f)
                .model(Constants.getLocation("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_1"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_1"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_2"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_2"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_3"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_3"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_maxed"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered").addStringToFileName();

        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_unlearned")
                .override()
                .predicate(Constants.getLocation("is_custom_renderer"), 1.0f)
                .model(Constants.getLocation("item/custom_renderer/mining_skill_card"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_1"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_1"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_2"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_2"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_3"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_3"))
                .end()
                .override()
                .predicate(Constants.getLocation("tier_maxed"), 1.0f)
                .model(Constants.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_1").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_2").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_3").addStringToFileName();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_mastered").addStringToFileName();

        specialRendererItem(Constants.getLocation("custom_renderer/mining_skill_card")).guiLight(BlockModel.GuiLight.FRONT);
        simpleItem(Constants.getLocation("custom_renderer/mining_skill_card"), "_unlearned").addStringToFileName();
        simpleItem(Constants.getLocation("custom_renderer/mining_skill_card"), "_1").addStringToFileName();
        simpleItem(Constants.getLocation("custom_renderer/mining_skill_card"), "_2").addStringToFileName();
        simpleItem(Constants.getLocation("custom_renderer/mining_skill_card"), "_3").addStringToFileName();
        simpleItem(Constants.getLocation("custom_renderer/mining_skill_card"), "_mastered").addStringToFileName();
    }

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", Constants.MOD_NAME, "Item Models");
    }
}
