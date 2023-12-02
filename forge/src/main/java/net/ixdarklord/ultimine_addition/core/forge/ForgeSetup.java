package net.ixdarklord.ultimine_addition.core.forge;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.platform.forge.EventBuses;
import net.ixdarklord.ultimine_addition.common.brewing.MineGoJuiceRecipe;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.data.player.forge.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ChunkUnloadEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.common.event.impl.ToolAction;
import net.ixdarklord.ultimine_addition.core.CommonSetup;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.integration.curios.CuriosIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ForgeSetup {
    public ForgeSetup() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Constants.MOD_ID, modEventBus);
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeClientSetup::new);
        CommonSetup.init();
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID)
    public static class Event {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(PlayerAbilityData.class);
        }
        @SubscribeEvent
        public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                if (!event.getObject().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).isPresent()) {
                    event.addCapability(new ResourceLocation(Constants.MOD_ID, "properties"), new PlayerUltimineCapabilityProvider());
                }
            }
        }
        @SubscribeEvent
        public static void onTagUpdate(final TagsUpdatedEvent event) {
            DatapackEvents.TagUpdate.Cause REASON = switch (event.getUpdateCause()) {
                case SERVER_DATA_LOAD -> DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD;
                case CLIENT_PACKET_RECEIVED -> DatapackEvents.TagUpdate.Cause.CLIENT_PACKET_RECEIVED;
            };
            DatapackEvents.TAG_UPDATE.invoker().init(event.getRegistryAccess(), REASON, event.shouldUpdateStaticData());
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                DatapackEvents.SYNC.invoker().init(player, true);
            }
        }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                event.getOriginal().reviveCaps();
                event.getOriginal().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(oldPlayer ->
                        event.getEntity().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(newPlayer ->
                                newPlayer.copyFrom(oldPlayer)));
                event.getOriginal().invalidateCaps();
            }
        }

        @SubscribeEvent
        public static void onDatapackSync(final OnDatapackSyncEvent event) {
            if (event.getPlayer() != null) {
                DatapackEvents.SYNC.invoker().init(event.getPlayer(), false);
            }
        }

        @SubscribeEvent
        public static void onChunkUnload(final ChunkDataEvent.Unload event) {
            if (event.getLevel() instanceof ServerLevel level)
                ChunkUnloadEvent.EVENT.invoker().Unload(event.getChunk(), level);
        }

        @SubscribeEvent
        public static void onBlockToolModification(final BlockEvent.BlockToolModificationEvent event) {
            CompoundEventResult<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(event.getState(), event.getFinalState(), event.getContext(), ToolAction.get(event.getToolAction().name()), event.isSimulated());
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
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventBus {
        @SubscribeEvent
        public static void onCommonSetup(final FMLCommonSetupEvent event) {
            event.enqueueWork(MineGoJuiceRecipe::register);
        }

        @SubscribeEvent
        public static void onIMC(final InterModEnqueueEvent event) {
            if (ServicePlatform.SlotAPI.isModLoaded())
                CuriosIntegration.sendIMC();
        }
    }
}
