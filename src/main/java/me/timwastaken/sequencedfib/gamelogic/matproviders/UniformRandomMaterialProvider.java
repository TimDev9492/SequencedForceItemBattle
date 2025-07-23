package me.timwastaken.sequencedfib.gamelogic.matproviders;

import me.timwastaken.sequencedfib.utils.RandomRemovalSet;
import org.bukkit.Material;

import java.util.*;

public class UniformRandomMaterialProvider extends SfibMaterialProvider {
    private final RandomRemovalSet<Material> materials;

    public UniformRandomMaterialProvider(Collection<Material> materials) {
        this.materials = new RandomRemovalSet<>();
        for (Material mat : materials) {
            this.materials.add(mat);
        }
    }

    @Override
    public boolean hasNext() {
        return !materials.isEmpty();
    }

    @Override
    public Material next() {
        if (!hasNext()) throw new NoSuchElementException("There are no materials left!");
        return this.materials.removeRandom();
    }
}
