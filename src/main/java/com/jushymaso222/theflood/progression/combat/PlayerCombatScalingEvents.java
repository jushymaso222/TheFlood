package com.jushymaso222.theflood.progression.combat;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.compat.mekanism.MekanismEnergyCompat;
import com.jushymaso222.theflood.compat.mekanism.TurretOwnershipManager;
import com.jushymaso222.theflood.progression.HeatManager;
import com.jushymaso222.theflood.progression.scaling.MobScaling;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import com.jushymaso222.theflood.elite.drops.boon.BoonData;
import com.jushymaso222.theflood.elite.drops.boon.BoonType;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;

import com.jushymaso222.theflood.elite.EliteManager;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PlayerCombatScalingEvents {

        /*
        * Extra MekaSuit energy consumed for each point
        * of Flood-scaled incoming damage.
        *
        * TEMPORARY BALANCE VALUE.
        * Move to config once the behavior feels right.
        */
        private static final int MEKASUIT_FIXED_DRAIN_PER_DAMAGE = 50_000;

/*
 * FULL MEKASUIT ENERGY PRESSURE
 *
 * A fully powered MekaSuit may prevent damage before
 * LivingHurtEvent ever reaches The Flood.
 *
 * LivingAttackEvent occurs earlier, so we use it only
 * to impose extra energy cost on a complete MekaSuit.
 *
 * The suit still protects the player normally.
 * We do NOT bypass armor and we do NOT force damage.
 */
        @SubscribeEvent(
                priority = EventPriority.HIGH
        )
        public static void onMekaSuitAttacked(
                LivingAttackEvent event
        ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
        }

        /*
        * This compatibility feature only exists when the
        * player is actually wearing all four MekaSuit pieces.
        */
        if (!isWearingFullMekaSuit(player)) {
                return;
        }

        Mob mob =
                getAttackingMob(
                        event.getSource()
                );

        if (mob == null) {
                return;
        }

        if (!MobScaling.isFloodMob(mob.getType())) {
                return;
        }

        int heat =
                HeatManager.getEffectiveHeat(
                        player
                );

        /*
        * Use the same effective mob damage scaling that
        * normal armor encounters.
        */
        int scalingHeat =
                EliteManager.getScalingHeat(
                        mob,
                        heat
                );

        double heatMultiplier =
                MobScaling.getMobDamageMultiplier(
                        mob.getType(),
                        scalingHeat
                );

        double swarmMultiplier =
                getSwarmMultiplier(
                        player
                );

        double effectiveDamage =
                event.getAmount()
                        * heatMultiplier
                        * swarmMultiplier;

        /*
        * Heat <= 40:
        * Do not impose any EXTRA MekaSuit drain.
        *
        * At this stage of progression, let obtaining
        * the MekaSuit feel appropriately powerful.
        */
        double capacityDrainPercent =
        getMekaCapacityDrainPercent(
                heat
        );

if (capacityDrainPercent <= 0.0) {
    return;
}

long totalCapacity =
        getTotalMekaSuitCapacity(
                player
        );

if (totalCapacity <= 0) {
    return;
}

/*
 * Fixed drain.
 *
 * This portion is based on the actual danger of
 * the attacking mob and already includes swarm
 * pressure through effectiveDamage.
 */
long fixedDrain =
        Math.round(
                effectiveDamage
                        * MEKASUIT_FIXED_DRAIN_PER_DAMAGE
        );

        /*
        * Percentage drain.
        *
        * This keeps massive energy-capacity upgrades from
        * eventually making Flood pressure irrelevant.
        *
        * Swarms increase this component as well.
        */
        long capacityDrain =
                Math.round(
                        totalCapacity
                                * capacityDrainPercent
                                * swarmMultiplier
                );

        long requestedDrain =
                fixedDrain
                        + capacityDrain;

        if (requestedDrain <= 0) {
        return;
        }

        long actuallyDrained =
                drainMekaSuitEnergy(
                        player,
                        requestedDrain
                );

        if (requestedDrain <= 0) {
                return;
        }

        /*
        * TEMP DEBUG
        */
        // player.sendSystemMessage(
        //         Component.literal(
        //                 "[Flood Meka Debug] "
        //                         + "Heat="
        //                         + heat
        //                         + " | Damage="
        //                         + format(effectiveDamage)
        //                         + " | Capacity="
        //                         + formatEnergy(totalCapacity)
        //                         + " J"
        //                         + " | Drain="
        //                         + String.format(
        //                                 "%.3f%%",
        //                                 capacityDrainPercent * 100.0
        //                         )
        //                         + " | Fixed="
        //                         + formatEnergy(fixedDrain)
        //                         + " J"
        //                         + " | CapacityDrain="
        //                         + formatEnergy(capacityDrain)
        //                         + " J"
        //                         + " | Total="
        //                         + formatEnergy(requestedDrain)
        //                         + " J"
        //                         + " | Drained="
        //                         + formatEnergy(actuallyDrained)
        //                         + " J"
        //         )
        // );
        // debugMekaSuitEnergy(
        //         player
        // );
        }

        private static Mob getAttackingMob(
                net.minecraft.world.damagesource.DamageSource source
        ) {
        Entity sourceEntity =
                source.getEntity();

        if (sourceEntity instanceof Mob mob) {
                return mob;
        }

        Entity directEntity =
                source.getDirectEntity();

        if (
                directEntity instanceof Projectile projectile
                && projectile.getOwner() instanceof Mob mob
        ) {
                return mob;
        }

        return null;
        }

    private PlayerCombatScalingEvents() {
    }

    /*
     * PLAYER -> MOB
     *
     * Mobs retain their real server-side health.
     * Player damage is modified so that each mob
     * feels like it has the effective health
     * appropriate for this player's Heat.
     *
     * Also handles automated registered turret damage.
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

        int heat;

        /*
         * Normal player-owned attack.
         *
         * Includes:
         * - melee
         * - vanilla projectiles
         * - TACZ
         * - Create guns
         * - magic
         * - Mekanism handheld weapons
         * - other modded attacks that preserve
         *   their responsible player
         */
        if (player != null) {
            heat =
                    HeatManager.getEffectiveHeat(
                            player
                    );
        }

        /*
         * Mekanism Turrets fallback.
         *
         * Their laser DamageSource provides neither
         * a causing entity nor a direct entity, so
         * ownership is recovered from our registered
         * turret position data.
         */
        else if (
                "mekanism_turrets.laser".equals(
                        event.getSource().getMsgId()
                )
                        && mob.level()
                        instanceof ServerLevel level
        ) {
            TurretOwnershipManager.ResolvedTurretOwner turretOwner =
                    TurretOwnershipManager.findHighestHeatOwner(
                            level,
                            mob.blockPosition(),
                            64
                    );

            if (turretOwner == null) {
                return;
            }

            heat =
                    turretOwner.heat();
        }

        /*
         * No identifiable player progression.
         *
         * Leave the damage unchanged.
         */
        else {
            return;
        }

        /*
        * ============================================
        * CAPABILITY OBSERVATION
        * ============================================
        *
        * Record the player's damage BEFORE The Flood
        * modifies it.
        *
        * This is important because Capability should
        * measure the player's actual offensive power,
        * not the consequences of our own adaptive
        * scaling.
        *
        * Automated turrets are intentionally excluded
        * for now because there is no direct player
        * DamageSource associated with them.
        */
        if (player != null) {
        CapabilityManager.recordOffensiveDamage(
                player,
                event.getAmount()
        );
        }

        int scalingHeat =
                EliteManager.getScalingHeat(
                        mob,
                        heat
                );

        double multiplier =
                MobScaling.getPlayerDamageMultiplier(
                        mob.getType(),
                        scalingHeat
                );

        double boonMultiplier =
                1.0D;

        if (
                player != null
                && BoonData.hasBoon(
                        player,
                        BoonType.ATTACK
                )
        ) {
        boonMultiplier =
                1.50D;
        }

        event.setAmount(
                (float) (
                        event.getAmount()
                                * multiplier
                                * boonMultiplier
                )
        );
    }

    /*
     * MOB -> PLAYER
     *
     * Runs at HIGH priority so our Flood scaling is
     * applied before normal-priority protection systems
     * get a chance to process the incoming damage.
     *
     * Damage pressure consists of:
     *
     * Heat scaling
     *      ×
     * Swarm scaling
     *      ×
     * MekaSuit pressure
     */
    @SubscribeEvent(
            priority = EventPriority.HIGH
    )
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
                HeatManager.getEffectiveHeat(
                        player
                );

        boolean wearingMekaSuit =
                isWearingFullMekaSuit(
                        player
                );

        int scalingHeat =
                EliteManager.getScalingHeat(
                        mob,
                        heat
                );

        double heatMultiplier =
                MobScaling.getMobDamageMultiplier(
                        mob.getType(),
                        scalingHeat
                );

        double swarmMultiplier =
                getSwarmMultiplier(
                        player
                );

        double finalMultiplier =
                heatMultiplier
                        * swarmMultiplier;

        float originalDamage =
                event.getAmount();

        double boonMultiplier =
                BoonData.hasBoon(
                        player,
                        BoonType.DEFENSE
                )
                        ? 0.60D
                        : 1.0D;

        float finalDamage =
                (float) (
                        originalDamage
                                * finalMultiplier
                                * boonMultiplier
                );

        event.setAmount(
                finalDamage
        );

        /*
         * TEMPORARY DEVELOPMENT DEBUGGING
         *
         * This tells us whether:
         *
         * 1. The full MekaSuit was detected.
         * 2. Heat scaling was applied.
         * 3. Swarm scaling was applied.
         * 4. Meka pressure was applied.
         * 5. The final LivingHurtEvent amount increased.
         *
         * Remove before release.
         */
        // player.sendSystemMessage(
        //         Component.literal(
        //                 "[Flood Armor Debug] "
        //                         + "MekaSuit="
        //                         + wearingMekaSuit
        //                         + " | Heat="
        //                         + heat
        //                         + " | BaseDamage="
        //                         + format(originalDamage)
        //                         + " | Heat×"
        //                         + format(heatMultiplier)
        //                         + " | Swarm×"
        //                         + format(swarmMultiplier)
        //                         + " | Final="
        //                         + format(finalDamage)
        //         )
        // );
    }

    /*
     * SWARM PRESSURE
     *
     * Only Flood mobs:
     * - within 10 blocks
     * - alive/loaded
     * - actively targeting this player
     *
     * contribute to swarm pressure.
     */
    private static double getSwarmMultiplier(
            ServerPlayer player
    ) {
        double radius =
                10.0;

        double radiusSquared =
                radius * radius;

        int attackers =
                0;

        for (Mob mob :
                player.serverLevel()
                        .getEntitiesOfClass(
                                Mob.class,
                                player.getBoundingBox()
                                        .inflate(radius)
                        )) {

            if (!MobScaling.isFloodMob(mob.getType())) {
                continue;
            }

            if (
                    player.distanceToSqr(mob)
                            > radiusSquared
            ) {
                continue;
            }

            /*
             * Don't count random nearby mobs.
             *
             * They must actually be attacking
             * this player.
             */
            if (mob.getTarget() != player) {
                continue;
            }

            attackers++;
        }

        /*
         * 1-3 attackers:
         * normal Flood damage.
         */
        if (attackers <= 3) {
            return 1.0;
        }

        /*
         * 4-7 attackers.
         */
        if (attackers <= 7) {
            return 1.20;
        }

        /*
         * 8-12 attackers.
         */
        if (attackers <= 12) {
            return 1.50;
        }

        /*
         * 13+ attackers.
         */
        return 1.80;
    }

    /*
     * Detects a complete Mekanism MekaSuit without
     * requiring Mekanism as a compile dependency.
     */
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

    /*
     * Generic registry-ID item check.
     */
    private static boolean isItem(
            ItemStack stack,
            String namespace,
            String path
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS
                        .getKey(
                                stack.getItem()
                        );

        return id != null
                && namespace.equals(
                        id.getNamespace()
                )
                && path.equals(
                        id.getPath()
                );
    }

    /*
     * TEMPORARY MEKASUIT DEBUGGING
     *
     * Prints the actual armor registry IDs into chat.
     *
     * This only runs when the full MekaSuit detector
     * returns false.
     */
