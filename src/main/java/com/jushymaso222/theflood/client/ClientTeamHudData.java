package com.jushymaso222.theflood.client;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.jushymaso222.theflood.config.TheFloodClientConfig;

public final class ClientTeamHudData {

    /*
     * =========================================================
     * HUD DISPLAY SETTINGS
     * =========================================================
     */

    public static final int PAGE_SIZE = 4;
    public static final int MAX_FAVORITES = 4;

    private static boolean favoritesLoaded =
        false;

    private static void ensureFavoritesLoaded() {
        if (favoritesLoaded) {
            return;
        }

        loadFavoritesFromConfig();

        favoritesLoaded =
                true;
    }

    public enum HudMode {
        MANUAL,
        NEAREST,
        FAVORITES
    }

    private static HudMode hudMode =
            HudMode.MANUAL;

    private static int currentPage =
            0;




    /*
     * =========================================================
     * TEAMMATES
     * =========================================================
     */

    public record Teammate(
            UUID playerId,
            String name,
            float health,
            float maxHealth,
            double x,
            double y,
            double z,
            String dimension
    ) {
    }

    private static final List<Teammate> TEAMMATES =
            new ArrayList<>();


    /*
     * =========================================================
     * FAVORITES
     * =========================================================
     */

    private static final List<UUID> FAVORITES =
            new ArrayList<>();


    private ClientTeamHudData() {
    }


    /*
     * =========================================================
     * TEAMMATE DATA
     * =========================================================
     */

    public static void setTeammates(
            List<Teammate> teammates
    ) {
        TEAMMATES.clear();
        TEAMMATES.addAll(teammates);

        /*
         * If somebody leaves and the current page no
         * longer exists, move back to the final valid page.
         */
        int pageCount =
                getPageCount();

        if (currentPage >= pageCount) {
            currentPage =
                    Math.max(
                            0,
                            pageCount - 1
                    );
        }
    }

    public static List<Teammate> getTeammates() {
        return Collections.unmodifiableList(
                TEAMMATES
        );
    }


    /*
     * =========================================================
     * HUD MODE
     * =========================================================
     */

    public static HudMode getHudMode() {
        return hudMode;
    }

    public static void setHudMode(
            HudMode mode
    ) {
        hudMode =
                mode;
    }


    /*
     * =========================================================
     * MANUAL PAGING
     * =========================================================
     */

    public static int getCurrentPage() {
        return currentPage;
    }

    public static int getPageCount() {
        if (TEAMMATES.isEmpty()) {
            return 1;
        }

        return Math.max(
                1,
                (TEAMMATES.size()
                        + PAGE_SIZE
                        - 1)
                        / PAGE_SIZE
        );
    }

    public static void nextPage() {
        hudMode =
                HudMode.MANUAL;

        currentPage =
                (currentPage + 1)
                        % getPageCount();
    }

    public static void previousPage() {
        hudMode =
                HudMode.MANUAL;

        currentPage =
                Math.floorMod(
                        currentPage - 1,
                        getPageCount()
                );
    }

    private static List<Teammate> getManualPage() {
        if (TEAMMATES.isEmpty()) {
            return List.of();
        }

        int start =
                currentPage
                        * PAGE_SIZE;

        if (start >= TEAMMATES.size()) {
            return List.of();
        }

        int end =
                Math.min(
                        start + PAGE_SIZE,
                        TEAMMATES.size()
                );

        return new ArrayList<>(
                TEAMMATES.subList(
                        start,
                        end
                )
        );
    }


    /*
     * =========================================================
     * NEAREST MODE
     * =========================================================
     */

    private static List<Teammate> getNearestTeammates(
            Minecraft minecraft
    ) {
        if (
                minecraft.player == null
                || minecraft.level == null
        ) {
            return List.of();
        }

        String currentDimension =
                minecraft.player
                        .level()
                        .dimension()
                        .location()
                        .toString();

        return TEAMMATES.stream()

                /*
                 * Only teammates in our dimension are
                 * meaningful for nearest-player mode.
                 */
                .filter(teammate ->
                        currentDimension.equals(
                                teammate.dimension()
                        )
                )

                /*
                 * Closest first.
                 */
                .sorted(
                        Comparator.comparingDouble(
                                teammate ->
                                        distanceSquared(
                                                minecraft,
                                                teammate
                                        )
                        )
                )

                /*
                 * Never exceed the HUD's four cards.
                 */
                .limit(PAGE_SIZE)

                .toList();
    }

