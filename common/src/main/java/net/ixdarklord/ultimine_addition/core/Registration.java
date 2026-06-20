package net.ixdarklord.ultimine_addition.core;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.ixdarklord.coolcatlib.api.core.commands.ArgumentTypeRegistry;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.advancement.UltimineObtainTrigger;
import net.ixdarklord.ultimine_addition.common.commands.arguments.CardHolderArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.CardTierArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.ChallengesArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.UltimineShapeArgument;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffectInstance;
import net.ixdarklord.ultimine_addition.common.effect.ModMobEffects;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.menu.ShapeSelectorMenu;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
import net.ixdarklord.ultimine_addition.common.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.MCRecipe;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Registration {
   public static final DeferredRegister<CreativeModeTab> TABS;
   public static final DeferredRegister<Item> ITEMS;
   public static final DeferredRegister<MobEffect> MOB_EFFECTS;
   public static final DeferredRegister<Potion> POTIONS;
   public static final DeferredRegister<MenuType<?>> CONTAINERS;
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS;
   public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES;
   public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES;
   public static final RegistrySupplier<CreativeModeTab> ULTIMINE_ADDITION_TAB;
   public static final RegistrySupplier<Item> MINER_CERTIFICATE;
   public static final RegistrySupplier<Item> SKILLS_RECORD;
   public static final RegistrySupplier<Item> SHAPE_SELECTOR;
   public static final RegistrySupplier<Item> INK_CHAMBER;
   public static final RegistrySupplier<Item> PEN;
   public static final RegistrySupplier<Item> CARD_BLUEPRINT;
   public static final RegistrySupplier<MiningSkillCardItem> MINING_SKILL_CARD_EMPTY;
   public static final RegistrySupplier<MiningSkillCardItem> MINING_SKILL_CARD_PICKAXE;
   public static final RegistrySupplier<MiningSkillCardItem> MINING_SKILL_CARD_AXE;
   public static final RegistrySupplier<MiningSkillCardItem> MINING_SKILL_CARD_SHOVEL;
   public static final RegistrySupplier<MiningSkillCardItem> MINING_SKILL_CARD_HOE;
   public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_PICKAXE;
   public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_AXE;
   public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_SHOVEL;
   public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_HOE;
   public static final RegistrySupplier<Potion> KNOWLEDGE_POTION;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_PICKAXE_POTION;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_PICKAXE_POTION2;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_PICKAXE_POTION3;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_AXE_POTION;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_AXE_POTION2;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_AXE_POTION3;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_SHOVEL_POTION;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_SHOVEL_POTION2;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_SHOVEL_POTION3;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_HOE_POTION;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_HOE_POTION2;
   public static final RegistrySupplier<Potion> MINE_GO_JUICE_HOE_POTION3;
   public static final RegistrySupplier<MenuType<SkillsRecordMenu>> SKILLS_RECORD_CONTAINER;
   public static final RegistrySupplier<MenuType<ShapeSelectorMenu>> SHAPE_SELECTOR_CONTAINER;
   public static final RegistrySupplier<ItemStorageDataRecipe.Serializer> ITEM_DATA_STORAGE_RECIPE_SERIALIZER;
   public static final RegistrySupplier<MCRecipe.Serializer> MC_RECIPE_SERIALIZER;
   public static final RegistrySupplier<SimpleParticleType> CELEBRATE_PARTICLE;
   public static final RegistrySupplier<ArgumentTypeInfo<CardTierArgument, ?>> CARD_TIER_ARGUMENT;
   public static final RegistrySupplier<ArgumentTypeInfo<CardHolderArgument, ?>> CARD_HOLDER_ALL_ARGUMENT;
   public static final RegistrySupplier<ArgumentTypeInfo<CardHolderArgument.RecordSlots, ?>> CARD_HOLDER_RECORD_ARGUMENT;
   public static final RegistrySupplier<ArgumentTypeInfo<ChallengesArgument, ?>> CHALLENGES_ARGUMENT;
   public static final RegistrySupplier<ArgumentTypeInfo<UltimineShapeArgument, ?>> ULTIMINE_SHAPE_ARGUMENT;
   public static final UltimineObtainTrigger ULTIMINE_OBTAIN_TRIGGER;
   private static final Map<String, RegistrySupplier<MobEffect>> mineGoJuiceList;

   public static void register() {
      registerItems();
      registerMobEffects();
      registerPotions();
      TABS.register();
      CONTAINERS.register();
      RECIPE_SERIALIZERS.register();
      ARGUMENT_TYPES.register();
      PARTICLE_TYPES.register();
   }

   private static void registerItems() {
      for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
         String name = "mining_skill_card_%s".formatted(type.id());
         ITEMS.register(name, () -> new MiningSkillCardItem((new Item.Properties()).stacksTo(1), type));
      }

      ITEMS.register();
   }

   private static void registerMobEffects() {
      for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
         String id = MineGoJuiceEffect.getId(type).getPath();
         MobEffect mobEffect = new MineGoJuiceEffect(type, MobEffectCategory.BENEFICIAL, type.potionColor().getRGB());
         mineGoJuiceList.put(id, MOB_EFFECTS.register(id, () -> mobEffect));
      }

      MOB_EFFECTS.register();
   }

   private static void registerPotions() {
      for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
         String id = MineGoJuiceEffect.getId(type).getPath();
         RegistrySupplier<MobEffect> mobEffect = mineGoJuiceList.get(id);
         if (mobEffect != null) {
            POTIONS.register(id, () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(mobEffect, 0)));
            POTIONS.register(id + "_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(mobEffect, 1)));
            POTIONS.register(id + "_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(mobEffect, 2)));
         }
      }

      mineGoJuiceList.clear();
      POTIONS.register();
   }

   static {
      TABS = DeferredRegister.create("ultimine_addition", Registries.CREATIVE_MODE_TAB);
      ITEMS = DeferredRegister.create("ultimine_addition", Registries.ITEM);
      MOB_EFFECTS = DeferredRegister.create("ultimine_addition", Registries.MOB_EFFECT);
      POTIONS = DeferredRegister.create("ultimine_addition", Registries.POTION);
      CONTAINERS = DeferredRegister.create("ultimine_addition", Registries.MENU);
      RECIPE_SERIALIZERS = DeferredRegister.create("ultimine_addition", Registries.RECIPE_SERIALIZER);
      PARTICLE_TYPES = DeferredRegister.create("ultimine_addition", Registries.PARTICLE_TYPE);
      ARGUMENT_TYPES = DeferredRegister.create("ultimine_addition", Registries.COMMAND_ARGUMENT_TYPE);
      ULTIMINE_ADDITION_TAB = TABS.register("general_tab", () -> CreativeTabRegistry.create((builder) -> {
         builder.title(Component.translatable("itemGroup.ultimine_addition.tab"));
         Item item2 = ModItems.MINER_CERTIFICATE;
         Objects.requireNonNull(item2);
         builder.icon(item2::getDefaultInstance);
         builder.displayItems((itemDisplayParameters, output) -> {
            output.accept(ModItems.MINER_CERTIFICATE);
            output.accept(ModItems.SHAPE_SELECTOR);
            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
               output.accept(ModItems.CARD_BLUEPRINT);
            }

            if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() != PlaystyleMode.LEGACY) {
               output.accept(ModItems.SKILLS_RECORD);
               output.accept(ModItems.INK_CHAMBER);
               output.accept(ModItems.PEN);
               ItemStack pen = ModItems.PEN.getDefaultInstance();
               ModItems.PEN.getData(pen).setToFullCapacity().save();
               output.accept(pen);
               output.accept(ModItems.CARD_BLUEPRINT);

               for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
                  String name = "mining_skill_card_" + type.id();
                  Item item = BuiltInRegistries.ITEM.get(FTBUltimineAddition.id(name));
                  if (item instanceof MiningSkillCardItem cardItem) {
                     output.accept(item);
                     if (cardItem.getType() != MiningSkillCardItem.Type.EMPTY) {
                        for (MiningSkillCardItem.Tier value : MiningSkillCardItem.Tier.values()) {
                           if (value != MiningSkillCardItem.Tier.Unlearned) {
                              output.accept(MiningSkillCardData.createForCreativeTab(cardItem, value));
                           }
                        }
                        }
                  }
               }
            }

         });
         builder.build();
      }));
      MINER_CERTIFICATE = ITEMS.register("miner_certificate", () -> ModItems.MINER_CERTIFICATE);
      SKILLS_RECORD = ITEMS.register("skills_record", () -> ModItems.SKILLS_RECORD);
      SHAPE_SELECTOR = ITEMS.register("shape_selector", () -> ModItems.SHAPE_SELECTOR);
      INK_CHAMBER = ITEMS.register("ink_chamber", () -> ModItems.INK_CHAMBER);
      PEN = ITEMS.register("pen", () -> ModItems.PEN);
      CARD_BLUEPRINT = ITEMS.register("card_blueprint", () -> ModItems.CARD_BLUEPRINT);
      MINING_SKILL_CARD_EMPTY = ITEMS.register("mining_skill_card_empty", () -> ModItems.MINING_SKILL_CARD_EMPTY);
      MINING_SKILL_CARD_PICKAXE = ITEMS.register("mining_skill_card_pickaxe", () -> ModItems.MINING_SKILL_CARD_PICKAXE);
      MINING_SKILL_CARD_AXE = ITEMS.register("mining_skill_card_axe", () -> ModItems.MINING_SKILL_CARD_AXE);
      MINING_SKILL_CARD_SHOVEL = ITEMS.register("mining_skill_card_shovel", () -> ModItems.MINING_SKILL_CARD_SHOVEL);
      MINING_SKILL_CARD_HOE = ITEMS.register("mining_skill_card_hoe", () -> ModItems.MINING_SKILL_CARD_HOE);
      MINE_GO_JUICE_PICKAXE = MOB_EFFECTS.register("mine_go_juice_pickaxe", () -> ModMobEffects.MINE_GO_JUICE_PICKAXE);
      MINE_GO_JUICE_AXE = MOB_EFFECTS.register("mine_go_juice_axe", () -> ModMobEffects.MINE_GO_JUICE_AXE);
      MINE_GO_JUICE_SHOVEL = MOB_EFFECTS.register("mine_go_juice_shovel", () -> ModMobEffects.MINE_GO_JUICE_SHOVEL);
      MINE_GO_JUICE_HOE = MOB_EFFECTS.register("mine_go_juice_hoe", () -> ModMobEffects.MINE_GO_JUICE_HOE);
      KNOWLEDGE_POTION = POTIONS.register("knowledge", Potion::new);
      MINE_GO_JUICE_PICKAXE_POTION = POTIONS.register("mine_go_juice_pickaxe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_PICKAXE, 0)));
      MINE_GO_JUICE_PICKAXE_POTION2 = POTIONS.register("mine_go_juice_pickaxe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_PICKAXE, 1)));
      MINE_GO_JUICE_PICKAXE_POTION3 = POTIONS.register("mine_go_juice_pickaxe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_PICKAXE, 2)));
      MINE_GO_JUICE_AXE_POTION = POTIONS.register("mine_go_juice_axe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_AXE, 0)));
      MINE_GO_JUICE_AXE_POTION2 = POTIONS.register("mine_go_juice_axe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_AXE, 1)));
      MINE_GO_JUICE_AXE_POTION3 = POTIONS.register("mine_go_juice_axe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_AXE, 2)));
      MINE_GO_JUICE_SHOVEL_POTION = POTIONS.register("mine_go_juice_shovel", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_SHOVEL, 0)));
      MINE_GO_JUICE_SHOVEL_POTION2 = POTIONS.register("mine_go_juice_shovel_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_SHOVEL, 1)));
      MINE_GO_JUICE_SHOVEL_POTION3 = POTIONS.register("mine_go_juice_shovel_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_SHOVEL, 2)));
      MINE_GO_JUICE_HOE_POTION = POTIONS.register("mine_go_juice_hoe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_HOE, 0)));
      MINE_GO_JUICE_HOE_POTION2 = POTIONS.register("mine_go_juice_hoe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_HOE, 1)));
      MINE_GO_JUICE_HOE_POTION3 = POTIONS.register("mine_go_juice_hoe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_HOE, 2)));
      SKILLS_RECORD_CONTAINER = CONTAINERS.register("skills_record", () -> MenuRegistry.ofExtended(SkillsRecordMenu::new));
      SHAPE_SELECTOR_CONTAINER = CONTAINERS.register("shape_selector", () -> MenuRegistry.ofExtended((id, inv, buf) -> new ShapeSelectorMenu(id, inv)));
      ITEM_DATA_STORAGE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(ItemStorageDataRecipe.Serializer.NAME, ItemStorageDataRecipe.Serializer::new);
      MC_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(MCRecipe.Serializer.NAME, MCRecipe.Serializer::new);
      CELEBRATE_PARTICLE = PARTICLE_TYPES.register("celebrate", () -> new SimpleParticleType(true));
      CARD_TIER_ARGUMENT = ARGUMENT_TYPES.register("card_tier", () -> ArgumentTypeRegistry.register(CardTierArgument.class, SingletonArgumentInfo.contextFree(CardTierArgument::tier)));
      CARD_HOLDER_ALL_ARGUMENT = ARGUMENT_TYPES.register("card_holder_all", () -> ArgumentTypeRegistry.register(CardHolderArgument.class, SingletonArgumentInfo.contextFree(CardHolderArgument::allSlots)));
      CARD_HOLDER_RECORD_ARGUMENT = ARGUMENT_TYPES.register("card_holder_record", () -> ArgumentTypeRegistry.register(CardHolderArgument.RecordSlots.class, SingletonArgumentInfo.contextFree(CardHolderArgument::recordSlots)));
      CHALLENGES_ARGUMENT = ARGUMENT_TYPES.register("challenges", () -> ArgumentTypeRegistry.register(ChallengesArgument.class, SingletonArgumentInfo.contextFree(ChallengesArgument::data)));
      ULTIMINE_SHAPE_ARGUMENT = ARGUMENT_TYPES.register("ultimine_shape", () -> ArgumentTypeRegistry.register(UltimineShapeArgument.class, SingletonArgumentInfo.contextFree(UltimineShapeArgument::shape)));
      ULTIMINE_OBTAIN_TRIGGER = CriteriaTriggers.register(new UltimineObtainTrigger());
      mineGoJuiceList = new HashMap<>();
   }
}
