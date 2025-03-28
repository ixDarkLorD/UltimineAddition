package net.ixdarklord.ultimine_addition.common.network.payloads;

import dev.architectury.networking.NetworkManager;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record MinerCertificatePayload(MinerCertificateData data, ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MinerCertificatePayload> TYPE = new CustomPacketPayload.Type<>(FTBUltimineAddition.rl("miner_certificate_sync_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinerCertificatePayload> STREAM_CODEC = StreamCodec.composite(
            MinerCertificateData.STREAM_CODEC, MinerCertificatePayload::data,
            ItemStack.STREAM_CODEC, MinerCertificatePayload::stack,
            MinerCertificatePayload::new
    );

    public static void handle(MinerCertificatePayload message, NetworkManager.PacketContext context) {
        context.queue(() -> message.data.setDataHolder(message.stack).clientUpdate().saveData(message.stack));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
