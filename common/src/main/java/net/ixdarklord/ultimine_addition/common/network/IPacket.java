package net.ixdarklord.ultimine_addition.common.network;

import dev.architectury.networking.NetworkChannel;
import org.jetbrains.annotations.NotNull;

public interface IPacket {
    @NotNull
    NetworkChannel getChannel();
}
