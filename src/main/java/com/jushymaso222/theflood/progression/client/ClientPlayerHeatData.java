package com.jushymaso222.theflood.progression.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientPlayerHeatData {

    private static final Map<UUID, Integer> HEAT_BY_PLAYER =
            new HashMap<>();

    private ClientPlayerHeatData() {
    }

    public static void setHeat(
            UUID playerId,
            int heat
    ) {
        HEAT_BY_PLAYER.put(
                playerId,
                heat
        );
    }

    public static int getHeat(
            UUID playerId
    ) {
        return HEAT_BY_PLAYER.getOrDefault(
                playerId,
                1
        );
    }

    public static void remove(
            UUID playerId
    ) {
        HEAT_BY_PLAYER.remove(
                playerId
        );
    }

    public static void clear() {
        HEAT_BY_PLAYER.clear();
    }
}