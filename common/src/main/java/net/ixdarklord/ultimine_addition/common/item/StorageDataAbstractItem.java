package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.ultimine_addition.common.data.item.ItemStorageData;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public abstract class StorageDataAbstractItem extends DataAbstractItem<ItemStorageData> {
    protected final String storageName;
    protected final int maxCapacity;
    public StorageDataAbstractItem(Properties properties, String storageName, int maxCapacity, ComponentType componentType) {
        super(properties, componentType);
        this.storageName = storageName;
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        return !getData(itemStack).isFull();
    }
    @Override
    public int getBarWidth(ItemStack itemStack) {
        var data = getData(itemStack);
        return Math.round((float) data.getCapacity() / data.getMaxCapacity() * 13.0F);
    }
    @Override
    public int getBarColor(ItemStack itemStack) {
        return Mth.hsvToRgb(Math.max(0.0F, (getBarWidth(itemStack) / 13.0F)) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public ItemStorageData getData(ItemStack stack) {
        return new ItemStorageData(this.storageName, this.maxCapacity).loadData(stack);
    }
}
