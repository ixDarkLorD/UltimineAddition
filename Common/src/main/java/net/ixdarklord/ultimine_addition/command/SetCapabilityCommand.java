package net.ixdarklord.ultimine_addition.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ixdarklord.ultimine_addition.helper.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;

public class SetCapabilityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored1, Commands.CommandSelection ignored2) {
        dispatcher.register(Commands.literal("ultimine_addition")
                .then(Commands.literal("set_capability").requires(p -> p.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.argument("state", BoolArgumentType.bool()).executes(context -> setCapability(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "state")))))
                ));
    }

    private static int setCapability(CommandSourceStack source, Collection<ServerPlayer> targets, boolean state) throws CommandSyntaxException {
        int i = 0;
        String State = String.valueOf(state);
        for (ServerPlayer player : targets) {
            if (Services.PLATFORM.isPlayerCapable(player) != state) {
                Services.PLATFORM.setPlayerCapability(player, state);
                i++;

                if (player == source.getPlayer()) {
                    source.sendSuccess(Component.translatable("commands.ultimine_addition.set_capability", State).withStyle(ChatFormatting.DARK_AQUA), true);
                }
                if (i > 1 && player != source.getPlayer() && !player.hasPermissions(2)) {
                    player.displayClientMessage(Component.translatable("commands.ultimine_addition.set_capability.other", State, Objects.requireNonNull(source.getPlayer()).getName().getString()).withStyle(ChatFormatting.GRAY), false);
                }
                if (i > 1) {
                    source.sendSuccess(Component.translatable("commands.ultimine_addition.set_capability.sender", State).withStyle(ChatFormatting.GRAY), true);
                    int x = 1;
                    for (ServerPlayer p : targets) {
                        if (p != source.getPlayer()) {
                            source.sendSuccess(Component.literal(x + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                            x++;
                        }
                    }
                }
            } else if (player == source.getPlayer()) {
                source.sendFailure(Component.translatable("commands.ultimine_addition.already_setted", State).withStyle(ChatFormatting.RED));
                i++;
            }
        }
        if (i == 0) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.ultimine_addition.failed").withStyle(ChatFormatting.RED)).create();
        }
        return i;
    }
}
