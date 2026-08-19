package com.jushymaso222.theflood.spawning;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.spawning.SpawnDirector;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FloodWardenEvents {

    private static final double DARKNESS_BLOCK_RADIUS = 32.0;

    private FloodWardenEvents() {
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

        /*
         * Nothing to do unless Darkness is actually present.
         */
        if (!player.hasEffect(MobEffects.DARKNESS)) {
            return;
        }

        AABB searchArea =
                player.getBoundingBox()
                        .inflate(DARKNESS_BLOCK_RADIUS);

        boolean floodWardenNearby =
                !player.serverLevel()
                        .getEntitiesOfClass(
                                Warden.class,
                                searchArea,
                                warden ->
                                        warden.isAlive()
                                        && !warden.isRemoved()
                                        && warden.getPersistentData()
                                                .getBoolean(
                                                        SpawnDirector.FLOOD_CONTROLLED_TAG
                                                )
                        )
                        .isEmpty();

        if (floodWardenNearby) {
            player.removeEffect(
                    MobEffects.DARKNESS
            );
        }
    }
}