//     private static void debugArmorIds(
//             ServerPlayer player
//     ) {
//         EquipmentSlot[] armorSlots = {
//                 EquipmentSlot.HEAD,
//                 EquipmentSlot.CHEST,
//                 EquipmentSlot.LEGS,
//                 EquipmentSlot.FEET
//         };

//         for (EquipmentSlot slot :
//                 armorSlots) {

//             ItemStack stack =
//                     player.getItemBySlot(
//                             slot
//                     );

//             ResourceLocation id =
//                     stack.isEmpty()
//                             ? null
//                             : ForgeRegistries.ITEMS
//                                     .getKey(
//                                             stack.getItem()
//                                     );

//             player.sendSystemMessage(
//                     Component.literal(
//                             "[Flood Armor Debug] "
//                                     + slot.getName()
//                                     + "="
//                                     + (
//                                     id == null
//                                             ? "EMPTY/UNKNOWN"
//                                             : id.toString()
//                                     )
//                     )
//             );
//         }
//     }

//     private static void debugMekaSuitEnergy(
//         ServerPlayer player
// ) {
//     EquipmentSlot[] slots = {
//             EquipmentSlot.HEAD,
//             EquipmentSlot.CHEST,
//             EquipmentSlot.LEGS,
//             EquipmentSlot.FEET
//     };

