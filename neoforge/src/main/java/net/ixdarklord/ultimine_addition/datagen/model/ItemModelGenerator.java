package net.ixdarklord.ultimine_addition.datagen.model;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UltimineAddition.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.INK_CHAMBER);
        handheldItem(ModItems.PEN);
        simpleItem(ModItems.MINER_CERTIFICATE)
                .override()
                .predicate(UltimineAddition.getLocation("opened"), 1.0f)
                .model(getItemModelName(ModItems.MINER_CERTIFICATE, "_opened"))
                .end();
        simpleItem(ModItems.MINER_CERTIFICATE, "_opened");

        handheldItem(ModItems.SKILLS_RECORD);
        simpleItem(ModItems.CARD_BLUEPRINT);
        simpleItem(ModItems.MINING_SKILL_CARD_EMPTY);
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_1"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_2"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_3"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_1");
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_2");
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_3");
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered");

        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_1"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_2"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_3"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_1");
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_2");
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_3");
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_mastered");

        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_1"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_2"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_3"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_1");
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_2");
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_3");
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered");

        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_1"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_2"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_3"))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_mastered"))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_1");
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_2");
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_3");
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_mastered");

        specialRendererItem(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card"));
        simpleItem(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card"), "_unlearned");
        simpleItem(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card"), "_1");
        simpleItem(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card"), "_2");
        simpleItem(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card"), "_3");
        simpleItem(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card"), "_mastered");
    }

    private ModelFile.UncheckedModelFile getItemModelName(Item item, String suffix) {
        return new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(item)).getPath() + suffix));
    }
}
