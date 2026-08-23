package com.jushymaso222.theflood.elite.drops;

import com.jushymaso222.theflood.TheFlood;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteDropReloadEvents {

    private EliteDropReloadEvents() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(
            AddReloadListenerEvent event
    ) {
        event.addListener(
                new EliteDropTableLoader()
        );
    }
}