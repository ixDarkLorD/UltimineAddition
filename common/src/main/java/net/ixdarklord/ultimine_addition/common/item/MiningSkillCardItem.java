package net.ixdarklord.ultimine_addition.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.CodecException;
import net.ixdarklord.coolcat_lib.util.ScreenUtils;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.client.gui.screen.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.handler.ItemRendererHandler;
import net.ixdarklord.ultimine_addition.client.renderer.item.IItemRenderer;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemRenderer;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.util.CodecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
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
            component = Component.translatable("tooltip.ultimine_addition.skill_card.info.empty").withStyle(ChatFormatting.GRAY);
            List<Component> components = ScreenUtils.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
            return;
        }

        component = Component.translatable("tooltip.ultimine_addition.skill_card.tier", !stack.hasTag() ? "§kNawaf" : getData(stack).getTier().getDisplayName());
        tooltipComponents.add(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.DARK_GRAY).append(component));
        if (stack.hasTag() && type != EMPTY && getData(stack).getTier() != Tier.Unlearned && getData(stack).getTier() != Tier.Mastered) {
            component = Component.translatable("tooltip.ultimine_addition.skill_card.potion_point", getData(stack).getPotionPoints());
            tooltipComponents.add(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY).append(component));
        }

        if (Minecraft.getInstance().screen instanceof SkillsRecordScreen screen &&
                screen.getMenu().getCardSlots().stream().anyMatch(slot -> slot.getItem().equals(stack))) return;

        component = Component.translatable("tooltip.ultimine_addition.skill_card.info").withStyle(ChatFormatting.GRAY);
        List<Component> components = ScreenUtils.splitComponent(component, getSplitterLength());
        tooltipComponents.addAll(components);
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (this.isLegacyMode()) return;

        if (getType() == EMPTY) super.fillItemCategory(creativeModeTab, nonNullList);
        if (this.allowedIn(creativeModeTab) && getType() != EMPTY) {
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

    public static class Type {
        public static final Type EMPTY = new Type(true, "empty", List.of(), Items.BARRIER);
        public static final Type PICKAXE = new Type(true, "pickaxe", List.of(), Items.NETHERITE_PICKAXE);
        public static final Type AXE = new Type(true, "axe", List.of(), Items.NETHERITE_AXE);
        public static final Type SHOVEL = new Type(true, "shovel", List.of(), Items.NETHERITE_SHOVEL);
        public static final Type HOE = new Type(true, "hoe", List.of(), Items.NETHERITE_HOE);
        public static List<Type> TYPES = new ArrayList<>();

        private final boolean active;
        private final String id;
        private final List<String> requiredTools;
        private final Color potionColor;
        private final Item defaultDisplayItem;

        public static final Codec<Type> CODEC = Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(Type.fromString(s));
            } catch (CodecException e) {
                return DataResult.error(s + " is not present.");
            }
        }, Type::getId);

        public static final Codec<Type> CARD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("active").forGetter(Type::isActive),
                Codec.STRING.fieldOf("card_id").forGetter(Type::getId),
                Codec.STRING.listOf().fieldOf("required_tools").forGetter(Type::getRequiredTools),
                CodecHelper.COLOR_CODEC.optionalFieldOf("potion_color", Color.WHITE).forGetter(Type::getPotionColor),
                CodecHelper.ITEM_CODEC.optionalFieldOf("default_display_item", Items.BARRIER).forGetter(Type::getDefaultDisplayItem)
        ).apply(instance, Type::new));



        public Type(boolean active, String id, List<String> requiredTools) {
            this(active, id, requiredTools, Color.WHITE, Items.BARRIER);
        }

        public Type(boolean active, String id, List<String> requiredTools, Item defaultDisplayItem) {
            this(active, id, requiredTools, Color.WHITE, defaultDisplayItem);
        }

        public Type(boolean active, String id, List<String> requiredTools, Color potionColor, Item defaultDisplayItem) {
            this.active = active;
            this.id = validateId(id);
            this.requiredTools = requiredTools;
            this.potionColor = potionColor;
            this.defaultDisplayItem = defaultDisplayItem;
        }

        private String validateId(String input) {
            String pattern = "^[a-z0-9_.-]+$";
            if (!input.matches(pattern))
                throw new IllegalArgumentException("Invalid Custom Card Id format! Non [a-z0-9_.-] character exists. (\"%s\")".formatted(input));
            return input;
        }

        public static void refreshTypes() {
            TYPES = new ArrayList<>(List.of(EMPTY, PICKAXE, AXE, SHOVEL, HOE));
            TYPES.addAll(CustomMSCApi.CUSTOM_TYPES);
        }

        public boolean isActive() {
            return active;
        }

        public String getId() {
            return id;
        }

        public List<String> getRequiredTools() {
            return requiredTools;
        }

        public Color getPotionColor() {
            return potionColor;
        }
        public Item getDefaultDisplayItem() {
            return defaultDisplayItem;
        }

        public static Type fromString(String input) {
            for (Type type : TYPES) {
                if (type.getId().equalsIgnoreCase(input) && !input.equalsIgnoreCase(EMPTY.getId())) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No type with the specified name");
        }

        public boolean isCustomType() {
            return TYPES.stream()
                    .filter(t -> !t.equals(EMPTY) && !t.equals(PICKAXE) && !t.equals(AXE) && !t.equals(SHOVEL) && !t.equals(HOE))
                    .toList().contains(this);
        }

        public List<Item> utilizeRequiredTools() {
            List<Item> list = new ArrayList<>();
            if (requiredTools == null) return list;
            for (String value : requiredTools) {
                if (value.startsWith("#")) {
                    List<Item> items = new ArrayList<>();
                    Registry.ITEM.getTag(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(value.replaceAll("#", "")))).ifPresent(holders ->
                            items.addAll(holders.stream().map(Holder::value).toList()));
                    if (!items.isEmpty()) list.addAll(items);
                } else {
                    Item item = Registry.ITEM.get(new ResourceLocation(value));
                    if (item != Items.AIR) list.add(item);
                }
            }
            return list;
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
                case 1 -> Component.translatable(this.descriptionId()).withStyle(ChatFormatting.GREEN);
                case 2 -> Component.translatable(this.descriptionId()).withStyle(ChatFormatting.AQUA);
                case 3 -> Component.translatable(this.descriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE);
                case 4 -> Component.translatable(this.descriptionId()).withStyle(ChatFormatting.GOLD);
                default -> Component.translatable(this.descriptionId());
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
