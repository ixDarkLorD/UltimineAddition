package net.ixdarklord.ultimine_addition.mixin;

import dev.ftb.mods.ftbultimine.ItemCollection;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// FTB Ultimine 2001 collects an operation's drops in an ItemCollection (ItemCollector on newer versions).
@Mixin(value = ItemCollection.class, remap = false)
public interface ItemCollectorAccessor {
    @Accessor("items")
    List<ItemStack> ua$getItems();
}
