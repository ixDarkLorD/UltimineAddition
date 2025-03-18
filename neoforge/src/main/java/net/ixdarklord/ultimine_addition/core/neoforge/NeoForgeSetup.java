package net.ixdarklord.ultimine_addition.core.neoforge;

import com.mojang.serialization.MapCodec;
import dev.architectury.event.CompoundEventResult;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ConfigLifecycleEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.config.ConfigInfo;
import net.ixdarklord.ultimine_addition.core.CommonSetup;
import net.ixdarklord.ultimine_addition.core.UltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(UltimineAddition.MOD_ID)
public class NeoForgeSetup {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, UltimineAddition.MOD_ID);

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, UltimineAddition.MOD_ID);

    public static final Supplier<AttachmentType<PlayerAbilityData>> PLAYER_ABILITY_DATA = ATTACHMENT_TYPES.register(
            "player_ability", () -> AttachmentType.builder(PlayerAbilityData::create).serialize(PlayerAbilityData.CODEC).build()
    );

    public NeoForgeSetup(IEventBus bus) {
        CommonSetup.init();
        CONDITION_CODECS.register("legacy_mode", () -> LegacyModeCondition.CODEC);
        CONDITION_CODECS.register(bus);
        ATTACHMENT_TYPES.register(bus);
    }

    @EventBusSubscriber(modid = UltimineAddition.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    private static class EventBus {
        @SubscribeEvent
        private static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(CommonSetup::setup);
        }

        @SubscribeEvent
        private static void onConfigLoading(ModConfigEvent.Loading event) {
            ModConfig config = event.getConfig();
            ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(
                    new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()),
                    ConfigLifecycleEvent.ConfigUpdateType.LOADING
            );
        }

        @SubscribeEvent
        private static void onConfigReloading(ModConfigEvent.Reloading event) {
            ModConfig config = event.getConfig();
            ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(
                    new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()),
                    ConfigLifecycleEvent.ConfigUpdateType.RELOADING
            );
        }

        @SubscribeEvent
        private static void onConfigUnloading(ModConfigEvent.Unloading event) {
            ModConfig config = event.getConfig();
            ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(
                    new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()),
                    ConfigLifecycleEvent.ConfigUpdateType.UNLOADING
            );
        }
    }

    @EventBusSubscriber(modid = UltimineAddition.MOD_ID)
    public static class Event {
        @SubscribeEvent
        private static void onTagsUpdate(TagsUpdatedEvent event) {
            DatapackEvents.TagUpdate.Cause cause = event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED ? DatapackEvents.TagUpdate.Cause.CLIENT_PACKET_RECEIVED : DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD;
            DatapackEvents.TAG_UPDATE.invoker().init(event.getRegistryAccess(), cause, cause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD || Minecraft.getInstance().getSingleplayerServer() == null);
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                DatapackEvents.SYNC.invoker().init(player, true);
            }
        }

        @SubscribeEvent
        private static void onPlayerClone(PlayerEvent.Clone event) {
            if (event.isWasDeath() && event.getOriginal().hasData(PLAYER_ABILITY_DATA)) {
                event.getEntity().getData(PLAYER_ABILITY_DATA).copyFrom(event.getOriginal().getData(PLAYER_ABILITY_DATA));
            }
        }

        @SubscribeEvent
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            if (event.getPlayer() != null) {
                DatapackEvents.SYNC.invoker().init(event.getPlayer(), false);
            }
        }

        @SubscribeEvent
        public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
            CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(event.getState(), event.getFinalState(), event.getContext(), ToolAction.get(event.getItemAbility().name()), event.isSimulated());
            if (result.isPresent()) {
                if (result.isFalse()) {
                    event.setCanceled(true);
                }
                if (result.object() != null) {
                    event.setFinalState(result.object());
                }
            }
        }
    }
}