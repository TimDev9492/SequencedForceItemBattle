package me.timwastaken.sequencedfib.utils;

import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {
    private final Material material;
    private int amount = 1;
    private Integer maxStackSize = null;
    private String displayName = null;
    private String id = null;
    private final List<String> lore = new ArrayList<>();
    private Consumer<ItemMeta> metaModifier = meta -> {};

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder maxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        return this;
    }

    public ItemBuilder name(String name) {
        this.displayName = name;
        return this;
    }

    public ItemBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        this.lore.clear();
        this.lore.addAll(lore);
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        this.lore.add(line);
        return this;
    }

    public ItemBuilder modifyMeta(Consumer<ItemMeta> consumer) {
        this.metaModifier = this.metaModifier.andThen(consumer);
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (maxStackSize != null) {
            meta.setMaxStackSize(maxStackSize);
        }

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (id != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(SequencedForceItemBattle.getInstance(), "item_id");
            container.set(key, PersistentDataType.STRING, id);
        }

        if (!lore.isEmpty()) {
            meta.setLore(new ArrayList<>(lore));
        }

        metaModifier.accept(meta);
        item.setItemMeta(meta);

        return item;
    }
}

