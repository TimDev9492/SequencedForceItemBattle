package me.timwastaken.sequencedfib;

import me.timwastaken.sequencedfib.gamelogic.GameManager;
import me.timwastaken.sequencedfib.gamelogic.SfibGame;
import me.timwastaken.sequencedfib.utils.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PluginResourceManager {
    private final JavaPlugin plugin;
    private final List<Integer> taskIds;
    private final Map<UUID, ItemStack> cachedPlayerHeads;

    public PluginResourceManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.taskIds = new ArrayList<>();
        this.cachedPlayerHeads = new HashMap<>();
    }

    public void registerEventListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this.plugin);
    }

    public void registerTask(int taskId) {
        this.taskIds.add(taskId);
    }

    public void unregisterTasks() {
        for (int taskId : this.taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void registerCommand(String commandName, CommandExecutor executor) {
        Optional.ofNullable(this.plugin.getCommand(commandName)).ifPresent(command -> command.setExecutor(executor));
    }

    public ItemStack getPlayerHeadCached(UUID uuid, String displayName) {
        if (this.cachedPlayerHeads.containsKey(uuid)) return this.cachedPlayerHeads.get(uuid);
        ItemStack playerHead = Items.playerHead(uuid, 1, displayName);
        this.cachedPlayerHeads.put(uuid, playerHead);
        return playerHead;
    }

    public void cleanUpGame() {
        Optional.ofNullable(GameManager.getCurrentGame()).ifPresent(SfibGame::cleanup);
    }
}
