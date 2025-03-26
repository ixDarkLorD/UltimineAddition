package net.ixdarklord.ultimine_addition.common.data;

public abstract class DataHandler<D extends DataHandler<?, ?>, T> {
    protected T dataHolder;
    protected final String NBTBase;
    protected boolean isDebug;

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

    public String getNBTBase() {
        return NBTBase;
    }
}