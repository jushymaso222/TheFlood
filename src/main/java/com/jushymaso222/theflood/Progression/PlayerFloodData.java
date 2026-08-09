package com.jushymaso222.theflood.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerFloodData {

    private static final String DATA_ROOT =
            "theflood_progression";

    private static final String SOLO_HEAT =
            "solo_heat";

    private static final String HEAT_PROGRESS_TICKS =
            "heat_progress_ticks";

    public static final int MAX_HEAT = 100;

    public static int clampHeat(int heat) {
        return Math.max(
                1,
                Math.min(MAX_HEAT, heat)
        );
    }

    private PlayerFloodData() {
    }

    private static CompoundTag getData(
            ServerPlayer player
    ) {
        CompoundTag persistent =
                player.getPersistentData();

        if (!persistent.contains(DATA_ROOT)) {
            persistent.put(
                    DATA_ROOT,
                    new CompoundTag()
            );
        }

        return persistent.getCompound(
                DATA_ROOT
        );
    }

    public static int getSoloHeat(
            ServerPlayer player
    ) {
        CompoundTag data =
                getData(player);

        if (!data.contains(SOLO_HEAT)) {
            data.putInt(SOLO_HEAT, 1);
        }

        return clampHeat(
                data.getInt(SOLO_HEAT)
        );
    }

    public static void setSoloHeat(
            ServerPlayer player,
            int heat
    ) {
        getData(player).putInt(
                SOLO_HEAT,
                clampHeat(heat)
        );
    }

    public static long getHeatProgressTicks(
            ServerPlayer player
    ) {
        return Math.max(
                0,
                getData(player)
                        .getLong(
                                HEAT_PROGRESS_TICKS
                        )
        );
    }

    public static void setHeatProgressTicks(
            ServerPlayer player,
            long ticks
    ) {
        getData(player).putLong(
                HEAT_PROGRESS_TICKS,
                Math.max(0, ticks)
        );
    }

    public static void addHeatProgressTick(
            ServerPlayer player
    ) {
        setHeatProgressTicks(
                player,
                getHeatProgressTicks(player) + 1
        );
    }

    public static void copy(
            ServerPlayer original,
            ServerPlayer replacement
    ) {
        CompoundTag originalData =
                original.getPersistentData()
                        .getCompound(DATA_ROOT);

        replacement.getPersistentData()
                .put(
                        DATA_ROOT,
                        originalData.copy()
                );
    }
}