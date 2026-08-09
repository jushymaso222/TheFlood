package com.jushymaso222.theflood.combat;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.progression.HeatManager;
import com.jushymaso222.theflood.scaling.MobScaling;

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
public final class PlayerCombatScalingEvents {

    private PlayerCombatScalingEvents() {
    }

    /*
     * PLAYER -> MOB
     *
     * Mobs retain their real server-side health.
     * Player damage is modified so that each mob
     * feels like it has the effective health
     * appropriate for this player's Heat.
     */
    @SubscribeEvent
    public static void onMobHurt(
            LivingHurtEvent event
    ) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (!MobScaling.isFloodMob(mob.getType())) {
            return;
        }

        ServerPlayer player =
                getAttackingPlayer(event);

        if (player == null) {
            return;
        }

        int heat =
                HeatManager.getEffectiveHeat(player);

        double multiplier =
                MobScaling.getPlayerDamageMultiplier(
                        mob.getType(),
                        heat
                );

        event.setAmount(
                (float) (
                        event.getAmount()
                                * multiplier
                )
        );
    }

    /*
     * MOB -> PLAYER
     *
     * Mobs retain their real server-side damage.
     * Incoming damage is modified according to the
     * victim player's Effective Heat.
     */
    @SubscribeEvent
    public static void onPlayerHurt(
            LivingHurtEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Mob mob =
                getAttackingMob(event);

        if (mob == null) {
            return;
        }

        if (!MobScaling.isFloodMob(mob.getType())) {
            return;
        }

        int heat =
                HeatManager.getEffectiveHeat(player);

        double multiplier =
                MobScaling.getMobDamageMultiplier(
                        mob.getType(),
                        heat
                );

        event.setAmount(
                (float) (
                        event.getAmount()
                                * multiplier
                )
        );
    }

    /*
     * Finds the player responsible for damage.
     *
     * Supports:
     * - melee
     * - bows
     * - crossbows
     * - other Projectile-based player attacks
     */
    private static ServerPlayer getAttackingPlayer(
            LivingHurtEvent event
    ) {
        Entity sourceEntity =
                event.getSource().getEntity();

        if (sourceEntity instanceof ServerPlayer player) {
            return player;
        }

        Entity directEntity =
                event.getSource().getDirectEntity();

        if (
                directEntity instanceof Projectile projectile
                && projectile.getOwner()
                instanceof ServerPlayer player
        ) {
            return player;
        }

        return null;
    }

    /*
     * Finds the mob responsible for damage.
     *
     * Supports:
     * - melee
     * - skeleton arrows
     * - other Projectile-based mob attacks
     */
    private static Mob getAttackingMob(
            LivingHurtEvent event
    ) {
        Entity sourceEntity =
                event.getSource().getEntity();

        if (sourceEntity instanceof Mob mob) {
            return mob;
        }

        Entity directEntity =
                event.getSource().getDirectEntity();

        if (
                directEntity instanceof Projectile projectile
                && projectile.getOwner() instanceof Mob mob
        ) {
            return mob;
        }

        return null;
    }
}