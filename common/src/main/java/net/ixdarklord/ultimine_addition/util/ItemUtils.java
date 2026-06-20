package net.ixdarklord.ultimine_addition.util;

import com.google.common.collect.Lists;
import net.ixdarklord.coolcatlib.api.utils.SlotReference;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ItemUtils {
   public static ItemStack getItemInHand(Player player, boolean checkBoth) {
      ItemStack stack = ItemStack.EMPTY;
      if (player.getMainHandItem() != ItemStack.EMPTY) {
         stack = player.getMainHandItem();
      } else if (checkBoth) {
         stack = player.getOffhandItem();
      }

      return stack.getItem() != Items.AIR ? stack : ItemStack.EMPTY;
   }

   public static ItemStack findItemInHand(Player player, Item item) {
      ItemStack stack = player.getMainHandItem();
      if (stack.getItem() != item) {
         stack = player.getOffhandItem();
      }

      if (ServicePlatform.get().slotAPI().isModLoaded() && stack.getItem() != item) {
         stack = ServicePlatform.get().slotAPI().getSkillsRecordItem(player);
      }

      return stack.getItem() == item ? stack : ItemStack.EMPTY;
   }

   public static int getSlotIndex(@Nullable InteractionHand hand) {
      int index = -1;
      if (hand != null) {
         switch (hand) {
            case MAIN_HAND -> index = EquipmentSlot.MAINHAND.getIndex(98);
            case OFF_HAND -> index = EquipmentSlot.OFFHAND.getIndex(98);
         }

      }
      return index;
   }

   public static ItemStack getSkillsRecord(Player player, @Nullable InteractionHand hand) {
      return hand == null ? ServicePlatform.get().slotAPI().getSkillsRecordItem(player) : player.getSlot(getSlotIndex(hand)).get();
   }

   public static List<SlotReference.Player> getSlotReferences(Player player, Item item, boolean onlyInventory) {
      return getSlotReferences(player, itemStack -> itemStack.is(item), onlyInventory);
   }

   public static List<SlotReference.Player> getSlotReferences(Player player, Predicate<ItemStack> predicate, boolean onlyInventory) {
      List<SlotReference.Player> result = Lists.newArrayList();
      List<Integer> slots = new ArrayList<>();

      for (int i = 0; i < 36; ++i) {
         slots.add(i);
      }

      if (!onlyInventory) {
         slots.add(EquipmentSlot.OFFHAND.getIndex(98));
      }

      for (int slot : slots) {
         boolean match = result.stream().anyMatch(ref -> ref.getIndex() == slot);
         if (!match && predicate.test(player.getSlot(slot).get())) {
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
      double distance = ServicePlatform.get().players().getBlockReachAttribute(player);
      HitResult hit = player.pick(player.isCreative() ? distance + (double) 0.5F : distance, 1.0F, false);
      if (hit instanceof BlockHitResult hitResult) {
         BlockState blockState = player.level().getBlockState(hitResult.getBlockPos());
         ItemStack tool = player.getItemInHand(player.getUsedItemHand());
         return ServicePlatform.get().players().isCorrectToolForBlock(tool, blockState);
      } else {
         return false;
      }
   }

   public static boolean isItemInHandTool(Player player) {
      return isToolItem(getItemInHand(player, true));
   }

   public static boolean isToolItem(ItemStack stack) {
      return stack.getItem() instanceof DiggerItem;
   }

   public static boolean isItemInHandCustomCardValid(Player player) {
      return FTBUltimineIntegration.getCustomCardTypes(player).stream().map(MiningSkillCardItem.Type::utilizeRequiredTools).flatMap(Collection::stream).toList().contains(getItemInHand(player, true).getItem());
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

   public record ItemSorter(ItemStack item, int slotId, int order) {
   }
}
