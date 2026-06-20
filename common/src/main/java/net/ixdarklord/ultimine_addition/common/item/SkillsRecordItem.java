package net.ixdarklord.ultimine_addition.common.item;

import dev.architectury.registry.menu.MenuRegistry;
import net.ixdarklord.coolcatlib.api.utils.ComponentHelper;
import net.ixdarklord.ultimine_addition.client.gui.tooltip.SkillsRecordTooltip;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
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

   public SkillsRecordItem(Item.Properties properties) {
      super(properties, ComponentType.TOOLS);
   }

   public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
      ItemStack stack = player.getItemInHand(usedHand);
      if (level.isClientSide()) {
         return new InteractionResultHolder<>(InteractionResult.PASS, stack);
      } else if (player.isShiftKeyDown()) {
         return new InteractionResultHolder<>(InteractionResult.PASS, stack);
      } else if (this.isLegacyMode()) {
         player.displayClientMessage(Component.translatable("info.ultimine_addition.legacy_mode").withStyle(ChatFormatting.RED), false);
         return new InteractionResultHolder<>(InteractionResult.PASS, stack);
      } else {
         if (stack.hasTag()) {
            MenuRegistry.openExtendedMenu((ServerPlayer) player, new SimpleMenuProvider((id, inv, p) -> new SkillsRecordMenu(id, inv, p, stack, usedHand), TITLE), (buf) -> {
               buf.writeItem(stack);
               buf.writeBoolean(true);
               buf.writeEnum(usedHand);
            });
         }

         return new InteractionResultHolder<>(InteractionResult.PASS, stack);
      }
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotID, boolean isSelected) {
      if (!this.isLegacyMode() && !level.isClientSide()) {
         if (entity instanceof ServerPlayer) {
            if (!stack.hasTag()) {
               this.getData(stack).save();
            }

            if (this.getData(stack).getCardSlots().stream().filter((s) -> !s.isEmpty()).toList().isEmpty() && this.getData(stack).isConsumeModeActive()) {
               this.getData(stack).setConsumeMode(false).save();
            }
         }

      }
   }

   public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
      super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
      if (!this.isShiftButtonNotPressed(tooltipComponents)) {
         if (!stack.hasTag()) {
            Component component = Component.translatable("tooltip.ultimine_addition.skills_record.info").withStyle(ChatFormatting.GRAY);
            List<Component> components = ComponentHelper.splitComponent(component, this.getSplitterLength());
            tooltipComponents.addAll(components);
         } else {
            if (this.isConsumeChallengeExists(stack)) {
               Component state = this.getData(stack).isConsumeModeActive() ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN) : Component.translatable("options.off").withStyle(ChatFormatting.RED);
               tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("gui.ultimine_addition.skills_record.consume", state).withStyle(ChatFormatting.GRAY)));
            }

            if (!this.getData(stack).getPenSlot().isEmpty()) {
               MutableComponent mutableComponent = Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY);
               Object[] object = new Object[1];
               Item components = this.getData(stack).getPenSlot().getItem();
               int integer;
               if (components instanceof PenItem item) {
                  integer = item.getData(this.getData(stack).getPenSlot()).getCapacity();
               } else {
                  integer = 0;
               }

               object[0] = integer;
               tooltipComponents.add(mutableComponent.append(Component.translatable("tooltip.ultimine_addition.pen.ink_chamber", object).withStyle(ChatFormatting.GRAY)));
            }

            tooltipComponents.add(Component.literal("§8• ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.skills_record.contents").withStyle(ChatFormatting.GRAY)));
            tooltipComponents.add(Component.literal("ultimine_addition.tooltip_image"));
         }
      }
   }

   public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
      NonNullList<ItemStack> nonNullList = NonNullList.create();
      Stream<ItemStack> itemStackStream = this.getData(stack).getAllSlots().stream();
      Objects.requireNonNull(nonNullList);
      Objects.requireNonNull(nonNullList);
      itemStackStream.forEach(nonNullList::add);
      CompoundTag tag = stack.getTag();
      return tag != null && !tag.getCompound(SkillsRecordData.DATA_ID.toString()).isEmpty() && !this.isShiftButtonNotPressed(null) ? Optional.of(new SkillsRecordTooltip(nonNullList)) : Optional.empty();
   }

   public boolean isConsumeChallengeExists(ItemStack stack) {
      AtomicBoolean result = new AtomicBoolean();
      this.getData(stack).getCardSlots().forEach((itemStack) -> {
         MiningSkillCardData cardData = MiningSkillCardData.load(itemStack);
         if (!cardData.getChallenges().stream().filter((challengeData) -> {
            ChallengeData data = ChallengesManager.INSTANCE.getAllChallenges().get(challengeData.getId());
            return data != null && data.challengeType().isConsuming();
         }).toList().isEmpty()) {
            result.set(true);
         }

      });
      return result.get();
   }

   public SkillsRecordData getData(ItemStack stack) {
      return SkillsRecordData.load(stack);
   }
}
