package net.ixdarklord.ultimine_addition.common.data.item;

import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatcore.api.data.ItemDataComponent;
import net.ixdarklord.ultimine_addition.common.item.StorageItem;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class StorageItemData extends ItemDataComponent<StorageItemData> {
    public static final Codec<StorageItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("StorageName").forGetter(StorageItemData::getStorageName),
            Codec.INT.fieldOf("Capacity").forGetter(StorageItemData::getCapacity),
            Codec.INT.fieldOf("MaxCapacity").forGetter(StorageItemData::getMaxCapacity)
    ).apply(instance, StorageItemData::new));

    public static final StreamCodec<FriendlyByteBuf, StorageItemData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StorageItemData::getStorageName,
            ByteBufCodecs.INT, StorageItemData::getCapacity,
            ByteBufCodecs.INT, StorageItemData::getMaxCapacity,
            StorageItemData::new
    );

    public static final ItemComponentType<StorageItemData> DATA_COMPONENT = new ItemComponentType<>(FTBUltimineAddition.id("item_storage_data"), CODEC);

    private final String storageName;
    private int capacity;
    private final int maxCapacity;

    private StorageItemData(String storageName, int maxCapacity) {
        this(storageName, 0, maxCapacity);
    }

    private StorageItemData(String storageName, int capacity, int maxCapacity) {
        super(DATA_COMPONENT.id(), CODEC);
        this.storageName = storageName;
        this.capacity = capacity;
        this.maxCapacity = maxCapacity;
    }

    public static StorageItemData create(String storageName, int maxCapacity) {
        return new StorageItemData(storageName, maxCapacity);
    }

    public static StorageItemData load(String storageName, int maxCapacity, ItemStack stack) {
        return DATA_COMPONENT.getOrDefault(stack, create(storageName, maxCapacity)).setStack(stack);
    }

    public static boolean hasData(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof StorageItem && DATA_COMPONENT.has(stack);
    }

    public String getStorageName() {
        return storageName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
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
        return this.capacity >= this.maxCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorageItemData data)) return false;
        return capacity == data.capacity && maxCapacity == data.maxCapacity && Objects.equals(storageName, data.storageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageName, capacity, maxCapacity);
    }
}
