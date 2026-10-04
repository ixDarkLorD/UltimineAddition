package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.progression.TimedChallenge;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

// "/ultimine_addition challenge": the daily (or weekly) challenge and how far the player is, for everyone.
// "challenge add <amount>" (ops) adds to the player's progress.
public final class TimedChallengeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder -> builder
                .then(Commands.literal("challenge")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> show(context.getSource()))
                        // For pack makers trying out their rewards.
                        .then(Commands.literal("add")
                                .requires(source -> source.isPlayer() && source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> add(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))))));
    }

    private static int add(CommandSourceStack source, int amount) throws CommandSyntaxException {
        TimedChallenge.Current current = TimedChallenge.current(source.getServer());
        if (current == null || !TimedChallenge.advance(source.getPlayerOrException(), current, amount)) {
            source.sendFailure(Component.translatable(current == null ? "info.ultimine_addition.timed.none" : "info.ultimine_addition.timed.done"));
            return 0;
        }
        return show(source);
    }

    private static int show(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TimedChallenge.Current current = TimedChallenge.current(source.getServer());
        if (current == null) {
            source.sendFailure(Component.translatable("info.ultimine_addition.timed.none"));
            return 0;
        }
        PlayerAbilityData.Timed timed = TimedChallenge.progress(player, current);
        source.sendSuccess(() -> current.title().copy().withStyle(ChatFormatting.GOLD), false);
        for (Component line : TimedChallenge.describeLines(current, timed)) source.sendSuccess(() -> line, false);
        return 1;
    }
}
