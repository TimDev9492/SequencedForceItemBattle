package me.timwastaken.sequencedfib.gamelogic.matproviders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.timwastaken.sequencedfib.SequencedForceItemBattle;
import me.timwastaken.sequencedfib.gamelogic.matproviders.jsonmodels.ItemGroup;
import me.timwastaken.sequencedfib.gamelogic.matproviders.jsonmodels.ItemWithDifficulty;
import me.timwastaken.sequencedfib.utils.CumulativeSelect;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class GroupedWeightedMaterialProvider extends SfibMaterialProvider {
    private final Function<Integer, Double> difficultyToWeightTransform = x -> 1 - (x-1)/12d;

    private static final Map<Integer, Double> weightLookup;
    static {
        weightLookup = Map.of(
                1, 0.65d,
                2, 0.8d,
                3, 0.8d,
                4, 0.6d,
                5, 0.8d,
                6, 0.4d
        );
    }

    private final List<ItemGroup> itemGroups;
    private final Map<ItemGroup, Double> groupWeights;

    public GroupedWeightedMaterialProvider(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.itemGroups = mapper.readValue(jsonFile, new TypeReference<>() {});
        this.groupWeights = new HashMap<>();
        for (ItemGroup group : this.itemGroups) {
            double totalWeight = 0;
            int itemAmount = 0;
            for (ItemWithDifficulty item : group.getValues()) {
                totalWeight += weightLookup.getOrDefault(item.getDifficulty(), 0d);
                itemAmount++;
            }
            this.groupWeights.put(group, totalWeight / itemAmount);
        }
    }

    public GroupedWeightedMaterialProvider(String jsonPath) throws IOException {
        this(new File(jsonPath));
    }

    private ItemGroup randomItemGroup() {
        // use cumulative weight selection
        CumulativeSelect<ItemGroup, Double> cumulativeSelect = new CumulativeSelect<>(
                this.itemGroups,
                this.groupWeights::get
        );
        return cumulativeSelect.selectRandom();
    }

    @Override
    public boolean hasNext() {
        return !this.itemGroups.isEmpty() && this.itemGroups.stream().anyMatch(group -> !group.getValues().isEmpty());
    }

    @Override
    public Material next() {
        ItemGroup randomGroup = this.randomItemGroup();
        CumulativeSelect<ItemWithDifficulty, Double> cumulativeMatSelect = new CumulativeSelect<>(
                randomGroup.getValues(),
                item -> weightLookup.getOrDefault(item.getDifficulty(), 0d)
        );
        while (true) {
            ItemWithDifficulty randomItem = cumulativeMatSelect.selectRandomAndRemove();
            if (randomGroup.getValues().isEmpty()) this.itemGroups.remove(randomGroup);
            if (randomItem == null) return null;
            try {
                return Material.valueOf(randomItem.getMaterial());
            } catch (IllegalArgumentException e) {
                SequencedForceItemBattle.getInstance().getLogger().warning(String.format(
                        "Could not decode material with key '%s', skipping...",
                        randomItem.getMaterial()
                ));
            }
        }
    }

    private int itemAmount() {
        return this.itemGroups.stream().flatMap(itemGroup -> itemGroup.getValues().stream()).toList().size();
    }
}
