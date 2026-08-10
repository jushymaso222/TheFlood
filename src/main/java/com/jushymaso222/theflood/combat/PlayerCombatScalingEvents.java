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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

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

        ServerPlayer player
                = getAttackingPlayer(event);

        if (player == null) {
            return;
        }

        int heat
                = HeatManager.getEffectiveHeat(player);

        double multiplier
                = MobScaling.getPlayerDamageMultiplier(
                        mob.getType(),
                        heat
                );

        event.setAmount(
                (float) (event.getAmount()
                * multiplier)
        );
    }

    private static double getSwarmMultiplier(
            ServerPlayer player
    ) {
        double radius = 10.0;
        double radiusSquared
                = radius * radius;

        int attackers = 0;

        for (Mob mob
                : player.serverLevel()
                        .getEntitiesOfClass(
                                Mob.class,
                                player.getBoundingBox()
                                        .inflate(radius)
                        )) {

            if (!MobScaling.isFloodMob(mob.getType())) {
                continue;
            }

            if (player.distanceToSqr(mob)
                    > radiusSquared) {
                continue;
            }

            /*
         * Only count mobs that are actually focused
         * on this player.
             */
            if (mob.getTarget() != player) {
                continue;
            }

            attackers++;
        }

        if (attackers <= 3) {
            return 1.0;
        }

        if (attackers <= 7) {
            return 1.20;
        }

        if (attackers <= 12) {
            return 1.50;
        }

        return 1.80;
    }

    private static double getMekaSuitPressureMultiplier(
            ServerPlayer player,
            int heat
    ) {
        if (!isWearingFullMekaSuit(player)) {
            return 1.0;
        }

        if (heat <= 40) {
            return 1.0;
        }

        if (heat <= 60) {
            return 1.25;
        }

        if (heat <= 75) {
            return 1.75;
        }

        if (heat <= 90) {
            return 2.50;
        }

        return 3.50;
    }

    private static boolean isWearingFullMekaSuit(
            ServerPlayer player
    ) {
        return isItem(
                player.getItemBySlot(
                        EquipmentSlot.HEAD
                ),
                "mekanism",
                "mekasuit_helmet"
        )
                && isItem(
                        player.getItemBySlot(
                                EquipmentSlot.CHEST
                        ),
                        "mekanism",
                        "mekasuit_bodyarmor"
                )
                && isItem(
                        player.getItemBySlot(
                                EquipmentSlot.LEGS
                        ),
                        "mekanism",
                        "mekasuit_pants"
                )
                && isItem(
                        player.getItemBySlot(
                                EquipmentSlot.FEET
                        ),
                        "mekanism",
                        "mekasuit_boots"
                );
    }

    private static boolean isItem(
            ItemStack stack,
            String namespace,
            String path
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id
                = ForgeRegistries.ITEMS
                        .getKey(
                                stack.getItem()
                        );

        return id != null
                && id.getNamespace()
                        .equals(namespace)
                && id.getPath()
                        .equals(path);
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

        Mob mob
                = getAttackingMob(event);

        if (mob == null) {
            return;
        }

        if (!MobScaling.isFloodMob(mob.getType())) {
            return;
        }

        int heat
                = HeatManager.getEffectiveHeat(
                        player
                );

        double heatMultiplier
                = MobScaling.getMobDamageMultiplier(
                        mob.getType(),
                        heat
                );

        double swarmMultiplier
                = getSwarmMultiplier(
                        player
                );

        double mekaPressureMultiplier
                = getMekaSuitPressureMultiplier(
                        player,
                        heat
                );

        double finalMultiplier
                = heatMultiplier
                * swarmMultiplier
                * mekaPressureMultiplier;

        event.setAmount(
                (float) (event.getAmount()
                * finalMultiplier)
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
        /*
        * DamageSource#getEntity() represents the entity
        * responsible for causing the damage.
        *
        * For melee this is normally the player directly.
        * Well-behaved modded ranged damage sources may
        * also identify the shooter here.
         */
        Entity causingEntity
                = event.getSource().getEntity();

        if (causingEntity instanceof ServerPlayer player) {
            return player;
        }

        /*
        * Fallback for Projectile-based attacks whose
        * player ownership is stored on the projectile.
         */
        Entity directEntity
                = event.getSource().getDirectEntity();

        if (directEntity instanceof Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) {
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
        Entity sourceEntity
                = event.getSource().getEntity();

        if (sourceEntity instanceof Mob mob) {
            return mob;
        }

        Entity directEntity
                = event.getSource().getDirectEntity();

        if (directEntity instanceof Projectile projectile
                && projectile.getOwner() instanceof Mob mob) {
            return mob;
        }

        return null;
    }

    /*
        * TEMPORARY DEVELOPMENT DEBUGGING
        *
        * Prints damage-source information so modded weapons
        * can be checked for compatibility with Flood scaling.
        *
        * Remove this before release.
     */
    @SubscribeEvent
    public static void debugMobDamageSource(
            LivingHurtEvent event
    ) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (!MobScaling.isFloodMob(mob.getType())) {
            return;
        }

        Entity causingEntity
                = event.getSource().getEntity();

        Entity directEntity
                = event.getSource().getDirectEntity();

        String causingClass
                = causingEntity == null
                        ? "null"
                        : causingEntity
                                .getClass()
                                .getName();

        String directClass
                = directEntity == null
                        ? "null"
                        : directEntity
                                .getClass()
                                .getName();

        System.out.println(
                "[The Flood Damage Debug]"
                + " victim="
                + mob.getType()
                + " source="
                + event.getSource().getMsgId()
                + " causing="
                + causingClass
                + " direct="
                + directClass
                + " damage="
                + event.getAmount()
        );
    }
}
