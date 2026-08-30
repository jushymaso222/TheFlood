package com.jushymaso222.theflood.progression.milestone.client;

import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Queue;

public final class ClientMilestoneNotifications {

    /*
     * 20 ticks = 1 second.
     *
     * 80 ticks gives each notification
     * approximately four seconds.
     */
    private static final long DISPLAY_TIME_MS =
            6000L;

    private static final Queue<Notification>
            QUEUE =
            new ArrayDeque<>();

    private static Notification current;

    private static long currentStartTime;


    private ClientMilestoneNotifications() {
    }


    /*
     * ============================================
     * QUEUE
     * ============================================
     */

    public static void add(
            Component title,
            Component description,
            int progressionValue,
            double floodXpReward
    ) {
        QUEUE.add(
                new Notification(
                        title,
                        description,
                        progressionValue,
                        floodXpReward
                )
        );

        if (current == null) {
            advance();
        }
    }


    private static void advance() {

        current =
                QUEUE.poll();

        currentStartTime =
                System.currentTimeMillis();
    }


    /*
     * ============================================
     * ACCESS
     * ============================================
     */

    public static Notification getCurrent() {

        update();

        return current;
    }


    public static float getProgress() {

        if (current == null) {
            return 0.0F;
        }

        long elapsed =
                System.currentTimeMillis()
                        - currentStartTime;

        return Math.min(
                1.0F,
                elapsed
                        / (float) DISPLAY_TIME_MS
        );
    }


    /*
     * ============================================
     * UPDATE
     * ============================================
     */

    private static void update() {

        if (current == null) {

            if (!QUEUE.isEmpty()) {
                advance();
            }

            return;
        }

        long elapsed =
                System.currentTimeMillis()
                        - currentStartTime;

        if (
                elapsed
                        >= DISPLAY_TIME_MS
        ) {
            advance();
        }
    }


    /*
     * ============================================
     * NOTIFICATION
     * ============================================
     */

    public record Notification(
            Component title,
            Component description,
            int progressionValue,
            double floodXpReward
    ) {
    }
}