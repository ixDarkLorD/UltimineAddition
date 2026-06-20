package net.ixdarklord.ultimine_addition.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.CodecException;
import net.ixdarklord.coolcatlib.api.utils.ChatFormattingUtils;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.coolcatlib.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.client.gui.screens.SkillsRecordScreen;
import net.ixdarklord.ultimine_addition.client.handler.ItemRendererHandler;
import net.ixdarklord.ultimine_addition.client.renderer.item.IItemRenderer;
import net.ixdarklord.ultimine_addition.client.renderer.item.UAItemRenderer;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MiningSkillCardItem extends DataAbstractItem<MiningSkillCardData> implements IItemRenderer {
   private final Type type;

   public MiningSkillCardItem(Item.Properties properties, Type type) {
      super(properties, ComponentType.CRAFTING);
      this.type = type;
   }

   public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
      ItemStack stack = player.getItemInHand(usedHand);
      return InteractionResultHolder.pass(stack);
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotID, boolean isSelected) {
      if (!this.isLegacyMode() && !level.isClientSide() && this.type != MiningSkillCardItem.Type.EMPTY) {
         if (entity instanceof ServerPlayer && !MiningSkillCardData.hasData(stack) && this.getData(stack).getChallenges().isEmpty()) {
            this.getData(stack).initChallenges().save();
         }

      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
      super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
      if (Minecraft.getInstance().screen instanceof SkillsRecordScreen || !this.isShiftButtonNotPressed(tooltipComponents)) {
         if (this.getType() == MiningSkillCardItem.Type.EMPTY) {
            MutableComponent component = Component.translatable("tooltip.ultimine_addition.skill_card.info.empty").withStyle(ChatFormatting.GRAY);
            List<Component> components = ComponentHelper.splitComponent(component, this.getSplitterLength());
            tooltipComponents.addAll(components);
         } else {
            MutableComponent component = Component.translatable("tooltip.ultimine_addition.skill_card.tier", !stack.hasTag() ? Component.literal("§kNawaf") : this.getData(stack).getTier().getDisplayName());
            tooltipComponents.add(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY).append(component.withStyle(ChatFormatting.GRAY)));
            if (stack.hasTag() && this.type != MiningSkillCardItem.Type.EMPTY && this.getData(stack).getTier() != MiningSkillCardItem.Tier.Unlearned && this.getData(stack).getTier() != MiningSkillCardItem.Tier.Mastered) {
               ChatFormatting formatting = ChatFormattingUtils.getProgressColor(this.getData(stack).getPotionPoints(), this.getData(stack).getMaxPotionPoints());
               component = Component.translatable("tooltip.ultimine_addition.skill_card.potion_point", Component.literal(String.valueOf(this.getData(stack).getPotionPoints())).withStyle(formatting));
               tooltipComponents.add(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY).append(component.withStyle(ChatFormatting.GRAY)));
            }

            Screen screen2 = Minecraft.getInstance().screen;
            if (screen2 instanceof SkillsRecordScreen screen) {
               if (screen.getMenu().getCardSlots().stream().anyMatch((slot) -> slot.getItem().equals(stack))) {
                  return;
               }
            }

            if (this.getData(stack).getTier() != MiningSkillCardItem.Tier.Mastered) {
               component = Component.translatable("tooltip.ultimine_addition.skill_card.info").withStyle(ChatFormatting.WHITE);
               List<Component> components = ComponentHelper.splitComponent(component, this.getSplitterLength());
               tooltipComponents.addAll(components);
            }

         }
      }
   }

   public boolean isBarVisible(@NotNull ItemStack stack) {
      if (!MiningSkillCardData.hasData(stack)) {
         return false;
      } else {
         MiningSkillCardData data = this.getData(stack);
         if (this.type != MiningSkillCardItem.Type.EMPTY && !data.isCreativeItem() && data.getTier() != MiningSkillCardItem.Tier.Unlearned && data.getTier() != MiningSkillCardItem.Tier.Mastered) {
            return !data.isPotionPointsFull();
         } else {
            return false;
         }
      }
   }

   public int getBarWidth(@NotNull ItemStack itemStack) {
      MiningSkillCardData data = this.getData(itemStack);
      return Math.round((float) data.getPotionPoints() / (float) data.getMaxPotionPoints() * 13.0F);
   }

   public int getBarColor(@NotNull ItemStack itemStack) {
      return Mth.hsvToRgb(Math.max(0.0F, (float) this.getBarWidth(itemStack) / 13.0F) / 3.0F, 1.0F, 1.0F);
   }

   public UAItemRenderer createItemRenderer() {
      return ItemRendererHandler.MiningSkillCardRenderer();
   }

   public MiningSkillCardData getData(ItemStack stack) {
      return MiningSkillCardData.load(stack);
   }

   public Type getType() {
      return this.type;
   }

   public static boolean isTierEqual(ItemStack stack, Tier tier) {
      return MiningSkillCardData.load(stack).getTier() == tier;
   }

   public record Type(boolean active, String id, List<String> requiredTools, Color potionColor,
                      Item defaultDisplayItem) {
      public static final Type EMPTY;
      public static final Type PICKAXE;
      public static final Type AXE;
      public static final Type SHOVEL;
      public static final Type HOE;
      public static List<Type> TYPES;
      public static final Codec<Type> CODEC;
      public static final Codec<Type> CARD_CODEC;

      public Type(boolean active, String id, List<String> requiredTools) {
            this(active, id, requiredTools, Color.WHITE, Items.BARRIER);
      }

      public Type(boolean active, String id, List<String> requiredTools, Item defaultDisplayItem) {
            this(active, id, requiredTools, Color.WHITE, defaultDisplayItem);
      }

      public Type(boolean active, String id, List<String> requiredTools, Color potionColor, Item defaultDisplayItem) {
            this.active = active;
         this.id = this.validateId(id);
            this.requiredTools = requiredTools;
            this.potionColor = potionColor;
            this.defaultDisplayItem = defaultDisplayItem;
      }

      private String validateId(String input) {
            String pattern = "^[a-z0-9_.-]+$";
         if (!input.matches(pattern)) {
            throw new IllegalArgumentException("Invalid Custom Card Id format! Non [a-z0-9_.-] character exists. (\"%s\")".formatted(input));
         } else {
            return input;
         }
      }

      public static void refreshTypes() {
            TYPES = new ArrayList<>(List.of(EMPTY, PICKAXE, AXE, SHOVEL, HOE));
            TYPES.addAll(CustomMSCApi.CUSTOM_TYPES);
      }

      public ResourceLocation getRegistryId() {
         return FTBUltimineAddition.id("mining_skill_card_%s".formatted(this.id));
      }

      public static Type fromString(String input) {
            for (Type type : TYPES) {
               if (type.id().equalsIgnoreCase(input) && !input.equalsIgnoreCase(EMPTY.id())) {
                  return type;
               }
            }

            throw new IllegalArgumentException("No type with the specified name");
      }

      public boolean isCustomType() {
         return TYPES.stream().filter((t) -> !t.equals(EMPTY) && !t.equals(PICKAXE) && !t.equals(AXE) && !t.equals(SHOVEL) && !t.equals(HOE)).toList().contains(this);
      }

      public List<Item> utilizeRequiredTools() {
            List<Item> list = new ArrayList<>();
         if (this.requiredTools != null) {
            for (String value : this.requiredTools) {
               if (value.startsWith("#")) {
                  List<Item> items = new ArrayList<>();
                  BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, new ResourceLocation(value.replace("#", "")))).ifPresent((holders) -> items.addAll(holders.stream().map(Holder::value).toList()));
                  if (!items.isEmpty()) {
                     list.addAll(items);
                  }
               } else {
                  Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(value));
                  if (item != Items.AIR) {
                     list.add(item);
                  }
               }
            }

         }
         return list;
      }

      static {
         EMPTY = new Type(true, "empty", List.of(), Items.BARRIER);
         PICKAXE = new Type(true, "pickaxe", List.of(), Items.NETHERITE_PICKAXE);
         AXE = new Type(true, "axe", List.of(), Items.NETHERITE_AXE);
         SHOVEL = new Type(true, "shovel", List.of(), Items.NETHERITE_SHOVEL);
         HOE = new Type(true, "hoe", List.of(), Items.NETHERITE_HOE);
         TYPES = new ArrayList<>();
         CODEC = Codec.STRING.comapFlatMap((s) -> {
            try {
               return DataResult.success(fromString(s));
            } catch (CodecException e) {
               return DataResult.error(() -> s + " is not present.");
            }
         }, Type::id);
         CARD_CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.BOOL.fieldOf("active").forGetter(Type::active), Codec.STRING.fieldOf("card_id").forGetter(Type::id), Codec.STRING.listOf().fieldOf("required_tools").forGetter(Type::requiredTools), CodecUtils.COLOR_CODEC.optionalFieldOf("potion_color", Color.WHITE).forGetter(Type::potionColor), CodecUtils.ITEM_CODEC.optionalFieldOf("default_display_item", Items.BARRIER).forGetter(Type::defaultDisplayItem)).apply(instance, Type::new));
      }
   }

   public enum Tier {
      Unlearned(0),
      Novice(1),
      Apprentice(2),
      Adept(3),
      Mastered(4);

      private final int tier;
      public static final Codec<Tier> CODEC = Codec.INT.comapFlatMap((i) -> {
         try {
            return DataResult.success(fromInt(i));
         } catch (EnumConstantNotPresentException e) {
            return DataResult.success(Unlearned);
         }
      }, Tier::getValue);

      Tier(int tier) {
         this.tier = tier;
      }

      public boolean isEligible(Tier tier) {
         return tier.getValue() >= this.tier;
      }

      public int getValue() {
         return this.tier;
      }

      public MutableComponent getDisplayName() {
         MutableComponent mutableComponent;
         switch (this.tier) {
            case 1 ->
                    mutableComponent = Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.GREEN);
            case 2 -> mutableComponent = Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.AQUA);
            case 3 ->
                    mutableComponent = Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE);
            case 4 -> mutableComponent = Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.GOLD);
            default -> mutableComponent = Component.translatable(this.getDescriptionId());
         }

         return mutableComponent;
      }

      private String getDescriptionId() {
         return String.format("tooltip.ultimine_addition.skill_card.tier.%s", this.name().toLowerCase());
      }

      public Tier next() {
         int nextIndex = (this.ordinal() + 1) % values().length;
         return values()[nextIndex];
      }

      public Tier previous() {
         int prevIndex = (this.ordinal() - 1 + values().length) % values().length;
         return values()[prevIndex];
      }

      public static String[] getNames() {
         String[] enumNames = new String[values().length];

         for (int i = 0; i < enumNames.length; ++i) {
            enumNames[i] = values()[i].name().toLowerCase();
         }

         return enumNames;
      }

      public static Tier fromString(String input) {
         for (Tier enumValue : values()) {
            if (enumValue.name().equalsIgnoreCase(input)) {
               return enumValue;
            }
         }
         throw new IllegalArgumentException("There is no tier with string: " + input);
      }

      public static Tier fromInt(int input) {
         for (Tier enumValue : values()) {
            if (enumValue.getValue() == input) {
               return enumValue;
            }
         }
         throw new IllegalArgumentException("There is no tier with int: " + input);
      }
   }
}
