//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ixdarklord.ultimine_addition.util;

import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record TillResult(BlockState resultState, ItemLike droppedItem) {
    public static TillResult getTillResult(Block block) {
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> entry = HoeItem.TILLABLES.get(block);
        if (entry == null) {
            return null;
        } else {
            Consumer<UseOnContext> action = entry.getSecond();
            String name = action.getClass().getName();
            if (name.contains("changeIntoState")) {
                try {
                    Field field = action.getClass().getDeclaredFields()[0];
                    field.setAccessible(true);
                    BlockState state = (BlockState)field.get(action);
                    return new TillResult(state, null);
                } catch (Exception ignored) {
                }
            }

            if (name.contains("changeIntoStateAndDropItem")) {
                try {
                    Field[] fields = action.getClass().getDeclaredFields();
                    fields[0].setAccessible(true);
                    fields[1].setAccessible(true);
                    BlockState state = (BlockState)fields[0].get(action);
                    ItemLike drop = (ItemLike)fields[1].get(action);
                    return new TillResult(state, drop);
                } catch (Exception ignored) {
                }
            }

            return null;
        }
    }
}
