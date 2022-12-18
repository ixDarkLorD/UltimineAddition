package net.ixdarklord.ultimine_addition.core;

import net.ixdarklord.ultimine_addition.item.ItemRegistries;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.particle.ParticlesList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ForgeSetup {
    public ForgeSetup() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        new CommonSetup();
        ItemRegistries.register(modEventBus);
        ParticlesList.register(modEventBus);
        PacketHandler.register();

        MinecraftForge.EVENT_BUS.register(this);
    }
}