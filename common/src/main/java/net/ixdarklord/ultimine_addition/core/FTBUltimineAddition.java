package net.ixdarklord.ultimine_addition.core;

import org.jetbrains.annotations.Nullable;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionCheck;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.function.Consumer;

public class FTBUltimineAddition {
	public static final String MOD_ID = "ultimine_addition";
	public static final String MOD_NAME = "FTB Ultimine Addition";
	public static final Logger LOGGER = LogManager.getLogger();
	private static final String GUI_DIR = "textures/gui/";

	public static Identifier id(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name.toLowerCase(Locale.ROOT));
	}
	public static Identifier getGuiTexture(String textureName, String fileType) {
		return Identifier.fromNamespaceAndPath(MOD_ID, GUI_DIR + textureName + "." + fileType);
	}

    // The root is open to everyone (e.g. "config"); subcommands set their own permission.
    public static void withCommandPrompt(
            CommandDispatcher<CommandSourceStack> dispatcher,
            Consumer<LiteralArgumentBuilder<CommandSourceStack>> builderConsumer) {

        registerWithSuffix(dispatcher, null, builderConsumer, "");
    }

    public static void withClientCommandPrompt(
            CommandDispatcher<SharedSuggestionProvider> dispatcher,
            PermissionCheck permission,
            Consumer<LiteralArgumentBuilder<SharedSuggestionProvider>> builderConsumer) {

        registerWithSuffix(dispatcher, permission, builderConsumer, "_client");
    }

    private static <T extends SharedSuggestionProvider> void registerWithSuffix(
            CommandDispatcher<T> dispatcher,
            @Nullable PermissionCheck permission,
            Consumer<LiteralArgumentBuilder<T>> builderConsumer,
            String suffix) {

        String[] allies = {MOD_ID, "ua"};
        for (String ally : allies) {
            LiteralArgumentBuilder<T> builder = LiteralArgumentBuilder.literal(ally + suffix);
            builderConsumer.accept(permission == null ? builder : builder.requires(Commands.hasPermission(permission)));
            dispatcher.register(builder);
        }
    }
}