package com.jushymaso222.theflood.elite.debug;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EliteStatsWatch {

    private static final Map<UUID, UUID> WATCHED =
            new HashMap<>();

    private EliteStatsWatch() {
    }

    public static void watch(
            ServerPlayer player,
            Mob mob
    ) {
        WATCHED.put(
                player.getUUID(),
                mob.getUUID()
        );
    }

    public static void clear(
            ServerPlayer player
    ) {
        WATCHED.remove(
                player.getUUID()
        );
    }

    public static UUID getWatched(
            ServerPlayer player
    ) {
        return WATCHED.get(
                player.getUUID()
        );
    }

    public static boolean isWatching(
            ServerPlayer player,
            Mob mob
    ) {
        UUID watched =
                getWatched(
                        player
                );

        return watched != null
                && watched.equals(
                        mob.getUUID()
                );
    }
}