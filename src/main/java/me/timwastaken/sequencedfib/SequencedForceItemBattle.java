package me.timwastaken.sequencedfib;

import me.timwastaken.sequencedfib.commands.*;
import me.timwastaken.sequencedfib.config.ConfigValueProvider;
import me.timwastaken.sequencedfib.config.YAMLConfig;
import me.timwastaken.sequencedfib.exceptions.YamlException;
import me.timwastaken.sequencedfib.listeners.ItemOffhandSwapListener;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class SequencedForceItemBattle extends JavaPlugin {
    private static SequencedForceItemBattle self;

    private PluginResourceManager resourceManager;
    private static ConfigValueProvider configValueProvider;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getLogger().info("Loading Plugin...");
        self = this;
        this.resourceManager = new PluginResourceManager(this);
        try {
            configValueProvider = new YAMLConfig(this, "sfib-config.yml", true);
            this.getLogger().info("Successfully loaded config values!");

            this.saveResource("item_groups.json", true);
            this.getLogger().info("Successfully loaded item groups!");
        } catch (YamlException e) {
            this.getLogger().severe("Failed to load config values! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.resourceManager.registerEventListener(new ItemOffhandSwapListener());

        this.resourceManager.registerCommand("start", new StartCommand());
        this.resourceManager.registerCommand("backpack", new BackpackCommand());
        this.resourceManager.registerCommand("overview", new OverviewCommand());
        this.resourceManager.registerCommand("skip", new SkipCommand());
        this.resourceManager.registerCommand("pause", new PauseCommand());
        this.resourceManager.registerCommand("sequence", new SequenceCommand());

        this.getLogger().info("Plugin loaded!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getLogger().info("Unloading Plugin...");
        this.resourceManager.unregisterEvents();
        this.resourceManager.unregisterTasks();
        this.resourceManager.cleanUpGame();
        if (configValueProvider != null) {
            if (configValueProvider.saveConfig()) {
                this.getLogger().info("Successfully saved config values!");
            } else {
                this.getLogger().warning("Failed to save config values!");
            }
        }
        this.getLogger().info("Successfully finished cleanup!");
    }

    public static SequencedForceItemBattle getInstance() {
        return self;
    }

    public static ConfigValueProvider getPluginConfig() {
        return configValueProvider;
    }

    public PluginResourceManager getResourceManager() {
        return resourceManager;
    }
}
