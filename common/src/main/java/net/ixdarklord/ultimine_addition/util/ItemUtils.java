package net.ixdarklord.ultimine_addition.util;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import com.google.common.collect.Lists;
import net.ixdarklord.coolcatcore.api.utils.SlotReference;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ItemUtils {

    public record ItemSorter(ItemStack item, int slotId, int order){}

    public static Optional<BlockState> getAxeStrippingState(BlockState originalState) {
        Block block = AxeItem.STRIPPABLES.get(originalState.getBlock());
        return block == null ? Optional.empty() : Optional.of(block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, (Direction.Axis)originalState.getValue(RotatedPillarBlock.AXIS)));
    }

    public static Optional<BlockState> getShovelFlatteningState(BlockState originalState) {
        BlockState state = ShovelItem.FLATTENABLES.get(originalState.getBlock());
        return Optional.ofNullable(state);
    }

    public static Optional<BlockState> getHoeTillingState(BlockState originalState) {
        TillResult tillResult = TillResult.getTillResult(originalState.getBlock());
        return tillResult == null ? Optional.empty() : Optional.of(tillResult.resultState());
    }

    public static ItemStack getItemInHand(Player player, boolean checkBoth) {
        ItemStack stack = ItemStack.EMPTY;
        if (player.getMainHandItem() != ItemStack.EMPTY)
            stack = player.getMainHandItem();
        else if (checkBoth)
            stack = player.getOffhandItem();

        return stack.getItem() != Items.AIR ? stack : ItemStack.EMPTY;
    }

    public static ItemStack findItemInHand(Player player, Item item) {
        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() != item)
            stack = player.getOffhandItem();

        if (ServicePlatform.get().slotAPI().isModLoaded() && stack.getItem() != item) {
            stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
        }

        return stack.getItem() == item ? stack : ItemStack.EMPTY;
    }

    public static int getSlotIndex(@Nullable InteractionHand hand) {
        // Player#getSlot's hand slots (1.20.1 has no SlotRanges: "weapon.mainhand" is 98, "weapon.offhand" 99).
        if (hand == null) return -1;
        return hand == InteractionHand.MAIN_HAND ? 98 : 99;
    }

    /**
     * Finds the {@link Player#getSlot(int)} index holding this exact stack instance, or -1.
     * {@code Item#inventoryTick} only gets the index within the stack's inventory section, so it is recovered by identity.
     */
    public static int findSlotIndex(Player player, ItemStack stack) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            if (inventory.items.get(i) == stack) return i;
        }
        if (player.getOffhandItem() == stack) return getSlotIndex(InteractionHand.OFF_HAND);
        return -1;
    }

    /** The item in a {@link Player#getSlot(int)} slot, or empty if the player has no such slot. */
    public static ItemStack getSlotItem(Player player, int slotIndex) {
        SlotAccess access = player.getSlot(slotIndex);
        return access != null ? access.get() : ItemStack.EMPTY;
    }

    public static ItemStack getSkillsRecord(Player player, @Nullable InteractionHand hand) {
        return hand == null ? ServicePlatform.get().slotAPI().getSkillsRecordItem(player) : getSlotItem(player, getSlotIndex(hand));
    }

    public static List<SlotReference.Player> getSlotReferences(Player player, Item item, boolean onlyInventory) {
        return getSlotReferences(player, itemStack -> itemStack.is(item), onlyInventory);
    }

    public static List<SlotReference.Player> getSlotReferences(Player player, Predicate<ItemStack> predicate, boolean onlyInventory) {
        List<SlotReference.Player> result = Lists.newArrayList();
        // 1.20.1 has no SlotRanges: Player#getSlot's ids of the hotbar and inventory (0-35), "weapon.offhand" (99) and
        // "armor.*" (100-103), plus "enderchest.*" (200-226) unless only the inventory is asked for.
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; i++) slots.add(i);
        slots.add(99);
        for (int i = 100; i < 104; i++) slots.add(i);
        if (!onlyInventory) {
            for (int i = 200; i < 227; i++) slots.add(i);
        }

        for (int slot : slots) {
            boolean match = result.stream().anyMatch(ref -> ref.getIndex() == slot);
            // Player#getSlot returns null (not SlotAccess.NULL) for slot names a player doesn't have.
            SlotAccess access = player.getSlot(slot);
            if (!match && access != null && predicate.test(access.get())) {
                result.add(new SlotReference.Player(player, slot));
            }
        }
        if (ServicePlatform.get().slotAPI().isModLoaded()) {
            ItemStack stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
            if (predicate.test(stack)) result.add(new SlotReference.Player(null, -1) {
                @Override
                public @NotNull ItemStack get() {
                    return ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
                }

                @Override
                public boolean set(ItemStack item) {
                    throw new UnsupportedOperationException("You can't set an item in the %s slot.".formatted(ServicePlatform.get().slotAPI().getAPIName()));
                }
            });
        }
        return result;
    }

    public static boolean checkTargetedBlock(Player player) {
        double distance = ServicePlatform.get().players().getBlockReach(player);
        HitResult hit = player.pick(player.isCreative() ? distance + (double)0.5F : distance, 1.0F, false);
        if (!(hit instanceof BlockHitResult hitResult)) return false;

        BlockState blockState = player.level().getBlockState(hitResult.getBlockPos());
        ItemStack tool = player.getItemInHand(player.getUsedItemHand());
        return ServicePlatform.get().players().isCorrectToolForBlock(tool, blockState);
    }

    public static boolean isItemInHandTool(Player player) {
        return isToolItem(getItemInHand(player, true));
    }

    // 1.20.1 has no tool component: the items that carry one on newer versions (diggers, swords, shears).
    public static boolean isToolItem(ItemStack stack) {
        return stack.getItem() instanceof DiggerItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof ShearsItem;
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
        return isItemInHandPaxel(player) || stack.is(ItemTags.PICKAXES);
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

    // The Mining Skill Card types whose tools include this item (a paxel counts for all four).
    public static List<MiningSkillCardItem.Type> getToolTypes(ItemStack stack) {
        List<MiningSkillCardItem.Type> types = new ArrayList<>();
        if (stack.isEmpty()) return types;
        boolean paxel = ServicePlatform.get().players().isToolPaxel(stack);
        if (paxel || stack.is(ItemTags.PICKAXES)) types.add(MiningSkillCardItem.Type.PICKAXE);
        if (paxel || stack.is(ItemTags.AXES) || stack.getItem() instanceof AxeItem) types.add(MiningSkillCardItem.Type.AXE);
        if (paxel || stack.is(ItemTags.SHOVELS) || stack.getItem() instanceof ShovelItem) types.add(MiningSkillCardItem.Type.SHOVEL);
        if (paxel || stack.is(ItemTags.HOES) || stack.getItem() instanceof HoeItem) types.add(MiningSkillCardItem.Type.HOE);
        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
            if (type.isCustomType() && type.utilizeRequiredTools().contains(stack.getItem())) types.add(type);
        }
        return types;
    }

    public static boolean isItemInHandPaxel(Player player) {
        ItemStack stack = getItemInHand(player, true);
        return ServicePlatform.get().players().isToolPaxel(stack);
    }
}
