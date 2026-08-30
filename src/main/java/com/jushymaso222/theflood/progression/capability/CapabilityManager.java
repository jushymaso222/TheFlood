package com.jushymaso222.theflood.progression.capability;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CapabilityManager {

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
     * DAMAGE
     * ============================================
     * 
    */
    public static void recordOffensiveDamage(
            ServerPlayer player,
            double damage
    ) {
        if (
                player == null
                || damage <= 0.0D
                || !Double.isFinite(damage)
        ) {
            return;
        }

        CapabilityProfile profile =
                get(player);

        if (profile == null) {
            return;
        }

        /*
        * Convert observed damage into a preliminary
        * 0-100 capability observation.
        *
        * IMPORTANT:
        * This curve is intentionally centralized here.
        * We WILL tune it from actual gameplay data.
        */
        double observation =
                damageToOffenseScore(
                        damage
                );

        profile.recordOffenseObservation(
                damage,
                observation
        );


        /*
        * Low confidence = new observations can move
        * the estimate relatively quickly.
        *
        * High confidence = established behavior moves
        * more slowly.
        */
        double confidence =
                profile.getOffenseConfidence();


        double alpha =
                0.25D
                        - (confidence * 0.15D);

        double current =
                profile.getOffenseScore();


        /*
        * Exponential moving average.
        */
        double updated =
                current
                        + alpha
                        * (observation - current);


        profile.setOffenseScore(
                updated
        );


        /*
        * Gradually become more confident as additional
        * observations arrive.
        */
        profile.setOffenseConfidence(
                Math.min(
                        1.0D,
                        confidence + 0.02D
                )
        );
    }

    private static double damageToOffenseScore(
            double damage
    ) {
        /*
        * Logarithmic scaling prevents absurd modded
        * damage values from completely destroying the
        * scale.
        *
        * This is only our INITIAL curve.
        */
        double score =
                25.0D
                        * (
                        Math.log1p(damage)
                                / Math.log(11.0D)
                );

        return Math.max(
                0.0D,
                Math.min(
                        100.0D,
                        score
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