package net.ixdarklord.ultimine_addition.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    private static final String KEY_CATEGORY = String.format("key.category.%s.general", UltimineAddition.MOD_ID);
    public static KeyMapping KEY_SHOW_PROGRESSION_BAR = create(UltimineAddition.getLocation("show_progression_bar"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, KEY_CATEGORY);
    public static KeyMapping KEY_OPEN_SKILLS_RECORD = create(UltimineAddition.getLocation("open_skills_record"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);

    public static KeyMapping create(ResourceLocation id, InputConstants.Type type, int key, String category) {
        return new KeyMapping("key.%s.%s".formatted(id.getNamespace(), id.getPath()), type, key, category);
    }

    public static void register() {
        KeyMappingRegistry.register(KEY_SHOW_PROGRESSION_BAR);
        if (ServicePlatform.SlotAPI.isModLoaded())
            KeyMappingRegistry.register(KEY_OPEN_SKILLS_RECORD);
    }
}
