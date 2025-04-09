package net.ixdarklord.ultimine_addition.network.payloads;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record MinerCertificatePayload(int slotIndex, MinerCertificateData data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MinerCertificatePayload> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.rl("miner_certificate_sync_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinerCertificatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MinerCertificatePayload::slotIndex,
            MinerCertificateData.STREAM_CODEC, MinerCertificatePayload::data,
            MinerCertificatePayload::new
    );

    public static void handle(MinerCertificatePayload message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            SlotAccess slot = player.getSlot(message.slotIndex);
            ItemStack stack = slot.get();
            if (stack.isEmpty()) return;
            message.data.saveData(stack);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