//     for (EquipmentSlot slot : slots) {

//         ItemStack stack =
//                 player.getItemBySlot(
//                         slot
//                 );

//         if (
//                 stack.isEmpty()
//                 || !isMekaSuitPiece(stack)
//         ) {
//             continue;
//         }

//         MekanismEnergyCompat.EnergyInfo info =
//                 MekanismEnergyCompat.getEnergyInfo(
//                         stack
//                 );

//         if (info == null) {
//             player.sendSystemMessage(
//                     Component.literal(
//                             "[Flood Meka Energy] "
//                                     + slot.getName()
//                                     + "=UNKNOWN"
//                     )
//             );

//             continue;
//         }

//         double percentage =
//                 info.capacity() <= 0
//                         ? 0.0
//                         : (
//                         info.stored()
//                                 / (double) info.capacity()
//                 ) * 100.0;

//         player.sendSystemMessage(
//                 Component.literal(
//                         "[Flood Meka Energy] "
//                                 + slot.getName()
//                                 + "="
//                                 + formatEnergy(
//                                         info.stored()
//                                 )
//                                 + " / "
//                                 + formatEnergy(
//                                         info.capacity()
//                                 )
//                                 + " J ("
//                                 + String.format(
//                                         "%.1f",
//                                         percentage
//                                 )
//                                 + "%)"
//                 )
//         );
//     }
// }

