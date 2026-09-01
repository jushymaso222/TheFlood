package com.jushymaso222.theflood.guide.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientFloodUnlockData {

    private static final Set<ResourceLocation>
            ACHIEVED_MILESTONES =
            new HashSet<>();

    private ClientFloodUnlockData() {
    }

    public static void setMilestones(
            List<ResourceLocation> milestones
    ) {
        ACHIEVED_MILESTONES.clear();

        if (milestones != null) {
            ACHIEVED_MILESTONES.addAll(
                    milestones
            );
        }
    }

    public static void addMilestone(
            ResourceLocation milestone
    ) {
        if (milestone != null) {
            ACHIEVED_MILESTONES.add(
                    milestone
            );
        }
    }

    public static boolean hasMilestone(
            String milestoneId
    ) {
        if (milestoneId == null
                || milestoneId.isBlank()) {
            return true;
        }

        try {
            return ACHIEVED_MILESTONES.contains(
                    new ResourceLocation(
                            milestoneId
                    )
            );
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void clear() {
        ACHIEVED_MILESTONES.clear();
    }
}