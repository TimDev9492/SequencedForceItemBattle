package me.timwastaken.sequencedfib.config;

import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import me.timwastaken.sequencedfib.exceptions.YamlException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class YAMLConfig implements ConfigValueProvider {
    private final String configName;
    private final YamlConfiguration yamlConfig;
    private File configFile = null;

    public YAMLConfig(Plugin plugin, String configName, boolean overwriteWithDefault) throws YamlException {
        this.configName = configName;
        yamlConfig = this.createYamlConfig(plugin, overwriteWithDefault);
    }

    private YamlConfiguration createYamlConfig(Plugin plugin, boolean overwrite) throws YamlException {
        File configFile = new File(plugin.getDataFolder(), this.configName);
        if (!configFile.exists()) {
            if (!configFile.getParentFile().mkdirs()) {
                throw new YamlException("Failed to create config directory!");
            }
            plugin.saveResource(configName, overwrite);
        }
        this.configFile = configFile;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // load defaults
        InputStream defaultStream = plugin.getResource(configName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            if (overwrite) return defaultConfig;
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);
        }

        return config;
    }

    @Override
    public Object getObject(String key) {
        return yamlConfig.getObject(key, Object.class);
    }

    @Override
    public boolean saveObject(String key, Object value) {
        yamlConfig.set(key, value);
        return true;
    }

    @Override
    public List<String> getStringList(String key) {
        return yamlConfig.getStringList(key);
    }

    @Override
    public String getString(String key) {
        return yamlConfig.getString(key);
    }

    @Override
    public int getInt(String key) {
        return yamlConfig.getInt(key);
    }

    @Override
    public double getDouble(String key) {
        return yamlConfig.getDouble(key);
    }

    @Override
    public boolean saveConfig() {
        if (this.configFile == null) return false;
        try {
            yamlConfig.save(configFile);
        } catch (IOException e) {
            SequencedForceItemBattle.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
        return true;
    }
}
