package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.elite.behavior.EliteMutationRegistry;
import net.minecraft.world.entity.monster.Creeper;

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
                int heat
        ) {
        if (EliteData.isElite(mob)) {
                return;
        }

        //Creepers are exempt temporarily
        if (mob instanceof Creeper) {
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
                heat
        );
        }

    public static void makeElite(
                Mob mob,
                int sourceHeat
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
                EliteMutationRegistry.random();

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

        EliteData.setAttributes(
                mob,
                List.of()
        );

        mob.setGlowingTag(
                true
        );
        }

    public static void makeElite(
        Mob mob,
        int sourceHeat,
        String mutationId,
        List<String> attributes
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
            EliteMutationRegistry.get(
                    mutationId
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

    EliteData.setAttributes(
            mob,
            attributes
    );

    mob.setGlowingTag(
            true
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
            mob.setGlowingTag(
                    true
            );
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