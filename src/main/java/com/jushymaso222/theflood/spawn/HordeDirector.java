package com.jushymaso222.theflood.horde;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.spawn.SpawnDirector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.Random;

public class HordeDirector {

    private static final Random RANDOM = new Random();
    private static long lastBloodMoonWaveTime = -1;
    private static final Map<UUID, Long> NEXT_MINI_HORDE_TIME =
        new HashMap<>();

    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) return;

        int day = getDay(level);
        long timeOfDay = level.getDayTime() % 24000L;

        if (day < TheFloodConfig.MOBS.zombie.unlockDay.get()) return;

        if (isBloodMoon(level)) {
            tickBloodMoon(level, day);
        } else {
            tickMiniHordes(level, day, timeOfDay);
        }
    }

    private static void tickMiniHordes(
            ServerLevel level,
            int day,
            long timeOfDay
    ) {
        int earliestDay =
                TheFloodConfig.HORDES.miniHordeEarliestDay.get();

        if (day < earliestDay) {
            return;
        }

        long gameTime = level.getGameTime();

        for (ServerPlayer player : level.players()) {
            UUID playerId = player.getUUID();

            long nextHordeTime = NEXT_MINI_HORDE_TIME.getOrDefault(
                    playerId,
                    gameTime + calculateMiniHordeCooldownTicks(day)
            );

            if (gameTime < nextHordeTime) {
                NEXT_MINI_HORDE_TIME.putIfAbsent(
                        playerId,
                        nextHordeTime
                );

                continue;
            }

            int size = randomBetween(
                    TheFloodConfig.HORDES.miniHordeMinSize.get(),
                    TheFloodConfig.HORDES.miniHordeMaxSize.get()
            );

            spawnHorde(
                    level,
                    player,
                    day,
                    size,
                    TheFloodConfig.SPAWNING.hostileMobCapPerPlayer.get()
            );

            NEXT_MINI_HORDE_TIME.put(
                    playerId,
                    gameTime + calculateMiniHordeCooldownTicks(day)
            );
        }
    }

    private static long calculateMiniHordeCooldownTicks(int currentDay) {
        int earliestDay =
                TheFloodConfig.HORDES.miniHordeEarliestDay.get();

        int scalingDays =
                TheFloodConfig.HORDES.miniHordeScalingDays.get();

        int daysSinceEnabled =
                Math.max(0, currentDay - earliestDay);

        double progression = Math.min(
                1.0,
                daysSinceEnabled / (double) scalingDays
        );

        double earlyMinutes =
                TheFloodConfig.HORDES.miniHordeBaseCooldownMinutes.get();

        double lateMinutes =
                TheFloodConfig.HORDES.miniHordeMinimumCooldownMinutes.get();

        double cooldownMinutes =
                earlyMinutes + ((lateMinutes - earlyMinutes) * progression);

        /*
        * Add ±20% variation so hordes do not become predictable.
        */
        double variation =
                0.8 + RANDOM.nextDouble() * 0.4;

        cooldownMinutes *= variation;

        return Math.max(
                20L,
                Math.round(cooldownMinutes * 60.0 * 20.0)
        );
    }

    private static void tickBloodMoon(ServerLevel level, int day) {
        long gameTime = level.getGameTime();
        long intervalTicks =
                TheFloodConfig.HORDES.bloodMoonWaveIntervalSeconds.get() * 20L;

        if (trackedBloodMoonDay != day) {
            trackedBloodMoonDay = day;
            BLOOD_MOON_SPAWN_COUNTS.clear();
            lastBloodMoonWaveTime = -1;
        }

        if (
                lastBloodMoonWaveTime >= 0
                && gameTime - lastBloodMoonWaveTime < intervalTicks
        ) {
            return;
        }

        int activeCap = getBloodMoonActiveCap(day);
        int totalBudget = getBloodMoonTotalBudget(day);

        double refillThreshold =
                TheFloodConfig.HORDES.bloodMoonRefillThreshold.get();

        int refillPopulation =
                Math.max(1, (int) Math.floor(activeCap * refillThreshold));

        boolean spawnedAnyWave = false;

        for (ServerPlayer player : level.players()) {
            UUID playerId = player.getUUID();

            int alreadySpawned =
                    BLOOD_MOON_SPAWN_COUNTS.getOrDefault(playerId, 0);

            int remainingBudget = totalBudget - alreadySpawned;

            if (remainingBudget <= 0) {
                continue;
            }

            int currentPopulation =
                    SpawnDirector.countFloodMobsNearPlayer(level, player);

            /*
            * Do not send another wave while the player is still handling
            * most of the previous one.
            */
            if (currentPopulation > refillPopulation) {
                continue;
            }

            int availableActiveSlots =
                    activeCap - currentPopulation;

            if (availableActiveSlots <= 0) {
                continue;
            }

            int requestedSize = randomBetween(
                    TheFloodConfig.HORDES.bloodMoonWaveMinSize.get(),
                    TheFloodConfig.HORDES.bloodMoonWaveMaxSize.get()
            );

            int actualSize = Math.min(
                    requestedSize,
                    Math.min(availableActiveSlots, remainingBudget)
            );

            if (actualSize <= 0) {
                continue;
            }

            int successfullySpawned = spawnHorde(
                    level,
                    player,
                    day,
                    actualSize,
                    activeCap
            );

            if (successfullySpawned > 0) {
                BLOOD_MOON_SPAWN_COUNTS.put(
                        playerId,
                        alreadySpawned + successfullySpawned
                );

                spawnedAnyWave = true;
            }
        }

        if (spawnedAnyWave) {
            lastBloodMoonWaveTime = gameTime;
        }
    }

    private static int spawnHorde(
            ServerLevel level,
            ServerPlayer player,
            int day,
            int requestedSize,
            int activeCap
    ) {
        BlockPos center =
                SpawnDirector.findSpawnPositionNearPlayer(level, player);

        if (center == null) {
            return 0;
        }

        boolean bloodMoon = isBloodMoon(level);
        int spawned = 0;

        for (int i = 0; i < requestedSize; i++) {
            boolean success = SpawnDirector.trySpawnNearPosition(
                    level,
                    player,
                    day,
                    center,
                    TheFloodConfig.HORDES.hordeClumpRadius.get(),
                    true,
                    bloodMoon,
                    activeCap
            );

            if (success) {
                spawned++;
            }
        }

        return spawned;
    }

    private static final Map<UUID, Integer> BLOOD_MOON_SPAWN_COUNTS =
        new HashMap<>();

    private static int trackedBloodMoonDay = -1;

    private static int getBloodMoonNumber(int day) {
        int frequency = TheFloodConfig.TIME.bloodMoonFrequencyDays.get();

        return Math.max(1, day / frequency);
    }

    private static int getBloodMoonTotalBudget(int day) {
        int moonNumber = getBloodMoonNumber(day);

        int base =
                TheFloodConfig.HORDES.bloodMoonBaseTotalMobsPerPlayer.get();

        int increase =
                TheFloodConfig.HORDES.bloodMoonTotalMobIncreasePerMoon.get();

        int maximum =
                TheFloodConfig.HORDES.bloodMoonMaximumTotalMobsPerPlayer.get();

        return Math.min(
                maximum,
                base + ((moonNumber - 1) * increase)
        );
    }

    private static int getBloodMoonActiveCap(int day) {
        int moonNumber = getBloodMoonNumber(day);

        int base =
                TheFloodConfig.HORDES.bloodMoonBaseActiveCapPerPlayer.get();

        int increase =
                TheFloodConfig.HORDES.bloodMoonActiveCapIncreasePerMoon.get();

        int maximum =
                TheFloodConfig.HORDES.bloodMoonMaximumActiveCapPerPlayer.get();

        return Math.min(
                maximum,
                base + ((moonNumber - 1) * increase)
        );
    }

    private static boolean isBloodMoon(ServerLevel level) {
        int day = getDay(level);
        long timeOfDay = level.getDayTime() % 24000L;

        return day % TheFloodConfig.TIME.bloodMoonFrequencyDays.get() == 0
                && timeOfDay >= 13000L
                && timeOfDay <= 23000L;
    }

    private static int getDay(ServerLevel level) {
        return (int) (level.getDayTime() / 24000L) + 1;
    }

    private static int randomBetween(int min, int max) {
        if (max < min) max = min;
        return min + RANDOM.nextInt((max - min) + 1);
    }
}