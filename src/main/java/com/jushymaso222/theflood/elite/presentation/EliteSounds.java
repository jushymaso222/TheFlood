package com.jushymaso222.theflood.elite.presentation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public final class EliteSounds {

    private static final Random RANDOM =
            new Random();

    private EliteSounds() {
    }

    public static void play(
            Mob elite,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        level.playSound(
                null,
                elite.blockPosition(),
                sound,
                SoundSource.HOSTILE,
                volume,
                pitch
        );
    }

    public static void playRandomPitch(
            Mob elite,
            SoundEvent sound,
            float volume,
            float basePitch,
            float pitchVariation
    ) {
        float pitch =
                basePitch
                        + (
                        RANDOM.nextFloat()
                                * 2.0F
                                - 1.0F
                )
                        * pitchVariation;

        play(
                elite,
                sound,
                volume,
                pitch
        );
    }

    public static void playToPlayer(
            Mob elite,
            Player player,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundSource.HOSTILE,
                volume,
                pitch
        );
    }

    public static void playDistanceScaled(
            Mob elite,
            Player player,
            SoundEvent sound,
            double maxDistance,
            float maxVolume,
            float pitch
    ) {
        double distance =
                elite.distanceTo(
                        player
                );

        if (
                distance >= maxDistance
        ) {
            return;
        }

        float strength =
                1.0F
                        - (float) (
                        distance
                                / maxDistance
                );

        float volume =
                maxVolume
                        * strength;

        playToPlayer(
                elite,
                player,
                sound,
                volume,
                pitch
        );
    }
}