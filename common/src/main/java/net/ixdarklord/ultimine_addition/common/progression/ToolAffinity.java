package net.ixdarklord.ultimine_addition.common.progression;

import com.mojang.serialization.Codec;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

// A tool gets to know its work: it counts the blocks Ultimined with it (the "affinity" component on the tool itself),
// and every 500 of them add a percent to the blocks an Ultimine with that tool can take, up to ten.
public final class ToolAffinity {
    public static final DataComponentType<Integer> COMPONENT = DataComponentType.<Integer>builder()
            .persistent(Codec.intRange(0, Integer.MAX_VALUE)).networkSynchronized(ByteBufCodecs.VAR_INT).build();
    public static final int BLOCKS_PER_PERCENT = 500;
    public static final int MAX_PERCENT = 10;

    private ToolAffinity() {}

    public static boolean enabled() {
        return UAServerConfig.TOOL_AFFINITY.get() && !PlaystyleModes.isLegacy();
    }

    public static int blocks(ItemStack tool) {
        Integer blocks = tool.get(COMPONENT);
        return blocks == null ? 0 : blocks;
    }

    /** The percent this tool adds to an Ultimine's blocks. */
    public static int percent(ItemStack tool) {
        return enabled() ? Math.min(MAX_PERCENT, blocks(tool) / BLOCKS_PER_PERCENT) : 0;
    }

    /** An Ultimine of this many blocks was done with this tool (only tools that wear count: they are the ones kept). */
    public static void add(ItemStack tool, int blocks) {
        if (!enabled() || blocks <= 0 || tool.isEmpty() || !tool.isDamageableItem()) return;
        tool.set(COMPONENT, (int) Math.min(Integer.MAX_VALUE, (long) blocks(tool) + blocks));
    }
}
