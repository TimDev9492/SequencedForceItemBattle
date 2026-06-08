package me.timwastaken.sequencedfib.commands;

import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import me.timwastaken.sequencedfib.config.ConfigValueProvider;
import me.timwastaken.sequencedfib.ui.SfibMessages;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ExcludeCommand implements CommandExecutor, TabCompleter {
    private final ConfigValueProvider excludeConfig;
    private final Set<Material> excluded;

    public ExcludeCommand(ConfigValueProvider config) {
        this.excludeConfig = config;
        this.excluded = new HashSet<>();
        for (String matString : config.getStringList(SequencedForceItemBattle.EXCLUDE_CONFIG_KEY)) {
            try {
                Material toExclude = Material.valueOf(matString);
                excluded.add(toExclude);
            } catch (IllegalArgumentException e) {
                SequencedForceItemBattle.getInstance().getLogger().warning(String.format(
                        "Failed to load material '%s', skipping...",
                        matString
                ));
            }
        }
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length != 1) return false;
        try {
            Material arg = Material.valueOf(args[0].toUpperCase());
            excluded.add(arg);
            boolean saved = excludeConfig.saveObject(SequencedForceItemBattle.EXCLUDE_CONFIG_KEY, excluded.stream().map(Material::name).toList());
            if (!saved) return false;
            excludeConfig.saveConfig();
            sender.sendMessage(SfibMessages.materialExcluded(args[0]));
            return true;
        } catch (IllegalArgumentException e) {
            sender.sendMessage(SfibMessages.materialDoesNotExist(args[0]));
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length != 1) return Collections.emptyList();
        String typed = args[0];
        return Arrays.stream(Material.values())
                .filter(mat -> !excluded.contains(mat))
                .map(Material::name)
                .filter(name -> typed.isEmpty() || name.startsWith(typed.toUpperCase()))
                .toList();
    }
}
