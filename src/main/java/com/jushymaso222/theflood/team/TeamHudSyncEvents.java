package com.jushymaso222.theflood.team;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class TeamHudSyncEvents {

    private TeamHudSyncEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {
        if (
                event.phase
                        != TickEvent.Phase.END
        ) {
            return;
        }

        long gameTime =
                event.getServer()
                        .overworld()
                        .getGameTime();

        /*
         * Twice per second.
         */
        if (gameTime % 10 != 0) {
            return;
        }

        for (ServerPlayer player :
                event.getServer()
                        .getPlayerList()
                        .getPlayers()) {

            TeamManager.syncTeamHudToPlayer(
                    player
            );
        }
    }
}