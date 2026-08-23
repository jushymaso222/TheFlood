package com.jushymaso222.theflood.elite.drops.boon;

import net.minecraft.server.level.ServerPlayer;

public final class BoonData {

    private static final String ACTIVE_BOON_KEY =
            "theflood_active_boon";

    private static final String BOON_END_TIME_KEY =
            "theflood_boon_end_time";


    private BoonData() {
    }


    public static boolean hasActiveBoon(
            ServerPlayer player
    ) {
        return !getActiveBoonId(
                player
        ).isBlank();
    }


    public static String getActiveBoonId(
            ServerPlayer player
    ) {
        return player.getPersistentData()
                .getString(
                        ACTIVE_BOON_KEY
                );
    }

    public static boolean hasBoon(
            ServerPlayer player,
            BoonType type
    ) {
        if (
                player == null
                || type == null
        ) {
            return false;
        }

        return type.id()
                .equals(
                        getActiveBoonId(
                                player
                        )
                );
    }

    public static long getRemainingTicks(
            ServerPlayer player
    ) {
        if (!hasActiveBoon(player)) {
            return 0L;
        }

        return Math.max(
                0L,
                getEndTime(player)
                        - player.level()
                                .getGameTime()
        );
    }

    public static long getEndTime(
            ServerPlayer player
    ) {
        return player.getPersistentData()
                .getLong(
                        BOON_END_TIME_KEY
                );
    }


    public static void setActiveBoon(
            ServerPlayer player,
            BoonType type,
            long endTime
    ) {
        player.getPersistentData()
                .putString(
                        ACTIVE_BOON_KEY,
                        type.id()
                );

        player.getPersistentData()
                .putLong(
                        BOON_END_TIME_KEY,
                        endTime
                );
    }


    public static void clear(
            ServerPlayer player
    ) {
        player.getPersistentData()
                .remove(
                        ACTIVE_BOON_KEY
                );

        player.getPersistentData()
                .remove(
                        BOON_END_TIME_KEY
                );
    }
}