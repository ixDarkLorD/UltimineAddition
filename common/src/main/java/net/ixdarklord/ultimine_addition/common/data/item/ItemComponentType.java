package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import net.ixdarklord.coolcatcore.api.data.DataComponent;
import net.ixdarklord.coolcatcore.api.utils.CodecUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// 1.20.1 has no data components: this stands in for the DataComponentTypes of the 26.1.2 version, with the same
// get/set/has/remove calls. The value is kept in the stack's NBT under its id ("ultimine_addition:<name>"), the same
// keys the earlier 1.20.1 releases used, so their items are read as they are.
public record ItemComponentType<T>(ResourceLocation id, Codec<T> codec) {
    public @Nullable T get(ItemStack stack) {
        return DataComponent.load(stack.getTag(), this.id, this.codec).orElse(null);
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        T value = this.get(stack);
        return value != null ? value : fallback;
    }

    public boolean has(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(this.id.toString());
    }

    public void set(ItemStack stack, T value) {
        stack.getOrCreateTag().put(this.id.toString(), CodecUtils.encode(this.codec, value));
    }

    public void remove(ItemStack stack) {
        stack.removeTagKey(this.id.toString());
    }
}
