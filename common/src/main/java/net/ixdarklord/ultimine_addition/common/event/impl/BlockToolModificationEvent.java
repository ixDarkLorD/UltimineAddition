package net.ixdarklord.ultimine_addition.common.event.impl;

import net.ixdarklord.coolcatcore.api.event.v2.core.EventInvoker;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResultHolder;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BlockToolModificationEvent {
    public static final EventInvoker<ToolModification> EVENT = EventInvoker.create(ToolModification.class);


    public interface ToolModification {
        EventResultHolder<BlockState> modify(BlockState originalState, @NotNull UseOnContext context, ToolAction toolAction, boolean simulate);
    }

}
