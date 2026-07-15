package com.jushymaso222.theflood.horde;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.spawn.SpawnDirector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.Random;

public class HordeDirector {

    private static final Random RANDOM = new Random();
    private static long lastBloodMoonWaveTime = -1;

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

    private static void tickMiniHordes(ServerLevel level, int day, long timeOfDay) {
        // Optional: avoid mini hordes during day 1 and during blood moon.
        int defaultMiniHordeChance = TheFloodConfig.HORDES.miniHordeChance.get();
        double adjustedMiniHordeChance = defaultMiniHordeChance - (day * TheFloodConfig.HORDES.miniHordeIncreaseRate.get());

        for (ServerPlayer player : level.players()) {
            if (RANDOM.nextInt((int) adjustedMiniHordeChance) == 0) {
                int size = randomBetween(
                        TheFloodConfig.HORDES.miniHordeMinSize.get(),
                        TheFloodConfig.HORDES.miniHordeMaxSize.get()
                );

                spawnHorde(level, player, day, size);
            }
        }
    }

    private static void tickBloodMoon(ServerLevel level, int day) {
        long gameTime = level.getGameTime();
        long intervalTicks = TheFloodConfig.HORDES.bloodMoonWaveIntervalSeconds.get() * 20L;

        if (lastBloodMoonWaveTime >= 0 && gameTime - lastBloodMoonWaveTime < intervalTicks) {
            return;
        }

        lastBloodMoonWaveTime = gameTime;

        for (ServerPlayer player : level.players()) {
            int size = randomBetween(
                    TheFloodConfig.HORDES.bloodMoonWaveMinSize.get(),
                    TheFloodConfig.HORDES.bloodMoonWaveMaxSize.get()
            );

            spawnHorde(level, player, day, size);
        }
    }

    private static void spawnHorde(ServerLevel level, ServerPlayer player, int day, int size) {
        BlockPos center = SpawnDirector.findSpawnPositionNearPlayer(level, player);

        if (center == null) return;

        for (int i = 0; i < size; i++) {
            SpawnDirector.trySpawnNearPosition(
                    level,
                    player,
                    day,
                    center,
                    6,
                    true,
                    isBloodMoon(level)
            );
        }
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