package net.ixdarklord.ultimine_addition.util;

import com.google.common.collect.Lists;
import net.ixdarklord.coolcatlib.api.util.SlotReference;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() != item)
            stack = player.getOffhandItem();

        if (ServicePlatform.get().slotAPI().isModLoaded() && stack.getItem() != item) {
            stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
        }
        return stack.getItem() == item ? stack : ItemStack.EMPTY;
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

    public static List<SlotReference.Player> getSlotReferences(Player player, Item item, boolean onlyInventory) {
        return getSlotReferences(player, itemStack -> itemStack.is(item), onlyInventory);
    }

    public static List<SlotReference.Player> getSlotReferences(Player player, Predicate<ItemStack> predicate, boolean onlyInventory) {
        List<SlotReference.Player> result = Lists.newArrayList();
        Predicate<String> filter = s -> {
            if (onlyInventory) {
                return s.contains("hotbar") || s.contains("inventory") || s.equals("weapon.offhand") || s.contains("armor");
            }
            return !s.equals("weapon") && !s.equals("weapon.mainhand");
        };
        List<String> slotNames = SlotRanges.singleSlotNames().filter(filter).toList();

        for (String slotName : slotNames) {
            SlotRange slotRange = SlotRanges.nameToIds(slotName);
            if (slotRange == null) continue;
            Integer slot = slotRange.slots().getFirst();
            boolean match = result.stream().anyMatch(ref -> ref.getIndex() == slot);
            if (!match && predicate.test(player.getSlot(slot).get())) {
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
        double distance = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        HitResult hit = player.pick(player.isCreative() ? distance + (double)0.5F : distance, 1.0F, false);
        if (!(hit instanceof BlockHitResult hitResult)) return false;

        BlockState blockState = player.level().getBlockState(hitResult.getBlockPos());
        ItemStack tool = player.getItemInHand(player.getUsedItemHand());
        return ServicePlatform.get().players().isCorrectToolForBlock(tool, blockState);
    }

    public static boolean isItemInHandTool(Player player) {
        return isToolItem(getItemInHand(player, true));
    }

    public static boolean isToolItem(ItemStack stack) {
        return stack.getItem() instanceof DiggerItem;
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
