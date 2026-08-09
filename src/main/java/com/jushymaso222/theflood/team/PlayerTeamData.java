package com.jushymaso222.theflood.team;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class PlayerTeamData {

    private static final String TEAM_ID_TAG =
            "theflood_team_id";

    private PlayerTeamData() {
    }

    public static boolean hasTeam(ServerPlayer player) {
        return player.getPersistentData()
                .contains(TEAM_ID_TAG);
    }

    public static UUID getTeamId(ServerPlayer player) {
        if (!hasTeam(player)) {
            return null;
        }

        return player.getPersistentData()
                .getUUID(TEAM_ID_TAG);
    }

    public static void setTeam(
            ServerPlayer player,
            UUID teamId
    ) {
        player.getPersistentData()
                .putUUID(
                        TEAM_ID_TAG,
                        teamId
                );
    }

    public static void clearTeam(ServerPlayer player) {
        player.getPersistentData()
                .remove(TEAM_ID_TAG);
    }
}