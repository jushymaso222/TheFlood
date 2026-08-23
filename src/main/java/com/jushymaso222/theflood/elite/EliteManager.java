package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.elite.behavior.EliteMutationRegistry;
import com.jushymaso222.theflood.elite.behavior.EliteMobCompatibility;

import com.jushymaso222.theflood.elite.drops.EliteDropContext;
import com.jushymaso222.theflood.elite.drops.EliteDropResolver;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

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
        if (
                !EliteMobCompatibility.canBecomeElite(
                        mob
                )
        ) {
                return;
        }

        if (EliteData.isElite(mob)) {
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

        private static ServerPlayer findResponsiblePlayer(
        DamageSource source
) {
    Entity sourceEntity =
            source.getEntity();

    if (
            sourceEntity instanceof ServerPlayer player
    ) {
        return player;
    }

    Entity directEntity =
            source.getDirectEntity();

    if (
            directEntity instanceof Projectile projectile
            && projectile.getOwner()
                    instanceof ServerPlayer player
    ) {
        return player;
    }

    return null;
}

        public static void restoreStatusEffect(
        ServerPlayer player
) {
    if (
            !BoonData.hasActiveBoon(
                    player
            )
    ) {
        return;
    }

    String boonId =
            BoonData.getActiveBoonId(
                    player
            );

    BoonType type =
            BoonType.fromId(
                    boonId
            );

    if (type == null) {
        BoonData.clear(
                player
        );

        return;
    }

    long remainingTicks =
            BoonData.getRemainingTicks(
                    player
            );

    /*
     * It expired while the player was dead / respawning.
     */
    if (remainingTicks <= 0L) {
        deactivate(
                player
        );

        return;
    }

    MobEffect statusEffect =
            getStatusEffect(
                    type
            );

    player.addEffect(
            new MobEffectInstance(
                    statusEffect,
                    (int) Math.min(
                            Integer.MAX_VALUE,
                            remainingTicks
                    ),
                    0,
                    false,
                    false,
                    true
            )
    );
}

private static final String ELITE_DROPS_PROCESSED_KEY =
        "theflood_elite_drops_processed";

        public static void handleEliteDeath(
        Mob mob,
        DamageSource source
) {
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
            !EliteData.isElite(
                    mob
            )
    ) {
        return;
    }

    if (
            !(mob.level() instanceof ServerLevel level)
    ) {
        return;
    }

    ServerPlayer killer =
            findResponsiblePlayer(
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

    EliteDropResolver.resolve(
            context
    );
}

    public static void makeElite(
                Mob mob,
                int sourceHeat
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
                EliteMutationRegistry.randomFor(
                        mob
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

        /*
        * Roll this Elite's attributes ONCE.
        */
        List<EliteAttributes.RolledAttribute> rolledAttributes =
                EliteAttributes.rollAttributes(
                        sourceHeat
                );

        /*
        * Store them permanently on the entity.
        */
        EliteData.setAttributes(
                mob,
                EliteAttributes.serialize(
                        rolledAttributes
                )
        );

        /*
        * Apply their actual stat modifiers.
        */
        EliteAttributes.applyAll(
                mob,
                rolledAttributes
        );

        // mob.setGlowingTag(
        //         true
        // );

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