package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.OpenConfigPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

// Same layout as FTB Ultimine's: "config" opens the mod's configs (CoolCatLib's screens let only ops save the server
// ones), "clientconfig" the client settings and "serverconfig" (ops only) the server settings.
public final class ConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder -> builder
                .then(Commands.literal("config")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> open(context.getSource(), OpenConfigPayload.Target.ALL)))
                .then(Commands.literal("clientconfig")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> open(context.getSource(), OpenConfigPayload.Target.CLIENT)))
                .then(Commands.literal("serverconfig")
                        .requires(source -> source.isPlayer() && source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> open(context.getSource(), OpenConfigPayload.Target.SERVER))));
    }

    private static int open(CommandSourceStack source, OpenConfigPayload.Target target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        PayloadHandler.sendToPlayer(new OpenConfigPayload(target), source.getPlayerOrException());
        return 1;
    }
}
