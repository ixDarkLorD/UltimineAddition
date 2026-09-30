package net.ixdarklord.ultimine_addition.hooks.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.ixdarklord.ultimine_addition.hooks.KeyBindingHooks;
import net.minecraft.client.KeyMapping;

public class KeyBindingHooksImpl {
    /**
     * {@link KeyBindingHooks#isMatches(KeyMapping, int, int)}
     */
    public static boolean isMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        return keyMapping.matches(keyCode, scanCode);
    }

    /**
     * {@link KeyBindingHooks#createControlKeyMapping(String, int, String)}: Fabric key mappings have no modifiers.
     */
    public static KeyMapping createControlKeyMapping(String name, int keyCode, String category) {
        return new KeyMapping(name, InputConstants.Type.KEYSYM, keyCode, category);
    }
}
