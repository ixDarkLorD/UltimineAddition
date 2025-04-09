package net.ixdarklord.ultimine_addition.config;

import com.google.common.collect.ImmutableList;
import net.ixdarklord.coolcatlib.api.util.ValueConverter;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ListConfigValue<T> {
    protected ModConfigSpec.ConfigValue<String> configValue;
    protected final int expectedSize;
    protected final Predicate<T> elementValidator;
    protected final ValueConverter<T> valueConverter;

    /**
     * Constructor for ListConfigValue with a custom element validator and value converter.
     *
     * @param expectedSize     The expected size of the list.
     * @param elementValidator A predicate to validate each element in the list.
     * @param valueConverter   A converter for parsing strings to type T and vice versa.
     */
    public ListConfigValue(int expectedSize, Predicate<T> elementValidator, ValueConverter<T> valueConverter) {
        this.expectedSize = expectedSize;
        this.elementValidator = elementValidator;
        this.valueConverter = valueConverter;
    }

    /**
     * Defines a configuration value for the list.
     *
     * @param builder      The ModConfigSpec.Builder instance.
     * @param key          The configuration key.
     * @param defaultValue The default value for the list (as a list of T).
     * @param comment      The comment describing the configuration.
     */
    protected void define(
            ModConfigSpec.Builder builder,
            String key,
            List<T> defaultValue,
            String... comment) {
        // Validate the default value
        if (defaultValue.size() != expectedSize) {
            throw new IllegalArgumentException("Default value list must have exactly " + expectedSize + " elements.");
        }
        for (T value : defaultValue) {
            if (!elementValidator.test(value)) {
                throw new IllegalArgumentException("Default value " + value + " does not meet validation criteria.");
            }
        }

        // Convert the default value list to a comma-separated string
        String defaultValueString = listToString(defaultValue);

        // Define the configuration value
        configValue = builder.comment(comment)
                .define(key, defaultValueString, obj -> {
                    if (!(obj instanceof String)) {
                        return false;
                    }

                    // Parse the input string into a list
                    List<T> parsedList = parseStringToList((String) obj);

                    // Check if the size of the parsed list matches the expected size
                    if (parsedList.size() != expectedSize) {
                        return false; // Size mismatch
                    }

                    // Check each element
                    for (T value : parsedList) {
                        if (!elementValidator.test(value)) {
                            return false; // Value validation failed
                        }
                    }

                    return true;
                });
    }

    /**
     * Converts a list of {@link T} into a comma-separated string.
     *
     * @param list The list to convert.
     * @return A string in the format "value1, value2, value3".
     */
    private String listToString(List<T> list) {
        StringBuilder sb = new StringBuilder();
        for (T value : list) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(valueConverter.toString(value));
        }
        return sb.toString();
    }

    /**
     * Parses a comma-separated string into a list of {@link T}.
     *
     * @param input The input string (e.g., "value1, value2, value3").
     * @return A list of {@link T}.
     */
    private List<T> parseStringToList(String input) {
        List<T> list = new ArrayList<>();
        String[] parts = input.split(",");
        for (String part : parts) {
            list.add(valueConverter.parse(part.trim()));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Gets the cached ConfigValue for the configuration.
     *
     * @return The ConfigValue for the configuration.
     */
    public ModConfigSpec.ConfigValue<String> getConfigValue() {
        if (configValue == null) {
            throw new IllegalStateException("ConfigValue has not been defined yet.");
        }
        return configValue;
    }

    public void set(List<T> values) {
        getConfigValue().set(listToString(values));
    }

    /**
     * Gets the current value of the configuration as a list of T.
     *
     * @return The current value of the list.
     */
    protected List<T> getValue() {
        return parseStringToList(getConfigValue().get());
    }

    /**
     * Gets the default value of the configuration as a list of T.
     *
     * @return The default value of the list.
     */
    protected List<T> getDefaultValue() {
        return parseStringToList(getConfigValue().getDefault());
    }

    /**
     * Validates the list stored in the ConfigValue.
     *
     * @return true if the list is valid, false otherwise.
     */
    public boolean validate() {
        List<T> list = getValue();

        // Check list size
        if (list.size() != expectedSize) {
            return false; // Size mismatch
        }

        // Check each element
        for (T value : list) {
            if (!elementValidator.test(value)) {
                return false; // Value validation failed
            }
        }

        return true;
    }

    public static final class RangeValue extends ListConfigValue<Integer> {
        /**
         * Constructor for IntegerList with a range-based validator.
         *
         * @param minValue     The minimum allowed value for each element.
         * @param maxValue     The maximum allowed value for each element.
         */
        public RangeValue(int minValue, int maxValue) {
            super(
                    2,
                    value -> value >= minValue && value <= maxValue,
                    ValueConverter.INTEGER
            );
        }

        public void define(ModConfigSpec.Builder builder, String key, int minRange, int maxRange, String... comment) {
            super.define(builder, key, ImmutableList.of(minRange, maxRange), comment);
        }

        public int getMin() {
            return getValue().getFirst();
        }

        public int getMax() {
            return getValue().getLast();
        }
    }

    public static final class EnumValue<E extends Enum<E>> extends ListConfigValue<Integer> {
        private final Class<E> enumClass;
        private final int minValue;
        private final int maxValue;

        /**
         * Constructor for EnumList.
         *
         * @param expectedSize The expected size of the list.
         * @param enumClass    The class of the enum.
         * @param min          The minimum allowed value for the map values.
         * @param max          The maximum allowed value for the map values.
         */
        public EnumValue(int expectedSize, Class<E> enumClass, int min, int max) {
            super(expectedSize, value -> value >= min && value <= max, ValueConverter.INTEGER);
            this.enumClass = enumClass;
            this.minValue = min;
            this.maxValue = max;
        }

        /**
         * Defines a configuration value for a map of enum keys and values.
         *
         * @param builder      The ModConfigSpec.Builder instance.
         * @param key          The configuration key.
         * @param defaultValue The default value for the map (as a map of enum keys and values).
         * @param comment      The comment describing the configuration.
         */
        public void defineEnums(
                ModConfigSpec.Builder builder,
                String key,
                java.util.Map<E, Integer> defaultValue,
                String... comment) {

            if (defaultValue.size() != expectedSize) {
                throw new IllegalArgumentException("Default value map must have exactly " + expectedSize + " elements.");
            }

            // Convert the default value map to a comma-separated string
            String defaultValueString = mapToString(defaultValue, valueConverter);

            // Define the configuration value
            configValue = builder.comment(comment)
                    .define(key, defaultValueString, obj -> {
                        if (!(obj instanceof String)) {
                            return false;
                        }

                        java.util.Map<E, Integer> parsedMap = parseStringToMap((String) obj, valueConverter);

                        if (parsedMap.size() != expectedSize) {
                            return false;
                        }

                        for (Integer value : parsedMap.values()) {
                            if (!elementValidator.test(value)) {
                                return false;
                            }
                        }
                        return true;
                    });
        }

        /**
         * Converts a map of enum keys and values into a comma-separated string.
         *
         * @param map            The map to convert.
         * @param valueConverter A converter for parsing strings to type V and vice versa.
         * @return A string in the format "key1=value1, key2=value2, key3=value3".
         */
        private String mapToString(java.util.Map<E, Integer> map, ValueConverter<Integer> valueConverter) {
            StringBuilder sb = new StringBuilder();
            for (java.util.Map.Entry<E, Integer> entry : map.entrySet()) {
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }
                sb.append(entry.getKey().name()) // Only store the enum value name (e.g., "VALUE2")
                        .append("=")
                        .append(valueConverter.toString(entry.getValue()));
            }
            return sb.toString();
        }

        /**
         * Parses a comma-separated string into a map of enum keys and values.
         *
         * @param input          The input string (e.g., "key1=value1, key2=value2, key3=value3").
         * @param valueConverter A converter for parsing strings to type V and vice versa.
         * @return A map of enum keys and values.
         */
        private java.util.Map<E, Integer> parseStringToMap(String input, ValueConverter<Integer> valueConverter) {
            java.util.Map<E, Integer> map = new java.util.HashMap<>();
            String[] parts = input.split(",");
            for (String part : parts) {
                String[] keyValue = part.trim().split("=");
                if (keyValue.length != 2)
                    continue;

                // Parse the enum key
                E enumKey = java.lang.Enum.valueOf(enumClass, keyValue[0].trim());

                // Parse the value
                int value = valueConverter.parse(keyValue[1].trim());

                // Validate the value
                if (!elementValidator.test(value)) {
                    FTBUltimineAddition.LOGGER.error("Value {} is not within the range [{}, {}]", value, minValue, maxValue);
                }

                // Add to the map
                map.put(enumKey, value);
            }
            return map;
        }

        /**
         * Gets the current value of the configuration as a map of enum keys and values.
         *
         * @return The current value of the map.
         */
        public java.util.Map<E, Integer> getMapValue() {
            return parseStringToMap(configValue.get(), valueConverter);
        }

        /**
         * Gets the default value of the configuration as a map of enum keys and values.
         *
         * @return The default value of the map.
         */
        public java.util.Map<E, Integer> getDefaultMapValue() {
            return parseStringToMap(configValue.getDefault(), valueConverter);
        }

        /**
         * Gets the value associated with the specified enum key from the current configuration.
         *
         * @param enumKey The enum key to look up.
         * @return The value associated with the enum key, or null if the key is not found.
         */
        public Integer getValue(E enumKey) {
            java.util.Map<E, Integer> map = getMapValue();
            return map.get(enumKey);
        }

        /**
         * Gets the default value associated with the specified enum key from the default configuration.
         *
         * @param enumKey The enum key to look up.
         * @return The default value associated with the enum key, or null if the key is not found.
         */
        public Integer getDefaultValue(E enumKey) {
            java.util.Map<E, Integer> map = getDefaultMapValue();
            return map.get(enumKey);
        }
    }
}