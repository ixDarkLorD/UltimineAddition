package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatlib.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.data.item.StorageItemData;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class StorageItem extends DataAbstractItem<StorageItemData> {
    protected final String storageName;
    protected final int initialMaxCapacity;

    public StorageItem(Item.Properties properties, String storageName, int initialMaxCapacity, ComponentItem.ComponentType componentType) {
        super(properties, componentType);
        this.storageName = storageName;
        this.initialMaxCapacity = initialMaxCapacity;
    }

    public boolean isBarVisible(@NotNull ItemStack itemStack) {
        StorageItemData data = this.getData(itemStack);
        return data.isFull();
    }

    public int getBarWidth(@NotNull ItemStack itemStack) {
        StorageItemData data = this.getData(itemStack);
        return Math.round((float) data.getCapacity() / (float) data.getMaxCapacity() * 13.0F);
    }

    public int getBarColor(@NotNull ItemStack itemStack) {
        return Mth.hsvToRgb(Math.max(0.0F, (float) this.getBarWidth(itemStack) / 13.0F) / 3.0F, 1.0F, 1.0F);
    }

    public int getInitialMaxCapacity() {
        return this.initialMaxCapacity;
    }

    public StorageItemData getData(ItemStack stack) {
        return StorageItemData.load(this.storageName, stack);
    }
}
