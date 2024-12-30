package net.ixdarklord.ultimine_addition.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;

public class PlayerAbilityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored1, Commands.CommandSelection ignored2) {
        dispatcher.register(UltimineAddition.getCommandPrompt(Commands.LEVEL_GAMEMASTERS)
                .then(Commands.literal("player_ability")
                .then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.literal("set")
                .then(Commands.argument("state", BoolArgumentType.bool()).executes(context ->
                        setAbility(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "state"))
                ))))));
    }

    private static int setAbility(CommandSourceStack source, Collection<ServerPlayer> targets, boolean state) {
        int i = 0;
        String State = String.valueOf(state);
        for (ServerPlayer player : targets) {
            if (ServicePlatform.Players.isPlayerUltimineCapable(player) != state) {
                ServicePlatform.Players.setPlayerUltimineCapability(player, state);
                i++;

                if (player == source.getPlayer()) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.set_ability.success", State).withStyle(ChatFormatting.DARK_AQUA), true);
                }
                if (i > 1 && player != source.getPlayer() && !player.hasPermissions(2)) {
                    player.displayClientMessage(Component.translatable("command.ultimine_addition.set_ability.receiver", State, Objects.requireNonNull(source.getPlayer()).getName().getString()).withStyle(ChatFormatting.GRAY), false);
                }
                if (i > 1) {
                    source.sendSuccess(() -> Component.translatable("command.ultimine_addition.set_ability.sender", State).withStyle(ChatFormatting.GRAY), true);
                    int x = 1;
                    for (ServerPlayer p : targets) {
                        if (p != source.getPlayer()) {
                            int finalX = x;
                            source.sendSuccess(() -> Component.literal(finalX + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                            x++;
                        }
                    }
                }
            } else if (player == source.getPlayer()) {
                source.sendFailure(Component.translatable("command.ultimine_addition.set_ability.already_setted", State).withStyle(ChatFormatting.RED));
                i++;
            }
        }
        return i;
    }
}
