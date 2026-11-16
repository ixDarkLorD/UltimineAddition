package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.coolcatcore.api.command.ModCommands;
import net.ixdarklord.ultimine_addition.common.commands.CardsCommand;
import net.ixdarklord.ultimine_addition.common.commands.ConfigCommand;
import net.ixdarklord.ultimine_addition.common.commands.FavoriteShapeCommand;
import net.ixdarklord.ultimine_addition.common.commands.MyShapesCommand;
import net.ixdarklord.ultimine_addition.common.commands.SkillsRecordCommand;
import net.ixdarklord.ultimine_addition.common.commands.StatsCommand;
import net.ixdarklord.ultimine_addition.common.commands.TimedChallengeCommand;
import net.ixdarklord.ultimine_addition.common.commands.UltimineAbilityCommand;
import net.ixdarklord.ultimine_addition.common.commands.UltimineShapeCommand;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;

import java.util.List;

public class CommandEvents {
    public static void init() {
        // "/ua" works the same as "/ultimine_addition". CoolCatLib adds "config" (and "config reload") under both.
        ModCommands.register(FTBUltimineAddition.MOD_ID, List.of("ultimine_addition", "ua"), (root, registry, selection) -> {
            UltimineAbilityCommand.register(root, registry, selection);
            CardsCommand.register(root, registry, selection);
            UltimineShapeCommand.register(root, registry, selection);
            ConfigCommand.register(root, registry, selection);
            SkillsRecordCommand.register(root, registry, selection);
            MyShapesCommand.register(root, registry, selection);
            FavoriteShapeCommand.register(root, registry, selection);
            StatsCommand.register(root, registry, selection);
            TimedChallengeCommand.register(root, registry, selection);
        });
    }
}
