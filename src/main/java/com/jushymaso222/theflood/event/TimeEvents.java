package com.jushymaso222.theflood.event;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class TimeEvents {

    private static double customWorldTime = -1;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        level.getGameRules()
                .getRule(GameRules.RULE_DAYLIGHT)
                .set(false, level.getServer());

        long actualWorldTime = level.getDayTime();

        if (customWorldTime < 0) {
            customWorldTime = actualWorldTime;
        }

        if (
                Math.abs(
                        actualWorldTime
                                - (long) customWorldTime
                )
                        > 20
        ) {
            customWorldTime =
                    actualWorldTime;
        }

        customWorldTime += getTimeIncreasePerTick(customWorldTime);

        level.setDayTime((long) customWorldTime);
    }

    public static void setCustomWorldTime(long newTime) {
        customWorldTime = newTime;
    }

    public static long resolveTimeSet(
            long requestedTime
    ) {
        if (customWorldTime < 0) {
            return requestedTime;
        }

        long currentAbsolute =
                (long) customWorldTime;

        long currentDay =
                Math.floorDiv(
                        currentAbsolute,
                        24000L
                );

        long currentTimeOfDay =
                Math.floorMod(
                        currentAbsolute,
                        24000L
                );

        long requestedTimeOfDay =
                Math.floorMod(
                        requestedTime,
                        24000L
                );

        if (
                requestedTimeOfDay
                        < currentTimeOfDay
        ) {
            currentDay++;
        }

        return currentDay
                * 24000L
                + requestedTimeOfDay;
    }

    private static double getTimeIncreasePerTick(double worldTime) {
        long timeOfDay =
                Math.floorMod(
                        (long) worldTime,
                        24000L
                );

        int dayMinutes = TheFloodConfig.TIME.dayLengthMinutes.get();
        int nightMinutes = TheFloodConfig.TIME.nightLengthMinutes.get();

        double vanillaDayTicks = 13000.0;
        double vanillaNightTicks = 11000.0;

        double realTicksPerDay = dayMinutes * 60.0 * 20.0;
        double realTicksPerNight = nightMinutes * 60.0 * 20.0;

        if (timeOfDay < 13000L) {
            return vanillaDayTicks / realTicksPerDay;
        } else {
            return vanillaNightTicks / realTicksPerNight;
        }
    }
}