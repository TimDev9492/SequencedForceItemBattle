package me.timwastaken.sequencedfib.config;

import java.util.List;

public interface ConfigValueProvider {
    Object getObject(String key);
    boolean saveObject(String key, Object value);

    List<String> getStringList(String key);

    default String getString(String key) {
        return (String) this.getObject(key);
    }

    default int getInt(String key) {
        return (int) this.getObject(key);
    }

    default double getDouble(String key) {
        return (double) this.getObject(key);
    }

    boolean saveConfig();
}
