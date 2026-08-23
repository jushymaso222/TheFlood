package com.jushymaso222.theflood.elite.drops;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public record EliteDropContext(
        ServerLevel level,
        Mob elite,
        ServerPlayer killer,
        DamageSource damageSource,
        int sourceHeat
) {
}