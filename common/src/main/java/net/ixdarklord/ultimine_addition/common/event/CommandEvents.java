package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.ultimine_addition.common.commands.CardsCommand;
import net.ixdarklord.ultimine_addition.common.commands.ConfigCommand;
import net.ixdarklord.ultimine_addition.common.commands.FavoriteShapeCommand;
import net.ixdarklord.ultimine_addition.common.commands.MyShapesCommand;
import net.ixdarklord.ultimine_addition.common.commands.SkillsRecordCommand;
import net.ixdarklord.ultimine_addition.common.commands.StatsCommand;
import net.ixdarklord.ultimine_addition.common.commands.TimedChallengeCommand;
import net.ixdarklord.ultimine_addition.common.commands.UltimineAbilityCommand;
import net.ixdarklord.ultimine_addition.common.commands.UltimineShapeCommand;

public class CommandEvents {
    public static void init() {
        net.ixdarklord.coolcatcore.api.event.v2.common.CommandEvents.REGISTER.register((dispatcher, registry, selection) -> {
            UltimineAbilityCommand.register(dispatcher, registry, selection);
            CardsCommand.register(dispatcher, registry, selection);
            UltimineShapeCommand.register(dispatcher, registry, selection);
            ConfigCommand.register(dispatcher, registry, selection);
            SkillsRecordCommand.register(dispatcher, registry, selection);
            MyShapesCommand.register(dispatcher, registry, selection);
            FavoriteShapeCommand.register(dispatcher, registry, selection);
            StatsCommand.register(dispatcher, registry, selection);
            TimedChallengeCommand.register(dispatcher, registry, selection);
        });
    }
}
