package com.jushymaso222.theflood.progression.client;

public final class ClientHeatData {

    private static int soloHeat = 1;
    private static int teamHeat = 0;
    private static int baseHeat = 1;
    private static int proximityBonus = 0;
    private static int effectiveHeat = 1;

    private ClientHeatData() {
    }

    public static int getSoloHeat() {
        return soloHeat;
    }

    public static int getTeamHeat() {
        return teamHeat;
    }

    public static int getBaseHeat() {
        return baseHeat;
    }

    public static int getProximityBonus() {
        return proximityBonus;
    }

    public static int getEffectiveHeat() {
        return effectiveHeat;
    }

    public static void update(
            int newSoloHeat,
            int newTeamHeat,
            int newBaseHeat,
            int newProximityBonus,
            int newEffectiveHeat
    ) {
        soloHeat = newSoloHeat;
        teamHeat = newTeamHeat;
        baseHeat = newBaseHeat;
        proximityBonus = newProximityBonus;
        effectiveHeat = newEffectiveHeat;
    }

    public static void reset() {
        soloHeat = 1;
        teamHeat = 0;
        baseHeat = 1;
        proximityBonus = 0;
        effectiveHeat = 1;
    }
}