package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.item.ItemStorageData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;

public class ItemStorageDataPacket extends BaseS2CMessage {
    private final ItemStorageData data;

    public ItemStorageDataPacket(FriendlyByteBuf buf) {
        this(ItemStorageData.fromNetwork(buf));
    }
    public ItemStorageDataPacket(ItemStorageData data) {
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return PacketHandler.SYNC_ITEM_STORAGE_DATA;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.data.toNetwork(buf);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> data.saveData(data.get()));
    }
}
