package net.ixdarklord.ultimine_addition.core;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.ixdarklord.coolcat_lib.util.ArgumentTypeHelper;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.client.particle.CelebrateParticle;
import net.ixdarklord.ultimine_addition.common.advancement.UltimineAbilityTrigger;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardSlotsArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.CardTierArgument;
import net.ixdarklord.ultimine_addition.common.command.arguments.ChallengesArgument;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.menu.SkillsRecordMenu;
import net.ixdarklord.ultimine_addition.common.recipe.ItemStorageDataRecipe;
import net.ixdarklord.ultimine_addition.common.recipe.MCRecipe;
import net.ixdarklord.ultimine_addition.common.data.item.ItemStorageData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.effect.MineGoJuiceEffect;
import net.ixdarklord.ultimine_addition.common.effect.ModMobEffects;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.ixdarklord.ultimine_addition.common.item.ModItems;
import net.ixdarklord.ultimine_addition.common.item.PenItem;
import net.ixdarklord.ultimine_addition.common.potion.MineGoPotion;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.ixdarklord.ultimine_addition.core.Constants.MOD_ID;

public class Registration {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(MOD_ID, Registries.MOB_EFFECT);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(MOD_ID, Registries.POTION);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(MOD_ID, Registries.MENU);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(MOD_ID, Registries.RECIPE_SERIALIZER);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(MOD_ID, Registries.PARTICLE_TYPE);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(MOD_ID, Registries.COMMAND_ARGUMENT_TYPE);

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
        // Custom Mining Skills Card
        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            String name = "mining_skill_card_%s".formatted(type.getId());
            ITEMS.register(name, () -> new MiningSkillCardItem(new Item.Properties()
                    .stacksTo(1), type));
        }
        ITEMS.register();
    }

    private static final Map<String, Supplier<MobEffect>> mineGoJuiceList = new HashMap<>();

    private static void registerMobEffects() {
        // Custom Mine-Go Juice Effects
        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            String name = "mine_go_juice_%s".formatted(type.getId());
            MobEffect mobEffect = new MineGoJuiceEffect(type, MobEffectCategory.BENEFICIAL, type.getPotionColor().getRGB());
            MOB_EFFECTS.register(name, () -> mobEffect);
            mineGoJuiceList.put(name, () -> mobEffect);
        }
        MOB_EFFECTS.register();
    }

    private static void registerPotions() {
        // Custom Mine-Go Juice Potions
        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            String name = "mine_go_juice_%s".formatted(type.getId());
            Supplier<MobEffect> mobEffect = mineGoJuiceList.get(name);
            if (mobEffect == null) continue;

            POTIONS.register(name, () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MobEffectInstance(mobEffect.get(), ConfigHandler.COMMON.TIER_1_TIME_SAFE.get() * 20, 0)));
            POTIONS.register(name + "_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MobEffectInstance(mobEffect.get(), ConfigHandler.COMMON.TIER_2_TIME_SAFE.get() * 20, 1)));
            POTIONS.register(name + "_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MobEffectInstance(mobEffect.get(), ConfigHandler.COMMON.TIER_3_TIME_SAFE.get() * 20, 2)));
        }
        mineGoJuiceList.clear();
        POTIONS.register();
    }

    public static void registerParticleProviders() {
        ParticleProviderRegistry.register(Registration.CELEBRATE_PARTICLE, CelebrateParticle.Provider::new);
    }

    public static final RegistrySupplier<CreativeModeTab> ULTIMINE_ADDITION_TAB = TABS.register("general_tab", () ->
            CreativeTabRegistry.create(builder -> {
                builder.title(Component.translatable("itemGroup.ultimine_addition.tab"));
                builder.icon(ModItems.MINER_CERTIFICATE::getDefaultInstance);
                builder.displayItems((itemDisplayParameters, output) -> {
                    output.accept(ModItems.MINER_CERTIFICATE);
                    if (ConfigHandler.COMMON.PLAYSTYLE_MODE_SAFE.get() != PlaystyleMode.LEGACY) {
                        output.accept(ModItems.SKILLS_RECORD);
                        output.accept(ModItems.INK_CHAMBER);
                        output.accept(ModItems.PEN);

                        ItemStack penStack = ModItems.PEN.getDefaultInstance();
                        ItemStorageData storageData = ((PenItem) penStack.getItem()).getData(penStack);
                        storageData.setCapacity(storageData.getMaxCapacity()).saveData(penStack);
                        output.accept(penStack);

                        output.accept(ModItems.CARD_BLUEPRINT);
                        output.accept(ModItems.MINING_SKILL_CARD_EMPTY);
                        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
                            String name = "mining_skill_card_" + type.getId();
                            Item item = BuiltInRegistries.ITEM.get(Constants.getLocation(name));
                            if (item instanceof MiningSkillCardItem cardItem && cardItem.getType() != MiningSkillCardItem.Type.EMPTY) {
                                ItemStack cardStack = cardItem.getDefaultInstance();
                                MiningSkillCardData cardData = new MiningSkillCardData().loadData(cardStack).setTier(MiningSkillCardItem.Tier.Mastered);
                                cardData.saveData(cardStack);
                                output.accept(item);
                                output.accept(cardStack);
                            }
                        }
                    }
                });
                builder.build();
            }));


    // Items
    public static final RegistrySupplier<Item> MINER_CERTIFICATE = ITEMS.register("miner_certificate", () -> ModItems.MINER_CERTIFICATE);
    public static final RegistrySupplier<Item> SKILLS_RECORD = ITEMS.register("skills_record", () -> ModItems.SKILLS_RECORD);
    public static final RegistrySupplier<Item> INK_CHAMBER = ITEMS.register("ink_chamber", () -> ModItems.INK_CHAMBER);
    public static final RegistrySupplier<Item> PEN = ITEMS.register("pen", () -> ModItems.PEN);
    public static final RegistrySupplier<Item> CARD_BLUEPRINT = ITEMS.register("card_blueprint", () -> ModItems.CARD_BLUEPRINT);

    public static final RegistrySupplier<Item> MINING_SKILL_CARD_EMPTY = ITEMS.register("mining_skill_card_empty", () -> ModItems.MINING_SKILL_CARD_EMPTY);
    public static final RegistrySupplier<Item> MINING_SKILL_CARD_PICKAXE = ITEMS.register("mining_skill_card_pickaxe", () -> ModItems.MINING_SKILL_CARD_PICKAXE);
    public static final RegistrySupplier<Item> MINING_SKILL_CARD_AXE = ITEMS.register("mining_skill_card_axe", () -> ModItems.MINING_SKILL_CARD_AXE);
    public static final RegistrySupplier<Item> MINING_SKILL_CARD_SHOVEL = ITEMS.register("mining_skill_card_shovel", () -> ModItems.MINING_SKILL_CARD_SHOVEL);
    public static final RegistrySupplier<Item> MINING_SKILL_CARD_HOE = ITEMS.register("mining_skill_card_hoe", () -> ModItems.MINING_SKILL_CARD_HOE);

    // Mob Effects
    public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_PICKAXE = MOB_EFFECTS.register("mine_go_juice_pickaxe", () -> ModMobEffects.MINE_GO_JUICE_PICKAXE);
    public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_AXE = MOB_EFFECTS.register("mine_go_juice_axe", () -> ModMobEffects.MINE_GO_JUICE_AXE);
    public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_SHOVEL = MOB_EFFECTS.register("mine_go_juice_shovel", () -> ModMobEffects.MINE_GO_JUICE_SHOVEL);
    public static final RegistrySupplier<MobEffect> MINE_GO_JUICE_HOE = MOB_EFFECTS.register("mine_go_juice_hoe", () -> ModMobEffects.MINE_GO_JUICE_HOE);

    // Potions
    public static final RegistrySupplier<Potion> KNOWLEDGE_POTION = POTIONS.register("knowledge", Potion::new);
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_PICKAXE_POTION = POTIONS.register("mine_go_juice_pickaxe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_PICKAXE, ConfigHandler.COMMON.TIER_1_TIME_SAFE.get() * 20, 0)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_PICKAXE_POTION2 = POTIONS.register("mine_go_juice_pickaxe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_PICKAXE, ConfigHandler.COMMON.TIER_2_TIME_SAFE.get() * 20, 1)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_PICKAXE_POTION3 = POTIONS.register("mine_go_juice_pickaxe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_PICKAXE, ConfigHandler.COMMON.TIER_3_TIME_SAFE.get() * 20, 2)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_AXE_POTION = POTIONS.register("mine_go_juice_axe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_AXE, ConfigHandler.COMMON.TIER_1_TIME_SAFE.get() * 20, 0)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_AXE_POTION2 = POTIONS.register("mine_go_juice_axe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_AXE, ConfigHandler.COMMON.TIER_2_TIME_SAFE.get() * 20, 1)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_AXE_POTION3 = POTIONS.register("mine_go_juice_axe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_AXE, ConfigHandler.COMMON.TIER_3_TIME_SAFE.get() * 20, 2)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_SHOVEL_POTION = POTIONS.register("mine_go_juice_shovel", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_SHOVEL, ConfigHandler.COMMON.TIER_1_TIME_SAFE.get() * 20, 0)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_SHOVEL_POTION2 = POTIONS.register("mine_go_juice_shovel_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_SHOVEL, ConfigHandler.COMMON.TIER_2_TIME_SAFE.get() * 20, 1)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_SHOVEL_POTION3 = POTIONS.register("mine_go_juice_shovel_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_SHOVEL, ConfigHandler.COMMON.TIER_3_TIME_SAFE.get() * 20, 2)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_HOE_POTION = POTIONS.register("mine_go_juice_hoe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_HOE, ConfigHandler.COMMON.TIER_1_TIME_SAFE.get() * 20, 0)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_HOE_POTION2 = POTIONS.register("mine_go_juice_hoe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_HOE, ConfigHandler.COMMON.TIER_2_TIME_SAFE.get() * 20, 1)));
    public static final RegistrySupplier<Potion> MINE_GO_JUICE_HOE_POTION3 = POTIONS.register("mine_go_juice_hoe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MobEffectInstance(ModMobEffects.MINE_GO_JUICE_HOE, ConfigHandler.COMMON.TIER_3_TIME_SAFE.get() * 20, 2)));

    // Containers
    public static final RegistrySupplier<MenuType<SkillsRecordMenu>> SKILLS_RECORD_CONTAINER = CONTAINERS.register("skills_record", () -> MenuRegistry.ofExtended(SkillsRecordMenu::new));

    // Recipe Serializer
    public static final RegistrySupplier<ItemStorageDataRecipe.Serializer> ITEM_DATA_STORAGE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(ItemStorageDataRecipe.Serializer.NAME, ItemStorageDataRecipe.Serializer::new);
    public static final RegistrySupplier<MCRecipe.Serializer> MC_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(MCRecipe.Serializer.NAME, MCRecipe.Serializer::new);

    // Particles
    public static final RegistrySupplier<SimpleParticleType> CELEBRATE_PARTICLE = PARTICLE_TYPES.register("celebrate", () -> new SimpleParticleType(true));

    // Advancements
    public static final UltimineAbilityTrigger OBTAIN_ULTIMINE_TRIGGER = CriteriaTriggers.register(new UltimineAbilityTrigger());

    // Arguments
    public static final RegistrySupplier<ArgumentTypeInfo<CardTierArgument, ?>> CARD_TIER_ARGUMENT = ARGUMENT_TYPES.register("card_tier", () -> ArgumentTypeHelper.register(CardTierArgument.class, SingletonArgumentInfo.contextFree(CardTierArgument::tier)));
    public static final RegistrySupplier<ArgumentTypeInfo<CardSlotsArgument, ?>> CARD_SLOTS_ARGUMENT = ARGUMENT_TYPES.register("card_slots", () -> ArgumentTypeHelper.register(CardSlotsArgument.class, SingletonArgumentInfo.contextFree(CardSlotsArgument::slots)));
    public static final RegistrySupplier<ArgumentTypeInfo<ChallengesArgument, ?>> CHALLENGES_ARGUMENT = ARGUMENT_TYPES.register("challenges", () -> ArgumentTypeHelper.register(ChallengesArgument.class, SingletonArgumentInfo.contextFree(ChallengesArgument::data)));
}
