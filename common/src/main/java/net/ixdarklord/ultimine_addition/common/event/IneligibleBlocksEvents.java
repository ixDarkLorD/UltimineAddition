package net.ixdarklord.ultimine_addition.common.event;

import net.ixdarklord.coolcatcore.api.event.v2.common.BlockEvents;
import net.ixdarklord.coolcatcore.api.event.v2.common.ServerTickEvents;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResult;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResultHolder;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.atomic.AtomicBoolean;

public class IneligibleBlocksEvents {
    public static void init() {
        AtomicBoolean i = new AtomicBoolean();
        BlockToolModificationEvent.EVENT.register((originalState, context, toolAction, simulate) -> {
            i.set(true);
            return EventResultHolder.pass();
        });

        BlockEvents.PLACED.register((level, pos, state, placer) -> {
            if (level instanceof ServerLevel serverLevel) {
                if (state.is(Blocks.AIR)) return;
                var data = IneligibleBlocksSavedData.getOrCreate(serverLevel);
                if (!i.get() && placer != null) {
                    data.add(placer, new IneligibleBlocksSavedData.BlockInfo(pos, state));
                }
                i.set(false);
            }
        });

        BlockEvents.BREAK.register((level, pos, state, player) -> {
            IneligibleBlocksSavedData.getOrCreate(level).remove(pos);
            return EventResult.pass();
        });

        ServerTickEvents.START_LEVEL.register(instance -> IneligibleBlocksSavedData.getOrCreate(instance).validateBlocks(instance));
    }
}
