package net.ixdarklord.ultimine_addition.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SkillsRecordDebugCommand {
    public static void register(CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(UltimineAddition.getClientCommandPrompt(Commands.LEVEL_GAMEMASTERS)
                .then(ClientCommandRegistrationEvent.literal("skills_record")
                        .then(ClientCommandRegistrationEvent.literal("debug_mode")
                                .then(ClientCommandRegistrationEvent.argument("state", BoolArgumentType.bool()).executes(context -> setEditMode(context.getSource(), BoolArgumentType.getBool(context, "state")))))));
    }

    private static int setEditMode(ClientCommandRegistrationEvent.ClientCommandSourceStack source, boolean state) {
        if (ConfigHandler.CLIENT.SR_EDIT_MODE.get() != state) {
            ConfigHandler.CLIENT.SR_EDIT_MODE.set(state);
            ConfigHandler.CLIENT.SR_EDIT_MODE.save();
            source.arch$sendSuccess(() -> Component.translatable("command.ultimine_addition.skills_record.edit_mode.success", state), true);
            return 1;
        } else {
            source.arch$sendFailure(Component.translatable("command.ultimine_addition.skills_record.edit_mode.already_setted", state));
            return 0;
        }
    }
}
