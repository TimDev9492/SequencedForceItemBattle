package me.timwastaken.sequencedfib;

import me.timwastaken.sequencedfib.commands.*;
import me.timwastaken.sequencedfib.config.ConfigValueProvider;
import me.timwastaken.sequencedfib.config.YAMLConfig;
import me.timwastaken.sequencedfib.exceptions.YamlException;
import me.timwastaken.sequencedfib.gamelogic.matproviders.FilteredMaterialProvider;
import me.timwastaken.sequencedfib.gamelogic.matproviders.GroupedWeightedMaterialProvider;
import me.timwastaken.sequencedfib.gamelogic.matproviders.SfibMaterialProvider;
import me.timwastaken.sequencedfib.listeners.ItemOffhandSwapListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class SequencedForceItemBattle extends JavaPlugin {
    public static final String EXCLUDE_CONFIG_KEY = "material-exclude";
    private static SequencedForceItemBattle self;

    private PluginResourceManager resourceManager;
    private ConfigValueProvider configValueProvider;
    private ConfigValueProvider excludeProvider;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getLogger().info("Loading Plugin...");
        self = this;
        this.resourceManager = new PluginResourceManager(this);
        SfibMaterialProvider materialProvider;
        try {
            configValueProvider = new YAMLConfig(this, "sfib-config.yml", true);
            excludeProvider = new YAMLConfig(this, "exclude.yml", false);

            materialProvider = new FilteredMaterialProvider(
                    new GroupedWeightedMaterialProvider(new File(
                            SequencedForceItemBattle.getInstance().getDataFolder(),
                            "item_groups.json"
                    )),
                    material ->!excludeProvider.getStringList(EXCLUDE_CONFIG_KEY)
                            .contains(material.name())
            );

            this.getLogger().info("Successfully loaded config values!");

            this.saveResource("item_groups.json", true);
            this.getLogger().info("Successfully loaded item groups!");
        } catch (YamlException | IOException e) {
            this.getLogger().severe("Failed to load config values! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.resourceManager.registerEventListener(new ItemOffhandSwapListener());

        this.resourceManager.registerCommand("start", new StartCommand(materialProvider));
        this.resourceManager.registerCommand("backpack", new BackpackCommand());
        this.resourceManager.registerCommand("overview", new OverviewCommand());
        this.resourceManager.registerCommand("skip", new SkipCommand());
        this.resourceManager.registerCommand("pause", new PauseCommand());
        this.resourceManager.registerCommand("sequence", new SequenceCommand());
        this.resourceManager.registerCommand("info", new InfoCommand());
        try {
            ExcludeCommand excludeCommand = new ExcludeCommand(
                    new YAMLConfig(this, "exclude.yml", false)
            );
            this.resourceManager.registerCommand("exclude", excludeCommand);
            this.resourceManager.registerTabCompletions("exclude", excludeCommand);
        } catch (YamlException | IOException e) {
            this.getLogger().warning("Failed to load exclude config:\n" + e.getMessage() + "\nSkipping...");
        }

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
