package net.ixdarklord.ultimine_addition.event;

import net.ixdarklord.ultimine_addition.command.SetCapabilityCommand;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineData;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packet.PlayerCapabilityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class EventsHandler {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        SetCapabilityCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        if (!event.getLevel().isClientSide()) {
            BlockState blockState = event.getLevel().getBlockState(event.getPos());
            MinerCertificate.onBreakBlock(blockState, event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(final EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(capability ->
                        PacketHandler.sendToPlayer(new PlayerCapabilityPacket.DataSyncS2C(capability.getCapability()), player)
                );
            }
        }
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
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(oldStore ->
                    event.getOriginal().getCapability(PlayerUltimineCapabilityProvider.CAPABILITY).ifPresent(newStore ->
                    newStore.copyFrom(oldStore)));
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerUltimineData.class);
        event.register(MinerCertificateData.class);
    }
}
