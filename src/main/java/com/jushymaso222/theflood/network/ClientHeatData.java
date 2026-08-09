package com.jushymaso222.theflood.client;

public final class ClientHeatData {

    private static int effectiveHeat = 1;

    private ClientHeatData() {
    }

    public static int getEffectiveHeat() {
        return effectiveHeat;
    }

    public static void setEffectiveHeat(int heat) {
        effectiveHeat = Math.max(1, heat);
    }

    public static void reset() {
        effectiveHeat = 1;
    }
}