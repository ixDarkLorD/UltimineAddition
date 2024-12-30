package net.ixdarklord.ultimine_addition.mixin;

import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BrewingStandBlockEntity.class)
abstract class BrewingStandBlockEntityMixin {

    @Redirect(method = "doBrew", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void onItemStackShrink(ItemStack stack, int i) {
        if (stack.getItem() instanceof MiningSkillCardItem item) {
            item.getData(stack).consumePotionPoint(1).saveData(stack);
        } else stack.shrink(i);
    }
}
