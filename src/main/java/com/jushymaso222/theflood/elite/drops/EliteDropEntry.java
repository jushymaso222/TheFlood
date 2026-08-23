package com.jushymaso222.theflood.elite.drops;

public final class EliteDropEntry {

    private String item;

    private String tag;

    private String generator;

    private double weight;

    private double weightPerHeat;

    private Integer minHeat;

    private Integer maxHeat;

    private Integer min;

    private Integer max;


    public String getItem() {
        return item;
    }

    public String getTag() {
        return tag;
    }

    public String getGenerator() {
        return generator;
    }

    public double getWeight() {
        return weight;
    }

    public double getWeightPerHeat() {
        return weightPerHeat;
    }

    public Integer getMinHeat() {
        return minHeat;
    }

    public Integer getMaxHeat() {
        return maxHeat;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }


    public boolean hasCustomAmount() {
        return min != null
                || max != null;
    }


    public boolean isAvailableAtHeat(
            int heat
    ) {
        if (
                minHeat != null
                && heat < minHeat
        ) {
            return false;
        }

        if (
                maxHeat != null
                && heat > maxHeat
        ) {
            return false;
        }

        return true;
    }


    public double getEffectiveWeight(
            int heat
    ) {
        if (
                !isAvailableAtHeat(
                        heat
                )
        ) {
            return 0.0D;
        }

        int startingHeat =
                minHeat != null
                        ? minHeat
                        : 0;

        double effective =
                weight
                        + (
                        Math.max(
                                0,
                                heat - startingHeat
                        )
                                * weightPerHeat
                );

        return Math.max(
                0.0D,
                effective
        );
    }


    /*
     * Exactly one reward source should be supplied:
     *
     * item
     * tag
     * generator
     */
    public int getSourceCount() {
        int count =
                0;

        if (
                item != null
                && !item.isBlank()
        ) {
            count++;
        }

        if (
                tag != null
                && !tag.isBlank()
        ) {
            count++;
        }

        if (
                generator != null
                && !generator.isBlank()
        ) {
            count++;
        }

        return count;
    }
}