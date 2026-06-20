package net.ixdarklord.ultimine_addition.common.menu;

import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.slot.CustomSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.MiningSkillCardSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PaperSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PenSlot;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkillsRecordMenu extends DataAbstractContainerMenu<SkillsRecordData> implements ContainerListener {
   public static final int CONTAINER_SIZE = 6;
   public static final int[] CARD_SLOTS = new int[]{0, 1, 2, 3};
   private final Player player;
   private final Inventory playerInventory;
   private final SimpleContainer container;
   public final @Nullable InteractionHand interactionHand;

   public SkillsRecordMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
      this(id, inventory, inventory.player, buf.readItem(), buf.readBoolean() ? buf.readEnum(InteractionHand.class) : null);
   }

   public SkillsRecordMenu(int id, Inventory playerInventory, Player player, ItemStack stack, @Nullable InteractionHand interactionHand) {
      super(Registration.SKILLS_RECORD_CONTAINER.get(), id);
      if (!(stack.getItem() instanceof SkillsRecordItem)) {
         throw new IllegalArgumentException("Invalid item! This container only accepts Skills Record.");
      } else {
         this.player = player;
         this.playerInventory = playerInventory;
         this.container = SkillsRecordData.load(stack).getContainer();
         this.interactionHand = interactionHand;
         this.addSlotBox(this.container, 0, 8, 107, 4, 22, 1, 0);
         this.addSlot(new PenSlot(this.container, 4, 129, 107));
         this.addSlot(new PaperSlot(this.container, 5, 151, 107));
         this.layoutPlayerInventorySlots(16, 140);
         this.addSlotListener(this);
      }
   }

   public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack) {
      if (slotIndex < 6) {
         if (this.player instanceof ServerPlayer serverPlayer) {
            SkillsRecordData data = this.getData();
            data.sendToClient(serverPlayer, this.interactionHand).save();
         }
      }
   }

   public void dataChanged(AbstractContainerMenu containerMenu, int slotIndex, int value) {
   }

   public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
      Slot sourceSlot = this.slots.get(index);
      if (!sourceSlot.hasItem()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack sourceStack = sourceSlot.getItem();
         ItemStack copyOfSourceStack = sourceStack.copy();
         if (index >= this.container.getContainerSize() && index < this.container.getContainerSize() + 36) {
            if (!this.moveItemStackTo(sourceStack, 0, this.container.getContainerSize(), false)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (index <= -1) {
               FTBUltimineAddition.LOGGER.error("Invalid slotIndex:{}", index);
               return ItemStack.EMPTY;
            }

            if (!this.moveItemStackTo(sourceStack, this.container.getContainerSize(), this.container.getContainerSize() + 36, true)) {
               return ItemStack.EMPTY;
            }
         }

         if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
         } else {
            sourceSlot.setChanged();
         }

         sourceSlot.onTake(player, sourceStack);
         return copyOfSourceStack;
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return !ItemUtils.getSkillsRecord(this.getPlayer(), this.interactionHand).isEmpty();
   }

   public Player getPlayer() {
      return this.player;
   }

   private int addSlotRange(Container container, int index, int x, int y, int amount, int dx) {
      for (int i = 0; i < amount; ++i) {
         if (container.getContainerSize() == this.container.getContainerSize()) {
            this.addSlot(new MiningSkillCardSlot(container, index, x, y));
         } else {
            this.addSlot(new Slot(container, index, x, y));
         }

         x += dx;
         ++index;
      }

      return index;
   }

   private void addSlotBox(Container container, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
      for (int j = 0; j < verAmount; ++j) {
         index = this.addSlotRange(container, index, x, y, horAmount, dx);
         y += dy;
      }

   }

   private void layoutPlayerInventorySlots(int leftCol, int topRow) {
      this.addSlotBox(this.playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
      topRow += 58;
      this.addSlotRange(this.playerInventory, 0, leftCol, topRow, 9, 18);
   }

   public NonNullList<Slot> getAllSlots() {
      NonNullList<Slot> SLOTS = NonNullList.create();

      for (int i = 0; i < this.container.getContainerSize(); ++i) {
         if (this.slots.get(i) instanceof CustomSlot) {
            SLOTS.add(i, this.slots.get(i));
         }
      }

      return SLOTS;
   }

   public NonNullList<Slot> getCardSlots() {
      NonNullList<Slot> SLOTS = NonNullList.create();

      for (int i = 0; i < this.container.getContainerSize(); ++i) {
         if (this.slots.get(i) instanceof MiningSkillCardSlot) {
            SLOTS.add(i, this.slots.get(i));
         }
      }

      return SLOTS;
   }

   public boolean isCardSlotsEmpty() {
      return this.getCardSlots().stream().noneMatch(Slot::hasItem);
   }

   public int getInkAmount() {
      ItemStack stack = this.getAllSlots().get(4).getItem();
      Item item2 = stack.getItem();
      if (item2 instanceof PenItem item) {
         return item.getData(stack).getCapacity();
      } else {
         return 0;
      }
   }

   public SkillsRecordData getData() {
      ItemStack stack = ItemUtils.getSkillsRecord(this.getPlayer(), this.interactionHand);
      return SkillsRecordData.load(stack).insertContainer(this.container);
   }
}
