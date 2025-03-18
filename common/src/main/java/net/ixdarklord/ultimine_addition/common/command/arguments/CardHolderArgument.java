package net.ixdarklord.ultimine_addition.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParserUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CardHolderArgument implements ArgumentType<Integer> {
    private static final Collection<String> SLOTS = Util.make(new ArrayList<>(), list -> {
        list.add("self");
        for (int slot : SkillsRecordMenu.CARD_SLOTS)
            list.add("skills_record." + slot);
    });

    private static final Collection<String> EXAMPLES = Arrays.asList("none", "skills_record.slot.1");

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT =
            new DynamicCommandExceptionType((slot) -> Component.translatableEscape("slot.unknown", slot));

    public static CardHolderArgument slot() {
        return new CardHolderArgument();
    }

    public static Integer getSlot(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String string = ParserUtils.readWhile(reader, (c) -> c != ' ');

        if (!SLOTS.contains(string))
            throw ERROR_UNKNOWN_SLOT.create(string);

        if (string.startsWith("skills_record.")) {
            try {
                return Integer.parseInt(string.substring("skills_record.".length()));
            } catch (NumberFormatException e) {
                throw ERROR_UNKNOWN_SLOT.create(string);
            }
        }

        return -1;
    }


    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(SLOTS, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
