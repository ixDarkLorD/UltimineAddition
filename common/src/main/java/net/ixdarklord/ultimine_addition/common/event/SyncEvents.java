package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ConfigLifecycleEvent;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.PlayerAbilityPacket;
import net.ixdarklord.ultimine_addition.common.network.packet.config.SyncPlaystyleModeConfig;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.server.level.ServerPlayer;

public class SyncEvents {
    public static void init() {
        EntityEvent.ADD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(new PlayerAbilityPacket(ServicePlatform.get().players().isPlayerUltimineCapable(player)), player);
            }
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_JOIN.register(serverPlayer ->
                PacketHandler.sendToPlayer(new SyncPlaystyleModeConfig(ConfigHandler.COMMON.PLAYSTYLE_MODE.get()), serverPlayer));

        ConfigLifecycleEvent.EVENT.register((configInfo, updateType) -> {
            if (!configInfo.modId().equals(UltimineAddition.MOD_ID) || updateType == ConfigLifecycleEvent.ConfigUpdateType.UNLOADING)
                return;

            if (configInfo.spec() == ConfigHandler.COMMON.SPEC) {
                PacketHandler.sendToPlayers(new SyncPlaystyleModeConfig(ConfigHandler.COMMON.PLAYSTYLE_MODE.get()));
            }
        });
    }
}
