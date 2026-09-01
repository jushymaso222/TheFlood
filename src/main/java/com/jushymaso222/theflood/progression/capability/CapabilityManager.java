package com.jushymaso222.theflood.progression.capability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;

public final class CapabilityManager {

        /*
     * ============================================
     * FLOOD RESPONSE
     * ============================================
     *
     * Capability scores describe what the player
     * has demonstrated they can actually handle.
     *
     * These methods convert capability ABOVE the
     * expected vanilla range into additional Flood
     * pressure.
     *
     * Capability can only increase difficulty.
     * It can never reduce the normal Heat curve.
     */


    /*
     * Temporary vanilla reference points.
     *
     * These are intentionally kept here rather than
     * buried inside the sensors. Sensors measure.
     * This layer decides what those measurements mean
     * for difficulty.
     *
     * We'll calibrate these from vanilla testing.
     */
    private static final double VANILLA_OFFENSE_REFERENCE =
            30.0D;

    private static final double VANILLA_DEFENSE_REFERENCE =
            75.0D;

    private static final double VANILLA_SURVIVAL_REFERENCE =
            50.0D;

    private static final double VANILLA_MOBILITY_REFERENCE =
            55.0D;


    /*
     * We don't let uncertain observations affect
     * difficulty.
     */
    private static final double MIN_RESPONSE_CONFIDENCE =
            0.20D;


    public static double getOffenseResponse(
            ServerPlayer player
    ) {
        CapabilityProfile profile =
                get(player);

        if (profile == null) {
            return 0.0D;
        }

        return calculateResponse(
                profile.getEffectiveOffense(),
                profile.getOffenseConfidence(),
                VANILLA_OFFENSE_REFERENCE
        );
    }


    public static double getDefenseResponse(
            ServerPlayer player
    ) {
        CapabilityProfile profile =
                get(player);

        if (profile == null) {
            return 0.0D;
        }

        return calculateResponse(
                profile.getEffectiveDefense(),
                profile.getDefenseConfidence(),
                VANILLA_DEFENSE_REFERENCE
        );
    }


    public static double getSurvivalResponse(
            ServerPlayer player
    ) {
        CapabilityProfile profile =
                get(player);

        if (profile == null) {
            return 0.0D;
        }

        return calculateResponse(
                profile.getEffectiveSurvival(),
                profile.getSurvivalConfidence(),
                VANILLA_SURVIVAL_REFERENCE
        );
    }


    public static double getMobilityResponse(
            ServerPlayer player
    ) {
        CapabilityProfile profile =
                get(player);

        if (profile == null) {
            return 0.0D;
        }

        return calculateResponse(
                profile.getEffectiveMobility(),
                profile.getMobilityConfidence(),
                VANILLA_MOBILITY_REFERENCE
        );
    }


    private static double calculateResponse(
            double capability,
            double confidence,
            double vanillaReference
    ) {
        if (
                !Double.isFinite(capability)
                        || !Double.isFinite(confidence)
                        || confidence < MIN_RESPONSE_CONFIDENCE
        ) {
            return 0.0D;
        }


        /*
         * Anything at or below the vanilla reference
         * receives absolutely no difficulty adjustment.
         */
        if (capability <= vanillaReference) {
            return 0.0D;
        }


        /*
         * Normalize the portion of the capability scale
         * above the vanilla reference.
         *
         * Example with reference = 40:
         *
         * capability 40 -> excess 0.00
         * capability 55 -> excess 0.25
         * capability 70 -> excess 0.50
         * capability 85 -> excess 0.75
         * capability100 -> excess 1.00
         */
        double availableRange =
                100.0D - vanillaReference;

        if (availableRange <= 0.0D) {
            return 0.0D;
        }

        double excess =
                (capability - vanillaReference)
                        / availableRange;

        excess =
                Math.max(
                        0.0D,
                        Math.min(
                                1.0D,
                                excess
                        )
                );


        /*
         * Partial compensation.
         *
         * The Flood reacts to excessive player power,
         * but deliberately does NOT completely cancel
         * that power.
         *
         * This curve produces approximately:
         *
         * excess 0.25 ->  8%
         * excess 0.50 -> 17%
         * excess 0.75 -> 27%
         * excess 1.00 -> 40%
         */
        double response =
                0.40D
                        * Math.pow(
                                excess,
                                1.25D
                        );


        /*
         * Final safety clamp.
         *
         * Response is always:
         *
         * 0.00 = no additional difficulty
         * 0.40 = maximum +40% adjustment
         */
        return Math.max(
                0.0D,
                Math.min(
                        0.40D,
                        response
                )
        );
    }

