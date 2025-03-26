package net.ixdarklord.ultimine_addition.common.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.common.command.arguments.UltimineShapeArgument;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class UltimineShapeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, Commands.LEVEL_GAMEMASTERS, builder ->
                builder.then(Commands.literal("blacklist_shape")
                        .then(Commands.literal("add")
                                .then(Commands.argument("shape_id", UltimineShapeArgument.shape())
                                        .executes(context -> updateBlacklistedShapes(context.getSource(), UltimineShapeArgument.getShape(context, "shape_id"), true))))
                        .then(Commands.literal("remove").then(Commands.argument("shape_id", UltimineShapeArgument.shape())
                                .executes(context -> updateBlacklistedShapes(context.getSource(), UltimineShapeArgument.getShape(context, "shape_id"), false))))
                ));
    }

    private static int updateBlacklistedShapes(CommandSourceStack source, Shape shape, boolean adding) {
        ModConfigSpec.ConfigValue<List<? extends String>> config = ConfigHandler.SERVER.BLACKLISTED_SHAPES;
        List<String> shapeIds = Lists.newArrayList(config.get());

        if (shapeIds.contains(shape.getName())) {
            if (adding)
                source.sendFailure(Component.translatable("command.ultimine_addition.ultimine_shape.included", shape.getName()));
            else
                source.sendFailure(Component.translatable("command.ultimine_addition.ultimine_shape.excluded", shape.getName()));
            return 0;
        }

        if (adding) {
            shapeIds.add(shape.getName());
            config.set(shapeIds);
            config.save();
            source.sendSuccess(() -> Component.translatable("command.ultimine_addition.ultimine_shape.success.add", shape.getName()), true);
        } else {
            shapeIds.remove(shape.getName());
            config.set(shapeIds);
            config.save();
            source.sendSuccess(() -> Component.translatable("command.ultimine_addition.ultimine_shape.success.remove", shape.getName()), true);
        }
        return 1;
    }
}
