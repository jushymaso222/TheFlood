package com.jushymaso222.theflood.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TheFloodConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final ForgeConfigSpec.IntValue DAY_LENGTH_MINUTES;
    public static final ForgeConfigSpec.IntValue NIGHT_LENGTH_MINUTES;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_FREQUENCY_DAYS;
    public static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_HOSTILE_SPAWNS;

    public static final ForgeConfigSpec.IntValue HOSTILE_MOB_CAP_PER_PLAYER;
    public static final ForgeConfigSpec.DoubleValue SPAWN_SCALING_FACTOR;

    public static final ForgeConfigSpec.IntValue MIN_SPAWN_DISTANCE_FROM_PLAYER;
    public static final ForgeConfigSpec.IntValue MAX_SPAWN_DISTANCE_FROM_PLAYER;

    public static final ForgeConfigSpec.IntValue ZOMBIE_UNLOCK_DAY;
    public static final ForgeConfigSpec.IntValue SKELETON_UNLOCK_DAY;
    public static final ForgeConfigSpec.IntValue SPIDER_UNLOCK_DAY;
    public static final ForgeConfigSpec.IntValue CREEPER_UNLOCK_DAY;
    public static final ForgeConfigSpec.IntValue ENDERMAN_UNLOCK_DAY;
    public static final ForgeConfigSpec.IntValue WARDEN_UNLOCK_DAY;

    public static final ForgeConfigSpec.IntValue BASE_SPAWN_ATTEMPTS;
    public static final ForgeConfigSpec.DoubleValue SPAWN_ATTEMPTS_PER_DAY;
    public static final ForgeConfigSpec.IntValue SPAWN_ROLL_CHANCE;

    public static final ForgeConfigSpec.IntValue MINI_HORDE_CHANCE;
    public static final ForgeConfigSpec.IntValue MINI_HORDE_MIN_SIZE;
    public static final ForgeConfigSpec.IntValue MINI_HORDE_MAX_SIZE;

    public static final ForgeConfigSpec.IntValue BLOOD_MOON_MOB_CAP_PER_PLAYER;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_WAVE_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_WAVE_MIN_SIZE;
    public static final ForgeConfigSpec.IntValue BLOOD_MOON_WAVE_MAX_SIZE;
    public static final ForgeConfigSpec.DoubleValue HORDE_FOLLOW_RANGE;
    public static final ForgeConfigSpec.DoubleValue BLOOD_MOON_FOLLOW_RANGE;

    public static final ForgeConfigSpec.DoubleValue FLOOD_WARDEN_HEALTH;
    public static final ForgeConfigSpec.DoubleValue FLOOD_WARDEN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue FLOOD_WARDEN_SPEED;

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

        builder.push("Mob Spawning");

        DISABLE_VANILLA_HOSTILE_SPAWNS = builder
                .comment("If true, vanilla hostile mob spawning is disabled so The Flood can control hostile spawns.")
                .define("disableVanillaHostileSpawns", true);

        HOSTILE_MOB_CAP_PER_PLAYER = builder
        .comment("Maximum hostile mobs allowed near each player.")
        .defineInRange("hostileMobCapPerPlayer", 50, 0, 500);

        SPAWN_SCALING_FACTOR = builder
                .comment("How aggressively hostile mob spawns increase as days pass. Higher = more spawns.")
                .defineInRange("spawnScalingFactor", 3, 0.0, 20.0);

        MIN_SPAWN_DISTANCE_FROM_PLAYER = builder
                .comment("Minimum distance hostile mobs can spawn from a player.")
                .defineInRange("minSpawnDistanceFromPlayer", 28, 1, 256);

        MAX_SPAWN_DISTANCE_FROM_PLAYER = builder
                .comment("Maximum distance hostile mobs can spawn from a player.")
                .defineInRange("maxSpawnDistanceFromPlayer", 72, 1, 512);

        ZOMBIE_UNLOCK_DAY = builder
                .comment("The first day zombies are allowed to spawn.")
                .defineInRange("zombieUnlockDay", 2, 1, 365);

        SKELETON_UNLOCK_DAY = builder
                .comment("The first day skeletons are allowed to spawn.")
                .defineInRange("skeletonUnlockDay", 8, 1, 365);

        SPIDER_UNLOCK_DAY = builder
                .comment("The first day spiders are allowed to spawn.")
                .defineInRange("spiderUnlockDay", 12, 1, 365);

        CREEPER_UNLOCK_DAY = builder
                .comment("The first day creepers are allowed to spawn.")
                .defineInRange("creeperUnlockDay", 16, 1, 365);

        ENDERMAN_UNLOCK_DAY = builder
                .comment("The first day endermen are allowed to spawn.")
                .defineInRange("endermanUnlockDay", 20, 1, 365);

        WARDEN_UNLOCK_DAY = builder
                .comment("The first day wardens are allowed to spawn. Recommended to keep this high.")
                .defineInRange("wardenUnlockDay", 35, 1, 365);
        
        BASE_SPAWN_ATTEMPTS = builder
        .comment("Base number of spawn attempts per player per tick once hostile mobs unlock.")
        .defineInRange("baseSpawnAttempts", 4, 0, 100);

        SPAWN_ATTEMPTS_PER_DAY = builder
                .comment("How many extra spawn attempts are added per day survived.")
                .defineInRange("spawnAttemptsPerDay", 0.75, 0.0, 20.0);

        SPAWN_ROLL_CHANCE = builder
                .comment("Chance denominator for each spawn attempt. Lower means more frequent spawns. Example: 120 = 1 in 120.")
                .defineInRange("spawnRollChance", 40, 1, 10000);

        builder.pop();

        builder.push("Horde Spawns");

        MINI_HORDE_CHANCE = builder
                .comment("Random chance denominator per player per tick for a mini horde. Higher = rarer.")
                .defineInRange("miniHordeChance", 2000, 1, 1000000);

        MINI_HORDE_MIN_SIZE = builder
                .comment("Minimum mobs in a mini horde.")
                .defineInRange("miniHordeMinSize", 4, 1, 100);

        MINI_HORDE_MAX_SIZE = builder
                .comment("Maximum mobs in a mini horde.")
                .defineInRange("miniHordeMaxSize", 12, 1, 100);

        BLOOD_MOON_MOB_CAP_PER_PLAYER = builder
                .comment("Mob cap near each player during blood moons.")
                .defineInRange("bloodMoonMobCapPerPlayer", 64, 1, 500);

        BLOOD_MOON_WAVE_INTERVAL_SECONDS = builder
                .comment("Seconds between blood moon waves.")
                .defineInRange("bloodMoonWaveIntervalSeconds", 15, 1, 600);

        BLOOD_MOON_WAVE_MIN_SIZE = builder
                .comment("Minimum mobs per blood moon wave per player.")
                .defineInRange("bloodMoonWaveMinSize", 8, 1, 200);

        BLOOD_MOON_WAVE_MAX_SIZE = builder
                .comment("Maximum mobs per blood moon wave per player.")
                .defineInRange("bloodMoonWaveMaxSize", 18, 1, 200);

        HORDE_FOLLOW_RANGE = builder
                .comment("Follow range for mini-horde mobs.")
                .defineInRange("hordeFollowRange", 80.0, 16.0, 256.0);

        BLOOD_MOON_FOLLOW_RANGE = builder
                .comment("Follow range for blood moon horde mobs.")
                .defineInRange("bloodMoonFollowRange", 128.0, 16.0, 512.0);

        builder.pop();

        builder.push("Flood Warden");

        FLOOD_WARDEN_HEALTH = builder
                .comment("Maximum health of Flood Wardens.")
                .defineInRange("health", 80.0, 1.0, 1000.0);

        FLOOD_WARDEN_DAMAGE = builder
                .comment("Attack damage of Flood Wardens.")
                .defineInRange("damage", 8.0, 0.0, 100.0);

        FLOOD_WARDEN_SPEED = builder
                .comment("Movement speed of Flood Wardens.")
                .defineInRange("speed", 0.25, 0.05, 2.0);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }
}