    /*
     * ============================================
     * LIVE PROFILES
     * ============================================
     *
     * CapabilityData is our persistent storage.
     *
     * This map is the live runtime representation
     * used while players are online.
     *
     * We do NOT want to deserialize NBT every time
     * a combat event occurs.
     */

    private static final Map<UUID, CapabilityProfile> PROFILES =
            new HashMap<>();


    private CapabilityManager() {
    }

    /*
 * ============================================
 * OFFENSE
 * ============================================
 */

public static void recordOffenseObservation(
        ServerPlayer player,
        double rawDamage,
        double observation
) {
    if (
            player == null
            || !Double.isFinite(rawDamage)
            || rawDamage <= 0.0D
            || !Double.isFinite(observation)
    ) {
        return;
    }

    CapabilityProfile profile =
            get(
                    player
            );

    if (profile == null) {
        return;
    }

    profile.recordOffenseObservation(
            rawDamage,
            observation
    );

    double confidence =
            profile.getOffenseConfidence();

    profile.setOffenseScore(
            updateScore(
                    profile.getOffenseScore(),
                    observation,
                    confidence
            )
    );

    profile.setOffenseConfidence(
            increaseConfidence(
                    confidence,
                    0.02D
            )
    );
}




    /*
     * ============================================
     * GET
     * ============================================
     *
     * This is the main entry point other systems
     * should use to access a player's capability
     * profile.
     *
     * If the profile isn't currently loaded, it
     * will be loaded from persistent data.
     */

    public static CapabilityProfile get(
            ServerPlayer player
    ) {
        if (player == null) {
            return null;
        }

        UUID playerId =
                player.getUUID();

        CapabilityProfile existing =
                PROFILES.get(
                        playerId
                );

        if (existing != null) {
            return existing;
        }

        return load(
                player
        );
    }


    /*
     * ============================================
     * LOAD
     * ============================================
     *
     * Loads the player's persistent capability
     * data into the live runtime cache.
     */

    public static CapabilityProfile load(
            ServerPlayer player
    ) {
        if (player == null) {
            return null;
        }

        CapabilityProfile profile =
                CapabilityData.get(
                        player
                );

        PROFILES.put(
                player.getUUID(),
                profile
        );

        return profile;
    }


    /*
     * ============================================
     * SAVE
     * ============================================
     *
     * Writes the live profile back into the
     * player's persistent NBT.
     */

    public static void save(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        CapabilityProfile profile =
                PROFILES.get(
                        player.getUUID()
                );

        if (profile == null) {
            return;
        }

        CapabilityData.save(
                player,
                profile
        );
    }


    /*
     * ============================================
     * SAVE AND UNLOAD
     * ============================================
     *
     * Used when a player leaves the server.
     *
     * Save first, then remove the live object so
     * we don't retain profiles for offline players.
     */

    public static void saveAndUnload(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        save(
                player
        );

        PROFILES.remove(
                player.getUUID()
        );
    }


    /*
     * ============================================
     * UNLOAD
     * ============================================
     *
     * Removes a profile without saving it.
     *
     * This is intentionally separate from
     * saveAndUnload because there are lifecycle
     * situations where we've already handled
     * persistence ourselves.
     */

    public static void unload(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        PROFILES.remove(
                player.getUUID()
        );
    }


    /*
     * ============================================
     * CLONE
     * ============================================
     *
     * Minecraft creates a new player entity after
     * death.
     *
     * CapabilityData handles copying the persistent
     * NBT. We then replace the cached profile with
     * one loaded from the new player entity.
     */

