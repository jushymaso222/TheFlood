package com.jushymaso222.theflood.progression.capability;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.progression.capability.sensor.DefenseCapabilitySensor;
import com.jushymaso222.theflood.progression.capability.sensor.MobilityCapabilitySensor;
import com.jushymaso222.theflood.progression.capability.sensor.SurvivalCapabilitySensor;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class CapabilitySensorEvents {

    private CapabilitySensorEvents() {
    }


    /*
     * =====================================================
     * DAMAGE
     * =====================================================
     *
     * LivingDamageEvent gives us the damage that actually
     * gets through the player's defenses.
     *
     * Defense uses that value to determine how much of the
     * player's health bar the hit removed.
     *
     * Survival uses the same final damage to update its
     * current pressure encounter.
     */

    @SubscribeEvent
    public static void onPlayerDamage(
            LivingDamageEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        float finalDamage =
                event.getAmount();

        if (
                finalDamage < 0.0F
                || !Float.isFinite(finalDamage)
        ) {
            return;
        }


        /*
         * =================================================
         * DEFENSE
         * =================================================
         */

        DefenseCapabilitySensor.observeDamage(
                player,
                finalDamage
        );


        /*
         * =================================================
         * SURVIVAL
         * =================================================
         */

        SurvivalCapabilitySensor.observeDamage(
                player,
                finalDamage
        );
    }


    /*
     * =====================================================
     * PLAYER TICK
     * =====================================================
     *
     * Mobility samples player movement over time.
     *
     * Survival uses ticks to determine when a pressure
     * encounter has ended successfully.
     */

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (
                event.phase
                        != TickEvent.Phase.END
        ) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }


        MobilityCapabilitySensor.tick(
                player
        );

        SurvivalCapabilitySensor.tick(
                player
        );
    }


    /*
     * =====================================================
     * DEATH
     * =====================================================
     *
     * An actual death is a failed Survival encounter.
     */

    @SubscribeEvent
    public static void onPlayerDeath(
            LivingDeathEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        SurvivalCapabilitySensor.observeDeath(
                player
        );
    }


    /*
     * =====================================================
     * LOGOUT CLEANUP
     * =====================================================
     *
     * Sensor runtime state does not need to persist while
     * the player is offline.
     */

    @SubscribeEvent
    public static void onPlayerLogout(
            PlayerEvent.PlayerLoggedOutEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        MobilityCapabilitySensor.remove(
                player
        );

        SurvivalCapabilitySensor.remove(
                player
        );
    }
}