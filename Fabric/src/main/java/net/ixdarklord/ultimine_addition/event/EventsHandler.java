package net.ixdarklord.ultimine_addition.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.ixdarklord.ultimine_addition.command.SetCapabilityCommand;
import net.ixdarklord.ultimine_addition.data.DataHandler;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class EventsHandler {
    public static void register() {
        onCommandRegister();
        onWorldJoin();
        playerTickEvent();
        blockBreakEvent();
    }

    private static void onCommandRegister() {
        CommandRegistrationCallback.EVENT.register(SetCapabilityCommand::register);
    }

    private static void onWorldJoin() {
        ServerWorldEvents.LOAD.register((server, world) ->
                DataHandler.initialize());
    }

    private static void playerTickEvent() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            assert Minecraft.getInstance().player != null;
            ServerPlayer player = server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID());
            MinerCertificate.checkingBlockInFront(player);
        });
    }

    private static void blockBreakEvent() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) ->
                MinerCertificate.onBreakBlock(player));
    }
}
