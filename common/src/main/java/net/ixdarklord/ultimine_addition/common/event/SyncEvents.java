package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ConfigLifecycleEvent;
import net.ixdarklord.ultimine_addition.common.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.common.network.payloads.PlayerAbilityPayload;
import net.ixdarklord.ultimine_addition.common.network.payloads.config.SyncPlaystyleModePayload;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.server.level.ServerPlayer;

public class SyncEvents {
    public static void init() {
        EntityEvent.ADD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                PayloadHandler.sendToPlayer(new PlayerAbilityPayload(ServicePlatform.get().players().isPlayerUltimineCapable(player)), player);
            }
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_JOIN.register(serverPlayer ->
                PayloadHandler.sendToPlayer(new SyncPlaystyleModePayload(ConfigHandler.COMMON.PLAYSTYLE_MODE.get()), serverPlayer));

        ConfigLifecycleEvent.EVENT.register((configInfo, updateType) -> {
            if (!configInfo.modId().equals(FTBUltimineAddition.MOD_ID) || updateType == ConfigLifecycleEvent.ConfigUpdateType.UNLOADING)
                return;

            if (configInfo.spec() == ConfigHandler.COMMON.SPEC) {
                PayloadHandler.sendToPlayers(new SyncPlaystyleModePayload(ConfigHandler.COMMON.PLAYSTYLE_MODE.get()));
            }
        });
    }
}
