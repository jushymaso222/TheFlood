package com.jushymaso222.theflood.elite.drops.boon.mimic.client;

import com.jushymaso222.theflood.TheFlood;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT
)
public final class MimicClientEvents {

    private MimicClientEvents() {
    }


    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ) {
        if (
                event.phase != TickEvent.Phase.END
        ) {
            return;
        }

        ClientMimicMutationData.tick();
    }
}