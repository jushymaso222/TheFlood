package com.jushymaso222.theflood.progression.event;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.progression.HeatManager;
import com.jushymaso222.theflood.progression.PlayerFloodData;
import com.jushymaso222.theflood.progression.capability.CapabilityManager;
import com.jushymaso222.theflood.progression.milestone.MilestoneManager;
import com.jushymaso222.theflood.progression.milestone.MilestoneData;

import com.jushymaso222.theflood.progression.capability.debug.CapabilityStatsWatch;
import com.jushymaso222.theflood.progression.capability.network.SyncCapabilityStatsPacket;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PlayerHeatEvents {

    private PlayerHeatEvents() {
    }


    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (
                event.phase
                        != TickEvent.Phase.END
        ) {
            return;
        }

        if (
                !(event.player
                        instanceof ServerPlayer player)
        ) {
            return;
        }


        boolean heatIncreased =
                HeatManager.advanceSoloHeat(
                        player
                );


        /*
         * Sync immediately when Solo Heat changes,
         * and once per second so proximity changes
         * are reflected on the HUD.
         */
        if (
                heatIncreased
                || player.tickCount % 20 == 0
        ) {
            HeatManager.syncHeatToPlayer(
                    player
            );
        }

        /*
        * Keep the Capability Inspector updated while open.
        *
        * 5 ticks = 4 updates per second.
        * Plenty for a debug panel without spamming a packet
        * every single game tick.
        */
        if (
                CapabilityStatsWatch.isWatching(player)
                && player.tickCount % 5 == 0
        ) {
                SyncCapabilityStatsPacket.sendSnapshot(
                        player
                );
        }


        /*
         * Milestone evaluation.
         *
         * Every 2 seconds.
         */
        if (
                player.tickCount % 40 == 0
        ) {
            MilestoneManager.evaluate(
                    player
            );
        }


        /*
         * Capability autosave.
         *
         * Every 60 seconds.
         */
        if (
                player.tickCount % 1200 == 0
        ) {
            CapabilityManager.save(
                    player
            );
        }
    }


    @SubscribeEvent
    public static void onPlayerClone(
            PlayerEvent.Clone event
    ) {
        if (
                !(event.getOriginal()
                        instanceof ServerPlayer original)
                ||
                !(event.getEntity()
                        instanceof ServerPlayer replacement)
        ) {
            return;
        }


        /*
         * Preserve Flood progression.
         */
        PlayerFloodData.copy(
                original,
                replacement
        );


        /*
         * Preserve Capability learning.
         */
        CapabilityManager.handleClone(
                original,
                replacement
        );

        MilestoneData.copy(
                original,
                replacement
        );
    }


    @SubscribeEvent
    public static void onPlayerLoggedIn(
            PlayerEvent.PlayerLoggedInEvent event
    ) {
        if (
                !(event.getEntity()
                        instanceof ServerPlayer player)
        ) {
            return;
        }

        CapabilityManager.load(
                player
        );
    }


    @SubscribeEvent
    public static void onPlayerLoggedOut(
            PlayerEvent.PlayerLoggedOutEvent event
    ) {
        if (
                !(event.getEntity()
                        instanceof ServerPlayer player)
        ) {
            return;
        }

        CapabilityManager.saveAndUnload(
                player
        );
    }
}