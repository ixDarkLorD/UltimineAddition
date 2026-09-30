package net.ixdarklord.ultimine_addition.common.menu;

import net.ixdarklord.coolcatcore.api.menu.ExtendedMenus;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.data.record.CardSync;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordClientCache;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordLink;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordSavedData;
import net.ixdarklord.coolcatcore.api.network.codec.ByteBufCodecs;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.world.SimpleMenuProvider;
import java.util.List;
import java.util.UUID;

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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkillsRecordMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = 6;
    public static final int[] CARD_SLOTS = {0, 1, 2, 3};
    private final Player player;
    private final Inventory playerInventory;
    private final SkillsRecordData data;
    private final SimpleContainer container;
    public final @Nullable InteractionHand interactionHand;
    // The record's dye, which picks its look on the client.
    private final @Nullable DyeColor recordColor;

    public SkillsRecordMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player,
                acceptOpenData(buf),
                buf.readBoolean() ? buf.readEnum(InteractionHand.class) : null,
                buf.readBoolean() ? buf.readEnum(DyeColor.class) : null);
    }

    private SkillsRecordMenu(int id, Inventory playerInventory, Player player, SkillsRecordData data, @Nullable InteractionHand interactionHand,
                             @Nullable DyeColor recordColor) {
        super(Registration.SKILLS_RECORD_CONTAINER.get(), id);
        this.player = player;
        this.playerInventory = playerInventory;
        this.data = data;
        this.container = data.getContainer();
        this.interactionHand = interactionHand;
        this.recordColor = recordColor;

        addSlotBox(container, 0, 8, 115, 4, 22, 1, 0);
        addSlot(new PenSlot(container, 4, 125, 115));
        addSlot(new PaperSlot(container, 5, 147, 115));
        layoutPlayerInventorySlots(14, 148);
    }

    private static final StreamCodec<FriendlyByteBuf, List<CardSync>> OPEN_CARDS_CODEC = CardSync.STREAM_CODEC.apply(ByteBufCodecs.list());

    private static SkillsRecordData acceptOpenData(FriendlyByteBuf buf) {
        SkillsRecordData data = SkillsRecordClientCache.accept(SkillsRecordData.STREAM_CODEC.decode(buf));
        SkillsRecordClientCache.acceptCards(OPEN_CARDS_CODEC.decode(buf));
        return data;
    }

    public static void open(ServerPlayer player, ItemStack stack, @Nullable InteractionHand hand) {
        if (!(stack.getItem() instanceof SkillsRecordItem))
            throw new IllegalArgumentException("Invalid item! This container only accepts Skills Record.");

        SkillsRecordSavedData storage = SkillsRecordSavedData.get(player.level().getServer());
        SkillsRecordData data = storage.resolve(stack);
        DyeColor color = SkillsRecordItem.getColor(stack);
        ExtendedMenus.open(player, new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, data, hand, color), SkillsRecordItem.TITLE), buf -> {
            FriendlyByteBuf registryBuf = buf;
            SkillsRecordData.STREAM_CODEC.encode(registryBuf, data);
            OPEN_CARDS_CODEC.encode(registryBuf, storage.createSyncsFor(data));
            buf.writeBoolean(hand != null);
            if (hand != null) buf.writeEnum(hand);
            buf.writeBoolean(color != null);
            if (color != null) buf.writeEnum(color);
        });
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
            FTBUltimineAddition.LOGGER.error("Invalid slotIndex:{}", index);
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
        return this.data.getUUID().equals(SkillsRecordLink.getId(ItemUtils.getSkillsRecord(this.getPlayer(), this.interactionHand)));
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
        return this.data.getInkAmount();
    }

    public @Nullable DyeColor getRecordColor() {
        return this.recordColor;
    }

    public Optional<InteractionHand> getInteractionHand() {
        return Optional.ofNullable(interactionHand);
    }

    public SkillsRecordData getData() {
        return this.data;
    }
}
