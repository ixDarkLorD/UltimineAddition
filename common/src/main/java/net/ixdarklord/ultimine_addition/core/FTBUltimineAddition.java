package net.ixdarklord.ultimine_addition.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class FTBUltimineAddition {
    public static final String MOD_ID = "ultimine_addition";
    public static final String MOD_NAME = "Ultimine Addition";
    public static final Logger LOGGER = LoggerFactory.getLogger("Ultimine Addition");
    private static final String GUI_DIR = "textures/gui/";

    public static ResourceLocation id(String path) {
        return new ResourceLocation("ultimine_addition", path);
    }

    public static ResourceLocation getGuiSprite(String textureName) {
        return getGuiTexture("sprites/" + textureName);
    }

    public static ResourceLocation getGuiTexture(String textureName) {
        return getGuiTexture(textureName, "png");
    }

    public static ResourceLocation getGuiTexture(String textureName, String fileType) {
        return id("textures/gui/" + textureName + "." + fileType);
    }

    public static <T extends CommandSourceStack> void withCommandPrompt(CommandDispatcher<T> dispatcher, int permissionLevel, Consumer<LiteralArgumentBuilder<T>> builderConsumer) {
        withCommandPromptInternal(dispatcher, permissionLevel, false, builderConsumer);
    }

    public static <T extends ClientCommandRegistrationEvent.ClientCommandSourceStack> void withClientCommandPrompt(CommandDispatcher<T> dispatcher, int permissionLevel, Consumer<LiteralArgumentBuilder<T>> builderConsumer) {
        withCommandPromptInternal(dispatcher, permissionLevel, true, builderConsumer);
    }

    private static <T extends SharedSuggestionProvider> void withCommandPromptInternal(CommandDispatcher<T> dispatcher, int permissionLevel, boolean isClientSide, Consumer<LiteralArgumentBuilder<T>> builderConsumer) {
        String[] allies = new String[]{"ultimine_addition", "ua"};

        for (String ally : allies) {
            LiteralArgumentBuilder<T> builder = new LiteralArgumentBuilder<T>(ally + (isClientSide ? "_client" : "")) {
            };
            builderConsumer.accept(builder.requires((provider) -> provider.hasPermission(permissionLevel)));
            dispatcher.register(builder);
        }

    }
}
