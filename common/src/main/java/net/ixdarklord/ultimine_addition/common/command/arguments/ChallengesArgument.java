package net.ixdarklord.ultimine_addition.common.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ChallengesArgument implements ArgumentType<Pair<ResourceLocation, ChallengesData>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("ultimine_addition:test_id", "ultimine_addition:breaking_block", "ultimine_addition:pickaxe/gathering_stones");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_CHALLENGE = new DynamicCommandExceptionType((entry) ->
            Component.translatable("argument.ultimine_addition.challenge.unknown", entry));

    public static ChallengesArgument data() {
        return new ChallengesArgument();
    }

    public static Pair<ResourceLocation, ChallengesData> getData(CommandContext<CommandSourceStack> pContext, String pName) {
        return pContext.getArgument(pName, Pair.class);
    }

    @Override
    public Pair<ResourceLocation, ChallengesData> parse(StringReader reader) throws CommandSyntaxException {
        var id = read(reader);
        if (ChallengesManager.INSTANCE.getAllChallenges().containsKey(id))
            return Pair.of(id, ChallengesManager.INSTANCE.getAllChallenges().get(id));
        throw ERROR_UNKNOWN_CHALLENGE.create(id.toString());
    }

    public static ResourceLocation read(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        while(reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }
        String string = reader.getString().substring(i, reader.getCursor());

        try {
            return string.contains(":") ? ResourceLocation.parse(string) : FTBUltimineAddition.rl(string);
        } catch (ResourceLocationException var4) {
            reader.setCursor(i);
            throw ResourceLocation.ERROR_INVALID.createWithContext(reader);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return SharedSuggestionProvider.suggestResource(ChallengesManager.INSTANCE.getAllChallenges().keySet(), pBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
