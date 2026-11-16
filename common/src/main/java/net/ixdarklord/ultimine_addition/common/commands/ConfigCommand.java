package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.OpenConfigPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

// Same layout as FTB Ultimine's: "clientconfig" opens the client settings and "serverconfig" (ops only) the server
// settings. "config" (every config of the mod; "config reload" for ops) is CoolCatLib's, added under the mod's names.
public final class ConfigCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> root, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        root
                .then(Commands.literal("clientconfig")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> open(context.getSource(), OpenConfigPayload.Target.CLIENT)))
                .then(Commands.literal("serverconfig")
                        .requires(source -> source.isPlayer() && source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(context -> open(context.getSource(), OpenConfigPayload.Target.SERVER)));
    }

    private static int open(CommandSourceStack source, OpenConfigPayload.Target target) throws CommandSyntaxException {
        PayloadHandler.sendToPlayer(new OpenConfigPayload(target), source.getPlayerOrException());
        return 1;
    }
}
