package net.ixdarklord.ultimine_addition.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(FTBUltimineAddition.id("general"));
    public static KeyMapping KEY_OPEN_SKILLS_RECORD = create(FTBUltimineAddition.id("open_skills_record"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);

    public static KeyMapping create(Identifier id, InputConstants.Type type, int key, KeyMapping.Category category) {
        return new KeyMapping("key.%s.%s".formatted(id.getNamespace(), id.getPath()), type, key, category);
    }

    public static void register() {
        if (ServicePlatform.get().slotAPI().isModLoaded())
            KeyMappingRegistry.register(KEY_OPEN_SKILLS_RECORD);
    }
}
