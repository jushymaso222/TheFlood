package com.jushymaso222.theflood.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TheFloodConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final ForgeConfigSpec.IntValue DAY_LENGTH_MINUTES;
    public static final ForgeConfigSpec.IntValue NIGHT_LENGTH_MINUTES;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_FREQUENCY_DAYS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Time Settings");

        DAY_LENGTH_MINUTES = builder
                .comment("How long daytime lasts in real minutes.")
                .defineInRange("dayLengthMinutes", 30, 1, 240);

        NIGHT_LENGTH_MINUTES = builder
                .comment("How long nighttime lasts in real minutes.")
                .defineInRange("nightLengthMinutes", 10, 1, 240);

        BLOOD_MOON_FREQUENCY_DAYS = builder
                .comment("How often a blood moon happens, in Minecraft days.")
                .defineInRange("bloodMoonFrequencyDays", 7, 1, 365);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }
}