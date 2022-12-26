package net.ixdarklord.ultimine_addition.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ixdarklord.ultimine_addition.command.SetCapabilityCommand;
import net.ixdarklord.ultimine_addition.data.IDataHandler;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.ixdarklord.ultimine_addition.util.PlayerUtils;

public class EventsHandler {
    public static void register() {
        onCommandRegister();
        onWorldJoin();
        onBlockBreak();
    }

    private static void onCommandRegister() {
        CommandRegistrationCallback.EVENT.register(SetCapabilityCommand::register);
    }

    private static void onWorldJoin() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PlayerUtils.CapabilityData.sync((IDataHandler) handler.player));
    }

    private static void onBlockBreak() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide()) {
                MinerCertificate.onBreakBlock(state, player);
            }
        });
    }
}
