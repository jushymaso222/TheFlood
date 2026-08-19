package com.jushymaso222.theflood.team.client;

import net.minecraft.world.item.DyeColor;

import java.util.UUID;

public final class ClientTeamData {

    private static boolean inTeam = false;
    private static boolean owner = false;

    private static UUID teamId = null;

    private static String teamName = "";
    private static DyeColor teamColor = DyeColor.WHITE;

    private static int teamHeat = 1;

    private ClientTeamData() {
    }

    public static boolean isInTeam() {
        return inTeam;
    }

    public static boolean isOwner() {
        return owner;
    }

    public static UUID getTeamId() {
        return teamId;
    }

    public static String getTeamName() {
        return teamName;
    }

    public static DyeColor getTeamColor() {
        return teamColor;
    }

    public static int getTeamHeat() {
        return teamHeat;
    }

    public static void update(
            boolean newInTeam,
            boolean newOwner,
            UUID newTeamId,
            String newTeamName,
            DyeColor newTeamColor,
            int newTeamHeat
    ) {
        inTeam = newInTeam;
        owner = newOwner;

        teamId = newTeamId;
        teamName = newTeamName;
        teamColor = newTeamColor;

        teamHeat = newTeamHeat;
    }

    public static void clear() {
        update(
                false,
                false,
                null,
                "",
                DyeColor.WHITE,
                1
        );
    }
}