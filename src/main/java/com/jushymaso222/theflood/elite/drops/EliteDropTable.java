package com.jushymaso222.theflood.elite.drops;

import java.util.List;
import java.util.Map;

public final class EliteDropTable {

    private Map<String, Integer> rolls;

    private Map<String, Integer> amounts;

    private List<EliteDropEntry> items;


    public Map<String, Integer> getRolls() {
        return rolls;
    }

    public Map<String, Integer> getAmounts() {
        return amounts;
    }

    public List<EliteDropEntry> getItems() {
        return items;
    }
}