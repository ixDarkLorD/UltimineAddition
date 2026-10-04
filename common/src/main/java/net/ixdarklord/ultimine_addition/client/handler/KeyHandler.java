package net.ixdarklord.ultimine_addition.client.handler;

import net.ixdarklord.coolcatcore.api.client.registry.KeyMappingRegistry;
import net.ixdarklord.coolcatcore.api.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.minecraft.network.chat.Component;
import dev.ftb.mods.ftblibrary.platform.client.input.InputHelper;
import dev.ftb.mods.ftblibrary.platform.client.input.KeyModifier;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(FTBUltimineAddition.id("general"));
    public static KeyMapping KEY_OPEN_SKILLS_RECORD = create(FTBUltimineAddition.id("open_skills_record"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);
    // Ctrl + Z through FTB Library, which supports key modifiers on NeoForge (and on Fabric with Amecs).
    public static KeyMapping KEY_UNDO = InputHelper.createSimpleKeyMapping("undo_ultimine", KEY_CATEGORY, GLFW.GLFW_KEY_Z, KeyModifier.CONTROL);

    // Ctrl + H: the list of operations that can still be undone.
    public static KeyMapping KEY_UNDO_HISTORY = InputHelper.createSimpleKeyMapping("undo_history", KEY_CATEGORY, GLFW.GLFW_KEY_H, KeyModifier.CONTROL);

    public static KeyMapping create(Identifier id, InputConstants.Type type, int key, KeyMapping.Category category) {
        return new KeyMapping("key.%s.%s".formatted(id.getNamespace(), id.getPath()), type, key, category);
    }

    // Without modifier support the mapping is plain Z, so Ctrl is checked by hand.
    // The undo key as the player presses it. Where the Ctrl modifier is checked here instead of by the key mapping
    // (Fabric without Amecs), the mapping's own name leaves it out.
    public static Component undoKeyName() {
        Component key = PlatformClient.get().input().getKeyMappingDisplayName(KEY_UNDO);
        if (Platform.isNeoForge() || Platform.isModLoaded("amecs")) return key;
        return Component.translatable("gui.ultimine_addition.undo.key_with_ctrl", key);
    }

    public static boolean undoModifierHeld() {
        return Platform.isNeoForge() || Platform.isModLoaded("amecs") || Minecraft.getInstance().hasControlDown();
    }

    public static void register() {
        KeyMappingRegistry.register(KEY_UNDO);
        KeyMappingRegistry.register(KEY_UNDO_HISTORY);
        if (ServicePlatform.get().slotAPI().isModLoaded())
            KeyMappingRegistry.register(KEY_OPEN_SKILLS_RECORD);
    }
}
