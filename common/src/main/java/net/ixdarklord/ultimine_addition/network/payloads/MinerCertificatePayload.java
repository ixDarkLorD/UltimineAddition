package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record MinerCertificatePayload(int slotIndex, ItemStack backupStack, MinerCertificateData data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MinerCertificatePayload> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.id("miner_certificate_sync"));
    public static final StreamCodec<FriendlyByteBuf, MinerCertificatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MinerCertificatePayload::slotIndex,
            ByteBufCodecs.ITEM_STACK, MinerCertificatePayload::backupStack,
            MinerCertificateData.STREAM_CODEC, MinerCertificatePayload::data,
            MinerCertificatePayload::new
    );

    public static void handle(MinerCertificatePayload message, PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            ItemStack stack = ItemUtils.getSlotItem(player, message.slotIndex);
            if (stack.isEmpty()) {
                message.data.setStack(message.backupStack).onClientUpdate();
            } else {
                message.data.setStack(stack).onClientUpdate().save();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
