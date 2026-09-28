package net.ixdarklord.ultimine_addition.core.fabric;

import net.ixdarklord.ultimine_addition.core.FTBUltimineIntegration;
import dev.ftb.mods.ftbultimine.api.fabric.FTBUltimineEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.minecraft.client.Minecraft;

// Fabric-only setup. The mod itself is constructed by CoolCatLib: Core from the "coolcatcore:common" entrypoint
// (UltimineAdditionConstructor).
public class FabricSetup implements ModInitializer {

    @Override
    public void onInitialize() {
        FTBUltimineEvents.REGISTER_RESTRICTION_HANDLER.register(data -> data.register(FTBUltimineIntegration.INSTANCE));
        ResourceConditions.register(LegacyModeCondition.RESOURCE_CONDITION_TYPE);
        this.initEvents();
    }

    private void initEvents() {
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            DatapackEvents.TagUpdate.Cause cause = client ? DatapackEvents.TagUpdate.Cause.CLIENT_PACKET_RECEIVED : DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD;
            DatapackEvents.TAG_UPDATE.invoker().init(registries, cause, cause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD || Minecraft.getInstance().getSingleplayerServer() == null);
        });
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> DatapackEvents.PRE_RELOAD.invoker().init(server, resourceManager));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> DatapackEvents.SYNC.invoker().init(player, joined));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> DatapackEvents.POST_RELOAD.invoker().init(server, resourceManager, success));
    }
}
