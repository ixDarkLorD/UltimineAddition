package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.ixdarklord.coolcatlib.api.hooks.ServerLifecycleHooks;
import net.ixdarklord.ultimine_addition.common.event.impl.ConfigLifecycleEvent;
import net.ixdarklord.ultimine_addition.config.ClientSideConfigPreserver;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.PlayerAbilityPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class SyncEvents {
    public static void init() {
        EntityEvent.ADD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                PayloadHandler.sendToPlayer(new PlayerAbilityPayload(ServicePlatform.get().players().isPlayerUltimineCapable(player)), player);
            }
            return EventResult.pass();
        });


        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.server.isDedicatedServer()) {
                ConfigHandler.COMMON.syncConfigToClient(true, player);
            }
        });

        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player ->
                    ClientSideConfigPreserver.restoreOriginalValues());
        }

        ConfigLifecycleEvent.EVENT.register((configInfo, updateType) -> {
            if (!configInfo.modId().equals(FTBUltimineAddition.MOD_ID) || updateType != ConfigLifecycleEvent.ConfigUpdateType.RELOADING)
                return;

            if (configInfo.spec() == ConfigHandler.COMMON.SPEC) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null && server.isDedicatedServer()) {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        ConfigHandler.COMMON.syncConfigToClient(false, player);
                    }
                }
            }
        });
    }
}
