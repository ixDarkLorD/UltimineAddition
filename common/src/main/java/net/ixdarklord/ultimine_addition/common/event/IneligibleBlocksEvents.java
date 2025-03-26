package net.ixdarklord.ultimine_addition.common.event;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import net.ixdarklord.ultimine_addition.common.data.challenge.IneligibleBlocksSavedData;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.atomic.AtomicBoolean;

public class IneligibleBlocksEvents {
    public static void init() {
        AtomicBoolean i = new AtomicBoolean();
        BlockToolModificationEvent.EVENT.register((originalState, finalState, context, toolAction, simulate) -> {
            i.set(true);
            return CompoundEventResult.pass();
        });

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (level instanceof ServerLevel serverLevel) {
                if (state.is(Blocks.AIR)) return EventResult.pass();
                var data = IneligibleBlocksSavedData.getOrCreate(serverLevel);
                if (!i.get() && placer != null) {
                    data.add(placer, new IneligibleBlocksSavedData.BlockInfo(pos, state));
                }
                i.set(false);
            }
            return EventResult.pass();
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (level instanceof ServerLevel serverLevel) {
                var data = IneligibleBlocksSavedData.getOrCreate(serverLevel);
                data.remove(pos);
            }
            return EventResult.pass();
        });

        TickEvent.SERVER_LEVEL_PRE.register(instance -> IneligibleBlocksSavedData.getOrCreate(instance).validateBlocks(instance));
    }
}
