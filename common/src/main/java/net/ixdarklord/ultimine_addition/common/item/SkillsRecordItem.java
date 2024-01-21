package net.ixdarklord.ultimine_addition.common.item;

import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.client.gui.component.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class SkillsRecordItem extends DataAbstractItem<SkillsRecordData> {
    public static final Component TITLE = new TranslatableComponent("item.ultimine_addition.skills_record");
    public static final int CONTAINER_SIZE = 6;
    public SkillsRecordItem(Properties properties) {
        super(properties, ComponentType.TOOLS);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        final ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, stack);

        if (player.isShiftKeyDown()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        if (this.isLegacyMode()) {
            player.displayClientMessage(new TranslatableComponent("info.ultimine_addition.legacy_mode").withStyle(ChatFormatting.RED), false);
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        if (stack.hasTag()) {
            MenuRegistry.openExtendedMenu((ServerPlayer) player,
                    new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, stack, false), TITLE),
                    buf -> buf.writeItem(stack));
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotID, boolean isSelected) {
        if (this.isLegacyMode() || level.isClientSide()) return;
        if (entity instanceof ServerPlayer player) {
            if (!stack.hasTag()) getData(stack).sendToClient(player).saveData(stack);
            if (getData(stack).getCardSlots().stream().filter(s -> !s.isEmpty()).toList().isEmpty() && getData(stack).isConsumeMode()) {
                getData(stack).sendToClient(player).toggleConsumeMode();
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (isShiftButtonNotPressed(tooltipComponents)) return;
        if (!stack.hasTag()) {
            Component component = new TranslatableComponent("tooltip.ultimine_addition.skills_record.info").withStyle(ChatFormatting.GRAY);
            List<Component> components = ScreenUtils.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
            return;
        }
        if (isConsumeChallengeExists(stack)) {
            Component state = getData(stack).isConsumeMode() ? new TranslatableComponent("options.on").withStyle(ChatFormatting.GREEN) : new TranslatableComponent("options.off").withStyle(ChatFormatting.RED);
            tooltipComponents.add(new TextComponent("• ").withStyle(ChatFormatting.DARK_GRAY).append(new TranslatableComponent("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.GRAY)));
        }
        if (!getData(stack).getPenSlot().isEmpty()) {
            tooltipComponents.add(new TextComponent("• ").withStyle(ChatFormatting.DARK_GRAY).append(new TranslatableComponent("tooltip.ultimine_addition.pen.ink_chamber",
                    (getData(stack).getPenSlot().getItem() instanceof PenItem item)
                            ? item.getData(getData(stack).getPenSlot()).getCapacity()
                            : 0
            ).withStyle(ChatFormatting.GRAY)));
        }
        tooltipComponents.add(new TextComponent("• ").withStyle(ChatFormatting.DARK_GRAY).append(new TranslatableComponent("tooltip.ultimine_addition.skills_record.contents").withStyle(ChatFormatting.GRAY)));
        tooltipComponents.add(new TextComponent(Constants.MOD_ID + ".tooltip_image"));
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        if (this.isLegacyMode()) return;
        super.fillItemCategory(category, items);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        Stream<ItemStack> itemStackStream = getData(stack).getAllSlots().stream();
        Objects.requireNonNull(nonNullList);
        itemStackStream.forEach(nonNullList::add);
        if (stack.hasTag()) {
            if (!isShiftButtonNotPressed(null)) {
                return Optional.of(new SkillsRecordTooltip(nonNullList));
            }
        }
        return Optional.empty();
    }


    public boolean isConsumeChallengeExists(ItemStack stack) {
        AtomicBoolean result = new AtomicBoolean();
        getData(stack).getCardSlots().forEach(itemStack -> {
            MiningSkillCardData cardData = new MiningSkillCardData().loadData(itemStack);
            if (!cardData.getChallenges().keySet().stream().filter(identifier -> {
                var data = ChallengesManager.INSTANCE.getAllChallenges().get(identifier.id());
                if (data != null) return data.getChallengeType().isConsuming();
                else return false;
            }).toList().isEmpty())
                result.set(true);
        });
        return result.get();
    }

    @Override
    public SkillsRecordData getData(ItemStack stack) {
        return new SkillsRecordData().loadData(stack);
    }
}
