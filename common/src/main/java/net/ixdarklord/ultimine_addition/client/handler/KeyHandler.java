package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.coolcatcore.api.client.registry.KeyMappingRegistry;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import net.ixdarklord.ultimine_addition.hooks.KeyBindingHooks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    private static final String KEY_CATEGORY = String.format("key.category.%s.general", FTBUltimineAddition.MOD_ID);
    public static KeyMapping KEY_OPEN_SKILLS_RECORD = create(FTBUltimineAddition.id("open_skills_record"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);
    // Ctrl + Z: a key modifier on Forge; Fabric has none, so there the mapping is plain Z and Ctrl is checked by hand.
    public static KeyMapping KEY_UNDO = KeyBindingHooks.createControlKeyMapping("key.%s.general.undo_ultimine".formatted(FTBUltimineAddition.MOD_ID), GLFW.GLFW_KEY_Z, KEY_CATEGORY);

    public static KeyMapping create(ResourceLocation id, InputConstants.Type type, int key, String category) {
        return new KeyMapping("key.%s.%s".formatted(id.getNamespace(), id.getPath()), type, key, category);
    }

    // The undo key as the player presses it. Where the Ctrl modifier is checked here instead of by the key mapping
    // (Fabric), the mapping's own name leaves it out.
    public static Component undoKeyName() {
        Component key = KEY_UNDO.getTranslatedKeyMessage();
        if (Platform.isForge()) return key;
        return Component.translatable("gui.ultimine_addition.undo.key_with_ctrl", key);
    }

    public static boolean undoModifierHeld() {
        return Platform.isForge() || Screen.hasControlDown();
    }

    public static void register() {
        KeyMappingRegistry.register(KEY_UNDO);
        if (ServicePlatform.get().slotAPI().isModLoaded())
            KeyMappingRegistry.register(KEY_OPEN_SKILLS_RECORD);
    }
}
