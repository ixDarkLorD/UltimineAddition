package net.ixdarklord.ultimine_addition.config;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record ConfigValueWrapper<T>(Class<T> type, T value) {
    private static final Map<Class<?>, EncoderDecoder<?>> TYPE_CODECS = new HashMap<>();
    private static final Map<String, Supplier<Class<?>>> TYPE_FACTORIES = new HashMap<>();

    public static <T> void registerType(Class<T> typeClass, BiConsumer<FriendlyByteBuf, T> encoder, Function<FriendlyByteBuf, T> decoder) {
        TYPE_CODECS.put(typeClass, new EncoderDecoder<>(encoder, decoder));
        TYPE_FACTORIES.put(typeClass.getName(), () -> typeClass);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.type.getName());
        getCodec(this.type).encode(buffer, this.value);
    }

    public static @NotNull ConfigValueWrapper<?> decode(FriendlyByteBuf buffer) {
        String className = buffer.readUtf();
        Class<?> typeClass = resolveClass(className);
        EncoderDecoder<Object> codec = getCodec(typeClass);
        Object value = codec.decode(buffer);
        return createWrapper(typeClass, value);
    }

    private static Class<?> resolveClass(String className) {
        Supplier<Class<?>> factory = TYPE_FACTORIES.get(className);
        if (factory == null) {
            throw new IllegalArgumentException("Unregistered type: " + className);
        } else {
            return factory.get();
        }
    }

    private static <V> EncoderDecoder<V> getCodec(Class<?> typeClass) {
        EncoderDecoder<V> codec = (EncoderDecoder<V>) TYPE_CODECS.get(typeClass);
        if (codec == null) {
            throw new IllegalArgumentException("No codec for type: " + typeClass.getSimpleName());
        } else {
            return codec;
        }
    }

    private static <V> ConfigValueWrapper<V> createWrapper(Class<?> typeClass, Object value) {
        return new ConfigValueWrapper<>((Class<V>) typeClass, (V) value);
    }

    public <V> V getValueAs(Class<V> targetType) {
        if (targetType.isAssignableFrom(this.type)) {
            return (V) this.value;
        } else {
            String string = this.type.getSimpleName();
            throw new ClassCastException("Cannot cast " + string + " to " + targetType.getSimpleName());
        }
    }

    static {
        registerType(Boolean.class, FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
        registerType(Integer.class, FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt);
        registerType(Double.class, FriendlyByteBuf::writeDouble, FriendlyByteBuf::readDouble);
        registerType(String.class, FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
        registerType(PlaystyleMode.class, (buf, v) -> buf.writeUtf(v.name()), (buf) -> PlaystyleMode.valueOf(buf.readUtf()));
    }

    private record EncoderDecoder<T>(BiConsumer<FriendlyByteBuf, T> encoder,
                                     Function<FriendlyByteBuf, T> decoder) {
        void encode(FriendlyByteBuf buf, T value) {
            this.encoder.accept(buf, value);
        }

        T decode(FriendlyByteBuf buf) {
            return this.decoder.apply(buf);
        }
    }
}
