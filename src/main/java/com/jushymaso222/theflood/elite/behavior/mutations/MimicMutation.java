package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.elite.behavior.EliteMutationRegistry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;

import com.jushymaso222.theflood.elite.behavior.EliteMobCompatibility;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.jushymaso222.theflood.elite.presentation.EliteSounds;
import com.jushymaso222.theflood.elite.presentation.EliteVisuals;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MimicMutation
        implements EliteMutation {

    /*
     * Whether the Mimic has been revealed.
     *
     * false:
     * looks/acts like an ordinary Flood mob
     *
     * true:
     * actively fighting and copying mutations
     */
    private static final String ACTIVE_KEY =
            "theflood_mimic_active";

    /*
     * Mutation currently being copied.
     *
     * IMPORTANT:
     * EliteData itself still says "mimic".
     */
    private static final String COPIED_MUTATION_KEY =
            "theflood_mimic_copied_mutation";

    /*
     * Player who woke this Mimic.
     */
    private static final String TARGET_UUID_KEY =
            "theflood_mimic_target";

    /*
     * Next time the Mimic is allowed to change forms.
     */
    private static final String NEXT_SHIFT_KEY =
            "theflood_mimic_next_shift";

    /*
     * How long the Mimic remains without a valid target
     * before returning to hiding.
     */
    private static final String LOST_TARGET_TIME_KEY =
            "theflood_mimic_lost_target_time";

    /*
     * First test value:
     * change copied mutation every 10 seconds.
     */
    private static final int MUTATION_DURATION_TICKS =
            400;

    /*
     * Don't instantly hide because vanilla AI briefly
     * loses the target behind a wall.
     *
     * 100 ticks = 5 seconds.
     */
    private static final int DEAGGRO_GRACE_TICKS =
            100;

    /*
     * Maximum distance at which the Mimic considers
     * the encounter still active.
     */
    private static final double MAX_COMBAT_DISTANCE =
            64.0D;

    private static final UUID MIMIC_HEALTH_MODIFIER_ID =
            UUID.fromString(
                    "6a37d7f6-53ce-4e40-82b4-f2a808be2189"
            );

    private static final double MIMIC_HEALTH_BONUS =
            2.0D;

    @Override
    public String id() {
        return "mimic";
    }

    @Override
    public String displayName() {
        return "Mimic";
    }

    @Override
        public boolean canApplyTo(
                Mob mob
        ) {
        return EliteMobCompatibility.isZombie(
                mob
        )
                || EliteMobCompatibility.isSkeleton(
                        mob
                );
        }

    private static void ensureMimicHealthBonus(
            Mob elite
    ) {
        AttributeInstance maxHealth =
                elite.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (
                maxHealth == null
                || maxHealth.getModifier(
                        MIMIC_HEALTH_MODIFIER_ID
                ) != null
        ) {
            return;
        }

        float healthPercent =
                elite.getHealth()
                        / elite.getMaxHealth();

        maxHealth.addPermanentModifier(
                new AttributeModifier(
                        MIMIC_HEALTH_MODIFIER_ID,
                        "The Flood Mimic health bonus",
                        MIMIC_HEALTH_BONUS,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );

        elite.setHealth(
                elite.getMaxHealth()
                        * healthPercent
        );
    }

    @Override
    public void tick(
            Mob elite
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        ensureMimicHealthBonus(
                elite
        );

        if (!isActive(elite)) {
            tickDormant(
                    elite
            );

            return;
        }

        tickActive(
                level,
                elite
        );
    }

    /*
     * Dormant Mimics do absolutely nothing hostile.
     *
     * This is the giveaway:
     * hostile Flood mob + zero interest in the player.
     */
    private static void tickDormant(
            Mob elite
    ) {
        elite.setTarget(
                null
        );

        elite.getNavigation()
                .stop();

        // elite.setGlowingTag(
        //         false
        // );

        elite.setCustomNameVisible(
                false
        );
    }

    private static void tickActive(
            ServerLevel level,
            Mob elite
    ) {
        ServerPlayer target =
                getStoredTarget(
                        level,
                        elite
                );

        /*
         * Player died, logged out, changed dimension,
         * or otherwise disappeared.
         */
        if (target == null) {
            beginLostTarget(
                    level,
                    elite
            );

            return;
        }

        /*
        * Killing the target immediately ends the encounter.
        *
        * No grace period here — the Mimic won.
        */
        if (
                !target.isAlive()
                || target.isSpectator()
                || target.serverLevel() != level
        ) {
            hide(
                    elite
            );

            return;
        }

        /*
         * Player successfully escaped.
         */
        if (
                elite.distanceToSqr(
                        target
                )
                        > MAX_COMBAT_DISTANCE
                        * MAX_COMBAT_DISTANCE
        ) {
            beginLostTarget(
                    level,
                    elite
            );

            return;
        }

        /*
         * We have a valid quarry again.
         * Cancel any pending hide timer.
         */
        elite.getPersistentData()
                .putLong(
                        LOST_TARGET_TIME_KEY,
                        0L
                );

        /*
         * Keep the Mimic engaged.
         */
        if (
                elite.getTarget()
                        != target
        ) {
            elite.setTarget(
                    target
            );
        }

        /*
         * Run whatever mutation we're currently copying.
         */
        EliteMutation copied =
                getCopiedMutation(
                        elite
                );

        if (copied == null) {
            chooseNewMutation(
                    elite
            );

            copied =
                    getCopiedMutation(
                            elite
                    );
        }

        if (copied != null) {
            copied.tick(
                    elite
            );
        }

        long gameTime =
                level.getGameTime();

        long nextShift =
                elite.getPersistentData()
                        .getLong(
                                NEXT_SHIFT_KEY
                        );

        if (nextShift <= 0L) {
            nextShift =
                    gameTime
                            + MUTATION_DURATION_TICKS;

            elite.getPersistentData()
                    .putLong(
                            NEXT_SHIFT_KEY,
                            nextShift
                    );
        }

        /*
         * Certain mutation states should eventually be
         * allowed to lock transformation temporarily.
         *
         * UNDying execution phase will be one of those.
         *
         * For the first test we simply swap normally.
         */
        if (gameTime >= nextShift) {
            chooseNewMutation(
                    elite
            );

            return;
        }
    }

    public static boolean isRevealed(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getBoolean(
                        ACTIVE_KEY
                );
    }

    public static String getCopiedMutationId(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getString(
                        COPIED_MUTATION_KEY
                );
    }

    @Override
    public float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        /*
         * The first player attack wakes the Mimic.
         */
        if (!isActive(elite)) {
            ServerPlayer attacker =
                    findResponsiblePlayer(
                            source
                    );

            if (attacker != null) {
                reveal(
                        elite,
                        attacker
                );
            }

            /*
             * The waking hit still deals normal damage.
             */
            return damage;
        }

        EliteMutation copied =
                getCopiedMutation(
                        elite
                );

        if (copied == null) {
            return damage;
        }

        return copied.modifyIncomingDamage(
                elite,
                source,
                damage
        );
    }

    @Override
    public float modifyOutgoingDamage(
            Mob elite,
            float damage
    ) {
        if (!isActive(elite)) {
            return damage;
        }

        EliteMutation copied =
                getCopiedMutation(
                        elite
                );

        if (copied == null) {
            return damage;
        }

        return copied.modifyOutgoingDamage(
                elite,
                damage
        );
    }

    @Override
    public boolean handleLethalDamage(
            Mob elite,
            float incomingDamage
    ) {
        if (!isActive(elite)) {
            return false;
        }

        EliteMutation copied =
                getCopiedMutation(
                        elite
                );

        if (copied == null) {
            return false;
        }

        return copied.handleLethalDamage(
                elite,
                incomingDamage
        );
    }

    /*
     * Player has made the terrible decision.
     */
    private static void reveal(
            Mob elite,
            ServerPlayer attacker
    ) {
        elite.getPersistentData()
                .putBoolean(
                        ACTIVE_KEY,
                        true
                );

        elite.getPersistentData()
                .putUUID(
                        TARGET_UUID_KEY,
                        attacker.getUUID()
                );

        elite.getPersistentData()
                .putLong(
                        LOST_TARGET_TIME_KEY,
                        0L
                );

        // elite.setGlowingTag(
        //         true
        // );

        elite.setTarget(
                attacker
        );

        chooseNewMutation(
                elite
        );
    }

    private static void chooseNewMutation(
            Mob elite
    ) {
        EliteMutation current =
                getCopiedMutation(
                        elite
                );

        if (
                current != null
                && !current.canDeactivate(
                        elite
                )
        ) {
            /*
            * Try again shortly rather than transforming.
            */
            elite.getPersistentData()
                    .putLong(
                            NEXT_SHIFT_KEY,
                            elite.level()
                                    .getGameTime()
                                    + 20L
                    );

            return;
        }

        EliteMutation oldMutation =
                getCopiedMutation(
                        elite
                );

        if (oldMutation != null) {
            oldMutation.onDeactivated(
                    elite
            );
        }

        List<EliteMutation> choices =
                getMimicableMutations(
                        elite
                );

        if (
                oldMutation != null
                && choices.size() > 1
        ) {
        choices.removeIf(
                mutation ->
                        mutation.id()
                                .equals(
                                        oldMutation.id()
                                )
        );
        }

        if (choices.isEmpty()) {
            elite.getPersistentData()
                    .remove(
                            COPIED_MUTATION_KEY
                    );

            return;
        }

        EliteMutation selected =
                choices.get(
                        elite.getRandom()
                                .nextInt(
                                        choices.size()
                                )
                );

        /*
        * We already deactivated the old mutation above.
        *
        * Only play the transformation effect if we're actually
        * switching to a different behavior.
        */
        String oldId =
                oldMutation != null
                        ? oldMutation.id()
                        : "";

        String newId =
                selected.id();

        if (
                !newId.equals(
                        oldId
                )
        ) {
        playMutationShiftEffect(
                elite
        );
        }

        elite.getPersistentData()
                .putString(
                        COPIED_MUTATION_KEY,
                        newId
                );

        selected.onActivated(
                elite
        );

        EliteStateSync.syncBasic(
                elite
        );

        elite.getPersistentData()
                .putLong(
                        NEXT_SHIFT_KEY,
                        elite.level()
                                .getGameTime()
                                + MUTATION_DURATION_TICKS
                );

        /*
        * Synced display state for the client renderer.
        */
        elite.setCustomNameVisible(
                false
        );
    }

    private static List<EliteMutation> getMimicableMutations(
                Mob elite
        ) {
        List<EliteMutation> choices =
                new ArrayList<>();

        for (
                EliteMutation mutation :
                EliteMutationRegistry.validFor(
                        elite
                )
        ) {
                if (
                        mutation == null
                        || !mutation.canBeMimicked()
                ) {
                continue;
                }

                choices.add(
                        mutation
                );
        }

        return choices;
        }

    private static EliteMutation getCopiedMutation(
            Mob elite
    ) {
        String id =
                elite.getPersistentData()
                        .getString(
                                COPIED_MUTATION_KEY
                        );

        if (
                id == null
                || id.isBlank()
        ) {
            return null;
        }

        EliteMutation mutation =
                EliteMutationRegistry.get(
                        id
                );

        /*
         * Safety against Mimic recursively copying Mimic.
         */
        if (
                mutation != null
                && "mimic".equals(
                        mutation.id()
                )
        ) {
            return null;
        }

        return mutation;
    }

    private static boolean isActive(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getBoolean(
                        ACTIVE_KEY
                );
    }

    private static void beginLostTarget(
            ServerLevel level,
            Mob elite
    ) {
        long lostSince =
                elite.getPersistentData()
                        .getLong(
                                LOST_TARGET_TIME_KEY
                        );

        if (lostSince <= 0L) {
            elite.getPersistentData()
                    .putLong(
                            LOST_TARGET_TIME_KEY,
                            level.getGameTime()
                    );

            return;
        }

        if (
                level.getGameTime()
                        - lostSince
                        < DEAGGRO_GRACE_TICKS
        ) {
            return;
        }

        hide(
                elite
        );
    }

    private static void hide(
            Mob elite
    ) {
        EliteMutation copied =
                getCopiedMutation(
                        elite
                );

        if (copied != null) {
            copied.onDeactivated(
                    elite
            );
        }

        elite.getPersistentData()
                .putBoolean(
                        ACTIVE_KEY,
                        false
                );

        elite.getPersistentData()
                .remove(
                        TARGET_UUID_KEY
                );

        elite.getPersistentData()
                .remove(
                        COPIED_MUTATION_KEY
                );

        elite.getPersistentData()
                .putLong(
                        NEXT_SHIFT_KEY,
                        0L
                );

        elite.getPersistentData()
                .putLong(
                        LOST_TARGET_TIME_KEY,
                        0L
                );

        elite.setTarget(
                null
        );

        elite.setLastHurtByMob(
                null
        );

        elite.getNavigation()
                .stop();

        // elite.setGlowingTag(
        //         false
        // );

        elite.setCustomNameVisible(
                false
        );

        /*
         * Eventually the client should completely remove
         * the Mimic overhead display here.
         */
        EliteStateSync.syncBasic(
                elite
        );
    }

    private static void playMutationShiftEffect(
                Mob elite
        ) {
        EliteVisuals.burst(
                elite,
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                18,
                0.55D,
                0.025D
        );

        EliteVisuals.burst(
                elite,
                ParticleTypes.POOF,
                12,
                0.45D,
                0.04D
        );

        EliteSounds.playRandomPitch(
                elite,
                SoundEvents.ILLUSIONER_MIRROR_MOVE,
                0.9F,
                1.05F,
                0.06F
        );
        }

    private static ServerPlayer getStoredTarget(
            ServerLevel level,
            Mob elite
    ) {
        if (
                !elite.getPersistentData()
                        .hasUUID(
                                TARGET_UUID_KEY
                        )
        ) {
            return null;
        }

        UUID targetId =
                elite.getPersistentData()
                        .getUUID(
                                TARGET_UUID_KEY
                        );

        return level.getServer()
                .getPlayerList()
                .getPlayer(
                        targetId
                );
    }

    private static ServerPlayer findResponsiblePlayer(
            DamageSource source
    ) {
        Entity sourceEntity =
                source.getEntity();

        if (sourceEntity instanceof ServerPlayer player) {
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

    @Override
    public boolean canBeMimicked() {
        return false;
    }

    // private static void syncState(
    //         Mob elite,
    //         long gameTime,
    //         long nextShift
    // ) {
    //     float remaining =
    //             Math.max(
    //                     0.0F,
    //                     nextShift
    //                             - gameTime
    //             );

    //     EliteStateSync.sync(
    //             elite,
    //             remaining,
    //             MUTATION_DURATION_TICKS,
    //             true,
    //             STATUS_COLOR
    //     );
    // }
}