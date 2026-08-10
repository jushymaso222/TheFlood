package com.jushymaso222.theflood.progression;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.team.FloodTeam;
import com.jushymaso222.theflood.team.TeamManager;
import net.minecraft.server.level.ServerPlayer;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.network.packet.SyncHeatPacket;
import net.minecraftforge.network.PacketDistributor;

import com.jushymaso222.theflood.debug.DummyPlayerManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

    public static int getBaseHeat(
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

    public static int getProximityHeatBonus(
        ServerPlayer player
    ) {
        int radius =
                TheFloodConfig.HEAT
                        .proximityRadius
                        .get();

        double radiusSquared =
                radius * (double) radius;

        int nearbyPlayers = 0;

        /*
        * Combine real players and development dummies.
        * UUID keys prevent a FakePlayer from being counted twice.
        */
        Map<UUID, ServerPlayer> candidates =
                new LinkedHashMap<>();

        for (ServerPlayer realPlayer :
                player.serverLevel().players()) {

                candidates.put(
                        realPlayer.getUUID(),
                        realPlayer
                );
        }

        for (ServerPlayer dummy :
                DummyPlayerManager.getDummies()) {

                if (
                        dummy.serverLevel()
                                != player.serverLevel()
                ) {
                continue;
                }

                candidates.put(
                        dummy.getUUID(),
                        dummy
                );
        }

        for (ServerPlayer other :
                candidates.values()) {

                if (
                        other.getUUID()
                                .equals(player.getUUID())
                ) {
                continue;
                }

                if (!other.isAlive()) {
                continue;
                }

                if (other.isSpectator()) {
                continue;
                }

                if (
                        player.distanceToSqr(other)
                                > radiusSquared
                ) {
                continue;
                }

                /*
                * Members of the same formal team do not count here.
                * Their multiplayer scaling is already represented
                * by Team Heat.
                */
                if (areOnSameTeam(player, other)) {
                continue;
                }

                nearbyPlayers++;
        }

        if (nearbyPlayers <= 0) {
                return 0;
        }

        int basePerPlayer =
                TheFloodConfig.HEAT
                        .proximityBaseHeatPerPlayer
                        .get();

        int synergyPerPair =
                TheFloodConfig.HEAT
                        .proximitySynergyPerPlayer
                        .get();

        /*
        * Every nearby player contributes the normal base bonus.
        *
        * Default:
        * 1 player  = +3
        * 2 players = +6
        * 3 players = +9
        */
        int baseBonus =
                nearbyPlayers * basePerPlayer;

        /*
        * Add an extra nonlinear bonus based on how many
        * player pairs exist in the nearby group.
        *
        * 1 nearby player  -> 0 pairs
        * 2 nearby players -> 1 pair
        * 3 nearby players -> 3 pairs
        * 4 nearby players -> 6 pairs
        * 5 nearby players -> 10 pairs
        */
        int playerPairs =
                nearbyPlayers
                        * (nearbyPlayers - 1)
                        / 2;

        int synergyBonus =
                playerPairs * synergyPerPair;

        return baseBonus + synergyBonus;
    }

    private static boolean areOnSameTeam(
                ServerPlayer first,
                ServerPlayer second
    ) {
        FloodTeam firstTeam =
                TeamManager.getTeamForPlayer(first);

        FloodTeam secondTeam =
                TeamManager.getTeamForPlayer(second);

        if (
                firstTeam == null
                || secondTeam == null
        ) {
                return false;
        }

        return firstTeam.getTeamId()
                .equals(
                        secondTeam.getTeamId()
                );
    }

    public static int getEffectiveHeat(
                ServerPlayer player
    ) {
        int baseHeat =
                getBaseHeat(player);

        int proximityBonus =
                getProximityHeatBonus(player);

        return PlayerFloodData.clampHeat(
                baseHeat + proximityBonus
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
        if (DummyPlayerManager.isDummy(player)) {
                return;
        }

        int soloHeat =
                getSoloHeat(player);

        int teamHeat =
                getTeamHeat(player);

        int baseHeat =
                getBaseHeat(player);

        int proximityBonus =
                getProximityHeatBonus(player);

        int effectiveHeat =
                PlayerFloodData.clampHeat(
                        baseHeat + proximityBonus
                );

        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new SyncHeatPacket(
                        soloHeat,
                        teamHeat,
                        baseHeat,
                        proximityBonus,
                        effectiveHeat
                )
        );
    }
}