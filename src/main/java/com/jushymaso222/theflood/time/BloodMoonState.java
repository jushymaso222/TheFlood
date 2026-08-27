package com.jushymaso222.theflood.time;

import com.jushymaso222.theflood.config.TheFloodConfig;

public final class BloodMoonState {

    private BloodMoonState() {
    }

    public static boolean isBloodMoon(
            long dayTime
    ) {
        int day =
                (int) (dayTime / 24000L)
                        + 1;

        long timeOfDay =
                dayTime % 24000L;

        return day
                % TheFloodConfig.TIME
                        .bloodMoonFrequencyDays
                        .get()
                == 0
                && timeOfDay >= 13000L
                && timeOfDay <= 23000L;
    }
}