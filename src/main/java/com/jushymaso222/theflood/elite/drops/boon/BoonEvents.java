package com.jushymaso222.theflood.elite.drops.boon;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.event.entity.player.PlayerEvent;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class BoonEvents {

    private BoonEvents() {
    }


    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (
                event.phase
                        != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
        ) {
            return;
        }

        BoonManager.tick(
                player
        );
    }

    @SubscribeEvent
    public static void onPlayerRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {
        if (
                !(event.getEntity() instanceof ServerPlayer player)
        ) {
            return;
        }

        BoonManager.restoreStatusEffect(
                player
        );
    }
}