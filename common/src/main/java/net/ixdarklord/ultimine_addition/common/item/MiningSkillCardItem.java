package net.ixdarklord.ultimine_addition.common.item;

import net.ixdarklord.coolcatcore.api.client.gui.ItemDecorator;
import net.ixdarklord.coolcatcore.api.item.DecoratedItem;
import net.ixdarklord.ultimine_addition.client.renderer.item.CardToolIcon;
import net.ixdarklord.ultimine_addition.client.renderer.item.PotionPointPips;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.CodecException;
import net.ixdarklord.coolcatcore.api.utils.ChatFormattingUtils;
import net.ixdarklord.coolcatcore.api.utils.CodecUtils;
import net.ixdarklord.coolcatcore.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.ixdarklord.coolcatcore.api.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem.Type.EMPTY;

public class MiningSkillCardItem extends DataAbstractItem<MiningSkillCardData> implements DecoratedItem {
    private final Type type;
    public MiningSkillCardItem(Type type, Properties properties) {
        super(properties, ComponentType.CRAFTING);
        this.type = type;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean isSelected) {
        MiningSkillCardItem.Type type = this.getType(stack);
        if (this.isLegacyMode() || type == EMPTY) return;

        if (entity instanceof ServerPlayer) {
            // A card without data yet (e.g. from /give): give it an identity; it's stored and rolls its challenges.
            if (!MiningSkillCardData.DATA_COMPONENT.has(stack)) {
                MiningSkillCardData.create(type).setStack(stack).save();
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        if (!(Minecraft.getInstance().screen instanceof SkillsRecordScreen) && this.isShiftButtonNotPressed(tooltipComponents::add)) return;
        MutableComponent component;
        if (getType() == EMPTY) {
            component = Component.translatable("tooltip.ultimine_addition.skill_card.info.empty").withStyle(ChatFormatting.GRAY);
            List<Component> components = ComponentHelper.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
            return;
        }

        component = Component.translatable("tooltip.ultimine_addition.skill_card.tier", !MiningSkillCardData.DATA_COMPONENT.has(stack) ? Component.literal("§kNawaf") : getData(stack).getTier().getDisplayName());
        tooltipComponents.add(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY).append(component.withStyle(ChatFormatting.GRAY)));

        MiningSkillCardData data = MiningSkillCardData.DATA_COMPONENT.get(stack);
        // Potion points live outside the item; skip the line until they've been synced.
        if (type != EMPTY && data != null && !data.isCreativeItem() && data.getTier() != Tier.Unlearned && data.getTier() != Tier.Mastered && data.hasProgress()) {
            ChatFormatting formatting = ChatFormattingUtils.getProgressColor(data.getPotionPoints(), data.getMaxPotionPoints());
            component = Component.translatable("tooltip.ultimine_addition.skill_card.potion_point", Component.literal(String.valueOf(data.getPotionPoints())).withStyle(formatting));
            tooltipComponents.add(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY).append(component.withStyle(ChatFormatting.GRAY)));
        }

        if (Minecraft.getInstance().screen instanceof SkillsRecordScreen screen &&
                screen.getMenu().getCardSlots().stream().anyMatch(slot -> slot.getItem().equals(stack))) return;

        if (data == null || data.getTier() != Tier.Mastered) {
            component = Component.translatable("tooltip.ultimine_addition.skill_card.info").withStyle(ChatFormatting.WHITE);
            List<Component> components = ComponentHelper.splitComponent(component, getSplitterLength());
            tooltipComponents.addAll(components);
        }
    }

