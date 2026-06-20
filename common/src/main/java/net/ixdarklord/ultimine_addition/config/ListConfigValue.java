package net.ixdarklord.ultimine_addition.config;

import com.google.common.collect.ImmutableList;
import net.ixdarklord.coolcatlib.api.utils.ValueConverter;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.function.Predicate;

public class ListConfigValue<T> {
    protected ForgeConfigSpec.ConfigValue<String> configValue;
    protected final int expectedSize;
    protected final Predicate<T> elementValidator;
    protected final ValueConverter<T> valueConverter;

    public ListConfigValue(int expectedSize, Predicate<T> elementValidator, ValueConverter<T> valueConverter) {
        this.expectedSize = expectedSize;
        this.elementValidator = elementValidator;
        this.valueConverter = valueConverter;
    }

    protected void define(ForgeConfigSpec.Builder builder, String key, List<T> defaultValue, String... comment) {
        if (defaultValue.size() != this.expectedSize) {
            throw new IllegalArgumentException("Default value list must have exactly " + this.expectedSize + " elements.");
        } else {
            for (T value : defaultValue) {
                if (!this.elementValidator.test(value)) {
                    throw new IllegalArgumentException("Default value " + value + " does not meet validation criteria.");
                }
            }

            String defaultValueString = this.listToString(defaultValue);
            this.configValue = builder.comment(comment).define(key, defaultValueString, (obj) -> {
                if (!(obj instanceof String)) {
                    return false;
                } else {
                    List<T> parsedList = this.parseStringToList((String) obj);
                    if (parsedList.size() != this.expectedSize) {
                        return false;
                    } else {
                        for (T value : parsedList) {
                            if (!this.elementValidator.test(value)) {
                                return false;
                            }
                        }

                        return true;
                    }
                }
            });
        }
    }

    private String listToString(List<T> list) {
        StringBuilder sb = new StringBuilder();

        for (T value : list) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }

            sb.append(this.valueConverter.toString(value));
        }

        return sb.toString();
    }

    private List<T> parseStringToList(String input) {
        List<T> list = new ArrayList<>();
        String[] parts = input.split(",");

        for (String part : parts) {
            list.add(this.valueConverter.parse(part.trim()));
        }

        return Collections.unmodifiableList(list);
    }

    public ForgeConfigSpec.ConfigValue<String> getConfigValue() {
        if (this.configValue == null) {
            throw new IllegalStateException("ConfigValue has not been defined yet.");
        } else {
            return this.configValue;
        }
    }

    public void set(List<T> values) {
        this.getConfigValue().set(this.listToString(values));
    }

    protected List<T> getValue() {
        return this.parseStringToList(this.getConfigValue().get());
    }

    protected List<T> getDefaultValue() {
        return this.parseStringToList(this.getConfigValue().getDefault());
    }

    public boolean validate() {
        List<T> list = this.getValue();
        if (list.size() != this.expectedSize) {
            return false;
        } else {
            for (T value : list) {
                if (!this.elementValidator.test(value)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static final class RangeValue extends ListConfigValue<Integer> {
        public RangeValue(int minValue, int maxValue) {
            super(2, (value) -> value >= minValue && value <= maxValue, ValueConverter.INTEGER);
        }

        public void define(ForgeConfigSpec.Builder builder, String key, int minRange, int maxRange, String... comment) {
            super.define(builder, key, ImmutableList.of(minRange, maxRange), comment);
        }

        public int getMin() {
            if (this.getValue().isEmpty()) {
                throw new NoSuchElementException();
            } else {
                return this.getValue().get(0);
            }
        }

        public int getMax() {
            if (this.getValue().isEmpty()) {
                throw new NoSuchElementException();
            } else {
                return this.getValue().get(this.getValue().size() - 1);
            }
        }
    }

    public static final class EnumValue<E extends Enum<E>> extends ListConfigValue<Integer> {
        private final Class<E> enumClass;
        private final int minValue;
        private final int maxValue;

        public EnumValue(int expectedSize, Class<E> enumClass, int min, int max) {
            super(expectedSize, (value) -> value >= min && value <= max, ValueConverter.INTEGER);
            this.enumClass = enumClass;
            this.minValue = min;
            this.maxValue = max;
        }

        public void defineEnums(ForgeConfigSpec.Builder builder, String key, Map<E, Integer> defaultValue, String... comment) {
            if (defaultValue.size() != this.expectedSize) {
                throw new IllegalArgumentException("Default value map must have exactly " + this.expectedSize + " elements.");
            } else {
                String defaultValueString = this.mapToString(defaultValue, this.valueConverter);
                this.configValue = builder.comment(comment).define(key, defaultValueString, (obj) -> {
                    if (!(obj instanceof String)) {
                        return false;
                    } else {
                        Map<E, Integer> parsedMap = this.parseStringToMap((String) obj, this.valueConverter);
                        if (parsedMap.size() != this.expectedSize) {
                            return false;
                        } else {
                            for (Integer value : parsedMap.values()) {
                                if (!this.elementValidator.test(value)) {
                                    return false;
                                }
                            }

                            return true;
                        }
                    }
                });
            }
        }

        private String mapToString(Map<E, Integer> map, ValueConverter<Integer> valueConverter) {
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<E, Integer> entry : map.entrySet()) {
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }

                sb.append(entry.getKey().name()).append("=").append(valueConverter.toString(entry.getValue()));
            }

            return sb.toString();
        }

        private Map<E, Integer> parseStringToMap(String input, ValueConverter<Integer> valueConverter) {
            Map<E, Integer> map = new HashMap<>();
            String[] parts = input.split(",");

            for (String part : parts) {
                String[] keyValue = part.trim().split("=");
                if (keyValue.length == 2) {
                    E enumKey = Enum.valueOf(this.enumClass, keyValue[0].trim());
                    int value = valueConverter.parse(keyValue[1].trim());
                    if (!this.elementValidator.test(value)) {
                        FTBUltimineAddition.LOGGER.error("Value {} is not within the range [{}, {}]", value, this.minValue, this.maxValue);
                    }

                    map.put(enumKey, value);
                }
            }

            return map;
        }

        public Map<E, Integer> getMapValue() {
            return this.parseStringToMap(this.configValue.get(), this.valueConverter);
        }

        public Map<E, Integer> getDefaultMapValue() {
            return this.parseStringToMap(this.configValue.getDefault(), this.valueConverter);
        }

        public Integer getValue(E enumKey) {
            return this.getMapValue().get(enumKey);
        }

        public Integer getDefaultValue(E enumKey) {
            return this.getDefaultMapValue().get(enumKey);
        }

        public Integer getOrDefault(E enumKey) {
            try {
                return this.getMapValue().get(enumKey);
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
