package com.jushymaso222.theflood.config;

import com.jushymaso222.theflood.client.guide.ServerSettingEntry;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class ServerSettingsSnapshot {

    private ServerSettingsSnapshot() {
    }

    public static List<ServerSettingEntry> create() {

        List<ServerSettingEntry> settings =
                new ArrayList<>();

        addTimeSettings(
                settings
        );

        addHeatSettings(
                settings
        );

        addSpawnSettings(
                settings
        );

        addHordeSettings(
                settings
        );

        addMobSettings(
                settings
        );

        return settings;
    }

        private static void addTimeSettings(
            List<ServerSettingEntry> settings
    ) {

        addInt(
                settings,
                "World",
                "Time & Blood Moons",
                "Day Length",
                TheFloodConfig.TIME.dayLengthMinutes,
                " minutes",
                "Real-world length of daytime."
        );

        addInt(
                settings,
                "World",
                "Time & Blood Moons",
                "Night Length",
                TheFloodConfig.TIME.nightLengthMinutes,
                " minutes",
                "Real-world length of nighttime."
        );

        addInt(
                settings,
                "World",
                "Time & Blood Moons",
                "Blood Moon Frequency",
                TheFloodConfig.TIME.bloodMoonFrequencyDays,
                " days",
                "Number of world days between Blood Moons."
        );
    }

        private static void addSpawnSettings(
            List<ServerSettingEntry> settings
    ) {

        addBoolean(
                settings,
                "Spawning",
                "General",
                "Flood-Controlled Hostile Spawning",
                TheFloodConfig.SPAWNING.disableVanillaHostileSpawns,
                "When enabled, The Flood replaces normal vanilla hostile spawning."
        );

        addInt(
                settings,
                "Spawning",
                "Population",
                "Hostile Hard Cap",
                TheFloodConfig.SPAWNING.hostileMobCapPerPlayer,
                " per player",
                "Maximum nearby Flood-controlled hostiles outside Blood Moons."
        );

        addIntRange(
                settings,
                "Spawning",
                "Spawn Distance",
                "Horizontal Spawn Distance",
                TheFloodConfig.SPAWNING.minSpawnDistanceFromPlayer,
                TheFloodConfig.SPAWNING.maxSpawnDistanceFromPlayer,
                " blocks",
                "Minimum and maximum horizontal distance used for Flood spawns."
        );

        addInt(
                settings,
                "Spawning",
                "Spawn Distance",
                "Maximum Vertical Distance",
                TheFloodConfig.SPAWNING.maximumVerticalSpawnDistance,
                " blocks",
                "Maximum vertical separation between a player and a normal Flood spawn."
        );

        addIntRange(
                settings,
                "Spawning",
                "Spawn Timing",
                "Early Progression Spawn Delay",
                TheFloodConfig.SPAWNING.earlyGameSpawnCooldownMinSeconds,
                TheFloodConfig.SPAWNING.earlyGameSpawnCooldownMaxSeconds,
                " seconds",
                "Approximate delay between ambient spawn opportunities early in progression."
        );

        addIntRange(
                settings,
                "Spawning",
                "Spawn Timing",
                "Late Progression Spawn Delay",
                TheFloodConfig.SPAWNING.lateGameSpawnCooldownMinSeconds,
                TheFloodConfig.SPAWNING.lateGameSpawnCooldownMaxSeconds,
                " seconds",
                "Approximate delay after spawning reaches maximum speed."
        );

        addInt(
                settings,
                "Spawning",
                "Lighting",
                "Maximum Artificial Light",
                TheFloodConfig.SPAWNING.maximumSpawnBlockLight,
                "",
                "Highest artificial block-light level where Flood mobs may spawn."
        );

        addInt(
                settings,
                "Spawning",
                "Population",
                "Starting Ambient Population",
                TheFloodConfig.SPAWNING.startingAmbientPopulation,
                " mobs",
                "Target ambient population when normal hostile progression begins."
        );

        addInt(
                settings,
                "Spawning",
                "Population",
                "Maximum Ambient Population",
                TheFloodConfig.SPAWNING.maximumAmbientPopulation,
                " mobs",
                "Maximum normal ambient population target."
        );

        addDouble(
                settings,
                "Spawning",
                "Underground",
                "Underground Population",
                TheFloodConfig.SPAWNING.undergroundPopulationMultiplier,
                "x",
                "Multiplier applied to normal population while underground."
        );

        addDouble(
                settings,
                "Spawning",
                "Underground",
                "Underground Spawn Delay",
                TheFloodConfig.SPAWNING.undergroundSpawnCooldownMultiplier,
                "x",
                "Multiplier applied to spawn delays underground. Higher means slower spawning."
        );

        addIntRange(
                settings,
                "Spawning",
                "Population",
                "Ambient Refill Delay",
                TheFloodConfig.SPAWNING.ambientRefillMinSeconds,
                TheFloodConfig.SPAWNING.ambientRefillMaxSeconds,
                " seconds",
                "Delay before missing ambient population is replenished."
        );

        addDouble(
                settings,
                "Spawning",
                "Mob Selection",
                "Spawn Weight Growth",
                TheFloodConfig.SPAWNING.spawnWeightScalingFactor,
                " /Heat",
                "How quickly unlocked mobs gain selection weight as progression increases."
        );

        addInt(
                settings,
                "Spawning",
                "Spawn Timing",
                "Full Spawn-Speed Progression",
                TheFloodConfig.SPAWNING.spawnCooldownScalingDays,
                " Heat",
                "Progression required for normal spawning to reach its fastest configured cooldown."
        );

        addDouble(
                settings,
                "Spawning",
                "Population",
                "Ambient Population Growth",
                TheFloodConfig.SPAWNING.ambientPopulationPerDay,
                " mobs/Heat",
                "How quickly the target ambient population grows after hostile spawning unlocks."
        );
    }

    private static void addHordeSettings(
        List<ServerSettingEntry> settings
) {

    /*
     * =========================================================
     * MINI-HORDES
     * =========================================================
     */

    addIntRange(
            settings,
            "Hordes",
            "Mini-Hordes",
            "Horde Size",
            TheFloodConfig.HORDES.miniHordeMinSize,
            TheFloodConfig.HORDES.miniHordeMaxSize,
            " mobs",
            "Minimum and maximum number of enemies in a mini-horde."
    );

    addInt(
            settings,
            "Hordes",
            "Mini-Hordes",
            "Earliest Horde Day",
            TheFloodConfig.HORDES.miniHordeEarliestDay,
            "",
            "First world day on which mini-hordes are allowed."
    );

    addInt(
            settings,
            "Hordes",
            "Mini-Hordes",
            "Early Horde Cooldown",
            TheFloodConfig.HORDES.miniHordeBaseCooldownMinutes,
            " minutes",
            "Approximate mini-horde cooldown early in progression."
    );

    addInt(
            settings,
            "Hordes",
            "Mini-Hordes",
            "Minimum Horde Cooldown",
            TheFloodConfig.HORDES.miniHordeMinimumCooldownMinutes,
            " minutes",
            "Shortest mini-horde cooldown after full scaling."
    );

    addInt(
            settings,
            "Hordes",
            "Mini-Hordes",
            "Horde Scaling Period",
            TheFloodConfig.HORDES.miniHordeScalingDays,
            " days",
            "Number of progression days required to reach the minimum mini-horde cooldown."
    );

    addDouble(
            settings,
            "Hordes",
            "Mini-Hordes",
            "Horde Follow Range",
            TheFloodConfig.HORDES.miniHordeFollowRange,
            " blocks",
            "Distance at which mini-horde mobs can continue tracking players."
    );

    addInt(
        settings,
        "Hordes",
        "Mini-Hordes",
        "Base Horde Chance",
        TheFloodConfig.HORDES.miniHordeChance,
        "",
        "Base chance denominator used when determining mini-horde opportunities. Lower values make hordes more likely."
    );

    addDouble(
        settings,
        "Hordes",
        "Mini-Hordes",
        "Horde Chance Growth",
        TheFloodConfig.HORDES.miniHordeIncreaseRate,
        "",
        "Controls how quickly mini-horde likelihood increases as progression advances."
    );


    /*
     * =========================================================
     * BLOOD MOON — TOTAL PRESSURE
     * =========================================================
     */

    addInt(
            settings,
            "Hordes",
            "Blood Moon Limits",
            "First Blood Moon Total",
            TheFloodConfig.HORDES.bloodMoonBaseTotalMobsPerPlayer,
            " mobs/player",
            "Total enemies allowed per player during the first Blood Moon."
    );

    addInt(
            settings,
            "Hordes",
            "Blood Moon Limits",
            "Total Increase Per Blood Moon",
            TheFloodConfig.HORDES.bloodMoonTotalMobIncreasePerMoon,
            " mobs/player",
            "Additional total enemy allowance added for each later Blood Moon."
    );

    addInt(
            settings,
            "Hordes",
            "Blood Moon Limits",
            "Maximum Blood Moon Total",
            TheFloodConfig.HORDES.bloodMoonMaximumTotalMobsPerPlayer,
            " mobs/player",
            "Maximum total enemies allowed per player during a Blood Moon."
    );


    /*
     * =========================================================
     * BLOOD MOON — ACTIVE PRESSURE
     * =========================================================
     */

    addInt(
            settings,
            "Hordes",
            "Blood Moon Active Enemies",
            "First Blood Moon Active Cap",
            TheFloodConfig.HORDES.bloodMoonBaseActiveCapPerPlayer,
            " mobs/player",
            "Maximum simultaneously active Blood Moon enemies per player on the first Blood Moon."
    );

    addInt(
            settings,
            "Hordes",
            "Blood Moon Active Enemies",
            "Active Cap Increase",
            TheFloodConfig.HORDES.bloodMoonActiveCapIncreasePerMoon,
            " mobs/player",
            "Additional active-enemy allowance added for each later Blood Moon."
    );

    addInt(
            settings,
            "Hordes",
            "Blood Moon Active Enemies",
            "Maximum Active Cap",
            TheFloodConfig.HORDES.bloodMoonMaximumActiveCapPerPlayer,
            " mobs/player",
            "Maximum simultaneously active Blood Moon enemies per player."
    );

    addPercent(
            settings,
            "Hordes",
            "Blood Moon Active Enemies",
            "Wave Refill Threshold",
            TheFloodConfig.HORDES.bloodMoonRefillThreshold,
            "A new wave may refill the encounter after active population falls below this percentage."
    );


    /*
     * =========================================================
     * BLOOD MOON WAVES
     * =========================================================
     */

    addInt(
            settings,
            "Hordes",
            "Blood Moon Waves",
            "Wave Interval",
            TheFloodConfig.HORDES.bloodMoonWaveIntervalSeconds,
            " seconds",
            "Minimum real-world interval between Blood Moon waves."
    );

    addIntRange(
            settings,
            "Hordes",
            "Blood Moon Waves",
            "Wave Size",
            TheFloodConfig.HORDES.bloodMoonWaveMinSize,
            TheFloodConfig.HORDES.bloodMoonWaveMaxSize,
            " mobs/player",
            "Minimum and maximum number of enemies spawned in each Blood Moon wave."
    );

    addDouble(
            settings,
            "Hordes",
            "Blood Moon Waves",
            "Follow Range",
            TheFloodConfig.HORDES.bloodMoonFollowRange,
            " blocks",
            "Distance at which Blood Moon enemies can continue tracking players."
    );

    addInt(
            settings,
            "Hordes",
            "General",
            "Horde Clump Radius",
            TheFloodConfig.HORDES.hordeClumpRadius,
            " blocks",
            "Radius around a horde anchor where horde enemies may appear."
    );
}

private static void addMobSettings(
        List<ServerSettingEntry> settings
) {

    addStandardMob(
            settings,
            "Zombie",
            TheFloodConfig.MOBS.zombie
    );

    addStandardMob(
            settings,
            "Skeleton",
            TheFloodConfig.MOBS.skeleton
    );

    addStandardMob(
            settings,
            "Spider",
            TheFloodConfig.MOBS.spider
    );

    addStandardMob(
            settings,
            "Creeper",
            TheFloodConfig.MOBS.creeper
    );

    addStandardMob(
            settings,
            "Enderman",
            TheFloodConfig.MOBS.enderman
    );

    addStandardMob(
            settings,
            "Warden",
            TheFloodConfig.MOBS.warden
    );
}

private static void addHeatSettings(
        List<ServerSettingEntry> settings
) {

    addInt(
            settings,
            "Heat",
            "Proximity Heat",
            "Proximity Radius",
            TheFloodConfig.HEAT.proximityRadius,
            " blocks",
            "Distance within which unrelated nearby players can increase Effective Heat."
    );

    addInt(
            settings,
            "Heat",
            "Proximity Heat",
            "Heat Per Nearby Player",
            TheFloodConfig.HEAT.proximityBaseHeatPerPlayer,
            " Heat",
            "Base Effective Heat added for each unrelated nearby player."
    );

    addInt(
            settings,
            "Heat",
            "Proximity Heat",
            "Group Synergy",
            TheFloodConfig.HEAT.proximitySynergyPerPlayer,
            " Heat",
            "Additional nonlinear Heat gained as more unrelated players gather nearby."
    );
}

private static void addStandardMob(
        List<ServerSettingEntry> settings,
        String displayName,
        TheFloodConfig.StandardMob mob
) {

    addInt(
            settings,
            "Mobs",
            displayName,
            "Unlock Heat",
            mob.unlockHeat,
            "",
            "Effective Heat required before this enemy becomes available to The Flood spawning system."
    );

    addInt(
            settings,
            "Mobs",
            displayName,
            "Base Spawn Weight",
            mob.baseSpawnWeight,
            "",
            "Relative chance this enemy is selected when it first unlocks."
    );

    addDouble(
            settings,
            "Mobs",
            displayName,
            "Health at Unlock",
            mob.baseHealth,
            " HP",
            "Effective health when this enemy first unlocks."
    );

    addDouble(
            settings,
            "Mobs",
            displayName,
            "Health Growth",
            mob.healthPerDay,
            " HP/Heat",
            "Additional effective health gained as Heat progresses."
    );

    addDouble(
            settings,
            "Mobs",
            displayName,
            "Maximum Health",
            mob.maximumHealth,
            " HP",
            "Maximum effective health this enemy can reach."
    );

    addDouble(
            settings,
            "Mobs",
            displayName,
            "Damage at Unlock",
            mob.baseDamage,
            "",
            "Attack damage when this enemy first unlocks."
    );

    addDouble(
            settings,
            "Mobs",
            displayName,
            "Damage Growth",
            mob.damagePerDay,
            " /Heat",
            "Additional attack damage gained as Heat progresses."
    );

    addDouble(
            settings,
            "Mobs",
            displayName,
            "Maximum Damage",
            mob.maximumDamage,
            "",
            "Maximum attack damage this enemy can reach."
    );
}

private static void addPercent(
        List<ServerSettingEntry> settings,
        String section,
        String group,
        String name,
        ForgeConfigSpec.DoubleValue config,
        String description
) {

    String current =
            formatDouble(
                    config.get() * 100.0
            ) + "%";

    String defaults =
            formatDouble(
                    config.getDefault() * 100.0
            ) + "%";

    settings.add(
            new ServerSettingEntry(
                    section,
                    group,
                    name,
                    current,
                    defaults,
                    description
            )
    );
}

        private static void addInt(
            List<ServerSettingEntry> settings,
            String section,
            String group,
            String name,
            ForgeConfigSpec.IntValue config,
            String suffix,
            String description
    ) {

        settings.add(
                new ServerSettingEntry(
                        section,
                        group,
                        name,
                        config.get()
                                + suffix,
                        config.getDefault()
                                + suffix,
                        description
                )
        );
    }

    private static void addDouble(
            List<ServerSettingEntry> settings,
            String section,
            String group,
            String name,
            ForgeConfigSpec.DoubleValue config,
            String suffix,
            String description
    ) {

        settings.add(
                new ServerSettingEntry(
                        section,
                        group,
                        name,
                        formatDouble(
                                config.get()
                        ) + suffix,
                        formatDouble(
                                config.getDefault()
                        ) + suffix,
                        description
                )
        );
    }

    private static void addBoolean(
            List<ServerSettingEntry> settings,
            String section,
            String group,
            String name,
            ForgeConfigSpec.BooleanValue config,
            String description
    ) {

        settings.add(
                new ServerSettingEntry(
                        section,
                        group,
                        name,
                        config.get()
                                ? "Enabled"
                                : "Disabled",
                        config.getDefault()
                                ? "Enabled"
                                : "Disabled",
                        description
                )
        );
    }

    private static void addIntRange(
            List<ServerSettingEntry> settings,
            String section,
            String group,
            String name,
            ForgeConfigSpec.IntValue minimum,
            ForgeConfigSpec.IntValue maximum,
            String suffix,
            String description
    ) {

        String current =
                minimum.get()
                        + "–"
                        + maximum.get()
                        + suffix;

        String defaults =
                minimum.getDefault()
                        + "–"
                        + maximum.getDefault()
                        + suffix;

        settings.add(
                new ServerSettingEntry(
                        section,
                        group,
                        name,
                        current,
                        defaults,
                        description
                )
        );
    }

    private static String formatDouble(
            double value
    ) {

        if (
                value
                        == Math.rint(
                                value
                        )
        ) {
            return Integer.toString(
                    (int) value
            );
        }

        return String.format(
                java.util.Locale.ROOT,
                "%.2f",
                value
        ).replaceAll(
                "0+$",
                ""
        ).replaceAll(
                "\\.$",
                ""
        );
    }

}