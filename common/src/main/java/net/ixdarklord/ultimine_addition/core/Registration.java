package net.ixdarklord.ultimine_addition.core;

import net.ixdarklord.coolcatcore.api.attachment.Attachment;
import net.ixdarklord.coolcatcore.api.attachment.AttachmentRegistry;
import net.ixdarklord.coolcatcore.api.attachment.SyncPolicy;
import net.ixdarklord.coolcatcore.api.menu.ExtendedMenus;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.coolcatcore.api.registry.DeferredRegister;
import net.ixdarklord.coolcatcore.api.registry.RegistryEntry;
import net.minecraft.resources.Identifier;
import net.ixdarklord.ultimine_addition.common.item.ShapeCertificateItem;
import net.ixdarklord.ultimine_addition.common.data.item.ShapeCertificateData;
import net.ixdarklord.ultimine_addition.config.PlaystyleModes;
import net.ixdarklord.ultimine_addition.common.data.record.SkillsRecordLink;
import net.ixdarklord.coolcatcore.api.core.commands.ArgumentTypeRegistry;
import net.ixdarklord.coolcatcore.api.utils.ParticleTypes;
import net.ixdarklord.ultimine_addition.api.CustomMSCApi;
import net.ixdarklord.ultimine_addition.common.advancement.UltimineObtainTrigger;
import net.ixdarklord.ultimine_addition.common.commands.arguments.CardHolderArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.CardTierArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.ChallengesArgument;
import net.ixdarklord.ultimine_addition.common.commands.arguments.UltimineShapeArgument;
import net.ixdarklord.ultimine_addition.common.data.item.*;
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
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.inventory.MenuType;
import net.ixdarklord.coolcatcore.api.registry.CreativeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.HashMap;
import java.util.Map;

import static net.ixdarklord.ultimine_addition.core.FTBUltimineAddition.MOD_ID;

public class Registration {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(MOD_ID, Registries.MOB_EFFECT);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(MOD_ID, Registries.POTION);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(MOD_ID, Registries.MENU);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(MOD_ID, Registries.RECIPE_SERIALIZER);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE = DeferredRegister.create(MOD_ID, Registries.DATA_COMPONENT_TYPE);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(MOD_ID, Registries.PARTICLE_TYPE);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(MOD_ID, Registries.COMMAND_ARGUMENT_TYPE);
    public static final DeferredRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS = DeferredRegister.create(MOD_ID, Registries.TRIGGER_TYPE);

    public static final AttachmentRegistry ATTACHMENTS = AttachmentRegistry.create(MOD_ID);
    // Saved with the player, sent to that player whenever it changes, and kept through death.
    public static final Attachment<PlayerAbilityData> PLAYER_ABILITY = ATTACHMENTS
            .mutableBuilder("player_ability", PlayerAbilityData.CODEC, PlayerAbilityData::create)
            .networkCodec(PlayerAbilityData.STREAM_CODEC)
            .sync(SyncPolicy.SELF)
            .copyOnDeath()
            .build();

    public static void register() {
        registerItems();
        registerMobEffects();
        registerPotions();
        TABS.register();
        CONTAINERS.register();
        RECIPE_SERIALIZERS.register();
        DATA_COMPONENT_TYPE.register();
        ARGUMENT_TYPES.register();
        PARTICLE_TYPES.register();
        CRITERIA_TRIGGERS.register();
        ATTACHMENTS.register();
    }

