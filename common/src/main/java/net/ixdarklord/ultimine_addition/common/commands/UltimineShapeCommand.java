package net.ixdarklord.ultimine_addition.common.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import net.ixdarklord.ultimine_addition.common.commands.arguments.UltimineShapeArgument;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class UltimineShapeCommand {
    private static final Component WHITELIST = Component.translatable("command.ultimine_addition.ultimine_shape.whitelist");
    private static final Component BLACKLIST = Component.translatable("command.ultimine_addition.ultimine_shape.blacklist");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, Commands.LEVEL_GAMEMASTERS, builder ->
                builder.then(Commands.literal("ultimine_shape")
                        .then(Commands.literal("blacklist")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("shape_id", UltimineShapeArgument.shape())
                                                .executes(context -> updateBlacklistedShapes(context.getSource(), UltimineShapeArgument.getShape(context, "shape_id"), true))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("shape_id", UltimineShapeArgument.shape())
                                                .executes(context -> updateBlacklistedShapes(context.getSource(), UltimineShapeArgument.getShape(context, "shape_id"), false))))
                                .then(Commands.literal("clear")
                                        .executes(context -> clearBlacklist(context.getSource())))
                        )));
    }

    public static Component lowercase(Component component) {
        return Component.literal(component.getString().toLowerCase());
    }

    public static int clearBlacklist(CommandSourceStack source) {
        ModConfigSpec.ConfigValue<List<? extends String>> config = ConfigHandler.SERVER.BLACKLISTED_SHAPES;
        if (config.get().isEmpty()) {
            source.sendFailure(Component.translatable("command.ultimine_addition.ultimine_shape.empty", lowercase(BLACKLIST)));
            return 0;
        }

        config.set(Lists.newArrayList());
        config.save();
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.ultimine_shape.clear", lowercase(BLACKLIST)), true);
        return 1;
    }

    private static int updateBlacklistedShapes(CommandSourceStack source, Shape shape, boolean adding) {
        ModConfigSpec.ConfigValue<List<? extends String>> config = ConfigHandler.SERVER.BLACKLISTED_SHAPES;
        List<String> shapeIds = Lists.newArrayList(config.get());

        if (adding) {
            if (shapeIds.contains(shape.getName().toString())) {
                source.sendFailure(Component.translatable("command.ultimine_addition.ultimine_shape.already_listed", shape.getName(), lowercase(BLACKLIST)));
                return 0;
            }

            shapeIds.add(shape.getName().toString());
            config.set(shapeIds);
            config.save();
            source.sendSuccess(() -> Component.translatable("command.ultimine_addition.ultimine_shape.added", shape.getName(), lowercase(BLACKLIST)), true);
        } else {
            if (!shapeIds.contains(shape.getName().toString())) {
                source.sendFailure(Component.translatable("command.ultimine_addition.ultimine_shape.not_listed", shape.getName(), lowercase(BLACKLIST)));
                return 0;
            }

            shapeIds.remove(shape.getName().toString());
            config.set(shapeIds);
            config.save();
            source.sendSuccess(() -> Component.translatable("command.ultimine_addition.ultimine_shape.removed", shape.getName(), lowercase(BLACKLIST)), true);
        }
        return 1;
    }
}
