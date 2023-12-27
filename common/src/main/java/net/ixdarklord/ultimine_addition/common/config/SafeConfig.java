package net.ixdarklord.ultimine_addition.common.config;

import dev.architectury.platform.Platform;
import net.ixdarklord.coolcat_lib.util.TomlConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SafeConfig<T> {
    private final T cachedValue;

    private SafeConfig(T cachedValue) {
        this.cachedValue = cachedValue;
    }

    public T get() {
        return cachedValue;
    }

    public static class Builder {
        private final String modId;
        private final String filePath;
        private final TomlConfigReader reader;
        private final Logger LOGGER;

        public Builder(String modId, String filePath) {
            this(modId, filePath, modId + "/SafeConfig");
        }

        public Builder(String modId, String filePath, String loggerName) {
            this.modId = modId;
            this.filePath = filePath;
            LOGGER = LogManager.getLogger(loggerName);
            this.reader = new TomlConfigReader(modId, "%s/%s/%s".formatted(Platform.getConfigFolder(), modId, filePath));

            if (this.reader.hasErrorOccurred()) {
                LOGGER.error("Failed to load config file!", filePath);
                switch (this.reader.getFileErrorState()) {
                    case FILE_NOT_FOUND -> LOGGER.error("There is no file exists with this path: {}", filePath);
                    case UNABLE_TO_READ -> LOGGER.error("Unable to read the file: {}" , filePath);
                    case UNEXPECTED_ERROR -> LOGGER.error("May the game need a restart to fix this issue: {}", filePath);
                }
            }
        }

        public SafeConfig<String> readString(String key, String fallbackValue) {
            if (this.reader.hasErrorOccurred()) {
                LOGGER.error("Unable to get (\"{}\")! Assign the fallback value... ({})", key, fallbackValue);
                return new SafeConfig<>(fallbackValue);
            }

            String value = this.reader.getResult(key);
            if (value == null) LOGGER.error("Unable to get \"{}\"! Assign the fallback value... ({})", key, fallbackValue);
            return new SafeConfig<>(value != null ? value : fallbackValue);
        }

        public SafeConfig<Integer> readInt(String key, int fallbackValue) {
            if (this.reader.hasErrorOccurred()) {
                LOGGER.error("Unable to get \"{}\"! Assign the fallback value... ({})", key, fallbackValue);
                return new SafeConfig<>(fallbackValue);
            }

            String value = this.reader.getResult(key);
            if (value != null) {
                try {
                    return new SafeConfig<>(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    LOGGER.error("This key does not contain an integer value: {}", key);
                }
            } else LOGGER.error("Unable to get \"{}\"! Assign the fallback value... ({})", key, fallbackValue);
            return new SafeConfig<>(fallbackValue);
        }

        public SafeConfig<Boolean> readBoolean(String key, boolean fallbackValue) {
            if (this.reader.hasErrorOccurred()) {
                LOGGER.error("Unable to get \"{}\"! Assign the fallback value... ({})", key, fallbackValue);
                return new SafeConfig<>(fallbackValue);
            }

            String value = this.reader.getResult(key);
            if (value == null) LOGGER.error("Unable to get \"{}\"! Assign the fallback value... ({})", key, fallbackValue);
            return new SafeConfig<>(value != null ? Boolean.parseBoolean(value) : fallbackValue);
        }

        public <E extends Enum<E>> SafeConfig<E> readEnum(String key, E fallbackValue) {;
            if (this.reader.hasErrorOccurred()) {
                LOGGER.error("Unable to get \"{}\"! Assign the fallback value... ({})", key, fallbackValue.name());
                return new SafeConfig<>(fallbackValue);
            }

            String value = this.reader.getResult(key);
            Class<E> enumClass = fallbackValue.getDeclaringClass();
            for (E enumConstant : enumClass.getEnumConstants()) {
                if (value == null) break;
                if (enumConstant.name().equalsIgnoreCase(value.replace("\"", ""))) {
                    return new SafeConfig<>(enumConstant);
                }
            }
            if (value != null) LOGGER.error("No enum constant with name " + value + " in enum class " + enumClass.getName());
            else LOGGER.error("Unable to get \"{}\"! Assign the fallback value... ({})", key, fallbackValue.name());
            return new SafeConfig<>(fallbackValue);
        }

        public Builder refreshBuilder() {
            return new Builder(this.modId, this.filePath);
        }
    }
}
