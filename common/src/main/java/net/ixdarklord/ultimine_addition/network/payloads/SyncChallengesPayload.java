package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.FriendlyByteBuf;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.ixdarklord.coolcatcore.api.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record SyncChallengesPayload(Map<ResourceLocation, ChallengeData> dataMap) implements CustomPacketPayload {
    public static final Type<SyncChallengesPayload> TYPE = new Type<>(FTBUltimineAddition.id("sync_challenges"));
    private static final StreamCodec<FriendlyByteBuf, Map<ResourceLocation, ChallengeData>> CHALLENGES_STREAM_CODEC =
            ByteBufCodecs.map(i -> new HashMap<>(), ByteBufCodecs.RESOURCE_LOCATION, ChallengeData.STREAM_CODEC);

    public static final StreamCodec<FriendlyByteBuf, SyncChallengesPayload> STREAM_CODEC = StreamCodec.composite(
            CHALLENGES_STREAM_CODEC, SyncChallengesPayload::dataMap,
            SyncChallengesPayload::new);

    public static void handle(SyncChallengesPayload message, PacketContext context) {
        context.queue(() -> ChallengesManager.INSTANCE.setChallenges(message.dataMap));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
