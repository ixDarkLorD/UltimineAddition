package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class MiningSkillsCardInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
   public static void init(ISubtypeRegistration registration) {
      registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_PICKAXE, new MiningSkillsCardInterpreter());
      registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_AXE, new MiningSkillsCardInterpreter());
      registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_SHOVEL, new MiningSkillsCardInterpreter());
      registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_HOE, new MiningSkillsCardInterpreter());

      for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
         ResourceLocation location = new ResourceLocation("ultimine_addition:mining_skill_card_" + type.id());
         Item item = BuiltInRegistries.ITEM.get(location);
         if (item != Items.AIR) {
            registration.registerSubtypeInterpreter(item, new MiningSkillsCardInterpreter());
         }
      }

   }

   public @NotNull String apply(ItemStack ingredient, @NotNull UidContext context) {
      if (!ingredient.hasTag()) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder(ingredient.getItem().getDescriptionId());
         MiningSkillCardData data = MiningSkillCardData.load(ingredient);
         switch (data.getTier()) {
            case Unlearned -> builder.append(":unlearned");
            case Novice -> builder.append(":novice");
            case Apprentice -> builder.append(":apprentice");
            case Adept -> builder.append(":adept");
            case Mastered -> builder.append(":mastered");
         }

         return builder.toString();
      }
   }
}
