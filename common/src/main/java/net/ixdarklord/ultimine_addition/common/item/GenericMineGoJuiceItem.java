package net.ixdarklord.ultimine_addition.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// The Mine-Go Juice of every card type a data pack defines: a drink that carries its type and tier. Effects and potions
// can't be registered from data packs, so these juices share one item and one effect (mine_go_juice_generic); which
// tool the effect is for is kept with the player who drank it. It looks like a potion in the type's color (the
// vanilla potion model, tinted through potion_contents), and is in no creative tab: it is brewed from a data pack card.
public class GenericMineGoJuiceItem extends Item {
    public record Juice(String type, int tier) {
        public static final Codec<Juice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(Juice::type),
                Codec.INT.fieldOf("tier").forGetter(Juice::tier)
        ).apply(instance, Juice::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Juice> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Juice::type, ByteBufCodecs.VAR_INT, Juice::tier, Juice::new);
    }

    public static final DataComponentType<Juice> COMPONENT = DataComponentType.<Juice>builder()
            .persistent(Juice.CODEC).networkSynchronized(Juice.STREAM_CODEC).build();

    public GenericMineGoJuiceItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(MiningSkillCardItem.Type type, MiningSkillCardItem.Tier tier) {
        ItemStack stack = new ItemStack(ModItems.MINE_GO_JUICE_GENERIC);
        stack.set(COMPONENT, new Juice(type.getId(), tier.getValue()));
        // The bottle's color; the contents themselves are applied by this item, so their tooltip stays hidden.
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(type.getPotionColor().getRGB() & 0xFFFFFF), List.of(), Optional.empty()));
        stack.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.POTION_CONTENTS, true));
        return stack;
    }

    private static MiningSkillCardItem.Type typeOf(ItemStack stack) {
        Juice juice = stack.get(COMPONENT);
        MiningSkillCardItem.Type type = juice == null ? null : MiningSkillCardItem.Type.byId(juice.type());
        return type != null && type.isData() ? type : null;
    }

    private static MiningSkillCardItem.Tier tierOf(ItemStack stack) {
        Juice juice = stack.get(COMPONENT);
        int tier = juice == null ? 1 : Math.clamp(juice.tier(), MiningSkillCardItem.Tier.Novice.getValue(), MiningSkillCardItem.Tier.Adept.getValue());
        return MiningSkillCardItem.Tier.fromInt(tier);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        MiningSkillCardItem.Type type = typeOf(stack);
        return type == null ? Component.translatable("item.ultimine_addition.mine_go_juice_generic.unknown") : MineGoJuiceEffect.juiceName(type);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        MiningSkillCardItem.Type type = typeOf(stack);
        if (entity instanceof ServerPlayer player && type != null && !PlaystyleModes.isLegacy()) {
            MiningSkillCardItem.Tier tier = tierOf(stack);
            MineGoJuiceEffect.grant(player, type, UAServerConfig.CARD_POTION_DURATIONS.getValue(tier) * 20, tier.getValue() - 1);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        MiningSkillCardItem.Type type = typeOf(stack);
        if (type == null) return;
        tooltip.accept(Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", tierOf(stack).getDisplayName())).withStyle(ChatFormatting.ITALIC));
        tooltip.accept(Component.translatable("tooltip.ultimine_addition.mine_go_juice.info", type.displayName().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        if (PlaystyleModes.isLegacy()) {
            tooltip.accept(Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
        }
    }
}
