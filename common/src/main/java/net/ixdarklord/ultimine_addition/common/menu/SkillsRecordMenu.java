package net.ixdarklord.ultimine_addition.common.menu;


import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.item.SkillsRecordItem;
import net.ixdarklord.ultimine_addition.common.menu.slot.CustomSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.MiningSkillCardSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PaperSlot;
import net.ixdarklord.ultimine_addition.common.menu.slot.PenSlot;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class SkillsRecordMenu extends DataAbstractContainerMenu<SkillsRecordData> {
    private final ItemStack stack;
    private final Player player;
    private final Inventory playerInventory;
    private final Container container;
    private final boolean isSlotAPI;

    public SkillsRecordMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player, buf.readItem(), buf.readBoolean());
    }

    public SkillsRecordMenu(int id, Inventory playerInventory, Player player, ItemStack stack, boolean isSlotAPI) {
        super(Registration.SKILLS_RECORD_CONTAINER.get(), id);
        if (!(stack.getItem() instanceof SkillsRecordItem))
            throw new IllegalArgumentException("Invalid item! This container only accepts Skills Record.");

        this.stack = stack;
        this.player = player;
        this.playerInventory = playerInventory;
        this.container = createData().getContainer();
        this.isSlotAPI = isSlotAPI;

        addSlotBox(container, 0, 8, 107, 4, 20, 1, 0);
        addSlot(new PenSlot(container, 4, 132, 107));
        addSlot(new PaperSlot(container, 5, 152, 107));
        layoutPlayerInventorySlots(8, 136);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);

        if (player instanceof ServerPlayer serverPlayer)
            createData().insertContainer(container).sendToClient(serverPlayer).saveData(stack);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index >= container.getContainerSize() && index < container.getContainerSize() + 36) {
            if (!moveItemStackTo(sourceStack, 0, container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (index > -1) {
            if (!moveItemStackTo(sourceStack, container.getContainerSize(), container.getContainerSize() + 36, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            UltimineAddition.LOGGER.error("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (isSlotAPI)
            return ServicePlatform.SlotAPI.getSkillsRecordItem(player).equals(stack);
        return player.getMainHandItem().equals(stack) || player.getOffhandItem().equals(stack);
    }

    public Player getPlayer() {
        return this.player;
    }

    private int addSlotRange(Container container, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if (container.getContainerSize() == this.container.getContainerSize()) {
                addSlot(new MiningSkillCardSlot(container, index, x, y));
            } else addSlot(new Slot(container, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private void addSlotBox(Container container, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(container, index, x, y, horAmount, dx);
            y += dy;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    public NonNullList<Slot> getAllSlots() {
        NonNullList<Slot> SLOTS = NonNullList.create();
        for (int i = 0; i < this.container.getContainerSize(); i++) {
            if (slots.get(i) instanceof CustomSlot) SLOTS.add(i, slots.get(i));
        }
        return SLOTS;
    }

    public NonNullList<Slot> getCardSlots() {
        NonNullList<Slot> SLOTS = NonNullList.create();
        for (int i = 0; i < this.container.getContainerSize(); i++) {
            if (slots.get(i) instanceof MiningSkillCardSlot) SLOTS.add(i, slots.get(i));
        }
        return SLOTS;
    }

    public boolean isCardSlotsEmpty() {
        AtomicBoolean value = new AtomicBoolean(true);
        this.getCardSlots().forEach(slot -> {
            if (!slot.getItem().isEmpty()) value.set(false);
        });
        return value.get();
    }

    public int getInkAmount() {
        ItemStack stack = getAllSlots().get(4).getItem();
        if (stack.getItem() instanceof PenItem item) {
            return item.getData(stack).getCapacity();
        }
        return 0;
    }

    public SkillsRecordData getData() {
        return createData().insertContainer(container);
    }

    @Override
    public SkillsRecordData createData() {
        return new SkillsRecordData().loadData(stack);
    }
}
