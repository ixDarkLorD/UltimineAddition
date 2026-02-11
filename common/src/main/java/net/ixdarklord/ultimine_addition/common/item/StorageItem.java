package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public abstract class StorageItem extends DataAbstractItem<StorageItemData> {
    protected final String storageName;
    protected final int maxCapacity;
    public StorageItem(Properties properties, StorageItemData storageData, ComponentType componentType) {
        super(properties, componentType);
        this.storageName = storageData.getStorageName();
        this.maxCapacity = storageData.getMaxCapacity();
    }

    public int getMaxCapacity() {
        return maxCapacity;
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
    public StorageItemData getData(ItemStack stack) {
        return StorageItemData.load(storageName, maxCapacity, stack);
    }
}
