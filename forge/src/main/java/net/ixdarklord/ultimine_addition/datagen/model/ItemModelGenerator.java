package net.ixdarklord.ultimine_addition.datagen.model;

import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class ItemModelGenerator extends ItemModelProvider {
   public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
      super(output, "ultimine_addition", existingFileHelper);
   }

   protected void registerModels() {
      this.handheldItem(ModItems.INK_CHAMBER);
      this.handheldItem(ModItems.PEN);
      this.simpleItem(ModItems.MINER_CERTIFICATE).override().predicate(FTBUltimineAddition.id("opened"), 1.0F).model(this.getItemModelName(ModItems.MINER_CERTIFICATE, "_opened")).end();
      this.simpleItem(ModItems.MINER_CERTIFICATE, "_opened");
      this.handheldItem(ModItems.SKILLS_RECORD).transforms().transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(1.0F, 4.0F, 0.0F).rotation(-10.0F, -45.0F, 0.0F).scale(0.65F, 0.65F, 0.65F).end().transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(1.0F, 4.0F, 0.0F).rotation(-10.0F, -45.0F, 0.0F).scale(0.65F, 0.65F, 0.65F).end().transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0.0F, 4.0F, 5.0F).rotation(45.0F, -10.0F, 0.0F).scale(0.75F, 0.75F, 0.75F).end().transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0.0F, 4.0F, 5.0F).rotation(45.0F, -10.0F, 0.0F).scale(0.75F, 0.75F, 0.75F).end().end();
      this.handheldItem(ModItems.SHAPE_SELECTOR).transforms().transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(1.0F, 4.0F, 0.0F).rotation(-10.0F, -45.0F, 0.0F).scale(0.65F, 0.65F, 0.65F).end().transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(1.0F, 4.0F, 0.0F).rotation(-10.0F, -45.0F, 0.0F).scale(0.65F, 0.65F, 0.65F).end().transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0.0F, 4.0F, 5.0F).rotation(45.0F, -10.0F, 0.0F).scale(0.75F, 0.75F, 0.75F).end().transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0.0F, 4.0F, 5.0F).rotation(45.0F, -10.0F, 0.0F).scale(0.75F, 0.75F, 0.75F).end().end();
      this.simpleItem(ModItems.CARD_BLUEPRINT);
      this.simpleItem(ModItems.MINING_SKILL_CARD_EMPTY);
      this.simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_unlearned", false).override().predicate(FTBUltimineAddition.id("is_custom_renderer"), 1.0F).model(new ModelFile.UncheckedModelFile(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"))).end().override().predicate(FTBUltimineAddition.id("tier_1"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_1")).end().override().predicate(FTBUltimineAddition.id("tier_2"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_2")).end().override().predicate(FTBUltimineAddition.id("tier_3"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_3")).end().override().predicate(FTBUltimineAddition.id("tier_maxed"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered")).end();
      this.simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_1");
      this.simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_2");
      this.simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_3");
      this.simpleItem(ModItems.MINING_SKILL_CARD_PICKAXE, "_mastered");
      this.simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_unlearned", false).override().predicate(FTBUltimineAddition.id("is_custom_renderer"), 1.0F).model(new ModelFile.UncheckedModelFile(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"))).end().override().predicate(FTBUltimineAddition.id("tier_1"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_1")).end().override().predicate(FTBUltimineAddition.id("tier_2"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_2")).end().override().predicate(FTBUltimineAddition.id("tier_3"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_3")).end().override().predicate(FTBUltimineAddition.id("tier_maxed"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_AXE, "_mastered")).end();
      this.simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_1");
      this.simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_2");
      this.simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_3");
      this.simpleItem(ModItems.MINING_SKILL_CARD_AXE, "_mastered");
      this.simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_unlearned", false).override().predicate(FTBUltimineAddition.id("is_custom_renderer"), 1.0F).model(new ModelFile.UncheckedModelFile(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"))).end().override().predicate(FTBUltimineAddition.id("tier_1"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_1")).end().override().predicate(FTBUltimineAddition.id("tier_2"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_2")).end().override().predicate(FTBUltimineAddition.id("tier_3"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_3")).end().override().predicate(FTBUltimineAddition.id("tier_maxed"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered")).end();
      this.simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_1");
      this.simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_2");
      this.simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_3");
      this.simpleItem(ModItems.MINING_SKILL_CARD_SHOVEL, "_mastered");
      this.simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_unlearned", false).override().predicate(FTBUltimineAddition.id("is_custom_renderer"), 1.0F).model(new ModelFile.UncheckedModelFile(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"))).end().override().predicate(FTBUltimineAddition.id("tier_1"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_1")).end().override().predicate(FTBUltimineAddition.id("tier_2"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_2")).end().override().predicate(FTBUltimineAddition.id("tier_3"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_3")).end().override().predicate(FTBUltimineAddition.id("tier_maxed"), 1.0F).model(this.getItemModelName(ModItems.MINING_SKILL_CARD_HOE, "_mastered")).end();
      this.simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_1");
      this.simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_2");
      this.simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_3");
      this.simpleItem(ModItems.MINING_SKILL_CARD_HOE, "_mastered");
      this.specialRendererItem(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"));
      this.simpleItem(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"), "_unlearned");
      this.simpleItem(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"), "_1");
      this.simpleItem(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"), "_2");
      this.simpleItem(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"), "_3");
      this.simpleItem(FTBUltimineAddition.id("item/custom_renderer/mining_skill_card"), "_mastered");
   }

   private ModelFile.UncheckedModelFile getItemModelName(Item item, String suffix) {
      String string = Objects.requireNonNull(Registration.ITEMS.getRegistrar().getId(item)).getPath();
      return new ModelFile.UncheckedModelFile(FTBUltimineAddition.id("item/" + string + suffix));
   }
}
