package net.ixdarklord.ultimine_addition.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.config.UAServerConfig;
import net.minecraft.ChatFormatting;
import net.ixdarklord.ultimine_addition.common.data.item.ItemComponentType;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// The Mine-Go Juice of every card type a data pack defines: a drink that carries its type and tier. Effects and potions
// can't be registered from data packs, so these juices share one item and one effect (mine_go_juice_generic); which
// tool the effect is for is kept with the player who drank it. It looks like a potion in the type's color (the potion
// textures, tinted by the loaders' item colors through tint), and is in no creative tab: it is brewed from a data pack
// card.
public class GenericMineGoJuiceItem extends Item {
    public record Juice(String type, int tier) {
        public static final Codec<Juice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(Juice::type),
                Codec.INT.fieldOf("tier").forGetter(Juice::tier)
        ).apply(instance, Juice::new));
    }

    public static final ItemComponentType<Juice> COMPONENT = new ItemComponentType<>(FTBUltimineAddition.id("generic_juice"), Juice.CODEC);

    public GenericMineGoJuiceItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(MiningSkillCardItem.Type type, MiningSkillCardItem.Tier tier) {
        ItemStack stack = new ItemStack(ModItems.MINE_GO_JUICE_GENERIC);
        COMPONENT.set(stack, new Juice(type.getId(), tier.getValue()));
        return stack;
    }

    private static MiningSkillCardItem.Type typeOf(ItemStack stack) {
        Juice juice = COMPONENT.get(stack);
        MiningSkillCardItem.Type type = juice == null ? null : MiningSkillCardItem.Type.byId(juice.type());
        return type != null && type.isData() ? type : null;
    }

    private static MiningSkillCardItem.Tier tierOf(ItemStack stack) {
        Juice juice = COMPONENT.get(stack);
        int tier = juice == null ? 1 : Mth.clamp(juice.tier(), MiningSkillCardItem.Tier.Novice.getValue(), MiningSkillCardItem.Tier.Adept.getValue());
        return MiningSkillCardItem.Tier.fromInt(tier);
    }

    /** The item color: the bottle's contents (the model's first layer) in the type's juice color. */
    public static int tint(ItemStack stack, int layer) {
        if (layer != 0) return 0xFFFFFFFF;
        MiningSkillCardItem.Type type = typeOf(stack);
        return 0xFF000000 | (type == null ? 0xFFFFFF : type.getPotionColor().getRGB() & 0xFFFFFF);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        MiningSkillCardItem.Type type = typeOf(stack);
        return type == null ? Component.translatable("item.ultimine_addition.mine_go_juice_generic.unknown") : MineGoJuiceEffect.juiceName(type);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return net.minecraft.world.item.ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        MiningSkillCardItem.Type type = typeOf(stack);
        if (entity instanceof ServerPlayer player && type != null && !PlaystyleModes.isLegacy()) {
            MiningSkillCardItem.Tier tier = tierOf(stack);
            MineGoJuiceEffect.grant(player, type, UAServerConfig.CARD_POTION_DURATIONS.getValue(tier) * 20, tier.getValue() - 1);
        }
        // Like a potion: the bottle is left, except in creative.
        if (entity instanceof Player player && player.getAbilities().instabuild) return stack;
        stack.shrink(1);
        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        MiningSkillCardItem.Type type = typeOf(stack);
        if (type == null) return;
        tooltip.add(Component.literal("§8• ").append(Component.translatable("tooltip.ultimine_addition.skill_card.tier", tierOf(stack).getDisplayName())).withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.ultimine_addition.mine_go_juice.info", type.displayName().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        if (PlaystyleModes.isLegacy()) {
            tooltip.add(Component.translatable("tooltip.ultimine_addition.legacy_mode.disabled_item").withStyle(ChatFormatting.RED));
        }
    }
}
