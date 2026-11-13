package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.utils.ItemCollector;
import net.ixdarklord.ultimine_addition.common.undo.UltimineInProgress;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Every drop of an Ultimine operation, as it goes into FTB Ultimine's collector. The collector's own storage changes
// between FTB Ultimine versions (a list until 26.1.2.6, merged counts since 26.1.2.7), so it isn't read directly.
@Mixin(ItemCollector.class)
public abstract class ItemCollectorMixin {
    @Inject(method = "accept(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void UA$collect(ItemStack stack, CallbackInfo ci) {
        if ((Object) this == UltimineInProgress.collector && !stack.isEmpty()) {
            UltimineInProgress.collected.add(stack.copy());
        }
    }
}
