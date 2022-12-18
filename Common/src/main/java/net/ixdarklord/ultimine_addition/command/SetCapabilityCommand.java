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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetCapabilityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        dispatcher.register(Commands.literal("ultimine_addition")
                .then(Commands.literal("set_capability").requires(p -> p.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("state", BoolArgumentType.bool()).executes(command ->
                        setCapability(command.getSource(), BoolArgumentType.getBool(command, "state"))))
                ));
    }

    private static int setCapability(CommandSourceStack source, boolean state) throws CommandSyntaxException {
        int i = 0;
        try {
            ServerPlayer player = source.getPlayer();
            Services.PLATFORM.setPlayerCapability(player, state);
            i++;
        } catch (Exception ignored) {}
        if (i == 0) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.ultimine_addition.failed").withStyle(ChatFormatting.RED)).create();
        } else {
            source.sendSuccess(Component.translatable("commands.ultimine_addition.set_capability", String.valueOf(state).substring(0,1).toUpperCase() + String.valueOf(state).substring(1).toLowerCase()).withStyle(ChatFormatting.DARK_AQUA), true);
        }
        return i;
    }
}
