package net.ixdarklord.ultimine_addition.config;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record ConfigValueWrapper<T>(Class<T> type, T value) {
    private static final Map<Class<?>, StreamCodec<? super FriendlyByteBuf, ?>> TYPE_CODECS = new HashMap<>();
    private static final Map<String, Supplier<Class<?>>> TYPE_FACTORIES = new HashMap<>();

    static {
        registerType(Boolean.class, ByteBufCodecs.BOOL);
        registerType(Integer.class, ByteBufCodecs.INT);
        registerType(Double.class, ByteBufCodecs.DOUBLE);
        registerType(String.class, ByteBufCodecs.STRING_UTF8);
        registerType(PlaystyleMode.class, PlaystyleMode.STREAM_CODEC);
    }

    public static <T> void registerType(Class<T> typeClass, StreamCodec<? super FriendlyByteBuf, T> codec) {
        TYPE_CODECS.put(typeClass, codec);
        TYPE_FACTORIES.put(typeClass.getName(), () -> typeClass);
    }

    public static final StreamCodec<FriendlyByteBuf, ConfigValueWrapper<?>> CODEC = new StreamCodec<>() {
        @Override
        public void encode(FriendlyByteBuf buffer, ConfigValueWrapper<?> wrapper) {
            buffer.writeUtf(wrapper.type().getName());
            getCodec(wrapper.type()).encode(buffer, wrapper.value());
        }

        @Override
        public @NotNull ConfigValueWrapper<?> decode(FriendlyByteBuf buffer) {
            String className = buffer.readUtf();
            Class<?> typeClass = resolveClass(className);
            StreamCodec<FriendlyByteBuf, Object> codec = getCodec(typeClass);
            Object value = codec.decode(buffer);
            return createWrapper(typeClass, value);
        }

        private Class<?> resolveClass(String className) {
            Supplier<Class<?>> factory = TYPE_FACTORIES.get(className);
            if (factory == null) {
                throw new IllegalArgumentException("Unregistered type: " + className);
            }
            return factory.get();
        }

        @SuppressWarnings("unchecked")
        private <V> StreamCodec<FriendlyByteBuf, V> getCodec(Class<?> typeClass) {
            StreamCodec<? super FriendlyByteBuf, ?> codec = TYPE_CODECS.get(typeClass);
            if (codec == null) {
                throw new IllegalArgumentException("No codec for type: " + typeClass.getSimpleName());
            }
            return (StreamCodec<FriendlyByteBuf, V>) codec;
        }

        @SuppressWarnings("unchecked")
        private <V> ConfigValueWrapper<V> createWrapper(Class<?> typeClass, Object value) {
            return new ConfigValueWrapper<>((Class<V>) typeClass, (V) value);
        }
    };

    @SuppressWarnings("unchecked")
    public <V> V getValueAs(Class<V> targetType) {
        if (targetType.isAssignableFrom(type)) {
            return (V) value;
        }
        throw new ClassCastException("Cannot cast " + type.getSimpleName() + " to " + targetType.getSimpleName());
    }
}