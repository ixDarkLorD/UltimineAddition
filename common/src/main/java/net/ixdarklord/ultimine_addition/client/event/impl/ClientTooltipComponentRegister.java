package net.ixdarklord.ultimine_addition.client.event.impl;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Allows registering a mapping from {@link TooltipComponent} to {@link ClientTooltipComponent}.
 * This allows custom tooltips for items: first, override {@link Item#getTooltipImage} and return a custom {@code TooltipData}.
 * Second, register a listener to this event and convert the data to your component implementation if it's an instance of your data class.
 *
 * <p>Note that failure to map some data to a component will throw an exception,
 * so make sure that any data you return in {@link Item#getTooltipImage} will be handled by one of the callbacks.
 */
@Environment(EnvType.CLIENT)
public interface ClientTooltipComponentRegister {
    Event<ClientTooltipComponentRegister> EVENT = EventFactory.createEventResult();

    /**
     * Return the tooltip component for the passed data, or null if none is available.
     */
    @Nullable
    ClientTooltipComponent getComponent(TooltipComponent data);
}
