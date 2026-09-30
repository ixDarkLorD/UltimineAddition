package net.ixdarklord.ultimine_addition.core.neoforge;

import net.ixdarklord.coolcatcore.api.event.v2.core.EventResultHolder;
import com.mojang.serialization.MapCodec;
import net.ixdarklord.ultimine_addition.common.data.player.PlayerAbilityData;
import net.ixdarklord.ultimine_addition.common.event.impl.BlockToolModificationEvent;
import net.ixdarklord.ultimine_addition.common.event.impl.DatapackEvents;
import net.ixdarklord.coolcatcore.api.core.neoforge.NeoForgeModEntrypoint;
import net.ixdarklord.ultimine_addition.core.UltimineAdditionClientConstructor;
import net.ixdarklord.ultimine_addition.core.UltimineAdditionConstructor;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.Registration;
import net.ixdarklord.ultimine_addition.datagen.recipe.conditions.LegacyModeCondition;
import net.ixdarklord.ultimine_addition.util.ToolAction;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

// The mod's @Mod class: CoolCatLib constructs the common and client entry points; NeoForge-only setup is here.
@Mod(FTBUltimineAddition.MOD_ID)
public final class NeoForgeSetup extends NeoForgeModEntrypoint {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, FTBUltimineAddition.MOD_ID);

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, FTBUltimineAddition.MOD_ID);

    // Where the ability data was kept before it became a CoolCatLib attachment (Registration.PLAYER_ABILITY). Still
    // registered so older saves are read; moved over when the player logs in.
    private static final Supplier<AttachmentType<PlayerAbilityData>> LEGACY_PLAYER_ABILITY_DATA = ATTACHMENT_TYPES.register(
            "player_ability", () -> AttachmentType.builder(PlayerAbilityData::create).serialize(PlayerAbilityData.CODEC).build()
    );

    public NeoForgeSetup(ModContainer container) {
        super(container);
        CONDITION_CODECS.register("legacy_mode", () -> LegacyModeCondition.CODEC);
        CONDITION_CODECS.register(this.modEventBus);
        ATTACHMENT_TYPES.register(this.modEventBus);
        this.common(UltimineAdditionConstructor::new);
        this.client(() -> UltimineAdditionClientConstructor::new);
    }

    @EventBusSubscriber(modid = FTBUltimineAddition.MOD_ID)
    public static class Event {
        @SubscribeEvent
        private static void onTagsUpdate(TagsUpdatedEvent event) {
            DatapackEvents.TagUpdate.Cause cause = event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED ? DatapackEvents.TagUpdate.Cause.CLIENT_PACKET_RECEIVED : DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD;
            DatapackEvents.TAG_UPDATE.invoker().init(event.getRegistryAccess(), cause, cause == DatapackEvents.TagUpdate.Cause.SERVER_DATA_LOAD || Minecraft.getInstance().getSingleplayerServer() == null);
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (player.hasData(LEGACY_PLAYER_ABILITY_DATA)) {
                    if (!Registration.PLAYER_ABILITY.has(player)) Registration.PLAYER_ABILITY.set(player, player.getData(LEGACY_PLAYER_ABILITY_DATA));
                    player.removeData(LEGACY_PLAYER_ABILITY_DATA);
                }
                DatapackEvents.SYNC.invoker().init(player, true);
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
            EventResultHolder<BlockState> result = BlockToolModificationEvent.EVENT.invoker().modify(event.getState(), event.getContext(), ToolAction.get(event.getItemAbility().name()), event.isSimulated());
            if (result.isInterrupt()) {
                if (result.result().getAsBoolean()) {
                    event.setCanceled(true);
                }
                result.getValue().ifPresent(event::setFinalState);
            }
        }
    }
}