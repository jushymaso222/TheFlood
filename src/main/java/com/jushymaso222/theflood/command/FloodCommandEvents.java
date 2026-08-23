package com.jushymaso222.theflood.command;

import com.jushymaso222.theflood.TheFlood;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FloodCommandEvents {

    private FloodCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(
            RegisterCommandsEvent event
    ) {
        FloodTimeCommand.register(
                event.getDispatcher()
        );
    }
}