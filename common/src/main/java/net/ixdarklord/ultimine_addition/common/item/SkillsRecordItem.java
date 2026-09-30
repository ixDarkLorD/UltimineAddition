package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.ultimine_addition.util.ARGB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SkillsRecordItem extends ComponentItem {
    public static final Component TITLE = Component.translatable("item.ultimine_addition.skills_record");
    // The record's dye, a dye name in the item's NBT (1.20.1 has no base_color component, which 26.1.2 uses).
    public static final String COLOR_TAG = "Color";
    public SkillsRecordItem(Properties properties) {
        super(properties, ComponentType.TOOLS);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        final ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) return InteractionResultHolder.pass(stack);

        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (this.isLegacyMode()) {
            player.sendSystemMessage(Component.translatable("info.ultimine_addition.legacy_mode").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.pass(stack);
        }

        SkillsRecordMenu.open((ServerPlayer) player, stack, usedHand);
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean isSelected) {
        if (this.isLegacyMode() || !(entity instanceof ServerPlayer)) return;
        // Links new stacks and moves pre-SavedData contents into the storage.
        SkillsRecordData data = SkillsRecordData.get(stack, level);
        if (data.isConsumeModeActive() && !data.hasCards()) {
            data.setConsumeMode(false).save();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        // An undyed record is the white edition.
        DyeColor color = getColorOrDefault(stack);
        {
            // The dye's color, lightened so dark dyes stay readable on the tooltip.
            int rgb = ARGB.srgbLerp(0.45F, ARGB.opaque(diffuseColor(color)), 0xFFFFFFFF);
            tooltipComponents.add(Component.literal("✦ ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("tooltip.ultimine_addition.skills_record.edition." + color.getSerializedName())
                            .withStyle(style -> style.withColor(TextColor.fromRgb(rgb & 0xFFFFFF)))));
        }
        if (isShiftButtonNotPressed(tooltipComponents::add)) return;
        if (!SkillsRecordLink.isLinked(stack)) {
            Component component = Component.translatable("tooltip.ultimine_addition.skills_record.info").withStyle(ChatFormatting.GRAY);
            tooltipComponents.addAll(ComponentHelper.splitComponent(component, getSplitterLength()));
            return;
        }

        Optional<SkillsRecordData> dataOpt = SkillsRecordData.getClient(stack);
        if (dataOpt.isEmpty()) {
            tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.skills_record.loading").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            return;
        }

        SkillsRecordData data = dataOpt.get();
        if (isConsumeChallengeExists(data)) {
            Component state = data.isConsumeModeActive() ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.GRAY)));
        }
        if (!data.getPenSlot().isEmpty()) {
            tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.pen.ink_chamber", data.getInkAmount()).withStyle(ChatFormatting.GRAY)));
        }
        tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.skills_record.contents").withStyle(ChatFormatting.GRAY)));
        tooltipComponents.add(Component.literal(FTBUltimineAddition.MOD_ID + ".tooltip_image"));
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (isShiftButtonNotPressed(null)) return Optional.empty();
        return SkillsRecordData.getClient(stack).map(data -> new SkillsRecordTooltip(NonNullList.of(ItemStack.EMPTY, data.getAllSlots().toArray(ItemStack[]::new)), getColor(stack)));
    }

    /** The record's dye, or null when it's undyed (the white edition). */
    public static @Nullable DyeColor getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(COLOR_TAG, CompoundTag.TAG_STRING)) return null;
        return DyeColor.byName(tag.getString(COLOR_TAG), null);
    }

    public static DyeColor getColorOrDefault(ItemStack stack) {
        DyeColor color = getColor(stack);
        return color != null ? color : DyeColor.WHITE;
    }

    public static void setColor(ItemStack stack, @Nullable DyeColor color) {
        if (color == null) stack.removeTagKey(COLOR_TAG);
        else stack.getOrCreateTag().putString(COLOR_TAG, color.getSerializedName());
    }

    /** A dye's color as opaque RGB (26.1's DyeColor.getTextureDiffuseColor; 1.20.1 keeps it as floats). */
    public static int diffuseColor(DyeColor color) {
        float[] rgb = color.getTextureDiffuseColors();
        return ARGB.colorFromFloat(1.0F, rgb[0], rgb[1], rgb[2]);
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
