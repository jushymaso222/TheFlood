package com.jushymaso222.theflood.elite.behavior;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.behavior.mutations.InfestedMutation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Silverfish;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class InfestedMinionEvents {

    private static final double DETONATION_DISTANCE =
            1.2D;

    private static final float EXPLOSION_DAMAGE =
            8.0F;

    private InfestedMinionEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(
            LivingEvent.LivingTickEvent event
    ) {
        if (
                !(event.getEntity() instanceof Silverfish silverfish)
        ) {
            return;
        }

        if (
                !(silverfish.level() instanceof ServerLevel level)
        ) {
            return;
        }

        if (
                !silverfish.getPersistentData()
                        .getBoolean(
                                InfestedMutation.INFESTED_MINION_TAG
                        )
        ) {
            return;
        }

        long expiration =
                silverfish.getPersistentData()
                        .getLong(
                                "theflood_infested_expire"
                        );

        if (
                expiration > 0
                && level.getGameTime()
                        >= expiration
        ) {
            silverfish.discard();
            return;
        }

        if (
                !(silverfish.getTarget() instanceof ServerPlayer player)
                || !player.isAlive()
        ) {
            return;
        }

        if (
                silverfish.distanceToSqr(
                        player
                )
                        > DETONATION_DISTANCE
                                * DETONATION_DISTANCE
        ) {
            return;
        }

        detonate(
                silverfish,
                player
        );
    }

    private static void detonate(
            Silverfish silverfish,
            ServerPlayer player
    ) {
        if (
                !(silverfish.level() instanceof ServerLevel level)
        ) {
            return;
        }

        player.hurt(
                level.damageSources()
                        .mobAttack(
                                silverfish
                        ),
                EXPLOSION_DAMAGE
        );

        /*
         * Visual/audio explosion only.
         *
         * No block destruction.
         */
        level.explode(
                silverfish,
                silverfish.getX(),
                silverfish.getY(),
                silverfish.getZ(),
                0.0F,
                ServerLevel.ExplosionInteraction.NONE
        );

        silverfish.discard();
    }
}