private static String formatEnergy(
        long energy
) {
    if (energy >= 1_000_000_000L) {
        return String.format(
                "%.2fG",
                energy / 1_000_000_000.0
        );
    }

    if (energy >= 1_000_000L) {
        return String.format(
                "%.2fM",
                energy / 1_000_000.0
        );
    }

    if (energy >= 1_000L) {
        return String.format(
                "%.2fk",
                energy / 1_000.0
        );
    }

    return Long.toString(
            energy
    );
}

    /*
     * Finds the player responsible for damage.
     *
     * Supports:
     * - vanilla melee
     * - modded melee
     * - bows
     * - crossbows
     * - vanilla projectiles
     * - TACZ
     * - Create guns
     * - magic
     * - Mekanism handheld weapons
     * - modded attacks that correctly preserve
     *   the responsible player in DamageSource
     */
    private static ServerPlayer getAttackingPlayer(
            LivingHurtEvent event
    ) {
        Entity causingEntity =
                event.getSource()
                        .getEntity();

        if (
                causingEntity
                instanceof ServerPlayer player
        ) {
            return player;
        }

        Entity directEntity =
                event.getSource()
                        .getDirectEntity();

        if (
                directEntity
                instanceof Projectile projectile
                && projectile.getOwner()
                instanceof ServerPlayer player
        ) {
            return player;
        }

        return null;
    }

    /*
     * Finds the Flood mob ultimately responsible
     * for player damage.
     *
     * Supports:
     * - melee
     * - skeleton arrows
     * - other Projectile-based mob attacks
     */
    private static Mob getAttackingMob(
                LivingHurtEvent event
        ) {
        return getAttackingMob(
                event.getSource()
        );
        }

        private static double getMekaCapacityDrainPercent(
        int heat
) {
    /*
     * Let early MekaSuit progression feel extremely
     * powerful.
     */
    if (heat <= 40) {
        return 0.0;
    }

    if (heat <= 60) {
        return 0.00025; // 0.025%
    }

    if (heat <= 75) {
        return 0.00050; // 0.050%
    }

    if (heat <= 90) {
        return 0.00100; // 0.100%
    }

    if (heat < 100) {
        return 0.00175; // 0.175%
    }

    return 0.00250; // 0.250%
}

