package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import net.ixdarklord.ultimine_addition.common.commands.arguments.UltimineShapeArgument;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.ArrayList;
import java.util.List;

public final class UltimineShapeCommand {
    private static final Component BLACKLIST = Component.translatable("command.ultimine_addition.ultimine_shape.blacklist");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder ->
                builder.then(Commands.literal("ultimine_shape")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
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

    private static Component lowercase(Component component) {
        return Component.literal(component.getString().toLowerCase());
    }

    // Chat arguments must be components; show the shape's name with its ID on hover.
    private static Component shapeName(Shape shape) {
        return shape.getDisplayName().copy().withStyle(style -> style.withColor(ChatFormatting.YELLOW)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(shape.getName().toString()))));
    }

    private static int clearBlacklist(CommandSourceStack source) {
        if (UAServerConfig.BLACKLISTED_SHAPES.get().isEmpty()) {
            source.sendFailure(Component.translatable("command.ultimine_addition.ultimine_shape.empty", lowercase(BLACKLIST)));
            return 0;
        }

        UAServerConfig.BLACKLISTED_SHAPES.set(new ArrayList<>());
        UAServerConfig.CONFIG.save();
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.ultimine_shape.clear", lowercase(BLACKLIST)), true);
        return 1;
    }

    private static int updateBlacklistedShapes(CommandSourceStack source, Shape shape, boolean adding) {
        List<String> shapeIds = new ArrayList<>(UAServerConfig.BLACKLISTED_SHAPES.get());
        String id = shape.getName().toString();

        if (adding == shapeIds.contains(id)) {
            String key = adding ? "already_listed" : "not_listed";
            source.sendFailure(Component.translatable("command.ultimine_addition.ultimine_shape." + key, shapeName(shape), lowercase(BLACKLIST)));
            return 0;
        }

        if (adding) shapeIds.add(id);
        else shapeIds.remove(id);
        UAServerConfig.BLACKLISTED_SHAPES.set(shapeIds);
        UAServerConfig.CONFIG.save();
        String key = adding ? "added" : "removed";
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.ultimine_shape." + key, shapeName(shape), lowercase(BLACKLIST)), true);
        return 1;
    }
}
