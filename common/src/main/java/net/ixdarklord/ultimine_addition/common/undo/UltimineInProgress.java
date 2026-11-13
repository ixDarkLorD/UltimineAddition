package net.ixdarklord.ultimine_addition.common.undo;

import dev.ftb.mods.ftbultimine.utils.ItemCollector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The FTB Ultimine operation being carried out on the server thread, shared by the mixins on {@code FTBUltimine} and its
 * drops accumulator: whose it is, how many blocks it broke, the tool's wear before it, and the collector FTB Ultimine
 * gathers the drops in. FTB Ultimine never runs two at once (it refuses to start one while one is going).
 */
public final class UltimineInProgress {
    public static @Nullable ServerPlayer player;
    public static int broken;
    public static ItemStack tool = ItemStack.EMPTY;
    public static int toolDamage;
    public static @Nullable ItemCollector collector;
    // What went into the collector, in order (filled by ItemCollectorMixin).
    public static final List<ItemStack> collected = new ArrayList<>();

    private UltimineInProgress() {}

    public static void begin(ServerPlayer who) {
        player = who;
        broken = 0;
        tool = who.getMainHandItem();
        toolDamage = tool.getDamageValue();
        collected.clear();
    }

    public static void clear() {
        player = null;
        tool = ItemStack.EMPTY;
        collector = null;
        collected.clear();
    }
}
