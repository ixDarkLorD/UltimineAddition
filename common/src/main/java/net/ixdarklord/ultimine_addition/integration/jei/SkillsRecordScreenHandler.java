package net.ixdarklord.ultimine_addition.integration.jei;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.ixdarklord.coolcatlib.api.util.RenderUtils;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class SkillsRecordScreenHandler implements IGlobalGuiHandler {
    @Override
    public @NotNull Collection<Rect2i> getGuiExtraAreas() {
        Screen SCREEN = Minecraft.getInstance().screen;
        if (SCREEN instanceof SkillsRecordScreen screen) {
            return screen.getComponentsRectangle();
        }
        return Collections.singleton(RenderUtils.EMPTY_RECT2I);
    }
}
