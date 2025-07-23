package me.timwastaken.sequencedfib.gamelogic.matproviders.jsonmodels;

public class ItemWithDifficulty {
    private String material;
    private int difficulty;

    public ItemWithDifficulty() {}

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "ItemWithDifficulty{" +
                "material='" + material + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }
}
