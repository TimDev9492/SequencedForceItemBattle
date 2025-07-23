package me.timwastaken.sequencedfib.utils;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CumulativeSelect<T, V extends Number> {
    private final Random random;
    private final List<T> samples;
    private final Function<T, V> weightTransform;

    public CumulativeSelect(List<T> samples, Function<T, V> weightTransform) {
        this.random = new Random();
        this.samples = samples;
        this.weightTransform = weightTransform;
    }

    public T selectRandomAndRemove() {
        T selected = this.selectRandom();
        return this.removeSample(selected);
    }

    public T selectRandom() {
        if (this.samples.isEmpty()) return null;
        double weightTotal = this.samples.stream().map(this.weightTransform).map(Number::doubleValue).reduce(Double::sum).get();
        double uniformRandom = random.nextDouble(weightTotal);
        double cumulativeWeight = 0;
        for (T sample : this.samples) {
            cumulativeWeight += this.weightTransform.apply(sample).doubleValue();
            if (cumulativeWeight > uniformRandom) return sample;
        }
        return null;
    }

    public T removeSample(T sample) {
        if (this.samples.remove(sample)) return sample;
        return null;
    }
}
