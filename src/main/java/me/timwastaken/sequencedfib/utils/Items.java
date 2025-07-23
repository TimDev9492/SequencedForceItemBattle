package me.timwastaken.sequencedfib.utils;

import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Items {
    public static boolean hasId(ItemStack stack, String id) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return Objects.equals(
                meta.getPersistentDataContainer().get(
                        new NamespacedKey(SequencedForceItemBattle.getInstance(), "item_id"),
                        PersistentDataType.STRING
                ),
                id
        );
    }

    public static void setId(ItemStack stack, String id) {
        if (stack == null || id == null) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(SequencedForceItemBattle.getInstance(), "item_id");
        container.set(key, PersistentDataType.STRING, id);
        stack.setItemMeta(meta);
    }

    public static String getId(ItemStack stack) {
        if (stack == null) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(
                new NamespacedKey(SequencedForceItemBattle.getInstance(), "item_id"),
                PersistentDataType.STRING
        );
    }

    public static ItemStack withName(Material material, int amount, String displayName) {
        ItemStack stack = new ItemStack(material, amount);
        Optional<ItemMeta> meta = Optional.ofNullable(stack.getItemMeta());
        meta.ifPresent(m -> m.setDisplayName(displayName));
        stack.setItemMeta(meta.orElse(null));
        return stack;
    }

    public static ItemStack playerHead(String playerName, int amount, String displayName) {
        return playerHead(Bukkit.getOfflinePlayer(playerName).getUniqueId(), amount, displayName);
    }

    public static ItemStack playerHead(UUID playerUUID, int amount, String displayName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);
        Optional<SkullMeta> meta = Optional.ofNullable((SkullMeta) head.getItemMeta());
        meta.ifPresent(s -> {
            s.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
            s.setDisplayName(displayName);
        });
        head.setItemMeta(meta.orElse(null));
        return head;
    }
}
