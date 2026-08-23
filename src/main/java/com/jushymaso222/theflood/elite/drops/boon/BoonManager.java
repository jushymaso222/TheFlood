package com.jushymaso222.theflood.elite.drops.boon;

import net.minecraft.server.level.ServerPlayer;

import com.jushymaso222.theflood.elite.drops.boon.effect.BoonEffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class BoonManager {

    private BoonManager() {
    }


    public static boolean tryActivate(
            ServerPlayer player,
            BoonType type
    ) {
        if (
                BoonData.hasActiveBoon(
                        player
                )
        ) {
            return false;
        }

        long endTime =
                player.level()
                        .getGameTime()
                        + type.durationTicks();

        BoonData.setActiveBoon(
                player,
                type,
                endTime
        );

        MobEffect statusEffect =
                getStatusEffect(
                        type
                );

        player.addEffect(
                new MobEffectInstance(
                        statusEffect,
                        type.durationTicks(),
                        0,
                        false,
                        false,
                        true
                )
        );

        Boon boon =
                BoonRegistry.get(
                        type.id()
                );

        if (boon != null) {
            boon.onActivated(
                    player
            );
        }

        return true;
    }

    private static MobEffect getStatusEffect(
            BoonType type
    ) {
        return switch (
                type
        ) {
            case HOARDERS ->
                    BoonEffects.BOON_HOARDERS.get();

            case ATTACK ->
                    BoonEffects.BOON_ATTACK.get();

            case DEFENSE ->
                    BoonEffects.BOON_DEFENSE.get();

            case TRANQUILITY ->
                    BoonEffects.BOON_TRANQUILITY.get();
        };
    }

    public static void tick(
            ServerPlayer player
    ) {
        if (
                !BoonData.hasActiveBoon(
                        player
                )
        ) {
            return;
        }

        long now =
                player.level()
                        .getGameTime();

        if (
                now >= BoonData.getEndTime(
                        player
                )
        ) {
            deactivate(
                    player
            );

            return;
        }

        Boon boon =
                BoonRegistry.get(
                        BoonData.getActiveBoonId(
                                player
                        )
                );

        if (boon != null) {
            boon.tick(
                    player
            );
        }
    }


    public static void deactivate(
            ServerPlayer player
    ) {
        Boon boon =
                BoonRegistry.get(
                        BoonData.getActiveBoonId(
                                player
                        )
                );

        if (boon != null) {
            boon.onDeactivated(
                    player
            );
        }

        String activeId =
                BoonData.getActiveBoonId(
                        player
                );

        BoonType activeType =
                BoonType.fromId(
                        activeId
                );

        if (activeType != null) {
            player.removeEffect(
                    getStatusEffect(
                            activeType
                    )
            );
        }

        BoonData.clear(
                player
        );
    }
}