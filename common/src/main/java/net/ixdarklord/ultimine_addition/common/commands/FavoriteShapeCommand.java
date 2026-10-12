package net.ixdarklord.ultimine_addition.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbultimine.shape.Shape;
import net.ixdarklord.ultimine_addition.common.commands.arguments.UltimineShapeArgument;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

// "/ultimine_addition favorite <shape>" marks or unmarks a shape; with any marked, cycling shapes only goes through
// those (of the ones the player can use). "favorite" alone lists them, "favorite clear" unmarks all. For everyone.
public final class FavoriteShapeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection ignored2) {
        FTBUltimineAddition.withCommandPrompt(dispatcher, builder -> builder
                .then(Commands.literal("favorite")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> list(context.getSource()))
                        .then(Commands.literal("clear").executes(context -> clear(context.getSource())))
                        .then(Commands.argument("shape_id", UltimineShapeArgument.shape())
                                .executes(context -> toggle(context.getSource(), UltimineShapeArgument.getShape(context, "shape_id"))))));
    }

    private static int toggle(CommandSourceStack source, Shape shape) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean[] now = new boolean[1];
        ServicePlatform.get().players().modifyAbilityData(player, data -> now[0] = data.toggleFavoriteShape(FTBUltimineIntegration.shapeId(shape)));
        source.sendSuccess(() -> Component.translatable(now[0] ? "command.ultimine_addition.favorite.added" : "command.ultimine_addition.favorite.removed",
                FTBUltimineIntegration.shapeName(shape).withStyle(ChatFormatting.YELLOW)), false);
        return 1;
    }

    private static int clear(CommandSourceStack source) throws CommandSyntaxException {
        ServicePlatform.get().players().modifyAbilityData(source.getPlayerOrException(), data -> data.clearFavoriteShapes());
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.favorite.cleared"), false);
        return 1;
    }

    private static int list(CommandSourceStack source) throws CommandSyntaxException {
        List<ResourceLocation> marked = ServicePlatform.get().players().getAbilityData(source.getPlayerOrException()).getFavoriteShapes();
        if (marked.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.ultimine_addition.favorite.none"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.ultimine_addition.favorite.list").withStyle(ChatFormatting.GOLD), false);
        for (ResourceLocation id : marked) {
            Shape shape = FTBUltimineIntegration.getShape(id);
            Component name = shape == null ? Component.literal(id.toString()) : FTBUltimineIntegration.shapeName(shape);
            source.sendSuccess(() -> Component.literal("• ").withStyle(ChatFormatting.GRAY).append(name.copy().withStyle(ChatFormatting.YELLOW)), false);
        }
        return marked.size();
    }
}
