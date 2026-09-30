package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordInspector;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// "/ultimine_addition skills_record inspect [targets]": the server's view of the Skills Records and cards players carry.
// "/ultimine_addition_client skills_record inspect" prints the client's, to compare (see SkillsRecordInspector).
public final class SkillsRecordCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder -> builder.then(Commands.literal("skills_record")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("inspect")
                        .executes(context -> inspect(context.getSource(), List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> inspect(context.getSource(), EntityArgument.getPlayers(context, "targets")))))));
    }

    private static int inspect(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            source.sendSuccess(() -> Component.literal("[server] " + player.getScoreboardName() + ":"), false);
            for (String line : SkillsRecordInspector.describe(player, stack -> Optional.of(SkillsRecordData.get(stack, player.level())))) {
                source.sendSuccess(() -> Component.literal(line), false);
            }
        }
        return targets.size();
    }
}
