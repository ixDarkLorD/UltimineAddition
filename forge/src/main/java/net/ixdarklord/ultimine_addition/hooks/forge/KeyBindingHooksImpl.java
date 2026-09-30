package net.ixdarklord.ultimine_addition.hooks.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.ixdarklord.ultimine_addition.hooks.KeyBindingHooks;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindingHooksImpl {
    /**
     * {@link KeyBindingHooks#isMatches(KeyMapping, int, int)}
     */
    public static boolean isMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        return keyMapping.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }

    /**
     * {@link KeyBindingHooks#createControlKeyMapping(String, int, String)}
     */
    public static KeyMapping createControlKeyMapping(String name, int keyCode, String category) {
        return new KeyMapping(name, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, keyCode, category);
    }
}
