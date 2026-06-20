package net.ixdarklord.ultimine_addition.common.event.impl;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BlockToolModificationEvent {
    public static final Event<ToolModification> EVENT = EventFactory.createCompoundEventResult(new ToolModification[0]);

    public interface ToolModification {
        CompoundEventResult<BlockState> modify(BlockState blockState, @NotNull UseOnContext useOnContext, ToolAction toolAction, boolean flag);
    }
}
