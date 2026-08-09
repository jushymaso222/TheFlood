package com.jushymaso222.theflood.progression;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.team.FloodTeam;
import com.jushymaso222.theflood.team.TeamManager;
import net.minecraft.server.level.ServerPlayer;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.network.packet.SyncHeatPacket;
import net.minecraftforge.network.PacketDistributor;

public final class HeatManager {

    private HeatManager() {
    }

    public static int getSoloHeat(
            ServerPlayer player
    ) {
        return PlayerFloodData.getSoloHeat(
                player
        );
    }

    public static int getTeamHeat(
            ServerPlayer player
    ) {
        FloodTeam team =
                TeamManager.getTeamForPlayer(
                        player
                );

        if (team == null) {
            return 0;
        }

        return Math.max(
                1,
                team.getTeamHeat()
        );
    }

    public static boolean isInTeam(
            ServerPlayer player
    ) {
        return TeamManager.getTeamForPlayer(
                player
        ) != null;
    }

    public static int getEffectiveHeat(
            ServerPlayer player
    ) {
        FloodTeam team =
                TeamManager.getTeamForPlayer(player);

        if (team != null) {
            return PlayerFloodData.clampHeat(
                    team.getTeamHeat()
            );
        }

        return PlayerFloodData.clampHeat(
                getSoloHeat(player)
        );
    }

    public static long getTicksPerHeatLevel() {
        long dayMinutes =
                TheFloodConfig.TIME
                        .dayLengthMinutes
                        .get();

        long nightMinutes =
                TheFloodConfig.TIME
                        .nightLengthMinutes
                        .get();

        long totalMinutes =
                dayMinutes + nightMinutes;

        return totalMinutes
                * 60L
                * 20L;
    }

    public static boolean advanceSoloHeat(
            ServerPlayer player
    ) {
        long ticksRequired =
                getTicksPerHeatLevel();

        long progress =
                PlayerFloodData
                        .getHeatProgressTicks(
                                player
                        )
                        + 1;

        if (progress < ticksRequired) {
            PlayerFloodData
                    .setHeatProgressTicks(
                            player,
                            progress
                    );

            return false;
        }

        /*
         * Preserve overflow just in case timing/config changes
         * ever cause progress to exceed one complete level.
         */
        progress -= ticksRequired;

        PlayerFloodData.setHeatProgressTicks(
                player,
                progress
        );

        PlayerFloodData.setSoloHeat(
                player,
                getSoloHeat(player) + 1
        );

        return true;
    }

    public static void syncHeatToPlayer(
            ServerPlayer player
    ) {
        int effectiveHeat =
                getEffectiveHeat(player);

        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new SyncHeatPacket(
                        effectiveHeat
                )
        );
    }
}