    private static double distanceSquared(
            Minecraft minecraft,
            Teammate teammate
    ) {
        double dx =
                teammate.x()
                        - minecraft.player.getX();

        double dy =
                teammate.y()
                        - minecraft.player.getY();

        double dz =
                teammate.z()
                        - minecraft.player.getZ();

        return dx * dx
                + dy * dy
                + dz * dz;
    }


    /*
     * =========================================================
     * FAVORITES
     * =========================================================
     */

    public static boolean isFavorite(
            UUID playerId
    ) {
        ensureFavoritesLoaded();

        return FAVORITES.contains(
                playerId
        );
    }

    public static boolean toggleFavorite(
            UUID playerId
    ) {
        ensureFavoritesLoaded();
        if (
                FAVORITES.contains(
                        playerId
                )
        ) {
            FAVORITES.remove(
                    playerId
            );

            saveFavoritesToConfig();

            return false;
        }

        if (
                FAVORITES.size()
                        >= MAX_FAVORITES
        ) {
            return false;
        }

        FAVORITES.add(
                playerId
        );

        saveFavoritesToConfig();

        return true;
    }

    public static List<UUID> getFavorites() {
        ensureFavoritesLoaded();

        return Collections.unmodifiableList(
                FAVORITES
        );
    }

    public static int getFavoriteCount() {
        ensureFavoritesLoaded();

        return FAVORITES.size();
    }

    public static void clearTeamState() {
        TEAMMATES.clear();

        currentPage = 0;
        hudMode = HudMode.MANUAL;
    }

    public static void clearFavorites() {
        FAVORITES.clear();
        saveFavoritesToConfig();
    }

    public static void clearAllTeamHudData() {
        clearTeamState();
        clearFavorites();
    }

    private static List<Teammate> getFavoriteTeammates() {
        ensureFavoritesLoaded();

        List<Teammate> result =
                new ArrayList<>();

        /*
         * Iterate FAVORITES rather than TEAMMATES so
         * favorite order stays stable.
         */
        for (UUID favoriteId :
                FAVORITES) {

            for (Teammate teammate :
                    TEAMMATES) {

                if (
                        teammate.playerId()
                                .equals(
                                        favoriteId
                                )
                ) {
                    result.add(
                            teammate
                    );

                    break;
                }
            }

            if (
                    result.size()
                            >= MAX_FAVORITES
            ) {
                break;
            }
        }

        return result;
    }


    /*
     * =========================================================
     * FINAL HUD LIST
     * =========================================================
     */

    public static List<Teammate> getVisibleTeammates(
            Minecraft minecraft
    ) {
        /*
         * FAVORITES
         *
         * Show the player's pinned teammates.
         *
         * If none are currently available, fall back
         * to the selected manual page rather than
         * leaving the HUD blank.
         */
        if (
                hudMode
                        == HudMode.FAVORITES
        ) {
            List<Teammate> favorites =
                    getFavoriteTeammates();

            if (!favorites.isEmpty()) {
                return favorites;
            }

            return getManualPage();
        }

        /*
         * NEAREST
         *
         * Show up to four closest teammates in the
         * player's current dimension.
         *
         * If nobody is in this dimension, fall back
         * to the selected manual page.
         */
        if (
                hudMode
                        == HudMode.NEAREST
        ) {
            List<Teammate> nearest =
                    getNearestTeammates(
                            minecraft
                    );

            if (!nearest.isEmpty()) {
                return nearest;
            }

            return getManualPage();
        }

        /*
         * MANUAL
         */
        return getManualPage();
    }


    /*
     * =========================================================
     * CLEANUP
     * =========================================================
     */

    public static void loadFavoritesFromConfig() {
        FAVORITES.clear();

        for (String value :
                TheFloodClientConfig
                        .TEAM_HUD_FAVORITES
                        .get()) {

            try {
                UUID id =
                        UUID.fromString(
                                value
                        );

                if (
                        !FAVORITES.contains(id)
                        && FAVORITES.size()
                                < MAX_FAVORITES
                ) {
                    FAVORITES.add(id);
                }

            } catch (IllegalArgumentException ignored) {
                /*
                * Ignore invalid UUID entries rather than
                * breaking the HUD because of a bad config.
                */
            }
        }
    }

    private static void saveFavoritesToConfig() {
        List<String> values =
                FAVORITES.stream()
                        .map(UUID::toString)
                        .toList();

        TheFloodClientConfig
                .TEAM_HUD_FAVORITES
                .set(values);
    }

    public static void clear() {
        clearTeamState();
    }
}