    // No durability bar: the potion points show as pips on the card, drawn by its decorator.
    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        return false;
    }

    // CoolCatLib takes it on the client, the first time a card is drawn (never on a dedicated server).
    @Override
    public void registerDecorators(Consumer<ItemDecorator> registrar) {
        registrar.accept(CardToolIcon.INSTANCE);
        registrar.accept(PotionPointPips.INSTANCE);
    }

    @Override
    public MiningSkillCardData getData(ItemStack stack) {
        return MiningSkillCardData.load(stack);
    }

    /** The item's own type. A stack's type is {@link #getType(ItemStack)}: the generic card keeps it on the stack. */
    public Type getType() {
        return type;
    }

    public Type getType(ItemStack stack) {
        return this.type;
    }

    public static boolean isTierEqual(ItemStack stack, Tier tier) {
        return MiningSkillCardData.load(stack).getTier() == tier;
    }

    public static class Type {
        public static final Type EMPTY = new Type(true, "empty", List.of());
        public static final Type PICKAXE = new Type(true, "pickaxe", List.of());
        public static final Type AXE = new Type(true, "axe", List.of());
        public static final Type SHOVEL = new Type(true, "shovel", List.of());
        public static final Type HOE = new Type(true, "hoe", List.of());
        // The stand-in type of the generic card item and the generic Mine-Go Juice: a data pack card's real type is on
        // its stack (GenericMiningSkillCardItem), and a generic juice's is with the player who drank it.
        public static final Type GENERIC = new Type(true, "generic", List.of());
        private static final List<Type> BUILT_IN = List.of(EMPTY, PICKAXE, AXE, SHOVEL, HOE);
        // The built-in types and the ones data packs define (DataCardTypes), which come and go with the data packs.
        public static List<Type> TYPES = new ArrayList<>(BUILT_IN);

        private final boolean active;
        private final String id;
        private final List<String> requiredTools;
        private final Color potionColor;
        // Only data pack types: what players read, and the item drawn on the card.
        private final boolean data;
        private final String name;
        private final String juiceName;
        private final String icon;

        public static final Codec<Type> CODEC = Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(Type.fromString(s));
            } catch (CodecException | IllegalArgumentException e) {
                return DataResult.error(() -> s + " is not present.");
            }
        }, Type::getId);

        public Type(boolean active, String id, List<String> requiredTools) {
            this(active, id, requiredTools, Color.WHITE);
        }

        public Type(boolean active, String id, List<String> requiredTools, Color potionColor) {
            this.active = active;
            this.id = validateId(id);
            this.requiredTools = requiredTools;
            this.potionColor = potionColor;
            this.data = false;
            this.name = "";
            this.juiceName = "";
            this.icon = "";
        }

        private Type(String id, String name, List<String> tools, Color juiceColor, String juiceName, String icon) {
            this.active = true;
            this.id = id;
            this.requiredTools = List.copyOf(tools);
            this.potionColor = juiceColor;
            this.data = true;
            this.name = name;
            this.juiceName = juiceName;
            this.icon = icon;
        }

        /** A card type from a data pack: its id is the file's, like "mypack:hammer". */
        public static Type data(String id, String name, List<String> tools, int juiceColor, String juiceName, String icon) {
            return new Type(id, name, tools, new Color(juiceColor), juiceName, icon);
        }

        private String validateId(String input) {
            String pattern = "^[a-z0-9_.-]+$";
            if (!input.matches(pattern))
                throw new IllegalArgumentException("Invalid Custom Card Id format! Non [a-z0-9_.-] character exists. (\"%s\")".formatted(input));
            return input;
        }

        /** Swaps the data pack types for these; the built-in ones stay. */
        public static void setDataTypes(List<Type> types) {
            List<Type> all = new ArrayList<>(BUILT_IN);
            all.addAll(types);
            TYPES = all;
        }

        public static List<Type> getDataTypes() {
            return TYPES.stream().filter(Type::isData).toList();
        }

        /** The type of this id, or null: unlike {@link #fromString} it doesn't throw. */
        public static @Nullable Type byId(String id) {
            for (Type type : TYPES) {
                if (type.getId().equals(id)) return type;
            }
            return null;
        }

        public boolean isData() {
            return data;
        }

        public String getName() {
            return name;
        }

        public String getJuiceName() {
            return juiceName;
        }

        public String getIcon() {
            return icon;
        }

        /**
         * The tool's name, as in "Required Skill for: Pickaxe". A data pack type's "name" is a translation key or plain
         * text (a key with no translation shows as it is written); without one it is the key
         * {@code ultimine_addition.card_type.<namespace>.<path>}, for a resource pack to translate.
         */
        public MutableComponent displayName() {
            if (!this.data) return Component.translatable("info.ultimine_addition.required_skill." + this.id);
            if (!this.name.isEmpty()) return Component.translatable(this.name);
            String path = this.id.substring(this.id.indexOf(':') + 1);
            return Component.translatableWithFallback(this.translationKey(), path.substring(path.lastIndexOf('/') + 1));
        }

        /** {@code ultimine_addition.card_type.<namespace>.<path>}: the default key of a data pack type's name. */
        public String translationKey() {
            return "ultimine_addition.card_type." + this.id.replace(':', '.').replace('/', '.');
        }

        /** The item drawn on the card's plate: the netherite tool for the built-in cards, a data pack card's "icon". */
        public ItemStack iconStack() {
            if (this == PICKAXE) return new ItemStack(Items.NETHERITE_PICKAXE);
            if (this == AXE) return new ItemStack(Items.NETHERITE_AXE);
            if (this == SHOVEL) return new ItemStack(Items.NETHERITE_SHOVEL);
            if (this == HOE) return new ItemStack(Items.NETHERITE_HOE);
            if (!this.data || this.icon.isEmpty()) return ItemStack.EMPTY;
            ResourceLocation itemId = ResourceLocation.tryParse(this.icon);
            Item item = itemId == null ? Items.AIR : BuiltInRegistries.ITEM.get(itemId);
            return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }

        // Data pack types are made again on every reload (and on the client, from the server's list): the same id is
        // the same type.
        @Override
        public boolean equals(Object other) {
            return other instanceof Type type && type.id.equals(this.id);
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        public boolean isActive() {
            return active;
        }

        public String getId() {
            return id;
        }

        public ResourceLocation getRegistryId() {
            return FTBUltimineAddition.id("mining_skill_card_%s".formatted(this.data ? GENERIC.id : id));
        }

        public List<String> getRequiredTools() {
            return requiredTools;
        }

        public Color getPotionColor() {
            return potionColor;
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
                    BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, new ResourceLocation(value.replaceAll("#", "")))).ifPresent(holders ->
                            items.addAll(holders.stream().map(Holder::value).toList()));
                    if (!items.isEmpty()) list.addAll(items);
                } else {
                    Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(value));
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

        public static StreamCodec<FriendlyByteBuf, Tier> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull Tier decode(FriendlyByteBuf buf) {
                return buf.readEnum(Tier.class);
            }

            @Override
            public void encode(FriendlyByteBuf buf, Tier tier) {
                buf.writeEnum(tier);
            }
        };

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
            throw new IllegalArgumentException("There is no tier enum with value: " + input);
        }

    }
}
