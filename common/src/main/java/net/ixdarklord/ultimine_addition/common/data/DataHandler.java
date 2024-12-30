package net.ixdarklord.ultimine_addition.common.data;

import org.slf4j.Logger;

public abstract class DataHandler<D extends DataHandler<?, ?>, T> {
    protected T dataHolder;
    protected final String NBTBase;
    protected boolean isDebug;
    protected Logger LOGGER;

    public DataHandler() {
        this.NBTBase = "UAProperties";
    }

    public D setDataHolder(T dataHolder) {
        this.dataHolder = dataHolder;
        return (D) this;
    }

    public T get() {
        return this.dataHolder;
    }

    public D clientUpdate() {
        return (D) this;
    }

    public void saveData(T data) {}

    public D enableDebug(Logger LOGGER) {
        this.isDebug = true;
        this.LOGGER = LOGGER;
        return (D) this;
    }

    public void printDebug() {
        if (LOGGER == null) return;
        LOGGER.info("Debug Mode is Enabled!");
    }

    public String getNBTBase() {
        return NBTBase;
    }
}