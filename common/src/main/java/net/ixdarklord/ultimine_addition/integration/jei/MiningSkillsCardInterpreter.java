package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

// JEI 15 (1.20.1) subtype interpreters return one string per subtype.
public class MiningSkillsCardInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    public static void init(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_PICKAXE, new MiningSkillsCardInterpreter());
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_AXE, new MiningSkillsCardInterpreter());
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_SHOVEL, new MiningSkillsCardInterpreter());
        registration.registerSubtypeInterpreter(ModItems.MINING_SKILL_CARD_HOE, new MiningSkillsCardInterpreter());

        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            ResourceLocation location = new ResourceLocation(FTBUltimineAddition.MOD_ID + ":mining_skill_card_" + type.getId());
            Item item = BuiltInRegistries.ITEM.get(location);
            if (item == Items.AIR) continue;
            registration.registerSubtypeInterpreter(item, new MiningSkillsCardInterpreter());
        }
    }

    @Override
    public @NotNull String apply(ItemStack stack, UidContext uidContext) {
        if (!MiningSkillCardData.DATA_COMPONENT.has(stack)) return "";
        StringBuilder builder = new StringBuilder(stack.getItem().getDescriptionId());
        var data = MiningSkillCardData.load(stack);
        switch (data.getTier()) {
            case Unlearned -> builder.append(".unlearned");
            case Novice -> builder.append(".novice");
            case Apprentice -> builder.append(".apprentice");
            case Adept -> builder.append(".adept");
            case Mastered -> builder.append(".mastered");
        }
        return builder.toString();
    }
}
