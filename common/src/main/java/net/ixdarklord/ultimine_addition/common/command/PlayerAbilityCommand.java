package net.ixdarklord.ultimine_addition.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;

public class PlayerAbilityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection ignored2) {
        dispatcher.register(Commands.literal("ultimine_addition").requires(p -> p.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("player_ability")
                .then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.literal("set")
                .then(Commands.argument("state", BoolArgumentType.bool()).executes(context ->
                        setAbility(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "state"))
                ))))));
    }

    private static int setAbility(CommandSourceStack source, Collection<ServerPlayer> targets, boolean state) throws CommandSyntaxException {
        int i = 0;
        String State = String.valueOf(state);
        for (ServerPlayer player : targets) {
            if (ServicePlatform.Players.isPlayerUltimineCapable(player) != state) {
                ServicePlatform.Players.setPlayerUltimineCapability(player, state);
                i++;

                if (player == source.getPlayerOrException()) {
                    source.sendSuccess(new TranslatableComponent("command.ultimine_addition.set_ability.success", State).withStyle(ChatFormatting.DARK_AQUA), true);
                }
                if (i > 1 && player != source.getPlayerOrException() && !player.hasPermissions(2)) {
                    player.displayClientMessage(new TranslatableComponent("command.ultimine_addition.set_ability.receiver", State, Objects.requireNonNull(source.getPlayerOrException()).getName().getString()).withStyle(ChatFormatting.GRAY), false);
                }
                if (i > 1) {
                    source.sendSuccess(new TranslatableComponent("command.ultimine_addition.set_ability.sender", State).withStyle(ChatFormatting.GRAY), true);
                    int x = 1;
                    for (ServerPlayer p : targets) {
                        if (p != source.getPlayerOrException()) {
                            source.sendSuccess(new TextComponent(x + ": " + p.getName().getString()).withStyle(ChatFormatting.YELLOW), true);
                            x++;
                        }
                    }
                }
            } else if (player == source.getPlayerOrException()) {
                source.sendFailure(new TranslatableComponent("command.ultimine_addition.set_ability.already_setted", State).withStyle(ChatFormatting.RED));
                i++;
            }
        }
        return i;
    }
}
