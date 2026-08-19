package com.jushymaso222.theflood.progression.combat;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.progression.scaling.MobScaling;
import com.jushymaso222.theflood.spawning.SpawnDirector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class CombatScalingEvents {

    private CombatScalingEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Mob mob) {
            scalePlayerDamageAgainstMob(event, mob);
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            scaleMobDamageAgainstPlayer(event, player);
        }
    }

    private static void scalePlayerDamageAgainstMob(
            LivingHurtEvent event,
            Mob mob
    ) {
        if (!isFloodControlled(mob)) {
            return;
        }

        ServerPlayer attackingPlayer =
                findResponsiblePlayer(event.getSource().getEntity());

        if (attackingPlayer == null) {
            attackingPlayer = findResponsiblePlayer(
                    event.getSource().getDirectEntity()
            );
        }

        if (attackingPlayer == null) {
            return;
        }

        int progressionDay =
                getProgressionDay(attackingPlayer);

        double multiplier =
                MobScaling.getPlayerDamageMultiplier(
                        mob.getType(),
                        progressionDay
                );

        event.setAmount(
                (float) (event.getAmount() * multiplier)
        );
    }

    private static void scaleMobDamageAgainstPlayer(
            LivingHurtEvent event,
            ServerPlayer victim
    ) {
        Mob attackingMob =
                findResponsibleMob(event.getSource().getEntity());

        if (attackingMob == null) {
            attackingMob = findResponsibleMob(
                    event.getSource().getDirectEntity()
            );
        }

        if (attackingMob == null || !isFloodControlled(attackingMob)) {
            return;
        }

        int progressionDay = getProgressionDay(victim);

        double multiplier =
                MobScaling.getMobDamageMultiplier(
                        attackingMob.getType(),
                        progressionDay
                );

        event.setAmount(
                (float) (event.getAmount() * multiplier)
        );
    }

    private static boolean isFloodControlled(Mob mob) {
        return mob.getPersistentData().getBoolean(
                SpawnDirector.FLOOD_CONTROLLED_TAG
        );
    }

    private static ServerPlayer findResponsiblePlayer(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return player;
        }

        if (
                entity instanceof Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player
        ) {
            return player;
        }

        return null;
    }

    private static Mob findResponsibleMob(Entity entity) {
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

    private static int getProgressionDay(ServerPlayer player) {
        return (int) (
                player.serverLevel().getDayTime() / 24_000L
        ) + 1;
    }
}