package net.ixdarklord.ultimine_addition.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

public class KeyHandler {
    private static final String KEY_CATEGORY = String.format("key.category.%s.general", "ultimine_addition");
    public static KeyMapping KEY_SHOW_PROGRESSION_BAR;
    public static KeyMapping KEY_OPEN_SKILLS_RECORD;

    public static KeyMapping create(ResourceLocation id, InputConstants.Type type, int key, String category) {
        return new KeyMapping("key.%s.%s".formatted(id.getNamespace(), id.getPath()), type, key, category);
    }

    public static void register() {
        KeyMappingRegistry.register(KEY_SHOW_PROGRESSION_BAR);
        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            KeyMappingRegistry.register(KEY_OPEN_SKILLS_RECORD);
        }

    }

    static {
        KEY_SHOW_PROGRESSION_BAR = create(FTBUltimineAddition.id("show_progression_bar"), Type.KEYSYM, 342, KEY_CATEGORY);
        KEY_OPEN_SKILLS_RECORD = create(FTBUltimineAddition.id("open_skills_record"), Type.KEYSYM, 82, KEY_CATEGORY);
    }
}
