package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.ultimine_addition.common.data.shape.DataShapesManager;
import net.ixdarklord.ultimine_addition.network.payloads.SyncCardTypesPayload;
import net.ixdarklord.ultimine_addition.network.payloads.SyncShapesPayload;
import net.ixdarklord.coolcatcore.api.registry.ReloadListeners;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.SyncChallengesPayload;

public class ChallengesEvents {
    public static void init() {
        ReloadListeners.registerServer(FTBUltimineAddition.id("challenges"), ChallengesManager.INSTANCE);
        ReloadListeners.registerServer(FTBUltimineAddition.id("ultimine_shapes"), DataShapesManager.INSTANCE);

        DatapackEvents.TAG_UPDATE.register((registryAccess, updateCause, shouldUpdateStaticData) -> {
            if (updateCause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD) {
                ChallengesManager.INSTANCE.validateAllChallenges();
            }
        });

        DatapackEvents.SYNC.register((player, isJoined) -> {
            // Before the challenges, which name card types. Always sent: an empty list clears another server's.
            PayloadHandler.sendToPlayer(SyncCardTypesPayload.current(), player);
            // FTB Ultimine registers a server's own shapes when it starts, after the data packs load: put the data
            // pack shapes back at the end of the list, where the client has them too.
            DataShapesManager.INSTANCE.install();
            // Always sent: an empty list clears the shapes of a server left before.
            PayloadHandler.sendToPlayer(SyncShapesPayload.of(DataShapesManager.INSTANCE.getShapes()), player);
            if (!ChallengesManager.INSTANCE.getAllChallenges().isEmpty()) {
                PayloadHandler.sendToPlayer(new SyncChallengesPayload(ChallengesManager.INSTANCE.getAllChallenges()), player);
            }
        });
    }
}
