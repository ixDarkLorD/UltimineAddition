package net.ixdarklord.ultimine_addition.event;

import net.ixdarklord.ultimine_addition.command.SetCapabilityCommand;
import net.ixdarklord.ultimine_addition.core.plugin.FTBUltimatePlugin;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.data.item.MinerCertificateProvider;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineData;
import net.ixdarklord.ultimine_addition.data.player.PlayerUltimineCapabilityProvider;
import net.ixdarklord.ultimine_addition.core.Constants;
import net.ixdarklord.ultimine_addition.helper.Services;
import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class EventsHandler {
    @SubscribeEvent
    public static void onKeyInput(final InputEvent.Key event) {
        FTBUltimatePlugin.keyEvent(Minecraft.getInstance().player);
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        SetCapabilityCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        MinerCertificate.onBreakBlock(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        MinerCertificate.checkingBlockInFront();
        if (!event.player.getLevel().isClientSide)
            FTBUltimatePlugin.canPlayerUltimine = Services.PLATFORM.isPlayerCapable(event.player);
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
    public static void onAttachCapabilitiesItem(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof MinerCertificate) {
            event.addCapability(new ResourceLocation(Constants.MOD_ID, "properties"), new MinerCertificateProvider());
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
