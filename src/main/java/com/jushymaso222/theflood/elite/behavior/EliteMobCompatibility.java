package com.jushymaso222.theflood.elite.behavior;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.Zombie;

public final class EliteMobCompatibility {

    private EliteMobCompatibility() {
    }

    public static boolean isZombie(
            Mob mob
    ) {
        return mob instanceof Zombie;
    }

    public static boolean isSkeleton(
            Mob mob
    ) {
        return mob instanceof AbstractSkeleton;
    }

    public static boolean isSpider(
            Mob mob
    ) {
        return mob instanceof Spider;
    }

    public static boolean isCreeper(
            Mob mob
    ) {
        return mob instanceof Creeper;
    }

    public static boolean isEnderman(
            Mob mob
    ) {
        return mob instanceof EnderMan;
    }

    public static boolean isWarden(
            Mob mob
    ) {
        return mob instanceof Warden;
    }

    public static boolean canBecomeElite(
        Mob mob
) {
    return isZombie(
            mob
    )
            || isSkeleton(
                    mob
            )
            || isSpider(
                    mob
            )
            || isCreeper(
                    mob
            )
            || isEnderman(
                    mob
            )
            || isWarden(
                    mob
            );
}
}