package com.jushymaso222.theflood.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TheFloodConfig {

    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final TimeSettings TIME;
    public static final SpawnSettings SPAWNING;
    public static final HordeSettings HORDES;
    public static final MobSettings MOBS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        TIME = new TimeSettings(builder);
        SPAWNING = new SpawnSettings(builder);
        HORDES = new HordeSettings(builder);
        MOBS = new MobSettings(builder);

        SERVER_CONFIG = builder.build();
    }

    private TheFloodConfig() {
    }

    public static final class TimeSettings {
        public final ForgeConfigSpec.IntValue dayLengthMinutes;
        public final ForgeConfigSpec.IntValue nightLengthMinutes;
        public final ForgeConfigSpec.IntValue bloodMoonFrequencyDays;

        private TimeSettings(ForgeConfigSpec.Builder builder) {
            builder.push("time");

            dayLengthMinutes = builder
                    .comment("How long daytime lasts in real-world minutes.")
                    .defineInRange("dayLengthMinutes", 30, 1, 240);

            nightLengthMinutes = builder
                    .comment("How long nighttime lasts in real-world minutes.")
                    .defineInRange("nightLengthMinutes", 10, 1, 240);

            bloodMoonFrequencyDays = builder
                    .comment("The number of Minecraft days between blood moons.")
                    .defineInRange("bloodMoonFrequencyDays", 7, 1, 365);

            builder.pop();
        }
    }

    public static final class SpawnSettings {
        public final ForgeConfigSpec.BooleanValue disableVanillaHostileSpawns;

        public final ForgeConfigSpec.IntValue hostileMobCapPerPlayer;
        public final ForgeConfigSpec.IntValue minSpawnDistanceFromPlayer;
        public final ForgeConfigSpec.IntValue maxSpawnDistanceFromPlayer;

        public final ForgeConfigSpec.IntValue baseSpawnAttempts;
        public final ForgeConfigSpec.DoubleValue spawnAttemptsPerDay;
        public final ForgeConfigSpec.IntValue spawnRollChance;
        public final ForgeConfigSpec.DoubleValue spawnWeightScalingFactor;

        private SpawnSettings(ForgeConfigSpec.Builder builder) {
            builder.push("spawning");

            disableVanillaHostileSpawns = builder
                    .comment("Disables vanilla hostile spawning so The Flood can control it.")
                    .define("disableVanillaHostileSpawns", true);

            hostileMobCapPerPlayer = builder
                    .comment("Maximum hostile mobs allowed near each player outside blood moons.")
                    .defineInRange("hostileMobCapPerPlayer", 50, 0, 500);

            minSpawnDistanceFromPlayer = builder
                    .comment("Minimum distance from a player for Flood-controlled spawns.")
                    .defineInRange("minSpawnDistanceFromPlayer", 28, 1, 256);

            maxSpawnDistanceFromPlayer = builder
                    .comment("Maximum distance from a player for Flood-controlled spawns.")
                    .defineInRange("maxSpawnDistanceFromPlayer", 72, 1, 512);

            baseSpawnAttempts = builder
                    .comment("Base spawn attempts per player per server tick.")
                    .defineInRange("baseSpawnAttempts", 4, 0, 100);

            spawnAttemptsPerDay = builder
                    .comment("Additional spawn attempts gained for each day survived.")
                    .defineInRange("spawnAttemptsPerDay", 0.75, 0.0, 20.0);

            spawnRollChance = builder
                    .comment(
                            "Chance denominator for each attempt.",
                            "Lower values create more frequent spawns.",
                            "Example: 40 means each attempt has a 1-in-40 chance."
                    )
                    .defineInRange("spawnRollChance", 40, 1, 10000);

            spawnWeightScalingFactor = builder
                    .comment(
                            "How quickly a mob's selection weight grows after it unlocks.",
                            "This affects mob distribution, not total spawn frequency."
                    )
                    .defineInRange("spawnWeightScalingFactor", 3.0, 0.0, 20.0);

            builder.pop();
        }
    }

    public static final class HordeSettings {
        public final ForgeConfigSpec.IntValue miniHordeChance;
        public final ForgeConfigSpec.IntValue miniHordeMinSize;
        public final ForgeConfigSpec.IntValue miniHordeMaxSize;

        public final ForgeConfigSpec.IntValue bloodMoonMobCapPerPlayer;
        public final ForgeConfigSpec.IntValue bloodMoonWaveIntervalSeconds;
        public final ForgeConfigSpec.IntValue bloodMoonWaveMinSize;
        public final ForgeConfigSpec.IntValue bloodMoonWaveMaxSize;

        public final ForgeConfigSpec.DoubleValue miniHordeFollowRange;
        public final ForgeConfigSpec.DoubleValue bloodMoonFollowRange;

        public final ForgeConfigSpec.IntValue hordeClumpRadius;

        private HordeSettings(ForgeConfigSpec.Builder builder) {
            builder.push("hordes");

            builder.push("miniHordes");

            miniHordeChance = builder
                    .comment(
                            "Chance denominator per player per server tick.",
                            "Higher values make mini-hordes rarer."
                    )
                    .defineInRange("chance", 2000, 1, 1_000_000);

            miniHordeMinSize = builder
                    .comment("Minimum number of mobs in a mini-horde.")
                    .defineInRange("minSize", 4, 1, 100);

            miniHordeMaxSize = builder
                    .comment("Maximum number of mobs in a mini-horde.")
                    .defineInRange("maxSize", 12, 1, 100);

            miniHordeFollowRange = builder
                    .comment("Follow range assigned to mini-horde mobs.")
                    .defineInRange("followRange", 80.0, 16.0, 256.0);

            builder.pop();

            builder.push("bloodMoon");

            bloodMoonMobCapPerPlayer = builder
                    .comment("Maximum hostile mobs near each player during a blood moon.")
                    .defineInRange("mobCapPerPlayer", 64, 1, 500);

            bloodMoonWaveIntervalSeconds = builder
                    .comment("Real-world seconds between blood moon waves.")
                    .defineInRange("waveIntervalSeconds", 15, 1, 600);

            bloodMoonWaveMinSize = builder
                    .comment("Minimum mobs spawned per wave for each player.")
                    .defineInRange("waveMinSize", 8, 1, 200);

            bloodMoonWaveMaxSize = builder
                    .comment("Maximum mobs spawned per wave for each player.")
                    .defineInRange("waveMaxSize", 18, 1, 200);

            bloodMoonFollowRange = builder
                    .comment("Follow range assigned to blood moon mobs.")
                    .defineInRange("followRange", 128.0, 16.0, 512.0);

            builder.pop();

            hordeClumpRadius = builder
                    .comment("Radius around a horde anchor in which its mobs can spawn.")
                    .defineInRange("clumpRadius", 6, 1, 32);

            builder.pop();
        }
    }

    public static final class MobSettings {
        public final StandardMob zombie;
        public final StandardMob skeleton;
        public final StandardMob spider;
        public final StandardMob creeper;
        public final StandardMob enderman;
        public final WardenMob warden;

        private MobSettings(ForgeConfigSpec.Builder builder) {
            builder.push("mobs");

            zombie = new StandardMob(
                builder,
                "zombie",
                2, //Unlock day
                10, //Base spawn weight
                20.0, //Base health
                3.0, //Base damage
                1.5, //Health increase per day
                0.20, //Damage increase per day
                60.0, //Maximum health
                40.0 //Maximum damage
        );

        skeleton = new StandardMob(
                builder,
                "skeleton",
                8,
                8,
                20.0,
                2.0,
                1.25,
                0.15,
                100.0,
                40.0
        );

        spider = new StandardMob(
                builder,
                "spider",
                12,
                8,
                16.0,
                2.0,
                1.25,
                0.20,
                40.0,
                40.0
        );

        creeper = new StandardMob(
                builder,
                "creeper",
                16,
                5,
                20.0,
                0.0,
                1.5,
                0.0,
                60.0,
                0.0
        );

        enderman = new StandardMob(
                builder,
                "enderman",
                20,
                4,
                40.0,
                7.0,
                2.0,
                0.30,
                200.0,
                50.0
        );

            warden = new WardenMob(
                    builder,
                    35, //Unlock day
                    1, //Default spawn weight
                    80.0, //Base health
                    10.0, //Base damage
                    0.25 //Base movement speed
            );

            builder.pop();
        }
    }

    public static class StandardMob {
    public final ForgeConfigSpec.IntValue unlockDay;
    public final ForgeConfigSpec.IntValue baseSpawnWeight;

    public final ForgeConfigSpec.DoubleValue baseHealth;
    public final ForgeConfigSpec.DoubleValue healthPerDay;
    public final ForgeConfigSpec.DoubleValue maximumHealth;

    public final ForgeConfigSpec.DoubleValue baseDamage;
    public final ForgeConfigSpec.DoubleValue damagePerDay;
    public final ForgeConfigSpec.DoubleValue maximumDamage;

    protected StandardMob(
            ForgeConfigSpec.Builder builder,
            String mobName,
            int defaultUnlockDay,
            int defaultSpawnWeight,
            double defaultBaseHealth,
            double defaultBaseDamage,
            double defaultHealthPerDay,
            double defaultDamagePerDay,
            double defaultMaximumHealth,
            double defaultMaximumDamage
        ) {
                builder.push(mobName);

                unlockDay = builder
                        .comment("The first day this mob can spawn through The Flood.")
                        .defineInRange("unlockDay", defaultUnlockDay, 1, 100_000);

                baseSpawnWeight = builder
                        .comment("The mob's initial selection weight when it unlocks.")
                        .defineInRange("baseSpawnWeight", defaultSpawnWeight, 0, 10_000);

                baseHealth = builder
                        .comment("Health this mob has on its unlock day.")
                        .defineInRange("baseHealth", defaultBaseHealth, 1.0, 10_000.0);

                healthPerDay = builder
                        .comment("Health added for every day after this mob unlocks.")
                        .defineInRange("healthPerDay", defaultHealthPerDay, 0.0, 1_000.0);

                maximumHealth = builder
                        .comment(
                                "Maximum health this mob can reach.",
                                "Set very high if you do not want a practical cap."
                        )
                        .defineInRange("maximumHealth", defaultMaximumHealth, 1.0, 100_000.0);

                baseDamage = builder
                        .comment("Attack damage this mob has on its unlock day.")
                        .defineInRange("baseDamage", defaultBaseDamage, 0.0, 1_000.0);

                damagePerDay = builder
                        .comment("Attack damage added for every day after this mob unlocks.")
                        .defineInRange("damagePerDay", defaultDamagePerDay, 0.0, 100.0);

                maximumDamage = builder
                        .comment(
                                "Maximum attack damage this mob can reach.",
                                "Set very high if you do not want a practical cap."
                        )
                        .defineInRange("maximumDamage", defaultMaximumDamage, 0.0, 10_000.0);

                builder.pop();
        }
        }

    public static final class WardenMob {
        public final ForgeConfigSpec.IntValue unlockDay;
        public final ForgeConfigSpec.IntValue baseSpawnWeight;

        public final ForgeConfigSpec.DoubleValue health;
        public final ForgeConfigSpec.DoubleValue damage;
        public final ForgeConfigSpec.DoubleValue movementSpeed;

        private WardenMob(
                ForgeConfigSpec.Builder builder,
                int defaultUnlockDay,
                int defaultSpawnWeight,
                double defaultHealth,
                double defaultDamage,
                double defaultMovementSpeed
        ) {
            builder.push("warden");

            unlockDay = builder
                    .comment("The first day Flood Wardens can spawn.")
                    .defineInRange("unlockDay", defaultUnlockDay, 1, 100_000);

            baseSpawnWeight = builder
                    .comment("The Flood Warden's initial selection weight.")
                    .defineInRange("baseSpawnWeight", defaultSpawnWeight, 0, 10_000);

            health = builder
                    .comment("Maximum health of Flood Wardens.")
                    .defineInRange("health", defaultHealth, 1.0, 1000.0);

            damage = builder
                    .comment("Attack damage of Flood Wardens.")
                    .defineInRange("damage", defaultDamage, 0.0, 100.0);

            movementSpeed = builder
                    .comment("Movement speed of Flood Wardens.")
                    .defineInRange("movementSpeed", defaultMovementSpeed, 0.05, 2.0);

            builder.pop();
        }
    }
}