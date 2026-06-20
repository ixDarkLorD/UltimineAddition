package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.registry.ReloadListenerRegistry;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.SyncChallengesPacket;
import net.minecraft.server.packs.PackType;

public final class ChallengesEvents {
    public static void init() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, ChallengesManager.INSTANCE);
        DatapackEvents.TAG_UPDATE.register((registryAccess, updateCause, shouldUpdateStaticData) -> {
            if (updateCause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD) {
                ChallengesManager.INSTANCE.validateAllChallenges();
            }

        });
        DatapackEvents.SYNC.register((player, isJoined) -> {
            if (!ChallengesManager.INSTANCE.getAllChallenges().isEmpty()) {
                PacketHandler.sendToPlayer(new SyncChallengesPacket(ChallengesManager.INSTANCE.getAllChallenges()), player);
            }

        });
    }
}
