package net.ixdarklord.ultimine_addition.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class UltimineAddition {

	public static final String MOD_ID = "ultimine_addition";
	public static final String MOD_NAME = "Ultimine Addition";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	private static final String GUI_DIR = "textures/gui/";

	public static ResourceLocation getLocation(String name) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, name.toLowerCase(Locale.ROOT));
	}
	public static ResourceLocation getGuiTexture(String textureName, String fileType) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, GUI_DIR + textureName + "." + fileType);
	}
	public static String getIdAsString(ResourceLocation id) {
		return id.toString();
	}

	public static LiteralArgumentBuilder<CommandSourceStack> getCommandPrompt(int permissionLevel) {
		return Commands.literal("ua").requires(p -> p.hasPermission(permissionLevel));
	}

	public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> getClientCommandPrompt(int permissionLevel) {
		return ClientCommandRegistrationEvent.literal("ua_client").requires(p -> p.hasPermission(permissionLevel));
	}
}