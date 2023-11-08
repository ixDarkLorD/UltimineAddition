package net.ixdarklord.ultimine_addition.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CardSlotsArgument implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0", "4");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType((entry) ->
            new TranslatableComponent("slot.unknown", entry));
    private final int slotCount = 4;

    public static CardSlotsArgument slots() {
        return new CardSlotsArgument();
    }

    public static Integer getSlot(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int value = reader.readInt();
        if (value < 0 || value >= slotCount) throw ERROR_UNKNOWN_SLOT.create(value);
        return value;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> integerList = IntStream.rangeClosed(0, slotCount-1).mapToObj(Integer::toString).collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(integerList, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
