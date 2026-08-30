package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.progression.HeatManager;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.elite.behavior.EliteMutationRegistry;
import com.jushymaso222.theflood.elite.behavior.EliteMobCompatibility;
import com.jushymaso222.theflood.progression.FloodKillCredit;

import com.jushymaso222.theflood.elite.drops.EliteDropContext;
import com.jushymaso222.theflood.elite.drops.EliteDropResolver;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

import com.jushymaso222.theflood.elite.behavior.mutations.UndyingMutation;

import net.minecraft.world.entity.Mob;

import java.util.List;
import java.util.Random;

public final class EliteManager {

    private static final Random RANDOM =
            new Random();

    private EliteManager() {
    }

    public static void tryMakeElite(
        Mob mob,
        int heat,
        boolean isHordeMob
) {
    if (EliteData.isElite(mob)) {
        return;
    }

    if (
            !EliteMobCompatibility.canBecomeElite(
                    mob
            )
    ) {
        return;
    }

    double chance =
            getEliteChance(
                    heat
            );

    if (
            RANDOM.nextDouble()
                    >= chance
    ) {
        return;
    }

    makeElite(
            mob,
            heat,
            isHordeMob
    );
}

public static void tryMakeElite(
        Mob mob,
        int heat
) {
    tryMakeElite(
            mob,
            heat,
            false
    );
}

private static final String ELITE_DROPS_PROCESSED_KEY =
        "theflood_elite_drops_processed";

        public static void handleEliteDeath(
        Mob mob,
        DamageSource source
) {
        if (
                !EliteData.isElite(
                        mob
                )
        ) {
                return;
        }

        /*
        * Undying's first lethal hit is not a real death.
        *
        * The elite must complete its Undying death sequence
        * before loot and Flood XP are allowed to process.
        */
        if (
                "undying".equals(
                        EliteData.getMutation(
                                mob
                        )
                )
                && !UndyingMutation.isFinalDeath(
                        mob
                )
        ) {
                return;
        }

        /*
        * Only mark rewards as processed AFTER we've confirmed
        * this is a legitimate final death.
        */
        if (
                mob.getPersistentData()
                        .getBoolean(
                                ELITE_DROPS_PROCESSED_KEY
                        )
        ) {
                return;
        }

        mob.getPersistentData()
                .putBoolean(
                        ELITE_DROPS_PROCESSED_KEY,
                        true
                );

        if (
                !(mob.level() instanceof ServerLevel level)
        ) {
                return;
        }

        ServerPlayer killer =
                FloodKillCredit.findResponsiblePlayer(
                        source
                );

    EliteDropContext context =
            new EliteDropContext(
                    level,
                    mob,
                    killer,
                    source,
                    EliteData.getSourceHeat(
                            mob
                    )
            );

    double basePercent =
        TheFloodConfig.ELITES
                .eliteKillFloodXpPercent
                .get();

double maxDangerBonus =
        TheFloodConfig.ELITES
                .eliteDangerXpBonusPercent
                .get();

double dangerPercent =
        EliteData.getDanger(mob)
                / 100.0D;

double totalPercent =
        basePercent
                + (
                maxDangerBonus
                        * dangerPercent
                );

    if (killer != null) {
        long eliteXp =
        Math.max(
                1L,
                Math.round(
                        HeatManager.getFloodXpRequired(
                                killer
                        )
                                * totalPercent
                )
        );

        HeatManager.addFloodXp(
                killer,
                eliteXp
        );

        HeatManager.addRewardFloodXp(
                killer,
                eliteXp
        );
    }

    EliteDropResolver.resolve(
            context
    );
}

public static void makeElite(
        Mob mob,
        int sourceHeat
) {
    makeElite(
            mob,
            sourceHeat,
            false
    );
}

