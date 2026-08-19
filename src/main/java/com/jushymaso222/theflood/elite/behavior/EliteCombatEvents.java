package com.jushymaso222.theflood.elite.behavior;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.EliteData;

import net.minecraft.world.entity.Mob;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteCombatEvents {

    private EliteCombatEvents() {
    }

    @SubscribeEvent
    public static void onEliteDeathAttempt(
            LivingDeathEvent event
    ) {
        if (
                !(event.getEntity() instanceof Mob mob)
        ) {
            return;
        }

        if (!EliteData.isElite(mob)) {
            return;
        }

        boolean intercepted =
                EliteBehaviorHooks.handleLethalDamage(
                        mob,
                        0.0F
                );

        if (!intercepted) {
            return;
        }

        /*
        * Prevent Minecraft from actually killing
        * the Elite.
        */
        event.setCanceled(
                true
        );

        /*
        * Keep it alive while the mutation enters
        * its special death-prevention phase.
        */
        if (mob.getHealth() <= 0.0F) {
            mob.setHealth(
                    1.0F
            );
        }
    }

    @SubscribeEvent(
            priority = EventPriority.LOW
    )
    public static void onEliteDealDamage(
            LivingHurtEvent event
    ) {
        if (
                event.getEntity()
                        .level()
                        .isClientSide()
        ) {
            return;
        }

        Mob attackingMob =
                findResponsibleMob(
                        event.getSource()
                                .getEntity()
                );

        if (attackingMob == null) {
            attackingMob =
                    findResponsibleMob(
                            event.getSource()
                                    .getDirectEntity()
                    );
        }

        if (
                attackingMob == null
                || !EliteData.isElite(
                        attackingMob
                )
        ) {
            return;
        }

        float modifiedDamage =
                EliteBehaviorHooks.modifyOutgoingDamage(
                        attackingMob,
                        event.getAmount()
                );

        event.setAmount(
                modifiedDamage
        );
    }

    @SubscribeEvent
    public static void onLivingHurt(
            LivingHurtEvent event
    ) {
        if (
                !(event.getEntity() instanceof Mob mob)
        ) {
            return;
        }

        if (!EliteData.isElite(mob)) {
            return;
        }

        float modifiedDamage =
                EliteBehaviorHooks.modifyIncomingDamage(
                        mob,
                        event.getSource(),
                        event.getAmount()
                );

        if (
                event.getAmount()
                        >= mob.getHealth()
        ) {
            boolean intercepted =
                    EliteBehaviorHooks.handleLethalDamage(
                            mob,
                            event.getAmount()
                    );

            if (intercepted) {
                event.setAmount(
                        0.0F
                );
            }
        }

        event.setAmount(
                modifiedDamage
        );
    }

    private static Mob findResponsibleMob(
            Entity entity
    ) {
        if (entity instanceof Mob mob) {
            return mob;
        }

        if (
                entity instanceof Projectile projectile
                && projectile.getOwner() instanceof Mob mob
        ) {
            return mob;
        }

        return null;
    }
}