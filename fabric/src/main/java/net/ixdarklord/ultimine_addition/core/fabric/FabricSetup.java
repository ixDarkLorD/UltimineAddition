package net.ixdarklord.ultimine_addition.core.fabric;

import dev.architectury.event.events.common.EntityEvent;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.ixdarklord.ultimine_addition.common.event.impl.ConfigLifecycleEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.config.ConfigInfo;
import net.ixdarklord.ultimine_addition.core.CommonSetup;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class FabricSetup implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonSetup.init();
        CommonSetup.setup();
        ResourceConditions.register(LegacyModeCondition.RESOURCE_CONDITION_TYPE);
        this.initEvents();
    }

    private void initEvents() {
        NeoForgeModConfigEvents.loading(FTBUltimineAddition.MOD_ID).register(config ->
                ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(
                        new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()),
                        ConfigLifecycleEvent.ConfigUpdateType.LOADING
                )
        );

        NeoForgeModConfigEvents.reloading(FTBUltimineAddition.MOD_ID).register(config ->
                ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(
                        new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()),
                        ConfigLifecycleEvent.ConfigUpdateType.RELOADING
                )
        );

        NeoForgeModConfigEvents.unloading(FTBUltimineAddition.MOD_ID).register(config ->
                ConfigLifecycleEvent.EVENT.invoker().onConfigUpdate(
                        new ConfigInfo(config.getModId(), config.getType().extension(), config.getSpec(), config.getFileName()),
                        ConfigLifecycleEvent.ConfigUpdateType.UNLOADING
                )
        );

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            DatapackEvents.TagUpdate.Cause cause = client ? DatapackEvents.TagUpdate.Cause.CLIENT_PACKET_RECEIVED : DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD;
            DatapackEvents.TAG_UPDATE.invoker().init(registries, cause, cause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD || Minecraft.getInstance().getSingleplayerServer() == null);
        });
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> DatapackEvents.PRE_RELOAD.invoker().init(server, resourceManager));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> DatapackEvents.SYNC.invoker().init(player, joined));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> DatapackEvents.POST_RELOAD.invoker().init(server, resourceManager, success));
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> EntityEvent.ADD.invoker().add(entity, world));
        ServerPlayerEvents.COPY_FROM.register(this::onPlayerClone);
    }

    private void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
        boolean state = ServicePlatform.get().players().isPlayerUltimineCapable(oldPlayer);
        ServicePlatform.get().players().setPlayerUltimineCapability(newPlayer, state);
    }
}
