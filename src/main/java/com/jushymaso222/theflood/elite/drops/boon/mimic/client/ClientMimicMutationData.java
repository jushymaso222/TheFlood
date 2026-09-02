package com.jushymaso222.theflood.elite.drops.boon.mimic.client;

import com.jushymaso222.theflood.elite.drops.boon.mimic.PlayerMimicMutation;

public final class ClientMimicMutationData {

    private static PlayerMimicMutation activeMutation;

    private static long remainingTicks;


    private ClientMimicMutationData() {
    }


    public static PlayerMimicMutation getActiveMutation() {
        return activeMutation;
    }


    public static long getRemainingTicks() {
        return remainingTicks;
    }


    public static boolean hasActiveMutation() {
        return activeMutation != null
                && remainingTicks > 0L;
    }


    public static void set(
            PlayerMimicMutation mutation,
            long ticks
    ) {
        activeMutation =
                mutation;

        remainingTicks =
                Math.max(
                        0L,
                        ticks
                );
    }


    public static void tick() {
        if (remainingTicks > 0L) {
            remainingTicks--;
        }

        if (remainingTicks <= 0L) {
            clear();
        }
    }


    public static void clear() {
        activeMutation =
                null;

        remainingTicks =
                0L;
    }
}