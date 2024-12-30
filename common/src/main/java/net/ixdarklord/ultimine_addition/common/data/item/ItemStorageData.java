package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemStorageData extends DataHandler<ItemStorageData, ItemStack> {
    public static final Codec<ItemStorageData> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStorageData> STREAM_CODEC;
    public static final DataComponentType<ItemStorageData> DATA_COMPONENT;

    private final String storageName;
    private int capacity;
    private final int maxCapacity;

    private ItemStorageData(String storageName, int maxCapacity) {
        this(storageName, 0, maxCapacity);
    }

    private ItemStorageData(String storageName, int capacity, int maxCapacity) {
        this.storageName = storageName;
        this.capacity = capacity;
        this.maxCapacity = maxCapacity;
    }

    public static ItemStorageData create(String storageName, int maxCapacity) {
        return new ItemStorageData(storageName, maxCapacity);
    }

    public static ItemStorageData loadData(String storageName, int maxCapacity, ItemStack stack) {
        return stack.getOrDefault(DATA_COMPONENT, create(storageName, maxCapacity)).setDataHolder(stack);
    }

    @Override
    public void saveData(ItemStack stack) {
        stack.set(DATA_COMPONENT, this);
        super.saveData(stack);
    }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("StorageName").forGetter(ItemStorageData::getStorageName),
                Codec.INT.fieldOf("Capacity").forGetter(ItemStorageData::getCapacity),
                Codec.INT.fieldOf("MaxCapacity").forGetter(ItemStorageData::getMaxCapacity)
        ).apply(instance, ItemStorageData::new));

        STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ItemStorageData::getStorageName,
                ByteBufCodecs.INT, ItemStorageData::getCapacity,
                ByteBufCodecs.INT, ItemStorageData::getMaxCapacity,
                ItemStorageData::new
        );

        DATA_COMPONENT = DataComponentType.<ItemStorageData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();
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

    public boolean isFull() {
        return this.capacity >= this.maxCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStorageData data)) return false;
        return capacity == data.capacity && maxCapacity == data.maxCapacity && Objects.equals(storageName, data.storageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageName, capacity, maxCapacity);
    }
}