    private static void registerItems() {
        // Custom Mining Skills Card
        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            ITEMS.register(type.getRegistryId().getPath(), () -> new MiningSkillCardItem(type, new Item.Properties()
                    .stacksTo(1)));
        }
        ITEMS.register();
    }

    private static final Map<String, RegistryEntry<MobEffect>> mineGoJuiceList = new HashMap<>();

    private static void registerMobEffects() {
        // Custom Mine-Go Juice Effects
        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            String id = MineGoJuiceEffect.getId(type).getPath();
            MobEffect mobEffect = new MineGoJuiceEffect(type, MobEffectCategory.BENEFICIAL, type.getPotionColor().getRGB());
            mineGoJuiceList.put(id, MOB_EFFECTS.register(id, () -> mobEffect));
        }
        MOB_EFFECTS.register();
    }

    private static void registerPotions() {
        // Custom Mine-Go Juice Potions
        for (MiningSkillCardItem.Type type : CustomMSCApi.CUSTOM_TYPES) {
            String id = MineGoJuiceEffect.getId(type).getPath();
            RegistryEntry<MobEffect> mobEffect = mineGoJuiceList.get(id);
            if (mobEffect == null) continue;

            POTIONS.register(id, () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(mobEffect, 0)));
            POTIONS.register(id + "_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(mobEffect, 1)));
            POTIONS.register(id + "_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(mobEffect, 2)));
        }
        mineGoJuiceList.clear();
        POTIONS.register();
    }

    public static final RegistryEntry<CreativeModeTab> ULTIMINE_ADDITION_TAB = TABS.register("general_tab", () -> {
                CreativeModeTab.Builder builder = CreativeTabs.builder();
                builder.title(Component.translatable("itemGroup.ultimine_addition.tab"));
                builder.icon(ModItems.MINER_CERTIFICATE::getDefaultInstance);
                builder.displayItems((itemDisplayParameters, output) -> {
                    output.accept(ModItems.MINER_CERTIFICATE);
                    output.accept(ModItems.SHAPE_SELECTOR);

                    if (PlaystyleModes.isLegacy()) {
                        output.accept(ModItems.CARD_BLUEPRINT);
                    }

                    if (!PlaystyleModes.isLegacy()) {
                        for (ShapeCertificateItem certificate : ShapeCertificateItem.all()) {
                            for (Identifier shape : ShapeCertificateItem.tierList(certificate.getTier())) {
                                for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
                                    if (type != MiningSkillCardItem.Type.EMPTY) output.accept(certificate.create(type, shape));
                                }
                            }
                        }
                        output.accept(ModItems.SKILLS_RECORD);
                        output.accept(ModItems.INK_CHAMBER);
                        output.accept(ModItems.PEN);
                        ItemStack pen = ModItems.PEN.getDefaultInstance();
                        ModItems.PEN.getData(pen).setToFullCapacity().save();
                        output.accept(pen);
                        output.accept(ModItems.CARD_BLUEPRINT);
                        for (MiningSkillCardItem.Type type : MiningSkillCardItem.Type.TYPES) {
                            String name = "mining_skill_card_" + type.getId();
                            Item item = BuiltInRegistries.ITEM.getValue(FTBUltimineAddition.id(name));
                            if (item instanceof MiningSkillCardItem) output.accept(item);
                        }
                    }
                });
                return builder.build();
            });


    // Items
    public static final RegistryEntry<Item> MINER_CERTIFICATE = ITEMS.register("miner_certificate", () -> ModItems.MINER_CERTIFICATE);
    public static final RegistryEntry<Item> SHAPE_CERTIFICATE_NOVICE = ITEMS.register("shape_certificate_novice", () -> ModItems.SHAPE_CERTIFICATE_NOVICE);
    public static final RegistryEntry<Item> SHAPE_CERTIFICATE_APPRENTICE = ITEMS.register("shape_certificate_apprentice", () -> ModItems.SHAPE_CERTIFICATE_APPRENTICE);
    public static final RegistryEntry<Item> SHAPE_CERTIFICATE_ADEPT = ITEMS.register("shape_certificate_adept", () -> ModItems.SHAPE_CERTIFICATE_ADEPT);
    public static final RegistryEntry<Item> SKILLS_RECORD = ITEMS.register("skills_record", () -> ModItems.SKILLS_RECORD);
    public static final RegistryEntry<Item> SHAPE_SELECTOR = ITEMS.register("shape_selector", () -> ModItems.SHAPE_SELECTOR);
    public static final RegistryEntry<Item> INK_CHAMBER = ITEMS.register("ink_chamber", () -> ModItems.INK_CHAMBER);
    public static final RegistryEntry<Item> PEN = ITEMS.register("pen", () -> ModItems.PEN);
    public static final RegistryEntry<Item> CARD_BLUEPRINT = ITEMS.register("card_blueprint", () -> ModItems.CARD_BLUEPRINT);

    public static final RegistryEntry<MiningSkillCardItem> MINING_SKILL_CARD_EMPTY = ITEMS.register("mining_skill_card_empty", () -> ModItems.MINING_SKILL_CARD_EMPTY);
    public static final RegistryEntry<MiningSkillCardItem> MINING_SKILL_CARD_PICKAXE = ITEMS.register("mining_skill_card_pickaxe", () -> ModItems.MINING_SKILL_CARD_PICKAXE);
    public static final RegistryEntry<MiningSkillCardItem> MINING_SKILL_CARD_AXE = ITEMS.register("mining_skill_card_axe", () -> ModItems.MINING_SKILL_CARD_AXE);
    public static final RegistryEntry<MiningSkillCardItem> MINING_SKILL_CARD_SHOVEL = ITEMS.register("mining_skill_card_shovel", () -> ModItems.MINING_SKILL_CARD_SHOVEL);
    public static final RegistryEntry<MiningSkillCardItem> MINING_SKILL_CARD_HOE = ITEMS.register("mining_skill_card_hoe", () -> ModItems.MINING_SKILL_CARD_HOE);

    // Mob Effects
    public static final RegistryEntry<MobEffect> MINE_GO_JUICE_PICKAXE = MOB_EFFECTS.register("mine_go_juice_pickaxe", () -> ModMobEffects.MINE_GO_JUICE_PICKAXE);
    public static final RegistryEntry<MobEffect> MINE_GO_JUICE_AXE = MOB_EFFECTS.register("mine_go_juice_axe", () -> ModMobEffects.MINE_GO_JUICE_AXE);
    public static final RegistryEntry<MobEffect> MINE_GO_JUICE_SHOVEL = MOB_EFFECTS.register("mine_go_juice_shovel", () -> ModMobEffects.MINE_GO_JUICE_SHOVEL);
    public static final RegistryEntry<MobEffect> MINE_GO_JUICE_HOE = MOB_EFFECTS.register("mine_go_juice_hoe", () -> ModMobEffects.MINE_GO_JUICE_HOE);

    // Potions
    public static final RegistryEntry<Potion> KNOWLEDGE_POTION = POTIONS.register("knowledge", () -> new Potion("knowledge"));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_PICKAXE_POTION = POTIONS.register("mine_go_juice_pickaxe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_PICKAXE, 0)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_PICKAXE_POTION2 = POTIONS.register("mine_go_juice_pickaxe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_PICKAXE, 1)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_PICKAXE_POTION3 = POTIONS.register("mine_go_juice_pickaxe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_PICKAXE, 2)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_AXE_POTION = POTIONS.register("mine_go_juice_axe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_AXE, 0)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_AXE_POTION2 = POTIONS.register("mine_go_juice_axe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_AXE, 1)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_AXE_POTION3 = POTIONS.register("mine_go_juice_axe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_AXE, 2)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_SHOVEL_POTION = POTIONS.register("mine_go_juice_shovel", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_SHOVEL, 0)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_SHOVEL_POTION2 = POTIONS.register("mine_go_juice_shovel_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_SHOVEL, 1)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_SHOVEL_POTION3 = POTIONS.register("mine_go_juice_shovel_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_SHOVEL, 2)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_HOE_POTION = POTIONS.register("mine_go_juice_hoe", () -> new MineGoPotion(MiningSkillCardItem.Tier.Novice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_HOE, 0)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_HOE_POTION2 = POTIONS.register("mine_go_juice_hoe_2", () -> new MineGoPotion(MiningSkillCardItem.Tier.Apprentice, new MineGoJuiceEffectInstance(MINE_GO_JUICE_HOE, 1)));
    public static final RegistryEntry<Potion> MINE_GO_JUICE_HOE_POTION3 = POTIONS.register("mine_go_juice_hoe_3", () -> new MineGoPotion(MiningSkillCardItem.Tier.Adept, new MineGoJuiceEffectInstance(MINE_GO_JUICE_HOE, 2)));

    // Containers
    public static final RegistryEntry<MenuType<SkillsRecordMenu>> SKILLS_RECORD_CONTAINER = CONTAINERS.register("skills_record", () -> ExtendedMenus.create((id, inv, buf) -> new SkillsRecordMenu(id, inv, new RegistryFriendlyByteBuf(buf, inv.player.level().registryAccess()))));
    public static final RegistryEntry<MenuType<ShapeSelectorMenu>> SHAPE_SELECTOR_CONTAINER = CONTAINERS.register("shape_selector", () -> ExtendedMenus.create((id, inv, buf) -> new ShapeSelectorMenu(id, inv)));

    // Recipe Serializer
    public static final RegistryEntry<RecipeSerializer<ItemStorageDataRecipe>> ITEM_DATA_STORAGE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("item_storage_data", () -> ItemStorageDataRecipe.Serializer.INSTANCE);
    public static final RegistryEntry<RecipeSerializer<MCRecipe>> MC_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("mining_card_recipe", () -> MCRecipe.Serializer.INSTANCE);

    // Data Component
    // Same id as the pre-SavedData component, whose data it still reads for migration.
    public static final RegistryEntry<DataComponentType<SkillsRecordLink>> SKILLS_RECORD_DATA = DATA_COMPONENT_TYPE.register("skills_record_data", () -> SkillsRecordLink.DATA_COMPONENT);
    public static final RegistryEntry<DataComponentType<MiningSkillCardData>> MINING_SKILL_CARD_DATA = DATA_COMPONENT_TYPE.register("mining_skill_card_data", () -> MiningSkillCardData.DATA_COMPONENT);
    public static final RegistryEntry<DataComponentType<MinerCertificateData>> MINER_CERTIFICATE_DATA = DATA_COMPONENT_TYPE.register("miner_certificate_data", () -> MinerCertificateData.DATA_COMPONENT);
    public static final RegistryEntry<DataComponentType<ShapeCertificateData>> SHAPE_CERTIFICATE_DATA = DATA_COMPONENT_TYPE.register("shape_certificate_data", () -> ShapeCertificateData.DATA_COMPONENT);
    public static final RegistryEntry<DataComponentType<Identifier>> SHAPE_CERTIFICATE_SHAPE = DATA_COMPONENT_TYPE.register("shape_certificate_shape", () -> ShapeCertificateData.SHAPE_COMPONENT);
    public static final RegistryEntry<DataComponentType<StorageItemData>> ITEM_STORAGE_DATA = DATA_COMPONENT_TYPE.register("item_storage_data", () -> StorageItemData.DATA_COMPONENT);
    public static final RegistryEntry<DataComponentType<SelectedShapeData>> SELECTED_SHAPE_COMPONENT = DATA_COMPONENT_TYPE.register("selected_shape", () -> DataComponentType.<SelectedShapeData>builder().persistent(SelectedShapeData.CODEC).networkSynchronized(SelectedShapeData.STREAM_CODEC).build());

    // Particles
    public static final RegistryEntry<SimpleParticleType> CELEBRATE_PARTICLE = PARTICLE_TYPES.register("celebrate", () -> ParticleTypes.simple(true));

    // Arguments
    public static final RegistryEntry<ArgumentTypeInfo<CardTierArgument, ?>> CARD_TIER_ARGUMENT = ARGUMENT_TYPES.register("card_tier", () -> ArgumentTypeRegistry.register(CardTierArgument.class, SingletonArgumentInfo.contextFree(CardTierArgument::tier)));
    public static final RegistryEntry<ArgumentTypeInfo<CardHolderArgument, ?>> CARD_SLOTS_ARGUMENT = ARGUMENT_TYPES.register("card_holder_all", () -> ArgumentTypeRegistry.register(CardHolderArgument.class, SingletonArgumentInfo.contextFree(CardHolderArgument::allSlots)));
    public static final RegistryEntry<ArgumentTypeInfo<CardHolderArgument.RecordSlots, ?>> CARD_HOLDER_ALL_ARGUMENT = ARGUMENT_TYPES.register("card_holder_record", () -> ArgumentTypeRegistry.register(CardHolderArgument.RecordSlots.class, SingletonArgumentInfo.contextFree(CardHolderArgument::recordSlots)));
    public static final RegistryEntry<ArgumentTypeInfo<ChallengesArgument, ?>> CARD_HOLDER_RECORD_ARGUMENT = ARGUMENT_TYPES.register("challenges", () -> ArgumentTypeRegistry.register(ChallengesArgument.class, SingletonArgumentInfo.contextFree(ChallengesArgument::data)));
    public static final RegistryEntry<ArgumentTypeInfo<UltimineShapeArgument, ?>> ULTIMINE_SHAPE_ARGUMENT = ARGUMENT_TYPES.register("ultimine_shape", () -> ArgumentTypeRegistry.register(UltimineShapeArgument.class, SingletonArgumentInfo.contextFree(UltimineShapeArgument::shape)));

    //Criterion Triggers
    public static final RegistryEntry<UltimineObtainTrigger> ULTIMINE_OBTAIN_TRIGGER = CRITERIA_TRIGGERS.register("ultimine_obtain", UltimineObtainTrigger::new);
}
