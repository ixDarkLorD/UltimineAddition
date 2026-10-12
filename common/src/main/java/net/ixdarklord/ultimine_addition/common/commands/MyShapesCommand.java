package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ixdarklord.ultimine_addition.common.data.shape.CustomShapes;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

// "/ultimine_addition my_shapes": the shapes the player drew in the Shape Designer, numbered.
// "/ultimine_addition my_shapes delete <number>": removes one, making room for another. For everyone.
public final class MyShapesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder -> builder
                .then(Commands.literal("my_shapes")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> list(context.getSource()))
                        .then(Commands.literal("delete")
                                .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                        .executes(context -> delete(context.getSource(), IntegerArgumentType.getInteger(context, "number")))))));
    }

    private static int list(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<CustomShapes.Entry> mine = CustomShapes.get(source.getServer()).ownedBy(player.getUUID());
        if (mine.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.ultimine_addition.my_shapes.none"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.my_shapes.title").withStyle(ChatFormatting.GOLD), false);
        for (int i = 0; i < mine.size(); i++) {
            int number = i + 1;
            String name = mine.get(i).name();
            source.sendSuccess(() -> Component.literal(number + ". ").withStyle(ChatFormatting.GRAY).append(Component.literal(name).withStyle(ChatFormatting.YELLOW)), false);
        }
        return mine.size();
    }

    private static int delete(CommandSourceStack source, int number) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<CustomShapes.Entry> mine = CustomShapes.get(source.getServer()).ownedBy(player.getUUID());
        if (number > mine.size()) {
            source.sendFailure(Component.translatable("command.ultimine_addition.my_shapes.unknown", number));
            return 0;
        }
        CustomShapes.Entry entry = mine.get(number - 1);
        CustomShapes.delete(source.getServer(), player.getUUID(), false, entry.id());
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.my_shapes.deleted", Component.literal(entry.name()).withStyle(ChatFormatting.YELLOW)), false);
        return 1;
    }
}
