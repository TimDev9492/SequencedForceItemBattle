package me.timwastaken.sequencedfib.listeners;

import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import me.timwastaken.sequencedfib.gamelogic.GameManager;
import me.timwastaken.sequencedfib.utils.OptionalOnlinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;

public class ItemOffhandSwapListener implements Listener {
    private final HashMap<OptionalOnlinePlayer, Long> lastEventTimestamps;
    private static final double OFFHAND_SWAP_DELAY = SequencedForceItemBattle.getPluginConfig().getDouble("offhand-swap-delay");

    public ItemOffhandSwapListener() {
        this.lastEventTimestamps = new HashMap<>();
    }

    @EventHandler
    public void onPlayerSwapItemInOffhand(PlayerSwapHandItemsEvent event) {
        long currentTime = System.currentTimeMillis();
        OptionalOnlinePlayer player = OptionalOnlinePlayer.of(event.getPlayer());
        Long lastTimestamp = lastEventTimestamps.put(player, currentTime);
        if (lastTimestamp == null) return;
        long millisBetween = currentTime - lastTimestamp.longValue();
        double secondsBetween = millisBetween / 1000d;
        if (secondsBetween <= OFFHAND_SWAP_DELAY) {
            lastEventTimestamps.remove(player);
            GameManager.openPlayerBackpack(player);
        }
    }
}
