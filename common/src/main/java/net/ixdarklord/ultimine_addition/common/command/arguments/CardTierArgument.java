package net.ixdarklord.ultimine_addition.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CardTierArgument implements ArgumentType<MiningSkillCardItem.Tier> {
    private static final Collection<String> EXAMPLES = Arrays.asList("novice", "adept");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_TIER = new DynamicCommandExceptionType((entry) ->
            new TranslatableComponent("argument.ultimine_addition.cards.tier.unknown", entry));

    public static CardTierArgument tier() {
        return new CardTierArgument();
    }

    public static MiningSkillCardItem.Tier getTier(CommandContext<CommandSourceStack> pContext, String pName) {
        return pContext.getArgument(pName, MiningSkillCardItem.Tier.class);
    }
    @Override
    public MiningSkillCardItem.Tier parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readUnquotedString();
        var tier = MiningSkillCardItem.Tier.fromString(input);
        if (tier != null) return tier;
        throw ERROR_UNKNOWN_TIER.create(input);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return SharedSuggestionProvider.suggest(MiningSkillCardItem.Tier.getNames(), pBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
