package com.jushymaso222.theflood.debug;

import com.jushymaso222.theflood.TheFlood;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class DummyPlayerPersistenceEvents {

    private DummyPlayerPersistenceEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(
            ServerStartedEvent event
    ) {
        DummyPlayerManager.restoreAll(
                event.getServer()
        );
    }

    @SubscribeEvent
    public static void onServerStopping(
            ServerStoppingEvent event
    ) {
        /*
         * Runtime cleanup only.
         *
         * Saved dummy definitions stay intact.
         */
        DummyPlayerManager.unloadRuntime();
    }
}