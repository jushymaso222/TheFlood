package com.jushymaso222.theflood.elite.behavior;

import com.jushymaso222.theflood.elite.EliteData;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class EliteBehaviorHooks {

    private EliteBehaviorHooks() {
    }

    private static EliteMutation getMutation(
            Mob elite
    ) {
        if (!EliteData.isElite(elite)) {
            return null;
        }

        return EliteMutationRegistry.get(
                EliteData.getMutation(
                        elite
                )
        );
    }

    public static boolean handleLethalDamage(
        Mob elite,
        float incomingDamage
) {
    EliteMutation mutation =
            getMutation(
                    elite
            );

    if (mutation == null) {
        return false;
    }

    return mutation.handleLethalDamage(
            elite,
            incomingDamage
    );
}

    public static float modifyOutgoingDamage(
                Mob elite,
                float damage
        ) {
        EliteMutation mutation =
                getMutation(
                        elite
                );

        if (mutation == null) {
                return damage;
        }

        return mutation.modifyOutgoingDamage(
                elite,
                damage
        );
        }

    public static float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        EliteMutation mutation =
                getMutation(
                        elite
                );

        if (mutation == null) {
            return damage;
        }

        return mutation.modifyIncomingDamage(
                elite,
                source,
                damage
        );
    }

    public static void tick(
            Mob elite
    ) {
        EliteMutation mutation =
                getMutation(
                        elite
                );

        if (mutation == null) {
            return;
        }

        mutation.tick(
                elite
        );
    }

    public static void onEliteHurtPlayer(
            Mob elite,
            Player player,
            float damage
    ) {
        EliteMutation mutation =
                getMutation(
                        elite
                );

        if (mutation == null) {
            return;
        }

        mutation.onHurtPlayer(
                elite,
                player,
                damage
        );
    }

    public static void onEliteDeath(
            Mob elite
    ) {
        EliteMutation mutation =
                getMutation(
                        elite
                );

        if (mutation == null) {
            return;
        }

        mutation.onDeath(
                elite
        );
    }
}