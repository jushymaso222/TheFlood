package com.jushymaso222.theflood.event;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.team.TeamChatManager;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PlayerSessionEvents {

    private PlayerSessionEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogout(
            PlayerEvent.PlayerLoggedOutEvent event
    ) {
        if (
                !(event.getEntity()
                        instanceof ServerPlayer player)
        ) {
            return;
        }

        TeamChatManager.clear(
                player.getUUID()
        );
    }
}