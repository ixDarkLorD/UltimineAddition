package net.ixdarklord.ultimine_addition.common.item;

import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.coolcatlib.api.util.ComponentHelper;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
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
    public static final Component TITLE = Component.translatable("item.ultimine_addition.skills_record");
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
            player.displayClientMessage(Component.translatable("info.ultimine_addition.legacy_mode").withStyle(ChatFormatting.RED), false);
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        if (stack.has(SkillsRecordData.DATA_COMPONENT)) {
            MenuRegistry.openExtendedMenu((ServerPlayer) player,
                    new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, stack, usedHand), TITLE),
                    buf -> {
                        ItemStack.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buf, ((ServerPlayer) player).serverLevel().registryAccess()), stack);
                        buf.writeBoolean(true);
                        buf.writeEnum(usedHand);
            });
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotID, boolean isSelected) {
        if (this.isLegacyMode() || level.isClientSide()) return;
        if (entity instanceof ServerPlayer) {
            if (!stack.has(SkillsRecordData.DATA_COMPONENT)) getData(stack).saveData(stack);
            if (getData(stack).getCardSlots().stream().filter(s -> !s.isEmpty()).toList().isEmpty() && getData(stack).isConsumeMode()) {
                getData(stack).toggleConsumeMode().saveData(stack);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        if (isShiftButtonNotPressed(tooltipComponents)) return;
        if (!stack.has(SkillsRecordData.DATA_COMPONENT)) {
            Component component = Component.translatable("tooltip.ultimine_addition.skills_record.info").withStyle(ChatFormatting.GRAY);
            List<Component> components = ComponentHelper.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
            return;
        }
        if (isConsumeChallengeExists(stack)) {
            Component state = getData(stack).isConsumeMode() ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.GRAY)));
        }
        if (!getData(stack).getPenSlot().isEmpty()) {
            tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.pen.ink_chamber",
                    (getData(stack).getPenSlot().getItem() instanceof PenItem item)
                            ? item.getData(getData(stack).getPenSlot()).getCapacity()
                            : 0
            ).withStyle(ChatFormatting.GRAY)));
        }
        tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.skills_record.contents").withStyle(ChatFormatting.GRAY)));
        tooltipComponents.add(Component.literal(UltimineAddition.MOD_ID + ".tooltip_image"));
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        Stream<ItemStack> itemStackStream = getData(stack).getAllSlots().stream();
        Objects.requireNonNull(nonNullList);
        itemStackStream.forEach(nonNullList::add);
        if (stack.has(SkillsRecordData.DATA_COMPONENT)) {
            if (!isShiftButtonNotPressed(null)) {
                return Optional.of(new SkillsRecordTooltip(nonNullList));
            }
        }
        return Optional.empty();
    }


    public boolean isConsumeChallengeExists(ItemStack stack) {
        AtomicBoolean result = new AtomicBoolean();
        getData(stack).getCardSlots().forEach(itemStack -> {
            MiningSkillCardData cardData = MiningSkillCardData.loadData(itemStack);
            if (!cardData.getChallenges().stream().filter(challengeData -> {
                var data = ChallengesManager.INSTANCE.getAllChallenges().get(challengeData.getId());
                if (data != null) return data.getChallengeType().isConsuming();
                else return false;
            }).toList().isEmpty())
                result.set(true);
        });
        return result.get();
    }

    @Override
    public SkillsRecordData getData(ItemStack stack) {
        return SkillsRecordData.loadData(stack);
    }
}
