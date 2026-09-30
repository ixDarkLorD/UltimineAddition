package net.ixdarklord.ultimine_addition.core.forge;

import net.ixdarklord.coolcatcore.api.core.forge.ForgeModEntrypoint;
import net.ixdarklord.coolcatcore.api.event.v2.core.EventResultHolder;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.UltimineAdditionClientConstructor;
import net.ixdarklord.ultimine_addition.core.UltimineAdditionConstructor;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The mod's @Mod class: CoolCatLib constructs the common and client entry points; Forge-only setup is here.
// (The ability data kept in a Forge capability by the earlier 1.20.1 releases is migrated by MixinLegacyPlayerData.)
@Mod(FTBUltimineAddition.MOD_ID)
public final class ForgeSetup extends ForgeModEntrypoint {
    public ForgeSetup(FMLJavaModLoadingContext context) {
        super(context);
        // Forge 47's recipe conditions are registered by their serializer.
        this.modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> CraftingHelper.register(LegacyModeCondition.Serializer.INSTANCE)));
        this.common(UltimineAdditionConstructor::new);
        this.client(() -> UltimineAdditionClientConstructor::new);
    }

    @Mod.EventBusSubscriber(modid = FTBUltimineAddition.MOD_ID)
    public static class Event {
        @SubscribeEvent
        public static void onTagsUpdate(TagsUpdatedEvent event) {
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
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            if (event.getPlayer() != null) {
                DatapackEvents.SYNC.invoker().init(event.getPlayer(), false);
            }
        }

        // Migration: the Card Blueprint was removed; worlds that still have some drop them without Forge's missing
        // registry entries warning.
        @SubscribeEvent
        public static void onMissingMappings(MissingMappingsEvent event) {
            for (MissingMappingsEvent.Mapping<?> mapping : event.getMappings(Registries.ITEM, FTBUltimineAddition.MOD_ID)) {
                if (mapping.getKey().getPath().equals("card_blueprint")) mapping.ignore();
            }
        }

        @SubscribeEvent
        public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
            EventResultHolder<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(event.getState(), event.getContext(), ToolAction.get(event.getToolAction().name()), event.isSimulated());
            if (result.isInterrupt()) {
                if (result.result().getAsBoolean()) {
                    event.setCanceled(true);
                }
                result.getValue().ifPresent(event::setFinalState);
            }
        }
    }
}
