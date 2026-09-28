package net.ixdarklord.ultimine_addition.client.commands;

import net.ixdarklord.coolcatcore.api.event.v2.client.ClientCommandEvents;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.ixdarklord.ultimine_addition.config.UAClientConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SkillsRecordDebugCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, CommandBuildContext ignored) {
        FTBUltimineAddition.withClientCommandPrompt(dispatcher, Commands.LEVEL_GAMEMASTERS, builder ->
                builder.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("skills_record")
                        .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("debug_mode")
                                .then(RequiredArgumentBuilder.<SharedSuggestionProvider, Boolean>argument("state", BoolArgumentType.bool()).executes(context -> setEditMode(context.getSource(), BoolArgumentType.getBool(context, "state")))))));
    }

    private static int setEditMode(SharedSuggestionProvider source, boolean state) {
        if (UAClientConfig.SR_EDIT_MODE.get() != state) {
            UAClientConfig.SR_EDIT_MODE.set(state);
            UAClientConfig.CONFIG.save();
            ClientCommandEvents.sendFeedback(Component.translatable("command.ultimine_addition.skills_record.edit_mode.success", state));
            return 1;
        } else {
            ClientCommandEvents.sendError(Component.translatable("command.ultimine_addition.skills_record.edit_mode.already_setted", state));
            return 0;
        }
    }
}
