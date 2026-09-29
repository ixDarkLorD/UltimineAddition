package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.ixdarklord.coolcatcore.api.utils.ComponentHelper;
import net.ixdarklord.coolcatcore.api.item.ComponentItem;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordLink;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SkillsRecordItem extends ComponentItem {
    public static final Component TITLE = Component.translatable("item.ultimine_addition.skills_record");
    public SkillsRecordItem(Properties properties) {
        super(properties, ComponentType.TOOLS);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand usedHand) {
        final ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (this.isLegacyMode()) {
            player.sendSystemMessage(Component.translatable("info.ultimine_addition.legacy_mode").withStyle(ChatFormatting.RED));
            return InteractionResult.PASS;
        }

        SkillsRecordMenu.open((ServerPlayer) player, stack, usedHand);
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        if (this.isLegacyMode() || !(entity instanceof ServerPlayer)) return;
        // Links new stacks and moves pre-SavedData contents into the storage.
        SkillsRecordData data = SkillsRecordData.get(stack, level);
        if (data.isConsumeModeActive() && !data.hasCards()) {
            data.setConsumeMode(false).save();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
        if (isShiftButtonNotPressed(tooltipComponents)) return;
        if (!SkillsRecordLink.isLinked(stack)) {
            Component component = Component.translatable("tooltip.ultimine_addition.skills_record.info").withStyle(ChatFormatting.GRAY);
            ComponentHelper.splitComponent(component, getSplitterLength()).forEach(tooltipComponents);
            return;
        }

        Optional<SkillsRecordData> dataOpt = SkillsRecordData.getClient(stack);
        if (dataOpt.isEmpty()) {
            tooltipComponents.accept(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.skills_record.loading").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            return;
        }

        SkillsRecordData data = dataOpt.get();
        if (isConsumeChallengeExists(data)) {
            Component state = data.isConsumeModeActive() ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            tooltipComponents.accept(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.GRAY)));
        }
        if (!data.getPenSlot().isEmpty()) {
            tooltipComponents.accept(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.pen.ink_chamber", data.getInkAmount()).withStyle(ChatFormatting.GRAY)));
        }
        tooltipComponents.accept(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.skills_record.contents").withStyle(ChatFormatting.GRAY)));
        tooltipComponents.accept(Component.literal(FTBUltimineAddition.MOD_ID + ".tooltip_image"));
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (isShiftButtonNotPressed(null)) return Optional.empty();
        return SkillsRecordData.getClient(stack).map(data -> new SkillsRecordTooltip(NonNullList.of(ItemStack.EMPTY, data.getAllSlots().toArray(ItemStack[]::new))));
    }

    public static boolean isConsumeChallengeExists(SkillsRecordData data) {
        for (int i = 0; i < SkillsRecordData.CARD_SLOTS; i++) {
            Optional<MiningSkillCardData> card = data.getCardData(i);
            if (card.isEmpty()) continue;
            for (MiningSkillCardData.Challenge challenge : card.get().getChallenges()) {
                ChallengeData challengeData = ChallengesManager.INSTANCE.getAllChallenges().get(challenge.getId());
                if (challengeData != null && challengeData.challengeType().isConsuming()) return true;
            }
        }
        return false;
    }

    public boolean isLegacyMode() {
        return PlaystyleModes.isLegacy();
    }

    @Override
    public boolean appendToName() {
        return true;
    }
}
