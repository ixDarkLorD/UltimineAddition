package net.ixdarklord.ultimine_addition.core;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

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
}
