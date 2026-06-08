package me.timwastaken.sequencedfib.gamelogic.matproviders;

import org.bukkit.Material;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilteredMaterialProvider extends SfibMaterialProvider {
    private final SfibMaterialProvider parent;
    private final Predicate<Material> predicate;

    private Material next;
    private boolean hasNextPrepared = false;

    public FilteredMaterialProvider(
            SfibMaterialProvider parent,
            Predicate<Material> predicate
    ) {
        this.parent = parent;
        this.predicate = predicate;
    }

    private void prepareNext() {
        if (hasNextPrepared) {
            return;
        }

        while (parent.hasNext()) {
            Material candidate = parent.next();

            if (predicate.test(candidate)) {
                next = candidate;
                hasNextPrepared = true;
                return;
            }
        }
    }

    @Override
    public boolean hasNext() {
        prepareNext();
        return hasNextPrepared;
    }

    @Override
    public Material next() {
        prepareNext();

        if (!hasNextPrepared) {
            throw new NoSuchElementException();
        }

        Material result = next;

        next = null;
        hasNextPrepared = false;

        return result;
    }
}