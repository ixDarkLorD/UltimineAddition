package net.ixdarklord.ultimine_addition.hooks;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;

public class KeyBindingHooks {
    @ExpectPlatform
    public static boolean isMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        throw new AssertionError();
    }

    // A key mapping pressed with Ctrl, where the loader supports key modifiers (Forge); otherwise the plain key.
    @ExpectPlatform
    public static KeyMapping createControlKeyMapping(String name, int keyCode, String category) {
        throw new AssertionError();
    }
}
