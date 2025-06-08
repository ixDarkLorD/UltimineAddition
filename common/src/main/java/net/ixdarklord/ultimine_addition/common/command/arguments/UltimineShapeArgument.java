package net.ixdarklord.ultimine_addition.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParserUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UltimineShapeArgument implements ArgumentType<Shape> {
    private static final Collection<String> EXAMPLES = Arrays.asList("shapeless", "small_tunnel");

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SHAPE =
            new DynamicCommandExceptionType((shape) -> Component.translatableEscape("argument.ultimine_addition.ultimine_shape.unknown", shape));

    public static UltimineShapeArgument shape() {
        return new UltimineShapeArgument();
    }

    public static Shape getShape(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Shape.class);
    }

    @Override
    public Shape parse(StringReader reader) throws CommandSyntaxException {
        String string = ParserUtils.readWhile(reader, (c) -> c != ' ');
        Optional<Shape> shape = FTBUltimineIntegration.getShapesList().stream()
                .filter(s -> string.equals(s.getName().toString()))
                .findFirst();

        if (shape.isEmpty())
            throw ERROR_UNKNOWN_SHAPE.create(string);

        return shape.get();
    }


    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> shapesId = FTBUltimineIntegration.getShapesList().stream().map(Shape::getName).map(ResourceLocation::toString).toList();
        return SharedSuggestionProvider.suggest(shapesId, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
