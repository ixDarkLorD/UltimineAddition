package net.ixdarklord.ultimine_addition.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MinerCertificatePacket extends BaseS2CMessage {
    private final int slotIndex;
    private final ItemStack backupStack;
    private final MinerCertificateData data;

    public MinerCertificatePacket(int slotIndex, ItemStack backupStack, MinerCertificateData data) {
        this.slotIndex = slotIndex;
        this.data = data;
        this.backupStack = backupStack;
    }

    public MinerCertificatePacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readItem(), MinerCertificateData.STREAM_CODEC.decode(buf));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.slotIndex);
        buf.writeItem(this.backupStack);
        MinerCertificateData.STREAM_CODEC.encode(buf, this.data);
    }

    public MessageType getType() {
        return PacketHandler.SYNC_MINER_CERTIFICATE;
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            SlotAccess slot = player.getSlot(this.slotIndex);
            ItemStack stack = slot.get();
            if (stack.isEmpty()) {
                this.data.setStack(this.backupStack).onClientUpdate();
            } else {
                this.data.setStack(stack).onClientUpdate().save();
            }
        });
    }
}