private static long getTotalMekaSuitCapacity(
        ServerPlayer player
) {
    long totalCapacity = 0;

    EquipmentSlot[] slots = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    for (EquipmentSlot slot : slots) {
        ItemStack stack =
                player.getItemBySlot(slot);

        if (
                stack.isEmpty()
                || !isMekaSuitPiece(stack)
        ) {
            continue;
        }

        MekanismEnergyCompat.EnergyInfo info =
                MekanismEnergyCompat.getEnergyInfo(
                        stack
                );

        if (info == null) {
            continue;
        }

        totalCapacity +=
                info.capacity();
    }

    return totalCapacity;
}

        private static long drainMekaSuitEnergy(
        ServerPlayer player,
        long requestedDrain
) {
    if (requestedDrain <= 0) {
        return 0;
    }

    if (!MekanismEnergyCompat.isAvailable()) {
        return 0;
    }

    EquipmentSlot[] slots = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    /*
     * Spread the extra Flood pressure across
     * all four MekaSuit pieces.
     */
    long baseDrainPerPiece =
            requestedDrain / slots.length;

    long remainder =
            requestedDrain % slots.length;

    long totalDrained = 0;

    for (int i = 0; i < slots.length; i++) {
        ItemStack stack =
                player.getItemBySlot(
                        slots[i]
                );

        if (
                stack.isEmpty()
                || !isMekaSuitPiece(stack)
        ) {
            continue;
        }

        long pieceDrain =
                baseDrainPerPiece;

        /*
         * Distribute remainder instead of losing
         * energy to integer division.
         */
        if (i < remainder) {
            pieceDrain++;
        }

        if (pieceDrain <= 0) {
            continue;
        }

        long drained =
                MekanismEnergyCompat.extractEnergy(
                        stack,
                        pieceDrain
                );

        totalDrained += drained;
    }

    return totalDrained;
}

        private static boolean isMekaSuitPiece(
                ItemStack stack
        ) {
        if (stack.isEmpty()) {
                return false;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS
                        .getKey(
                                stack.getItem()
                        );

        if (id == null) {
                return false;
        }

        if (!"mekanism".equals(
                id.getNamespace()
        )) {
                return false;
        }

        return switch (id.getPath()) {
                case "mekasuit_helmet",
                "mekasuit_bodyarmor",
                "mekasuit_pants",
                "mekasuit_boots" -> true;

                default -> false;
        };
        }

    /*
     * TEMPORARY DEVELOPMENT DEBUGGING
     *
     * Prints DamageSource information for attacks
     * against Flood mobs.
     *
     * Useful for compatibility testing.
     *
     * Remove before release.
     */
//     @SubscribeEvent
//     public static void debugMobDamageSource(
//             LivingHurtEvent event
//     ) {
//         if (!(event.getEntity() instanceof Mob mob)) {
//             return;
//         }

//         if (!MobScaling.isFloodMob(mob.getType())) {
//             return;
//         }

//         Entity causingEntity =
//                 event.getSource()
//                         .getEntity();

//         Entity directEntity =
//                 event.getSource()
//                         .getDirectEntity();

//         String causingClass =
//                 causingEntity == null
//                         ? "null"
//                         : causingEntity
//                                 .getClass()
//                                 .getName();

//         String directClass =
//                 directEntity == null
//                         ? "null"
//                         : directEntity
//                                 .getClass()
//                                 .getName();

//         System.out.println(
//                 "[The Flood Damage Debug]"
//                         + " victim="
//                         + mob.getType()
//                         + " source="
//                         + event.getSource()
//                                 .getMsgId()
//                         + " causing="
//                         + causingClass
//                         + " direct="
//                         + directClass
//                         + " damage="
//                         + event.getAmount()
//         );
//     }

    /*
     * Keeps debug numbers readable.
     */
    private static String format(
            double value
    ) {
        return String.format(
                "%.2f",
                value
        );
    }
}