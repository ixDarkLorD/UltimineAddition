package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.api.util.ItemCollector;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ItemCollector.class)
public interface ItemCollectorAccessor {
    @Accessor("items")
    List<ItemStack> ua$getItems();
}
