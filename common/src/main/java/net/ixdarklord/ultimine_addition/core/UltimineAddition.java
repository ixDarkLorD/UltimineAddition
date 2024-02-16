package net.ixdarklord.ultimine_addition.core;

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
		return new ResourceLocation(MOD_ID, name.toLowerCase(Locale.ROOT));
	}
	public static ResourceLocation getMCLocation(String name) {
		return new ResourceLocation(name.toLowerCase(Locale.ROOT));
	}
	public static ResourceLocation getGuiTexture(String textureName, String fileType) {
		return new ResourceLocation(MOD_ID, GUI_DIR + textureName + "." + fileType);
	}
	public static String getIdAsString(ResourceLocation id) {
		return id.toString();
	}
}