package me.timwastaken.sequencedfib.utils;

import java.util.*;

public class RandomRemovalSet<T> {
    private final Map<T, Integer> indexMap = new HashMap<>();
    private final List<T> itemList = new ArrayList<>();
    private final Random rand = new Random();

    public boolean add(T item) {
        if (indexMap.containsKey(item)) return false;
        indexMap.put(item, itemList.size());
        itemList.add(item);
        return true;
    }

    public T removeRandom() {
        if (itemList.isEmpty()) return null;

        int randomIndex = rand.nextInt(itemList.size());
        T removedItem = itemList.get(randomIndex);

        // Swap with the last item
        T lastItem = itemList.getLast();
        itemList.set(randomIndex, lastItem);
        indexMap.put(lastItem, randomIndex);

        // Remove last
        itemList.removeLast();
        indexMap.remove(removedItem);

        return removedItem;
    }

    public boolean contains(T item) {
        return indexMap.containsKey(item);
    }

    public int size() {
        return itemList.size();
    }

    public boolean isEmpty() {
        return itemList.isEmpty();
    }
}