    public static void handleClone(
            ServerPlayer oldPlayer,
            ServerPlayer newPlayer
    ) {
        if (
                oldPlayer == null
                || newPlayer == null
        ) {
            return;
        }


        /*
         * Make sure the latest runtime state is
         * written onto the old player before we
         * copy its persistent data.
         */
        save(
                oldPlayer
        );


        /*
         * Copy persistent capability data from the
         * old player entity to the new one.
         */
        CapabilityData.copy(
                oldPlayer,
                newPlayer
        );


        /*
         * Remove the old runtime entry.
         *
         * UUID will normally be identical, but
         * doing this explicitly keeps the lifecycle
         * clear.
         */
        PROFILES.remove(
                oldPlayer.getUUID()
        );


        /*
         * Load a fresh runtime profile from the new
         * player entity.
         */
        load(
                newPlayer
        );
    }

    public static void recordDefenseObservation(
        ServerPlayer player,
        double observation,
        double weight
) {
    if (
            player == null
                    || !Double.isFinite(observation)
                    || !Double.isFinite(weight)
    ) {
        return;
    }

    weight =
            Math.max(
                    0.0D,
                    Math.min(
                            1.0D,
                            weight
                    )
            );

    if (weight <= 0.0D) {
        return;
    }


    CapabilityProfile profile =
            get(
                    player
            );

    if (profile == null) {
        return;
    }


    /*
     * Store the actual observed defensive capability,
     * NOT the weighted value.
     */
    profile.recordDefenseObservation(
            observation
    );


    double confidence =
            profile.getDefenseConfidence();


    /*
     * Calculate the normal learning rate, then scale
     * how strongly this particular hit can influence
     * the learned Defense score.
     */
    double alpha =
            (
                    0.25D
                            - confidence * 0.15D
            )
                    * weight;


    observation =
            Math.max(
                    0.0D,
                    Math.min(
                            100.0D,
                            observation
                    )
            );


    profile.setDefenseScore(
            profile.getDefenseScore()
                    + alpha
                    * (
                            observation
                                    - profile.getDefenseScore()
                    )
    );


    /*
     * Strong attacks also teach us faster.
     *
     * Weak attacks still contribute information,
     * just much less of it.
     */
    profile.setDefenseConfidence(
            increaseConfidence(
                    confidence,
                    0.02D * weight
            )
    );
}


public static void recordSurvivalObservation(
        ServerPlayer player,
        double observation
) {
    CapabilityProfile profile =
            get(
                    player
            );

    if (profile == null) {
        return;
    }

    profile.recordSurvivalObservation(
            observation
    );

    double confidence =
            profile.getSurvivalConfidence();

    profile.setSurvivalScore(
            updateScore(
                    profile.getSurvivalScore(),
                    observation,
                    confidence
            )
    );

    profile.setSurvivalConfidence(
            increaseConfidence(
                    confidence,
                    0.02D
            )
    );
}


public static void recordMobilityObservation(
        ServerPlayer player,
        double observation
) {
    CapabilityProfile profile =
            get(
                    player
            );

    if (profile == null) {
        return;
    }

    profile.recordMobilityObservation(
            observation
    );

    double confidence =
            profile.getMobilityConfidence();

    profile.setMobilityScore(
            updateScore(
                    profile.getMobilityScore(),
                    observation,
                    confidence
            )
    );

    profile.setMobilityConfidence(
            increaseConfidence(
                    confidence,
                    0.005D
            )
    );
}


private static double updateScore(
        double current,
        double observation,
        double confidence
) {
    if (!Double.isFinite(observation)) {
        return current;
    }

    observation =
            Math.max(
                    0.0D,
                    Math.min(
                            100.0D,
                            observation
                    )
            );

    double alpha =
            0.25D
                    - confidence * 0.15D;

    return current
            + alpha
            * (
            observation
                    - current
    );
}


private static double increaseConfidence(
        double confidence,
        double amount
) {
    return Math.min(
            1.0D,
            confidence + amount
    );
}


    /*
     * ============================================
     * RESET
     * ============================================
     *
     * Primarily for development/debugging.
     *
     * Clears both the live profile and persistent
     * capability data.
     */

    public static void reset(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        PROFILES.remove(
                player.getUUID()
        );

        CapabilityData.reset(
                player
        );
    }


    /*
     * ============================================
     * DEBUG / UTILITY
     * ============================================
     */

    public static boolean isLoaded(
            ServerPlayer player
    ) {
        return player != null
                && PROFILES.containsKey(
                        player.getUUID()
                );
    }

    public static int getLoadedProfileCount() {
        return PROFILES.size();
    }
}