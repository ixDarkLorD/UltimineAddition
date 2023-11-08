package net.ixdarklord.ultimine_addition.common.data;

import net.ixdarklord.coolcat_lib.common.data.IDataHandler;
import net.ixdarklord.ultimine_addition.common.data.item.ItemStorageData;
import net.ixdarklord.ultimine_addition.common.data.item.MinerCertificateData;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.item.SkillsRecordData;
import net.ixdarklord.ultimine_addition.common.network.PacketHandler;
import net.ixdarklord.ultimine_addition.common.network.packet.ItemStorageDataPacket;
import net.ixdarklord.ultimine_addition.common.network.packet.MinerCertificatePacket;
import net.ixdarklord.ultimine_addition.common.network.packet.MiningSkillCardPacket;
import net.ixdarklord.ultimine_addition.common.network.packet.SkillsRecordPacket;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public abstract class DataHandler<E, T> implements IDataHandler<E, T> {
    protected final String NBTBase;
    protected ServerPlayer player;
    private boolean isS2CPacket;
    private boolean isC2SPacket;
    protected boolean isDebug;
    protected Logger LOGGER;

    protected DataHandler() {
        this.NBTBase = "UAProperties";
    }

    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this;
    }

    @Override
    public void saveData(T data) {
        if (this.isC2SPacket) {
            if (this instanceof SkillsRecordData)
                PacketHandler.sendToServer(new SkillsRecordPacket.Toggle((SkillsRecordData) this));
        }
        if (this.isS2CPacket) {
            if (this instanceof SkillsRecordData)
                PacketHandler.sendToPlayer(new SkillsRecordPacket((SkillsRecordData) this), player);
            if (this instanceof MiningSkillCardData)
                PacketHandler.sendToPlayer(new MiningSkillCardPacket((MiningSkillCardData) this), player);
            if (this instanceof ItemStorageData)
                PacketHandler.sendToPlayer(new ItemStorageDataPacket((ItemStorageData) this), player);
            if (this instanceof MinerCertificateData)
                PacketHandler.sendToPlayer(new MinerCertificatePacket((MinerCertificateData) this), player);
        } else this.clientUpdate();
    }

    @SuppressWarnings({"unchecked", "unused"})
    public E enableDebug(Logger LOGGER) {
        this.isDebug = true;
        this.LOGGER = LOGGER;
        return (E) this;
    }
    public void printDebug() {
        if (LOGGER == null) return;
        LOGGER.info("Debug Mode is Enabled!");
    }

    @Override
    @SuppressWarnings("unchecked")
    public E sendToServer() {
        isC2SPacket = true;
        return (E) this;
    }
    @Override
    @SuppressWarnings("unchecked")
    public E sendToClient(ServerPlayer player) {
        this.player = player;
        isS2CPacket = true;
        return (E) this;
    }

    public String getNBTBase() {
        return NBTBase;
    }
}
