package net.ixdarklord.ultimine_addition.datagen.model;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, UltimineAddition.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.INK_CHAMBER);
        handheldItem(ModItems.PEN);
        simpleItem(ModItems.MINER_CERTIFICATE)
                .override()
                .predicate(UltimineAddition.getLocation("opened"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINER_CERTIFICATE + "_opened")))
                .end();
        simpleItem(ModItems.MINER_CERTIFICATE, "_opened", true);

        simpleItem(ModItems.SKILLS_RECORD);
        simpleItem(ModItems.CARD_BLUEPRINT);
        simpleItem(ModItems.MINING_SKILL_CARD_EMPTY);
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_1")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_2")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_3")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_PICKAXE + "_mastered")))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_1", true);
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_2", true);
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_3", true);
        simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered", true);

        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_1")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_2")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_3")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_AXE + "_mastered")))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_1", true);
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_2", true);
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_3", true);
        simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_mastered", true);

        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_1")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_2")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_3")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_SHOVEL + "_mastered")))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_1", true);
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_2", true);
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_3", true);
        simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered", true);

        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_unlearned", false)
                .override()
                .predicate(UltimineAddition.getLocation("is_custom_renderer"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/custom_renderer/mining_skill_card")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_1"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_1")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_2"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_2")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_3"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_3")))
                .end()
                .override()
                .predicate(UltimineAddition.getLocation("tier_maxed"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(UltimineAddition.getLocation("item/" + ModItems.MINING_SKILL_CARD_HOE + "_mastered")))
                .end();
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_1", true);
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_2", true);
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_3", true);
        simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_mastered", true);

        specialRendererItem("item/custom_renderer/", UltimineAddition.getLocation("mining_skill_card")).guiLight(BlockModel.GuiLight.FRONT);
        simpleItem("item/custom_renderer/", UltimineAddition.getLocation("mining_skill_card"), "_unlearned", true);
        simpleItem("item/custom_renderer/", UltimineAddition.getLocation("mining_skill_card"), "_1", true);
        simpleItem("item/custom_renderer/", UltimineAddition.getLocation("mining_skill_card"), "_2", true);
        simpleItem("item/custom_renderer/", UltimineAddition.getLocation("mining_skill_card"), "_3", true);
        simpleItem("item/custom_renderer/", UltimineAddition.getLocation("mining_skill_card"), "_mastered", true);
    }

    private ItemModelBuilder simpleItem(Item item) {
        return simpleItem("item/", Registration.ITEMS.getRegistrar().getId(item), "", false);
    }

    private ItemModelBuilder simpleItem(Item item, String value, boolean isAnotherModel) {
        return simpleItem("item/", Registration.ITEMS.getRegistrar().getId(item), value, isAnotherModel);
    }

    private ItemModelBuilder simpleItem(String folder, ResourceLocation location, String value, boolean isAnotherModel) {
        if (isAnotherModel) {
            return withExistingParent(folder + location.getPath() + value,
                    new ResourceLocation("item/generated")).texture("layer0",
                    new ResourceLocation(UltimineAddition.MOD_ID, folder + location.getPath() + value));
        }

        return withExistingParent(folder + location.getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(UltimineAddition.MOD_ID, folder + location.getPath() + value));

    }

    @SuppressWarnings("UnusedReturnValue")
    private ItemModelBuilder handheldItem(Item item) {
        ResourceLocation location = Registration.ITEMS.getRegistrar().getId(item);
        assert location != null;
        return withExistingParent(location.getPath(),
                new ResourceLocation("item/handheld")).texture("layer0",
                new ResourceLocation(UltimineAddition.MOD_ID, "item/" + location.getPath()));
    }

    private ItemModelBuilder specialRendererItem(String folder, ResourceLocation location) {
        return getBuilder(folder + location.getPath()).parent(new ModelFile.UncheckedModelFile(new ResourceLocation("builtin/entity")));
    }

    @Override
    public @NotNull String getName() {
        return String.format("%s %s", UltimineAddition.MOD_NAME, "Item Models");
    }
}
