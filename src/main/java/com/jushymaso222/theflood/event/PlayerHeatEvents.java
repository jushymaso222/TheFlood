package com.jushymaso222.theflood.event;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.progression.HeatManager;
import com.jushymaso222.theflood.progression.PlayerFloodData;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.network.packet.SyncHeatPacket;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PlayerHeatEvents {

    private PlayerHeatEvents() {
    }

    private static void syncHeat(ServerPlayer player) {
        int effectiveHeat =
                HeatManager.getEffectiveHeat(player);

        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncHeatPacket(effectiveHeat)
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        HeatManager.advanceSoloHeat(
                player
        );
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
         * Player entities are replaced on death.
         * Explicitly preserve Flood progression.
         */
        PlayerFloodData.copy(
                original,
                replacement
        );
    }
}