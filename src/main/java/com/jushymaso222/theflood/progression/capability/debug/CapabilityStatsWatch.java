package com.jushymaso222.theflood.progression.capability.debug;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public final class CapabilityStatsWatch {

    /*
     * Players who currently have the
     * Capability Inspector enabled.
     */
    private static final Set<UUID> WATCHING =
            new HashSet<>();


    private CapabilityStatsWatch() {
    }


    /*
     * =====================================================
     * WATCH
     * =====================================================
     */

    public static void watch(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        WATCHING.add(
                player.getUUID()
        );
    }


    /*
     * =====================================================
     * CLEAR
     * =====================================================
     */

    public static void clear(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        WATCHING.remove(
                player.getUUID()
        );
    }


    public static void clear(
            UUID playerId
    ) {
        if (playerId == null) {
            return;
        }

        WATCHING.remove(
                playerId
        );
    }


    /*
     * =====================================================
     * QUERY
     * =====================================================
     */

    public static boolean isWatching(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        return WATCHING.contains(
                player.getUUID()
        );
    }


    public static boolean isWatching(
            UUID playerId
    ) {
        if (playerId == null) {
            return false;
        }

        return WATCHING.contains(
                playerId
        );
    }


    /*
     * Mostly useful for debugging / cleanup.
     */
    public static int getWatchingCount() {
        return WATCHING.size();
    }
}