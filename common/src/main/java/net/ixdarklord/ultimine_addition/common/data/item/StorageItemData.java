package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.ultimine_addition.common.item.StorageItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class StorageItemData extends ItemDataComponent<StorageItemData> {
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("item_storage_data");
    public static final Codec<StorageItemData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.STRING.fieldOf("StorageName").forGetter(StorageItemData::getStorageName), Codec.INT.fieldOf("Capacity").forGetter(StorageItemData::getCapacity), Codec.INT.fieldOf("MaxCapacity").forGetter(StorageItemData::getMaxCapacity)).apply(instance, StorageItemData::new));
    private final String storageName;
    private int capacity;
    private final int maxCapacity;

    private StorageItemData(String storageName, int maxCapacity) {
        this(storageName, 0, maxCapacity);
    }

    private StorageItemData(String storageName, int capacity, int maxCapacity) {
        super(DATA_ID, CODEC);
        this.storageName = storageName;
        this.capacity = capacity;
        this.maxCapacity = maxCapacity;
    }

    private static StorageItemData create(String storageName, int maxCapacity) {
        return new StorageItemData(storageName, maxCapacity);
    }

    public static StorageItemData load(String storageName, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof StorageItem storageItem) {
            CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
            StorageItemData data = tag.isEmpty() ? create(storageName, storageItem.getInitialMaxCapacity()) : CodecUtils.decode(CODEC, tag);
            return data.setStack(stack);
        } else {
            throw new IllegalArgumentException("The item provided is not StorageItem!");
        }
    }

    public static boolean hasData(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof StorageItem) {
            CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
            return !tag.isEmpty();
        } else {
            return false;
        }
    }

    public String getStorageName() {
        return this.storageName;
    }

    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public StorageItemData setToFullCapacity() {
        this.capacity = this.maxCapacity;
        return this;
    }

    public StorageItemData setCapacity(int amount) {
        this.capacity = Math.min(amount, this.maxCapacity);
        return this;
    }

    public StorageItemData removeAmount(int amount) {
        this.capacity -= Math.max(0, amount);
        return this;
    }

    public boolean isFull() {
        return this.capacity < this.maxCapacity;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof StorageItemData data)) {
            return false;
        } else {
            return this.capacity == data.capacity && this.maxCapacity == data.maxCapacity && Objects.equals(this.storageName, data.storageName);
        }
    }

    public int hashCode() {
        return Objects.hash(this.storageName, this.capacity, this.maxCapacity);
    }
}
