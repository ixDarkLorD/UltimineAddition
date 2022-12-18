package net.ixdarklord.ultimine_addition.network.packet;

import net.ixdarklord.ultimine_addition.item.MinerCertificate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CertificateEffectPacket {
    private final ItemStack stack;
    private Entity entity;

    public CertificateEffectPacket(ItemStack stack, Entity entity) {
        this.stack = stack;
        this.entity = entity;
    }

    public CertificateEffectPacket(FriendlyByteBuf buf) {
        Minecraft MC = Minecraft.getInstance();
        this.stack = buf.readItem();
        if (MC.level != null)
            this.entity = MC.level.getEntity(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(stack);
        buf.writeInt(entity.getId());
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                MinerCertificate.playAnimation(stack, entity)));
    }
}
