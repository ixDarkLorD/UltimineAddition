package net.ixdarklord.ultimine_addition.core.fabric;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.ftb.mods.ftbultimine.mixin.AxeItemAccess;
import dev.ftb.mods.ftbultimine.mixin.ShovelItemAccess;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.ConfigLifecycleEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.ultimine_addition.config.ConfigInfo;
import net.ixdarklord.ultimine_addition.core.CommonSetup;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.ixdarklord.ultimine_addition.util.ToolActions;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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
        UseBlockCallback.EVENT.register(this::onRightClick);
    }

    private void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
        boolean state = ServicePlatform.get().players().isPlayerUltimineCapable(oldPlayer);
        ServicePlatform.get().players().setPlayerUltimineCapability(newPlayer, state);
    }

    private InteractionResult onRightClick(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSpectator()) return InteractionResultHolder.pass(stack).getResult();
        UseOnContext context = new UseOnContext(player, hand, hitResult);
        BlockState originalState = level.getBlockState(hitResult.getBlockPos());
        BlockState finalState = originalState;
        ToolAction toolAction = null;
        if (stack.getItem() instanceof AxeItem item && ((AxeItemAccess) item).invokeGetStripped(originalState).isPresent()) {
            finalState = ((AxeItemAccess) item).invokeGetStripped(originalState).get();
            toolAction = ToolActions.AXE_STRIP;
        }

        //noinspection ConstantValue
        if (stack.getItem() instanceof ShovelItem && ShovelItemAccess.getFlattenables().get(originalState.getBlock()) != null) {
            finalState = ShovelItemAccess.getFlattenables().get(originalState.getBlock());
            toolAction = ToolActions.SHOVEL_FLATTEN;
        }
        if (stack.getItem() instanceof HoeItem) {
            /* TODO: I need to find a way to get the other BlockState from the HoeItem#TILLABLES */
            Block block = originalState.getBlock();
            if ((block == Blocks.GRASS_BLOCK
                    || block == Blocks.DIRT_PATH
                    || block == Blocks.DIRT
                    || block == Blocks.COARSE_DIRT)
                    && context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                finalState = block == Blocks.COARSE_DIRT ? Blocks.DIRT.defaultBlockState() : Blocks.FARMLAND.defaultBlockState();
            }
            toolAction = ToolActions.HOE_TILL;
        }

        if (toolAction == null) return InteractionResultHolder.pass(stack).getResult();
        CompoundEventResult<BlockState> event = BlockToolModificationEvent.EVENT.invoker().modify(originalState, finalState, context, toolAction, false);
        if (event.isPresent()) {
            if (event.isFalse()) {
                return event.result().asMinecraft();
            }
            if (event.object() != null) {
                level.setBlockAndUpdate(hitResult.getBlockPos(), event.object());
            }
        }
        return InteractionResultHolder.pass(stack).getResult();
    }
}
