package com.jushymaso222.theflood.elite.presentation;

import net.minecraft.world.entity.Mob;

public final class ElitePoseController {

    private static final String POSE_KEY =
            "theflood_elite_pose";

    private static final String POSE_END_KEY =
            "theflood_elite_pose_end";

    private ElitePoseController() {
    }

    public static void setPose(
            Mob elite,
            ElitePose pose
    ) {
        elite.getPersistentData()
                .putString(
                        POSE_KEY,
                        pose.name()
                );

        elite.getPersistentData()
                .remove(
                        POSE_END_KEY
                );
    }

    public static void setTemporaryPose(
            Mob elite,
            ElitePose pose,
            int durationTicks
    ) {
        elite.getPersistentData()
                .putString(
                        POSE_KEY,
                        pose.name()
                );

        elite.getPersistentData()
                .putLong(
                        POSE_END_KEY,
                        elite.level()
                                .getGameTime()
                                + durationTicks
                );
    }

    public static ElitePose getPose(
            Mob elite
    ) {
        String stored =
                elite.getPersistentData()
                        .getString(
                                POSE_KEY
                        );

        if (
                stored == null
                || stored.isBlank()
        ) {
            return ElitePose.DEFAULT;
        }

        try {
            return ElitePose.valueOf(
                    stored
            );
        } catch (IllegalArgumentException ignored) {
            return ElitePose.DEFAULT;
        }
    }

    public static void resetPose(
            Mob elite
    ) {
        elite.getPersistentData()
                .putString(
                        POSE_KEY,
                        ElitePose.DEFAULT.name()
                );

        elite.getPersistentData()
                .remove(
                        POSE_END_KEY
                );
    }

    public static void tick(
            Mob elite
    ) {
        long end =
                elite.getPersistentData()
                        .getLong(
                                POSE_END_KEY
                        );

        if (end <= 0L) {
            return;
        }

        if (
                elite.level()
                        .getGameTime()
                        >= end
        ) {
            resetPose(
                    elite
            );
        }
    }
}