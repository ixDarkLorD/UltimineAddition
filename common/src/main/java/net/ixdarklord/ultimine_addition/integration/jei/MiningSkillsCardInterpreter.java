package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.ixdarklord.ultimine_addition.api.UAApi;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MiningSkillsCardInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    public static void init(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_PICKAXE, new MiningSkillsCardInterpreter());
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_AXE, new MiningSkillsCardInterpreter());
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_SHOVEL, new MiningSkillsCardInterpreter());
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_HOE, new MiningSkillsCardInterpreter());

        for (MiningSkillCardItem.Type type : UAApi.getTypes()) {
            ResourceLocation location = new ResourceLocation(Constants.MOD_ID + ":mining_skill_card_" + new ResourceLocation(type.name()).getPath());
            Item item = Registration.ITEMS.getRegistrar().get(location);
            registration.registerSubtypeInterpreter(item, new MiningSkillsCardInterpreter());
        }
    }

    @Override
    public @NotNull String apply(ItemStack ingredient, UidContext context) {
        if (!ingredient.hasTag()) return IIngredientSubtypeInterpreter.NONE;
        StringBuilder builder = new StringBuilder(ingredient.getItem().getDescriptionId());
        var data = new MiningSkillCardData().loadData(ingredient);
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
