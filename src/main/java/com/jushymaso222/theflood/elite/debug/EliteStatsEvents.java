package com.jushymaso222.theflood.elite.debug;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.debug.network.SyncMobStatsPacket;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteStatsEvents {

    private static final int UPDATE_INTERVAL_TICKS =
            5;

    private EliteStatsEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        /*
         * Server side only.
         */
        if (
                event.phase
                        != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
        ) {
            return;
        }

        /*
         * Don't spam snapshots every tick.
         */
        if (
                player.tickCount
                        % UPDATE_INTERVAL_TICKS
                        != 0
        ) {
            return;
        }

        UUID watchedId =
                EliteStatsWatch.getWatched(
                        player
                );

        if (watchedId == null) {
            return;
        }

        if (
                !(player.level() instanceof ServerLevel level)
        ) {
            return;
        }

        Entity entity =
                level.getEntity(
                        watchedId
                );

        /*
         * Watched mob died, despawned, unloaded,
         * or is otherwise unavailable.
         *
         * Close the inspector automatically.
         */
        if (
                !(entity instanceof Mob mob)
                || !mob.isAlive()
        ) {
            EliteStatsWatch.clear(
                    player
            );

            SyncMobStatsPacket.sendClosed(
                    player
            );

            return;
        }

        /*
         * Send a fresh real-time snapshot.
         */
        SyncMobStatsPacket.sendSnapshot(
                player,
                mob
        );
    }
}