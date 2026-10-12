package net.ixdarklord.ultimine_addition.network.payloads;

import net.ixdarklord.coolcatcore.api.network.PacketContext;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.common.data.reward.DataRewards;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// What the server's data pack rewards give, for the card viewer's previews. Sent on join and after a data pack reload;
// an empty list clears another server's. Its codec comes from the record (CoolCatLib's PayloadCodecs).
public record SyncRewardsPayload(List<Entry> rewards) implements CustomPacketPayload {
    public static final Type<SyncRewardsPayload> TYPE = new Type<>(FTBUltimineAddition.id("sync_rewards"));

    public record Entry(String when, String challenge, String cardType, int tier, String description, int experience, boolean items, boolean other) {
    }

    public static SyncRewardsPayload current() {
        return new SyncRewardsPayload(DataRewards.previews().stream()
                .map(p -> new Entry(p.when(), p.challenge(), p.cardType(), p.tier(), p.description(), p.experience(), p.items(), p.other()))
                .toList());
    }

    public static void handle(SyncRewardsPayload message, PacketContext context) {
        context.queue(() -> {
            // With a server in this game the list is the one it loaded already.
            if (Platform.getServer() != null) return;
            DataRewards.setPreviews(message.rewards.stream()
                    .map(e -> new DataRewards.Preview(e.when(), e.challenge(), e.cardType(), e.tier(), e.description(), e.experience(), e.items(), e.other()))
                    .toList());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
