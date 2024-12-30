package net.ixdarklord.ultimine_addition.common.network.packet;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record MinerCertificatePacket(MinerCertificateData data, ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MinerCertificatePacket> TYPE = new CustomPacketPayload.Type<>(UltimineAddition.getLocation("miner_certificate_sync_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinerCertificatePacket> STREAM_CODEC = StreamCodec.composite(
            MinerCertificateData.STREAM_CODEC, MinerCertificatePacket::data,
            ItemStack.STREAM_CODEC, MinerCertificatePacket::stack,
            MinerCertificatePacket::new
    );

    public static void handle(MinerCertificatePacket message, NetworkManager.PacketContext context) {
        context.queue(() -> message.data.setDataHolder(message.stack).clientUpdate().saveData(message.stack));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
