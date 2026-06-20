package net.ixdarklord.ultimine_addition.core.forge;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.platform.forge.EventBuses;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.data.player.forge.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ChunkUnloadEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ConfigLifecycleEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.config.ConfigInfo;
import net.ixdarklord.ultimine_addition.core.CommonSetup;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBUltimineAddition.MOD_ID)
public final class ForgeSetup {
   public ForgeSetup() {
      EventBuses.registerModEventBus(FTBUltimineAddition.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
      CommonSetup.init();
   }

   @EventBusSubscriber(modid = FTBUltimineAddition.MOD_ID, bus = Bus.MOD)
   public static class EventBus {
      @SubscribeEvent
      public static void onCommonSetup(FMLCommonSetupEvent event) {
         event.enqueueWork(() -> {
            CraftingHelper.register(LegacyModeCondition.Serializer.INSTANCE);
            CommonSetup.setup();
         });
      }

      @SubscribeEvent
      public static void onConfigLoading(ModConfigEvent.Loading event) {
         ModConfig config = event.getConfig();
         ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()), ConfigLifecycleEvent.ConfigUpdateType.LOADING);
      }

      @SubscribeEvent
      public static void onConfigReloading(ModConfigEvent.Reloading event) {
         ModConfig config = event.getConfig();
         ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()), ConfigLifecycleEvent.ConfigUpdateType.RELOADING);
      }

      @SubscribeEvent
      public static void onConfigUnloading(ModConfigEvent.Unloading event) {
         ModConfig config = event.getConfig();
         ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()), ConfigLifecycleEvent.ConfigUpdateType.UNLOADING);
      }
   }

   @EventBusSubscriber(
           modid = FTBUltimineAddition.MOD_ID
   )
   public static class Event {
      @SubscribeEvent
      public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
         event.register(PlayerAbilityData.class);
      }

      @SubscribeEvent
      public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
         if (event.getObject() instanceof Player && !event.getObject().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).isPresent()) {
            event.addCapability(ResourceLocation.tryBuild(FTBUltimineAddition.MOD_ID, "properties"), new PlayerUltimineCapabilityProvider());
         }

      }

      @SubscribeEvent
      public static void onTagUpdate(TagsUpdatedEvent event) {
         DatapackEvents.TagUpdate.Cause cause;
         switch (event.getUpdateCause()) {
            case SERVER_DATA_LOAD -> cause = DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD;
            case CLIENT_PACKET_RECEIVED -> cause = DatapackEvents.TagUpdate.Cause.CLIENT_PACKET_RECEIVED;
            default -> throw new IncompatibleClassChangeError();
         }

         DatapackEvents.TagUpdate.Cause REASON = cause;
         DatapackEvents.TAG_UPDATE.invoker().init(event.getRegistryAccess(), REASON, event.shouldUpdateStaticData());
      }

      @SubscribeEvent
      public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.getEntity() instanceof ServerPlayer player) {
            DatapackEvents.SYNC.invoker().init(player, true);
         }

      }

      @SubscribeEvent
      public static void onPlayerClone(PlayerEvent.Clone event) {
         event.getOriginal().reviveCaps();
         event.getOriginal().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent((oldPlayer) -> event.getEntity().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent((newPlayer) -> newPlayer.copyFrom(oldPlayer)));
         event.getOriginal().invalidateCaps();
      }

      @SubscribeEvent
      public static void onDatapackSync(OnDatapackSyncEvent event) {
         if (event.getPlayer() != null) {
            DatapackEvents.SYNC.invoker().init(event.getPlayer(), false);
         }

      }

      @SubscribeEvent
      public static void onChunkUnload(ChunkEvent.Unload event) {
         LevelAccessor accessor = event.getLevel();
         if (accessor instanceof ServerLevel level) {
            ChunkUnloadEvent.EVENT.invoker().Unload(event.getChunk(), level);
         }

      }

      @SubscribeEvent
      public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
         CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(event.getState(), event.getContext(), ToolAction.get(event.getToolAction().name()), event.isSimulated());
         if (result.object() != null) {
            event.setFinalState(result.object());
         }

      }
   }
}