    public static void makeElite(
        Mob mob,
        int sourceHeat,
        boolean isHordeMob
) {
    EliteData.setElite(
            mob,
            true
    );

    EliteData.setSourceHeat(
            mob,
            sourceHeat
    );

    EliteMutation mutation =
            EliteMutationRegistry.randomFor(
                    mob,
                    isHordeMob
            );

    if (mutation != null) {
        EliteData.setMutation(
                mob,
                mutation.id()
        );
    } else {
        EliteData.setMutation(
                mob,
                "none"
        );
    }

    List<EliteAttributes.RolledAttribute> rolledAttributes =
            EliteAttributes.rollAttributes(
                    sourceHeat
            );

    EliteData.setAttributes(
            mob,
            EliteAttributes.serialize(
                    rolledAttributes
            )
    );

    EliteAttributes.applyAll(
            mob,
            rolledAttributes
    );

    int danger =
                EliteDanger.calculate(
                        sourceHeat,
                        rolledAttributes
                );

        EliteData.setDanger(
                mob,
                danger
        );

    EliteStateSync.syncBasic(
            mob
    );
}

    public static void makeElite(
        Mob mob,
        int sourceHeat,
        String mutationId,
        List<String> attributes
) {
        if (
                !EliteMobCompatibility.canBecomeElite(
                        mob
                )
        ) {
        return;
        }

    EliteData.setElite(
            mob,
            true
    );

    EliteData.setSourceHeat(
            mob,
            sourceHeat
    );

    EliteMutation mutation =
        null;

        if (
                mutationId != null
                && !mutationId.isBlank()
        ) {
        EliteMutation requested =
                EliteMutationRegistry.get(
                        mutationId
                );

        if (
                requested != null
                && requested.canApplyTo(
                        mob
                )
        ) {
                mutation =
                        requested;
        }
        }

        /*
        * No mutation supplied, or the supplied mutation
        * doesn't exist.
        *
        * Pick a real mutation instead.
        */
        if (mutation == null) {
                mutation =
                        EliteMutationRegistry.randomFor(
                                mob
                        );
                }

        if (mutation != null) {
        EliteData.setMutation(
                mob,
                mutation.id()
        );
        } else {
        EliteData.setMutation(
                mob,
                "none"
        );
        }

    List<String> storedAttributes;

        if (
                attributes == null
                || attributes.isEmpty()
        ) {
        storedAttributes =
                EliteAttributes.serialize(
                        EliteAttributes.rollAttributes(
                                sourceHeat
                        )
                );
        } else {
        storedAttributes =
                EliteAttributes.normalizeStoredAttributes(
                        attributes
                );
        }

    EliteData.setAttributes(
            mob,
            storedAttributes
    );

    EliteAttributes.applyAll(
            mob
    );

//     mob.setGlowingTag(
//             true
//     );

    EliteStateSync.syncBasic(
            mob
    );
}

        public static int getScalingHeat(
                Mob mob,
                int normalHeat
        ) {
        if (!EliteData.isElite(mob)) {
                return normalHeat;
        }

        int sourceHeat =
                EliteData.getSourceHeat(
                        mob
                );

        return sourceHeat
                + TheFloodConfig.ELITES
                        .eliteHeatBonus
                        .get();
        }

    public static void applyEliteVisual(
            Mob mob
    ) {
        if (
                EliteData.isElite(
                        mob
                )
        ) {
        //     mob.setGlowingTag(
        //             true
        //     );
        }
    }

    private static double getEliteChance(
            int heat
    ) {
        double baseChance =
                TheFloodConfig.ELITES
                        .baseEliteChance
                        .get();

        double chancePerHeat =
                TheFloodConfig.ELITES
                        .eliteChancePerHeat
                        .get();

        double maxChance =
                TheFloodConfig.ELITES
                        .maximumEliteChance
                        .get();

        double chance =
                baseChance
                        + (
                        heat
                                * chancePerHeat
                );

        return Math.min(
                chance,
                maxChance
        );
    }
}