package net.ixdarklord.ultimine_addition.common.data.item;

import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemStorageData extends DataHandler<ItemStorageData, ItemStack> {
    private final String storageName;
    private ItemStack stack;
    private int maxCapacity;
    private int capacity;
    public ItemStorageData(String storageName, int maxCapacity) {
        this.storageName = storageName;
        this.maxCapacity = maxCapacity;
    }

    public ItemStorageData(String storageName) {
        this.storageName = storageName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public ItemStorageData fullCapacity() {
        this.capacity = this.maxCapacity;
        return this;
    }

    public ItemStorageData setCapacity(int amount) {
        this.capacity = Math.min(amount, this.maxCapacity);
        return this;
    }

    public ItemStorageData removeAmount(int amount) {
        this.capacity -= Math.max(0, amount);
        return this;
    }

    @Override
    public ItemStack get() {
        return stack;
    }

    public boolean isFull() {
        return this.capacity >= this.maxCapacity;
    }

    @Override
    public void saveData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        NBT.putInt("max_" + this.storageName, this.maxCapacity);
        NBT.putInt(this.storageName, this.capacity);
        stack.getOrCreateTag().put(this.NBTBase, NBT);
        super.saveData(stack);
    }

    @Override
    public ItemStorageData loadData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        this.maxCapacity = this.maxCapacity == 0 ? NBT.getInt("max_" + this.storageName) == 0 ? 500 : NBT.getInt("max_" + this.storageName) : this.maxCapacity;
        this.capacity = NBT.getInt(this.storageName);
        this.stack = stack;
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.storageName);
        buf.writeInt(this.maxCapacity);
        buf.writeItem(this.stack);
    }

    public static ItemStorageData fromNetwork(FriendlyByteBuf buf) {
        return new ItemStorageData(buf.readUtf(), buf.readInt()).loadData(buf.readItem());
    }
}
