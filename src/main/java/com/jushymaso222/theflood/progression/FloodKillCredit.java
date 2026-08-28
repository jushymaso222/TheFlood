package com.jushymaso222.theflood.progression;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

public final class FloodKillCredit {

    private FloodKillCredit() {
    }

    public static ServerPlayer findResponsiblePlayer(
            DamageSource source
    ) {
        Entity sourceEntity =
                source.getEntity();

        if (sourceEntity instanceof ServerPlayer player) {
            return player;
        }

        Entity directEntity =
                source.getDirectEntity();

        if (
                directEntity instanceof Projectile projectile
                && projectile.getOwner()
                        instanceof ServerPlayer player
        ) {
            return player;
        }

        return null;
    }
}