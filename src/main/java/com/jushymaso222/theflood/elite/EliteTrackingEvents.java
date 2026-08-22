package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteTrackingEvents {

    private EliteTrackingEvents() {
    }

    @SubscribeEvent
    public static void onStartTracking(
            PlayerEvent.StartTracking event
    ) {
        if (
                !(event.getEntity() instanceof ServerPlayer player)
        ) {
            return;
        }

        if (
                !(event.getTarget() instanceof Mob mob)
        ) {
            return;
        }

        if (
                !EliteData.isElite(
                        mob
                )
        ) {
            return;
        }

        EliteStateSync.syncBasicTo(
                mob,
                player
        );
    }
}