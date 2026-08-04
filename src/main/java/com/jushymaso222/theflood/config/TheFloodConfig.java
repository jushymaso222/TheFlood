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

        // public final ForgeConfigSpec.IntValue baseSpawnAttempts;
        // public final ForgeConfigSpec.DoubleValue spawnAttemptsPerDay;
        // public final ForgeConfigSpec.IntValue spawnRollChance;
        public final ForgeConfigSpec.DoubleValue spawnWeightScalingFactor;
        public final ForgeConfigSpec.IntValue maximumSpawnBlockLight;

        public final ForgeConfigSpec.IntValue earlyGameSpawnCooldownMinSeconds;
        public final ForgeConfigSpec.IntValue earlyGameSpawnCooldownMaxSeconds;

        public final ForgeConfigSpec.IntValue lateGameSpawnCooldownMinSeconds;
        public final ForgeConfigSpec.IntValue lateGameSpawnCooldownMaxSeconds;

        public final ForgeConfigSpec.IntValue spawnCooldownScalingDays;
        public final ForgeConfigSpec.DoubleValue undergroundSpawnCooldownMultiplier;
        public final ForgeConfigSpec.IntValue maximumVerticalSpawnDistance;

        public final ForgeConfigSpec.IntValue startingAmbientPopulation;
        public final ForgeConfigSpec.DoubleValue ambientPopulationPerDay;
        public final ForgeConfigSpec.IntValue maximumAmbientPopulation;

        public final ForgeConfigSpec.DoubleValue undergroundPopulationMultiplier;

        public final ForgeConfigSpec.IntValue ambientRefillMinSeconds;
        public final ForgeConfigSpec.IntValue ambientRefillMaxSeconds;

        private SpawnSettings(ForgeConfigSpec.Builder builder) {
            builder.push("spawning");

            disableVanillaHostileSpawns = builder
                    .comment("Disables vanilla hostile spawning so The Flood can control it.")
                    .define("disableVanillaHostileSpawns", true);

            hostileMobCapPerPlayer = builder
                    .comment("Maximum hostile mobs allowed near each player outside blood moons.")
                    .defineInRange("hostileMobCapPerPlayer", 24, 0, 500);

            minSpawnDistanceFromPlayer = builder
                    .comment("Minimum distance from a player for Flood-controlled spawns.")
                    .defineInRange("minSpawnDistanceFromPlayer", 28, 1, 256);

            maxSpawnDistanceFromPlayer = builder
                    .comment("Maximum distance from a player for Flood-controlled spawns.")
                    .defineInRange("maxSpawnDistanceFromPlayer", 72, 1, 512);

            earlyGameSpawnCooldownMinSeconds = builder
                    .comment("Minimum seconds between normal spawn attempts during early progression.")
                    .defineInRange("earlyGameSpawnCooldownMinSeconds", 8, 1, 600);

            earlyGameSpawnCooldownMaxSeconds = builder
                    .comment("Maximum seconds between normal spawn attempts during early progression.")
                    .defineInRange("earlyGameSpawnCooldownMaxSeconds", 14, 1, 600);

            lateGameSpawnCooldownMinSeconds = builder
                    .comment("Minimum seconds between normal spawn attempts at maximum spawn scaling.")
                    .defineInRange("lateGameSpawnCooldownMinSeconds", 2, 1, 600);

            lateGameSpawnCooldownMaxSeconds = builder
                    .comment("Maximum seconds between normal spawn attempts at maximum spawn scaling.")
                    .defineInRange("lateGameSpawnCooldownMaxSeconds", 5, 1, 600);

            spawnCooldownScalingDays = builder
                    .comment(
                            "Number of days required for normal spawning to reach its fastest configured cooldown.",
                            "After this day, spawning remains at the late-game cooldown."
                    )
                    .defineInRange("spawnCooldownScalingDays", 50, 1, 100_000);

            undergroundSpawnCooldownMultiplier = builder
                    .comment(
                            "Multiplier applied to normal spawn cooldowns while a player is underground.",
                            "Higher values make underground spawning less frequent."
                    )
                    .defineInRange("undergroundSpawnCooldownMultiplier", 4.0, 1.0, 100.0);

            maximumVerticalSpawnDistance = builder
                    .comment(
                            "Maximum vertical distance between a player and a normal Flood spawn.",
                            "Prevents surface mobs from accumulating far above underground players."
                    )
                    .defineInRange("maximumVerticalSpawnDistance", 24, 1, 256);

        //     baseSpawnAttempts = builder
        //             .comment("Base spawn attempts per player per server tick.")
        //             .defineInRange("baseSpawnAttempts", 2, 0, 100);

        //     spawnAttemptsPerDay = builder
        //             .comment("Additional spawn attempts gained for each day survived.")
        //             .defineInRange("spawnAttemptsPerDay", 0.75, 0.0, 20.0);

        //     spawnRollChance = builder
        //             .comment(
        //                     "Chance denominator for each attempt.",
        //                     "Lower values create more frequent spawns.",
        //                     "Example: 40 means each attempt has a 1-in-40 chance."
        //             )
        //             .defineInRange("spawnRollChance", 100, 1, 10000);

            spawnWeightScalingFactor = builder
                    .comment(
                            "How quickly a mob's selection weight grows after it unlocks.",
                            "This affects mob distribution, not total spawn frequency."
                    )
                    .defineInRange("spawnWeightScalingFactor", 3.0, 0.0, 20.0);

            maximumSpawnBlockLight = builder
                    .comment(
                            "Highest artificial block-light level where Flood mobs may spawn.",
                            "This ignores sunlight and skylight.",
                            "Set to 0 to prevent spawning mobs anywhere reached by artificial light."
                    )
                    .defineInRange("maximumSpawnBlockLight", 0, 0, 15);

            startingAmbientPopulation = builder
                    .comment(
                        "Target number of normal Flood mobs near each player",
                        "when zombies first unlock."
                    )
                    .defineInRange("startingAmbientPopulation", 2, 0, 100);

            ambientPopulationPerDay = builder
                    .comment(
                        "Additional target ambient population per day after zombies unlock.",
                        "Example: 0.35 adds roughly one additional mob every three days."
                    )
                    .defineInRange("ambientPopulationPerDay", 0.35, 0.0, 20.0);

            maximumAmbientPopulation = builder
                    .comment("Maximum target population for normal, non-horde Flood mobs.")
                    .defineInRange("maximumAmbientPopulation", 20, 0, 500);

            undergroundPopulationMultiplier = builder
                    .comment(
                        "Multiplier applied to the ambient population target underground.",
                        "0.5 means caves maintain half the normal number of mobs."
                    )
                    .defineInRange("undergroundPopulationMultiplier", 0.5, 0.0, 1.0);

            ambientRefillMinSeconds = builder
                    .comment("Minimum delay before replacing a missing ambient mob.")
                    .defineInRange("ambientRefillMinSeconds", 12, 1, 600);

            ambientRefillMaxSeconds = builder
                    .comment("Maximum delay before replacing a missing ambient mob.")
                    .defineInRange("ambientRefillMaxSeconds", 20, 1, 600);

            builder.pop();
        }
    }

    public static final class HordeSettings {
        public final ForgeConfigSpec.IntValue miniHordeChance;
        public final ForgeConfigSpec.IntValue miniHordeMinSize;
        public final ForgeConfigSpec.IntValue miniHordeMaxSize;

        public final ForgeConfigSpec.IntValue bloodMoonWaveIntervalSeconds;
        public final ForgeConfigSpec.IntValue bloodMoonWaveMinSize;
        public final ForgeConfigSpec.IntValue bloodMoonWaveMaxSize;

        public final ForgeConfigSpec.DoubleValue miniHordeFollowRange;
        public final ForgeConfigSpec.DoubleValue bloodMoonFollowRange;

        public final ForgeConfigSpec.DoubleValue miniHordeIncreaseRate;

        public final ForgeConfigSpec.IntValue miniHordeEarliestDay;
        public final ForgeConfigSpec.IntValue miniHordeBaseCooldownMinutes;
        public final ForgeConfigSpec.IntValue miniHordeMinimumCooldownMinutes;
        public final ForgeConfigSpec.IntValue miniHordeScalingDays;

        public final ForgeConfigSpec.IntValue bloodMoonBaseTotalMobsPerPlayer;
        public final ForgeConfigSpec.IntValue bloodMoonTotalMobIncreasePerMoon;
        public final ForgeConfigSpec.IntValue bloodMoonMaximumTotalMobsPerPlayer;

        public final ForgeConfigSpec.IntValue bloodMoonBaseActiveCapPerPlayer;
        public final ForgeConfigSpec.IntValue bloodMoonActiveCapIncreasePerMoon;
        public final ForgeConfigSpec.IntValue bloodMoonMaximumActiveCapPerPlayer;

        public final ForgeConfigSpec.DoubleValue bloodMoonRefillThreshold;

        public final ForgeConfigSpec.IntValue hordeClumpRadius;

        private HordeSettings(ForgeConfigSpec.Builder builder) {
            builder.push("hordes");

            builder.push("miniHordes");

            miniHordeChance = builder
                    .comment(
                            "Chance denominator per player per server tick.",
                            "Higher values make mini-hordes rarer."
                    )
                    .defineInRange("chance", 10000, 1, 1_000_000);

            miniHordeMinSize = builder
                    .comment("Minimum number of mobs in a mini-horde.")
                    .defineInRange("minSize", 4, 1, 100);

            miniHordeMaxSize = builder
                    .comment("Maximum number of mobs in a mini-horde.")
                    .defineInRange("maxSize", 12, 1, 100);

            miniHordeFollowRange = builder
                    .comment("Follow range assigned to mini-horde mobs.")
                    .defineInRange("followRange", 80.0, 16.0, 256.0);

            miniHordeEarliestDay = builder
                    .comment("First day on which mini-hordes are allowed.")
                    .defineInRange("earliestDay", 3, 1, 100_000);

            miniHordeBaseCooldownMinutes = builder
                    .comment(
                            "Approximate cooldown between mini-horde opportunities early in progression."
                    )
                    .defineInRange("baseCooldownMinutes", 25, 1, 10_000);

            miniHordeMinimumCooldownMinutes = builder
                    .comment(
                            "Shortest possible mini-horde cooldown after full scaling."
                    )
                    .defineInRange("minimumCooldownMinutes", 6, 1, 10_000);

            miniHordeScalingDays = builder
                    .comment(
                            "Days required for mini-hordes to reach their minimum cooldown."
                    )
                    .defineInRange("scalingDays", 60, 1, 100_000);

            miniHordeIncreaseRate = builder
                    .comment(
                            "How much the chance of a mini-horde to spawn increases per day.",
                            "Higher values makes mini-horde chance increase faster."
                    )
                    .defineInRange("miniHordeIncreaseRate", 10.0, 1.0, 400.0);

            builder.pop();

            builder.push("bloodMoon");

            bloodMoonBaseTotalMobsPerPlayer = builder
                        .comment("Total mobs allowed to spawn for each player during the first Blood Moon.")
                        .defineInRange("baseTotalMobsPerPlayer", 45, 1, 10_000);

                bloodMoonTotalMobIncreasePerMoon = builder
                        .comment("Additional total mobs added for each later Blood Moon.")
                        .defineInRange("totalMobIncreasePerMoon", 15, 0, 1_000);

                bloodMoonMaximumTotalMobsPerPlayer = builder
                        .comment("Maximum total mobs that can spawn per player during one Blood Moon.")
                        .defineInRange("maximumTotalMobsPerPlayer", 180, 1, 10_000);

                bloodMoonBaseActiveCapPerPlayer = builder
                        .comment("Maximum active mobs near each player during the first Blood Moon.")
                        .defineInRange("baseActiveCapPerPlayer", 14, 1, 500);

                bloodMoonActiveCapIncreasePerMoon = builder
                        .comment("Active mob-cap increase for each later Blood Moon.")
                        .defineInRange("activeCapIncreasePerMoon", 3, 0, 100);

                bloodMoonMaximumActiveCapPerPlayer = builder
                        .comment("Maximum active Blood Moon mob cap per player.")
                        .defineInRange("maximumActiveCapPerPlayer", 36, 1, 500);

                bloodMoonRefillThreshold = builder
                        .comment(
                                "Fraction of the active cap below which another wave may spawn.",
                                "Example: 0.5 means waves refill after population falls below half the cap."
                        )
                        .defineInRange("refillThreshold", 0.5, 0.0, 1.0);

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
        public final StandardMob warden;

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

        warden = new StandardMob(
                builder,
                "warden",
                35, //Unlock day
                1, //Default spawn weight
                80.0, //Base health
                10.0, //Base damage
                0.0,
                0.0,
                80.0,
                10.0

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

        public final ForgeConfigSpec.DoubleValue baseHealth;
        public final ForgeConfigSpec.DoubleValue baseDamage;
        public final ForgeConfigSpec.DoubleValue movementSpeed;

        private WardenMob(
                ForgeConfigSpec.Builder builder,
                int defaultUnlockDay,
                int defaultSpawnWeight,
                double defaultBaseHealth,
                double defaultBaseDamage,
                double defaultMovementSpeed
        ) {
            builder.push("warden");

            unlockDay = builder
                    .comment("The first day Flood Wardens can spawn.")
                    .defineInRange("unlockDay", defaultUnlockDay, 1, 100_000);

            baseSpawnWeight = builder
                    .comment("The Flood Warden's initial selection weight.")
                    .defineInRange("baseSpawnWeight", defaultSpawnWeight, 0, 10_000);

            baseHealth = builder
                    .comment("Maximum health of Flood Wardens.")
                    .defineInRange("health", defaultBaseHealth, 1.0, 1000.0);

            baseDamage = builder
                    .comment("Attack damage of Flood Wardens.")
                    .defineInRange("damage", defaultBaseDamage, 0.0, 100.0);

            movementSpeed = builder
                    .comment("Movement speed of Flood Wardens.")
                    .defineInRange("movementSpeed", defaultMovementSpeed, 0.05, 2.0);

            builder.pop();
        }
    }
}