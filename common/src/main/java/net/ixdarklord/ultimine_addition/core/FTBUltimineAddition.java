package net.ixdarklord.ultimine_addition.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.function.Consumer;

public class FTBUltimineAddition {
	public static final String MOD_ID = "ultimine_addition";
	public static final String MOD_NAME = "FTB Ultimine Addition";
	public static final Logger LOGGER = LogManager.getLogger();
	private static final String GUI_DIR = "textures/gui/";

	public static ResourceLocation id(String name) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, name.toLowerCase(Locale.ROOT));
	}
	public static ResourceLocation getGuiTexture(String textureName, String fileType) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, GUI_DIR + textureName + "." + fileType);
	}

    public static void withCommandPrompt(
            CommandDispatcher<CommandSourceStack> dispatcher,
            int permissionLevel,
            Consumer<LiteralArgumentBuilder<CommandSourceStack>> builderConsumer) {

        registerWithSuffix(dispatcher, permissionLevel, builderConsumer, "");
    }

    public static void withClientCommandPrompt(
            CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher,
            int permissionLevel,
            Consumer<LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack>> builderConsumer) {

        registerWithSuffix(dispatcher, permissionLevel, builderConsumer, "_client");
    }

    private static <T extends SharedSuggestionProvider> void registerWithSuffix(
            CommandDispatcher<T> dispatcher,
            int permissionLevel,
            Consumer<LiteralArgumentBuilder<T>> builderConsumer,
            String suffix) {

        String[] allies = {MOD_ID, "ua"};
        for (String ally : allies) {
            LiteralArgumentBuilder<T> builder = LiteralArgumentBuilder.literal(ally + suffix);
            builderConsumer.accept(builder.requires(provider -> provider.hasPermission(permissionLevel)));
            dispatcher.register(builder);
        }
    }
}