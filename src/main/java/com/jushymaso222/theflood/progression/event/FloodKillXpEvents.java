package com.jushymaso222.theflood.progression.event;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.progression.HeatManager;
import com.jushymaso222.theflood.progression.scaling.MobScaling;
import com.jushymaso222.theflood.progression.FloodKillCredit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FloodKillXpEvents {

    private FloodKillXpEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(
            LivingDeathEvent event
    ) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (!MobScaling.isFloodMob(
                mob.getType()
        )) {
            return;
        }

        ServerPlayer killer =
                FloodKillCredit.findResponsiblePlayer(
                        event.getSource()
                );

        if (killer == null) {
            return;
        }

        double percent =
                TheFloodConfig.HEAT
                        .floodMobKillXpPercent
                        .get();

        long xp =
                Math.max(
                        1L,
                        Math.round(
                                HeatManager.getFloodXpRequired(
                                        killer
                                )
                                        * percent
                        )
                );

        HeatManager.addFloodXp(
                killer,
                xp
        );

        HeatManager.addRewardFloodXp(
                killer,
                xp
        );
    }
}