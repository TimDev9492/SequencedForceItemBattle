package me.timwastaken.sequencedfib.gamelogic.matproviders.jsonmodels;

import java.util.Arrays;
import java.util.List;

public class ItemGroup {
    private String group;
    private String description;
    private List<ItemWithDifficulty> values;

    public ItemGroup() {}

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ItemWithDifficulty> getValues() {
        return values;
    }

    public void setValues(List<ItemWithDifficulty> values) {
        this.values = values;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.group.getBytes());
    }

    @Override
    public String toString() {
        return "ItemGroup{" +
                "group='" + group + '\'' +
                ", description='" + description + '\'' +
                ", values=" + values +
                '}';
    }
}
