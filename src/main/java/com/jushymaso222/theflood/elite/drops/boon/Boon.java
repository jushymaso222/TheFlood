package com.jushymaso222.theflood.elite.drops.boon;

import net.minecraft.server.level.ServerPlayer;

public interface Boon {

    BoonType type();

    default void onActivated(
            ServerPlayer player
    ) {
    }

    default void onDeactivated(
            ServerPlayer player
    ) {
    }

    default void tick(
            ServerPlayer player
    ) {
    }
}