package com.jushymaso222.theflood.elite.presentation;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;

public final class ElitePresentation {

    private ElitePresentation() {
    }

    public static void setPose(
            Mob elite,
            ElitePose pose
    ) {
        ElitePoseController.setPose(
                elite,
                pose
        );
    }

    public static void setTemporaryPose(
            Mob elite,
            ElitePose pose,
            int durationTicks
    ) {
        ElitePoseController.setTemporaryPose(
                elite,
                pose,
                durationTicks
        );
    }

    public static void resetPose(
            Mob elite
    ) {
        ElitePoseController.resetPose(
                elite
        );
    }

    public static void burst(
            Mob elite,
            ParticleOptions particle,
            int count,
            double spread,
            double speed
    ) {
        EliteVisuals.burst(
                elite,
                particle,
                count,
                spread,
                speed
        );
    }

    public static void ring(
            Mob elite,
            ParticleOptions particle,
            double radius,
            int count,
            double yOffset
    ) {
        EliteVisuals.ring(
                elite,
                particle,
                radius,
                count,
                yOffset
        );
    }

    public static void sound(
            Mob elite,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        EliteSounds.play(
                elite,
                sound,
                volume,
                pitch
        );
    }
}