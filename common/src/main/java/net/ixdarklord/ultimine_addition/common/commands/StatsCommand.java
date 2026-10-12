package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData.Stats;
import net.ixdarklord.ultimine_addition.common.progression.UltimineStats;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

// "/ultimine_addition stats": the player's own numbers. "/ultimine_addition top [board]": the best of the players seen
// since the server started. Both are for everyone.
public final class StatsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder -> {
            builder.then(Commands.literal("stats")
                    .requires(CommandSourceStack::isPlayer)
                    .executes(context -> stats(context.getSource())));
            LiteralArgumentBuilder<CommandSourceStack> top = Commands.literal("top").executes(context -> top(context.getSource(), UltimineStats.Board.BLOCKS));
            for (UltimineStats.Board board : UltimineStats.Board.values()) {
                top.then(Commands.literal(board.name().toLowerCase(Locale.ROOT)).executes(context -> top(context.getSource(), board)));
            }
            builder.then(top);
        });
    }

    private static Component line(String key, Object value) {
        return Component.translatable("command.ultimine_addition.stats." + key, Component.literal(String.valueOf(value)).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.GRAY);
    }

    private static int stats(CommandSourceStack source) throws CommandSyntaxException {
        Stats stats = UltimineStats.of(source.getPlayerOrException());
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.stats.title").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> line("blocks", stats.blocks()), false);
        source.sendSuccess(() -> line("operations", stats.operations()), false);
        source.sendSuccess(() -> line("largest", stats.largest()), false);
        source.sendSuccess(() -> line("challenges", stats.challenges()), false);
        source.sendSuccess(() -> line("dailies", stats.dailies()), false);
        source.sendSuccess(() -> line("undos", stats.undos()), false);
        var ability = net.ixdarklord.ultimine_addition.core.ServicePlatform.get().players().getAbilityData(source.getPlayerOrException());
        ability.getAllPerks().forEach((tool, perks) -> {
            if (perks.isEmpty()) return;
            var names = Component.empty();
            for (String id : perks) {
                var perk = net.ixdarklord.ultimine_addition.common.progression.CardPerk.byId(id);
                if (perk == null) continue;
                if (!names.getSiblings().isEmpty()) names.append(", ");
                names.append(perk.displayName().withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            var type = net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Type.byId(tool);
            Component toolName = type == null ? Component.literal(tool) : type.displayName();
            source.sendSuccess(() -> Component.translatable("command.ultimine_addition.stats.perks", toolName, names).withStyle(ChatFormatting.GRAY), false);
        });
        return 1;
    }

    private static int top(CommandSourceStack source, UltimineStats.Board board) {
        List<UltimineStats.Row> rows = UltimineStats.top(board, 10);
        String name = board.name().toLowerCase(Locale.ROOT);
        if (rows.isEmpty()) {
            source.sendFailure(Component.translatable("command.ultimine_addition.top.empty"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.top.title", Component.translatable("command.ultimine_addition.top." + name))
                .withStyle(ChatFormatting.GOLD), false);
        for (int i = 0; i < rows.size(); i++) {
            UltimineStats.Row row = rows.get(i);
            int place = i + 1;
            source.sendSuccess(() -> Component.literal(place + ". ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(row.name()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("  " + board.of(row.stats())).withStyle(ChatFormatting.WHITE)), false);
        }
        return rows.size();
    }
}
