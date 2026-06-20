package net.ixdarklord.ultimine_addition.common.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.util.ParserUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CardHolderArgument implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("none", "skills_record.slot.1");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType((slot) -> Component.translatable("slot.unknown", slot));
    protected final Collection<String> SLOTS = Util.make(new ArrayList<>(), (list) -> {
        list.add("held_item");
        for (int slot : SkillsRecordMenu.CARD_SLOTS) {
            list.add("skills_record." + slot);
        }
    });

    private CardHolderArgument() {
    }

    public static CardHolderArgument allSlots() {
        return new CardHolderArgument();
    }

    public static RecordSlots recordSlots() {
        return new RecordSlots();
    }

    public static Integer getSlot(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String string = ParserUtils.readWhile(reader, (c) -> c != ' ');
        if (!this.SLOTS.contains(string)) {
            throw ERROR_UNKNOWN_SLOT.create(string);
        } else if (string.startsWith("skills_record.")) {
            try {
                return Integer.parseInt(string.substring("skills_record.".length()));
            } catch (NumberFormatException e) {
                throw ERROR_UNKNOWN_SLOT.create(string);
            }
        } else {
            return -1;
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(this.SLOTS, builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class RecordSlots extends CardHolderArgument {
        private RecordSlots() {
            this.SLOTS.remove("held_item");
        }
    }
}
