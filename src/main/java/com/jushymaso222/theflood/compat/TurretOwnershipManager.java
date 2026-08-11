package com.jushymaso222.theflood.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import com.jushymaso222.theflood.progression.HeatManager;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TurretOwnershipManager {

    private static final Map<TurretKey, TurretOwnerData> OWNERS =
        new HashMap<>();

    public record TurretOwnerData(
        UUID ownerId,
        int cachedBaseHeat
    ) {
    }

    public record ResolvedTurretOwner(
        UUID ownerId,
        int heat
    ) {
    }

    private TurretOwnershipManager() {
    }

    public static void register(
        ServerLevel level,
        BlockPos pos,
        ServerPlayer owner
    ) {
        OWNERS.put(
                new TurretKey(
                        level.dimension(),
                        pos.immutable()
                ),
                new TurretOwnerData(
                        owner.getUUID(),
                        HeatManager.getBaseHeat(owner)
                )
        );
    }

    public static void remove(
            ServerLevel level,
            BlockPos pos
    ) {
        OWNERS.remove(
                new TurretKey(
                        level.dimension(),
                        pos
                )
        );
    }

    public static UUID getOwner(
                ServerLevel level,
                BlockPos pos
        ) {
        TurretOwnerData data =
                OWNERS.get(
                        new TurretKey(
                                level.dimension(),
                                pos
                        )
                );

        if (data == null) {
                return null;
        }

        return data.ownerId();
        }

    public static Map<BlockPos, UUID> getTurretsNear(
            ServerLevel level,
            BlockPos center,
            int radius
    ) {
        Map<BlockPos, UUID> result
                = new HashMap<>();

        int radiusSquared
                = radius * radius;

        for (Map.Entry<TurretKey, TurretOwnerData> entry :
                OWNERS.entrySet()) {

            TurretKey key
                    = entry.getKey();

            if (key.dimension()
                    != level.dimension()) {
                continue;
            }

            if (key.pos()
                    .distSqr(center)
                    > radiusSquared) {
                continue;
            }

            result.put(
                    key.pos(),
                    entry.getValue().ownerId()
            );
        }

        return result;
    }

    public static ResolvedTurretOwner findHighestHeatOwner(
                ServerLevel level,
                BlockPos targetPos,
                int radius
        ) {
        ResolvedTurretOwner selected =
                null;

        int highestHeat = -1;
        int radiusSquared =
                radius * radius;

        /*
        * We cannot remove entries directly while
        * iterating the map, so collect stale entries.
        */
        java.util.List<TurretKey> staleEntries =
                new java.util.ArrayList<>();

        for (Map.Entry<TurretKey, TurretOwnerData> entry :
                OWNERS.entrySet()) {

                TurretKey key =
                        entry.getKey();

                TurretOwnerData data =
                        entry.getValue();

                if (!key.dimension().equals(level.dimension())) {
                continue;
                }

                /*
                * Make sure the registered turret still
                * physically exists.
                */
                ResourceLocation blockId =
                        ForgeRegistries.BLOCKS.getKey(
                                level.getBlockState(
                                        key.pos()
                                ).getBlock()
                        );

                if (
                        blockId == null
                        || !"mekanism_turrets".equals(
                                blockId.getNamespace()
                        )
                ) {
                staleEntries.add(key);
                continue;
                }

                if (
                        key.pos().distSqr(targetPos)
                        > radiusSquared
                ) {
                continue;
                }

                /*
                * If the owner is online, use their current
                * Base Heat and refresh the cached value.
                */
                ServerPlayer owner =
                        level.getServer()
                                .getPlayerList()
                                .getPlayer(
                                        data.ownerId()
                                );

                int heat;

                if (owner != null) {
                heat =
                        HeatManager.getBaseHeat(
                                owner
                        );

                /*
                * Refresh cached progression for use
                * while the owner is offline.
                */
                entry.setValue(
                        new TurretOwnerData(
                                data.ownerId(),
                                heat
                        )
                );
                } else {
                /*
                * Offline owner:
                * use their last known Base Heat.
                */
                heat =
                        data.cachedBaseHeat();
                }

                if (heat > highestHeat) {
                highestHeat =
                        heat;

                selected =
                        new ResolvedTurretOwner(
                                data.ownerId(),
                                heat
                        );
                }
        }

        /*
        * Self-clean turret records when blocks have
        * been destroyed/replaced without BreakEvent.
        */
        for (TurretKey stale :
                staleEntries) {

                OWNERS.remove(stale);
        }

        return selected;
        }

    private record TurretKey(
            ResourceKey<Level> dimension,
            BlockPos pos
            ) {

    }
}
