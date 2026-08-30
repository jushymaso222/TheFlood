package com.jushymaso222.theflood.progression.milestone.client;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientMilestoneData {

    private static final int MAX_RECENT =
            5;


    private static int progressionValue =
            0;


    /*
     * Every milestone the server says this player
     * has completed.
     */
    private static final Set<ResourceLocation> COMPLETED =
            new HashSet<>();


    /*
     * Newest completion is index 0.
     */
    private static final List<ResourceLocation> RECENT =
            new ArrayList<>();


    private ClientMilestoneData() {
    }


    /*
     * =================================================
     * FULL SERVER SNAPSHOT
     * =================================================
     */

    public static void setSnapshot(
            int newProgressionValue,
            List<ResourceLocation> completed,
            List<ResourceLocation> completionHistory
    ) {
        progressionValue =
                newProgressionValue;


        COMPLETED.clear();

        if (completed != null) {
            COMPLETED.addAll(
                    completed
            );
        }


        /*
         * Server history is:
         *
         * oldest -> newest
         *
         * Client recent list is:
         *
         * newest -> oldest
         */
        RECENT.clear();

        if (completionHistory == null) {
            return;
        }


        for (
                int i = completionHistory.size() - 1;
                i >= 0
                        && RECENT.size() < MAX_RECENT;
                i--
        ) {
            ResourceLocation id =
                    completionHistory.get(
                            i
                    );

            if (id != null) {
                RECENT.add(
                        id
                );
            }
        }
    }


    /*
     * =================================================
     * PROGRESSION
     * =================================================
     */

    public static int getProgressionValue() {
        return progressionValue;
    }


    /*
     * =================================================
     * COMPLETED STATE
     * =================================================
     */

    public static void setCompleted(
            List<ResourceLocation> completed
    ) {
        COMPLETED.clear();

        if (completed == null) {
            return;
        }

        COMPLETED.addAll(
                completed
        );
    }


    public static boolean isCompleted(
            ResourceLocation id
    ) {
        return id != null
                && COMPLETED.contains(
                        id
                );
    }


    public static Set<ResourceLocation> getCompleted() {
        return Set.copyOf(
                COMPLETED
        );
    }


    public static void markCompleted(
            ResourceLocation id
    ) {
        if (id == null) {
            return;
        }

        COMPLETED.add(
                id
        );
    }


    /*
     * =================================================
     * RECENT COMPLETIONS
     * =================================================
     */

    public static void addRecent(
            ResourceLocation id
    ) {
        if (id == null) {
            return;
        }


        /*
         * Remove any previous occurrence so the same
         * milestone can never appear twice.
         */
        RECENT.remove(
                id
        );


        /*
         * Newest milestone goes first.
         */
        RECENT.add(
                0,
                id
        );


        while (
                RECENT.size()
                        > MAX_RECENT
        ) {
            RECENT.remove(
                    RECENT.size() - 1
            );
        }


        markCompleted(
                id
        );
    }


    public static List<ResourceLocation> getRecent() {
        return List.copyOf(
                RECENT
        );
    }


    /*
     * =================================================
     * RESET
     * =================================================
     */

    public static void clear() {
        progressionValue =
                0;

        COMPLETED.clear();
        RECENT.clear();
    }
}