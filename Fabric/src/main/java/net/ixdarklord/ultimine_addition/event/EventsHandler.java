package net.ixdarklord.ultimine_addition.event;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ixdarklord.ultimine_addition.command.SetCapabilityCommand;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineData;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.ixdarklord.ultimine_addition.util.PlayerUtils;

public class EventsHandler {
    public static void register() {
        onCommandRegister();
        onWorldJoin();
        onPlayerDeath();
        onBlockBreak();
    }

    private static void onCommandRegister() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                SetCapabilityCommand.register(dispatcher));
    }

    private static void onWorldJoin() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PlayerUtils.CapabilityData.sync((IDataHandler) handler.player));
    }

    private static void onPlayerDeath() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PlayerUltimineData original = ((IDataHandler) oldPlayer).getPlayerUltimineData();
            PlayerUltimineData clone = ((IDataHandler) newPlayer).getPlayerUltimineData();
            clone.copyFrom(original);
            PlayerUtils.CapabilityData.sync((IDataHandler) newPlayer);
        });
    }

    private static void onBlockBreak() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide()) {
                MinerCertificate.onBreakBlock(state, player);
            }
        });
    }
}
