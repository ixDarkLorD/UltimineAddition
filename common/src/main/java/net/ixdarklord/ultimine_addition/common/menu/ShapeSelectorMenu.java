package net.ixdarklord.ultimine_addition.common.menu;

import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.util.ItemUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShapeSelectorMenu extends AbstractContainerMenu {
    private final Player player;

    public ShapeSelectorMenu(int id, Inventory inventory) {
        this(id, inventory, inventory.player);
    }

    public ShapeSelectorMenu(int id, Inventory inventory, Player player) {
        super(Registration.SHAPE_SELECTOR_CONTAINER.get(), id);
        this.player = player;
        this.addSlot(new Slot(new SimpleContainer(ItemStack.EMPTY), 0, 22, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ItemUtils.isToolItem(stack);
            }
        });
        this.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu containerToSend, int slotIndex, ItemStack stack) {

            }

            @Override
            public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {

            }
        });

        this.addPlayerInventory(inventory);
        this.addPlayerHotbar(inventory);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index == 0) {
            if (!moveItemStackTo(sourceStack, 1, 4 * 9 + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index > 0) {
            if (!moveItemStackTo(sourceStack, 0, 4 * 9, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        return copyOfSourceStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        ItemStack stack = slots.getFirst().getItem();
        if (stack.isEmpty()) return;

        if (serverPlayer.isAlive() && !serverPlayer.hasDisconnected()) {
            player.getInventory().placeItemBackInInventory(stack);
        } else serverPlayer.drop(stack, false);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 9 + l * 18, 88 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 9 + i * 18, 146));
        }
    }
}
