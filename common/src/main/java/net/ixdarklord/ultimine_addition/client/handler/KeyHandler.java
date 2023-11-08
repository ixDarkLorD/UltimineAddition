package net.ixdarklord.ultimine_addition.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    private static final String KEY_CATEGORY = String.format("key.category.%s.general", Constants.MOD_ID);
    public static KeyMapping KEY_SHOW_PROGRESSION_BAR = create(Constants.getLocation("show_progression_bar"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, KEY_CATEGORY);

    public static KeyMapping create(ResourceLocation id, InputConstants.Type type, int key, String category) {
        return new KeyMapping(String.format("key.%s", id.toString()),type, key, category);
    }

    public static void register() {
//        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
//            KeyMappingRegistry.register(KEY_SHOW_PROGRESSION_BAR);
//        });
    }
}
