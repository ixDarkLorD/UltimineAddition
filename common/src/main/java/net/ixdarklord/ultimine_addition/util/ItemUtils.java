package net.ixdarklord.ultimine_addition.util;

import net.ixdarklord.coolcatlib.api.util.SlotReference;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ItemUtils {
    public record ItemSorter(ItemStack item, int slotId, int order){}

    public static ItemStack getItemInHand(Player player, boolean checkBoth) {
        ItemStack stack = ItemStack.EMPTY;
        if (player.getMainHandItem() != ItemStack.EMPTY)
            stack = player.getMainHandItem();
        else if (checkBoth)
            stack = player.getOffhandItem();

        if (stack.getItem() != Items.AIR) return stack;
        return ItemStack.EMPTY;
    }

    public static ItemStack findItemInHand(Player player, Item item) {
        ItemStack stack = ServicePlatform.get().slotAPI().isModLoaded()
                ? ServicePlatform.get().slotAPI().getSkillsRecordItem(player)
                : player.getMainHandItem();
        if (stack.getItem() != item) stack = player.getOffhandItem();

        if (stack.getItem() == item) return stack;
        return ItemStack.EMPTY;
    }

    public static int getSlotIndex(@Nullable InteractionHand hand) {
        int index = -1;
        switch (hand) {
            case MAIN_HAND -> index = Objects.requireNonNull(SlotRanges.nameToIds("weapon.mainhand")).slots().getFirst();
            case OFF_HAND -> index = Objects.requireNonNull(SlotRanges.nameToIds("weapon.offhand")).slots().getFirst();
            case null -> {}
        }
        return index;
    }

    public static List<SlotReference.Player> getSlotReferences(Player player, Item item) {
        return getSlotReferences(player, itemStack -> itemStack.is(item));
    }

    public static List<SlotReference.Player> getSlotReferences(Player player, Predicate<ItemStack> predicate) {
        List<SlotReference.Player> result = new ArrayList<>();
        for (String slotName : SlotRanges.singleSlotNames().toList()) {
            SlotRange slotRange = SlotRanges.nameToIds(slotName);
            if (slotRange == null) continue;
            Integer slot = slotRange.slots().getFirst();
            if (predicate.test(player.getSlot(slot).get())) {
                result.add(new SlotReference.Player(player, slot));
            }
        }
        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            ItemStack stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
            if (predicate.test(stack)) result.add(new SlotReference.Player(null, -1) {
                @Override
                public @NotNull ItemStack getItem() {
                    return ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
                }

                @Override
                public boolean setItem(ItemStack item) {
                    throw new UnsupportedOperationException("You can't set an item in the %s slot.".formatted(ServicePlatform.get().slotAPI().getAPIName()));
                }
            });
        }
        return result;
    }

    public static boolean checkTargetedBlock(Player player) {
        HitResult hit = player.pick(ServicePlatform.get().players().getReachAttribute(player), 1.0F, false);
        if (!(hit instanceof BlockHitResult hitResult)) return false;

        BlockState blockState = player.level().getBlockState(hitResult.getBlockPos());
        ItemStack tool = player.getItemInHand(player.getUsedItemHand());
        return ServicePlatform.get().players().isCorrectToolForBlock(tool, blockState);
    }

    public static boolean isItemInHandNotTools(Player player) {
        return !(getItemInHand(player, true).getItem() instanceof DiggerItem);
    }
    public static boolean isItemInHandCustomCardValid(Player player) {
        return FTBUltimineIntegration.getCustomCardTypes(player).stream()
                .map(MiningSkillCardItem.Type::utilizeRequiredTools)
                .flatMap(Collection::stream)
                .toList()
                .contains(getItemInHand(player, true).getItem());
    }
    public static boolean isItemInHandPickaxe(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return isItemInHandPaxel(player) || stack.is(ItemTags.PICKAXES) || stack.getItem() instanceof PickaxeItem;
    }

    public static boolean isItemInHandAxe(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return isItemInHandPaxel(player) || stack.is(ItemTags.AXES) || stack.getItem() instanceof AxeItem;
    }

    public static boolean isItemInHandShovel(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return isItemInHandPaxel(player) || stack.is(ItemTags.SHOVELS) || stack.getItem() instanceof ShovelItem;
    }

    public static boolean isItemInHandHoe(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return isItemInHandPaxel(player) || stack.is(ItemTags.HOES) || stack.getItem() instanceof HoeItem;
    }

    public static boolean isItemInHandPaxel(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return ServicePlatform.get().players().isToolPaxel(stack);
    }
}
