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

    private static long floodXp = 0;
    private static long floodXpRequired = 100;

    public static float getFloodXpProgress() {
        if (floodXpRequired <= 0L) {
            return 0.0F;
        }

        return Math.max(
                0.0F,
                Math.min(
                        1.0F,
                        floodXp / (float) floodXpRequired
                )
        );
    }

    public static void update(
            int newSoloHeat,
            int newTeamHeat,
            int newBaseHeat,
            int newProximityBonus,
            int newEffectiveHeat,
            long newFloodXp,
            long newFloodXpRequired
    ) {
        soloHeat = newSoloHeat;
        teamHeat = newTeamHeat;
        baseHeat = newBaseHeat;
        proximityBonus = newProximityBonus;
        effectiveHeat = newEffectiveHeat;
        floodXp = newFloodXp;
        floodXpRequired = newFloodXpRequired;
    }

    public static void reset() {
        soloHeat = 1;
        teamHeat = 0;
        baseHeat = 1;
        proximityBonus = 0;
        effectiveHeat = 1;
        floodXp = 0;
        floodXpRequired = 100;
    }
}