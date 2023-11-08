package net.ixdarklord.ultimine_addition.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.handler.codec.CodecException;
import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.api.UAApi;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.handler.ItemRendererHandler;
import net.ixdarklord.ultimine_addition.client.renderer.item.IItemRenderer;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemRenderer;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Type.EMPTY;

public class MiningSkillCardItem extends DataAbstractItem<MiningSkillCardData> implements IItemRenderer {
    private final Type type;
    public MiningSkillCardItem(Properties properties, Type type) {
        super(properties, ComponentType.CRAFTING);
        this.type = type;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotID, boolean isSelected) {
        if (level.isClientSide()) return;
        if (this.type == EMPTY) return;
        if (entity instanceof ServerPlayer player) {
            if (!stack.hasTag() || getData(stack).getChallenges().isEmpty())
                getData(stack).sendToClient(player).initChallenges().saveData(stack);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (!(Minecraft.getInstance().screen instanceof SkillsRecordScreen) && this.isShiftButtonNotPressed(tooltipComponents)) return;
        MutableComponent component;
        if (getType() == EMPTY) {
            component = new TranslatableComponent("tooltip.ultimine_addition.skill_card.info.empty").withStyle(ChatFormatting.GRAY);
            List<Component> components = ScreenUtils.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
            return;
        }

        component = new TranslatableComponent("tooltip.ultimine_addition.skill_card.tier", !stack.hasTag() ? "§kNawaf" : getData(stack).getTier().getDisplayName());
        tooltipComponents.add(new TextComponent("§8• ").append(component));
        if (stack.hasTag() && type != EMPTY && getData(stack).getTier() != Tier.Unlearned && getData(stack).getTier() != Tier.Mastered) {
            component = new TranslatableComponent("tooltip.ultimine_addition.skill_card.potion_point", getData(stack).getPotionPoints());
            tooltipComponents.add(new TextComponent("§8• ").append(component));
        }

        if (Minecraft.getInstance().screen instanceof SkillsRecordScreen screen &&
                screen.getMenu().getCardSlots().stream().anyMatch(slot -> slot.getItem().equals(stack))) return;

        component = new TranslatableComponent("tooltip.ultimine_addition.skill_card.info").withStyle(ChatFormatting.GRAY);
        List<Component> components = ScreenUtils.splitComponent(component, getSplitterLength());
        tooltipComponents.addAll(components);
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (getType() == EMPTY) super.fillItemCategory(creativeModeTab, nonNullList);
        if (this.allowdedIn(creativeModeTab) && getType() != EMPTY) {
            ItemStack stack = getDefaultInstance();
            getData(stack).setTier(Tier.Mastered).saveData(stack);

            nonNullList.add(new ItemStack(this));
            nonNullList.add(stack);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        var data = getData(itemStack);
        if (!itemStack.hasTag() || type == EMPTY || data.getTier() == Tier.Unlearned || data.getTier() == Tier.Mastered) return false;
        return !data.isPotionPointsFull();
    }
    @Override
    public int getBarWidth(ItemStack itemStack) {
        var data = getData(itemStack);
        return Math.round((float) data.getPotionPoints() / data.getMaxPotionPoints() * 13.0F);
    }
    @Override
    public int getBarColor(ItemStack itemStack) {
        return Mth.hsvToRgb(Math.max(0.0F, (getBarWidth(itemStack) / 13.0F)) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public UAItemRenderer createItemRenderer() {
        return ItemRendererHandler.MiningSkillCardRenderer();
    }

    @Override
    public MiningSkillCardData getData(ItemStack stack) {
        return new MiningSkillCardData().loadData(stack);
    }

    public Type getType() {
        return type;
    }

    public static boolean isTierEqual(ItemStack stack, Tier tier) {
        return new MiningSkillCardData().loadData(stack).getTier() == tier;
    }

    public record Type(String name, ItemStack defaultDisplayItem) {
        public static final Type PICKAXE = new Type("pickaxe", Items.NETHERITE_PICKAXE.getDefaultInstance());
        public static final Type AXE = new Type("axe", Items.NETHERITE_AXE.getDefaultInstance());
        public static final Type SHOVEL = new Type("shovel", Items.NETHERITE_SHOVEL.getDefaultInstance());
        public static final Type HOE = new Type("hoe", Items.NETHERITE_HOE.getDefaultInstance());
        public static final Type EMPTY = new Type("empty", Items.BARRIER.getDefaultInstance());
        public static final List<Type> TYPES = Util.make(() -> {
            List<Type> list = new ArrayList<>();
            list.addAll(List.of(EMPTY,PICKAXE,AXE,SHOVEL,HOE));
            list.addAll(UAApi.getTypes());
            return list;
        });

        public static final Codec<Type> CODEC = Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(Type.fromString(s));
            } catch (CodecException e) {
                return DataResult.error(s + " is not present.");
            }
        }, Type::name);

        public static boolean isCustomType(Type type) {
            return TYPES.stream()
                    .filter(t -> !t.equals(EMPTY) && !t.equals(PICKAXE) && !t.equals(AXE) && !t.equals(SHOVEL) && !t.equals(HOE))
                    .toList().contains(type);
        }

        public static Type fromString(String input) {
            for (Type type : TYPES) {
                if (type.name.equalsIgnoreCase(input) && !input.equalsIgnoreCase(EMPTY.name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No type with the specified name");
        }

        @Override
        public String name() {
            return this.name.toLowerCase();
        }
    }

    public enum Tier {
        Unlearned(0),
        Novice(1),
        Apprentice(2),
        Adept(3),
        Mastered(4);

        private final int tier;
        Tier(int tier) {
            this.tier = tier;
        }
        public static final Codec<Tier> CODEC = Codec.INT.comapFlatMap(i -> {
            try {
                return DataResult.success(Tier.fromInt(i));
            } catch (EnumConstantNotPresentException e) {
                return DataResult.success(Tier.Unlearned);
            }
        }, Tier::getValue);

        public boolean isEligible(Tier tier) {
            return tier.getValue() >= this.tier;
        }

        public int getValue() {
            return this.tier;
        }

        public MutableComponent getDisplayName() {
            return switch (tier) {
                case 1 -> new TranslatableComponent(this.descriptionId()).withStyle(ChatFormatting.GREEN);
                case 2 -> new TranslatableComponent(this.descriptionId()).withStyle(ChatFormatting.AQUA);
                case 3 -> new TranslatableComponent(this.descriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE);
                case 4 -> new TranslatableComponent(this.descriptionId()).withStyle(ChatFormatting.GOLD);
                default -> new TranslatableComponent(this.descriptionId());
            };
        }

        private String descriptionId() {
            return String.format("tooltip.ultimine_addition.skill_card.tier.%s", this.name().toLowerCase());
        }

        public Tier next() {
            int nextIndex = (this.ordinal() + 1) % Tier.values().length;
            return Tier.values()[nextIndex];
        }
        @SuppressWarnings("unused")
        public Tier previous() {
            int prevIndex = (this.ordinal() - 1 + Tier.values().length) % Tier.values().length;
            return Tier.values()[prevIndex];
        }
        public static String[] getNames() {
            String[] enumNames = new String[Tier.values().length];
            for (int i = 0; i < enumNames.length; i++) {
                enumNames[i] = Tier.values()[i].name().toLowerCase();
            }
            return enumNames;
        }
        public static Tier fromString(String input) {
            for (Tier enumValue : Tier.values()) {
                if (enumValue.name().equalsIgnoreCase(input)) {
                    return enumValue;
                }
            }
            return null;
        }
        public static Tier fromInt(int input) {
            for (Tier enumValue : Tier.values()) {
                if (enumValue.getValue() == input) {
                    return enumValue;
                }
            }
            throw new RuntimeException("There is no tier enum with value: " + input);
        }

    }
}
