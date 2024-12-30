package net.ixdarklord.ultimine_addition.client.gui.components;

import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import org.jetbrains.annotations.NotNull;

public interface MovableElement {
    boolean moveTo(int x, int y);

    default boolean step(ScreenPosition position, ScreenDirection direction) {
        return this.moveTo(position.step(direction));
    }

    default boolean moveTo(@NotNull ScreenPosition position) {
        return this.moveTo(position.x(), position.y());
    }
}
