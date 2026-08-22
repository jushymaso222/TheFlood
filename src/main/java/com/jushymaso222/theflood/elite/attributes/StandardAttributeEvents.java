package com.jushymaso222.theflood.elite.attributes;

import java.util.List;
import java.util.UUID;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.EliteAttributes;
import com.jushymaso222.theflood.elite.EliteData;

import com.jushymaso222.theflood.elite.attributes.specials.CorrosiveAttribute;
import com.jushymaso222.theflood.elite.attributes.specials.SuppressingAttribute;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class StandardAttributeEvents {

    /*
     * =====================================================
     * NBT KEYS
     * =====================================================
     */

    private static final String LAST_HURT_TIME_KEY =
            "theflood_attribute_last_hurt_time";

    private static final String VENGEFUL_END_KEY =
            "theflood_attribute_vengeful_end";

    private static final String ADRENALIZED_END_KEY =
            "theflood_attribute_adrenalized_end";

    private static final String UNYIELDING_END_KEY =
            "theflood_attribute_unyielding_end";


    /*
     * =====================================================
     * TEMPORARY ATTRIBUTE MODIFIER UUIDs
     * =====================================================
     */

    private static final UUID VENGEFUL_DAMAGE_MODIFIER_ID =
            UUID.fromString(
                    "8bb5d147-3d55-4202-a8d1-87f22019d1d4"
            );

    private static final UUID ADRENALIZED_SPEED_MODIFIER_ID =
            UUID.fromString(
                    "10c67173-c327-4d5d-a513-578b25937eab"
            );

    private static final UUID UNYIELDING_ARMOR_MODIFIER_ID =
            UUID.fromString(
                    "a36f5ac3-cef7-44c1-93f2-08b53afad5ab"
            );


    /*
     * =====================================================
     * GENERAL BALANCE VALUES
     * =====================================================
     */

    private static final int REGEN_DELAY_TICKS =
            100;

    private static final int TEMPORARY_BUFF_DURATION_TICKS =
            60;

    private static final float UNYIELDING_TRIGGER_PERCENT =
            0.20F;


    private StandardAttributeEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (
                event.phase
                        != TickEvent.Phase.END
                || event.player.level()
                        .isClientSide()
        ) {
            return;
        }

        CorrosiveAttribute.tickPlayer(
                event.player
        );
        SuppressingAttribute.tickPlayer(
                event.player
        );
    }

    @SubscribeEvent
    public static void onLivingDamage(
            LivingDamageEvent event
    ) {
        /*
        * Server only.
        */
        if (
                event.getEntity()
                        .level()
                        .isClientSide()
        ) {
            return;
        }

        /*
        * Special outgoing effects currently care about
        * Elites successfully damaging players.
        */
        if (
                !(event.getEntity() instanceof ServerPlayer player)
        ) {
            return;
        }

        Mob attacker =
                findResponsibleMob(
                        event.getSource()
                );

        if (
                attacker == null
                || !EliteData.isElite(
                        attacker
                )
        ) {
            return;
        }

        float damageDealt =
                event.getAmount();

        if (damageDealt <= 0.0F) {
            return;
        }

        /*
        * Forward the completed damage event to every
        * Special Attribute owned by this Elite.
        *
        * Vampiric uses this to heal.
        */
        EliteAttributes.onDamageDealt(
                attacker,
                player,
                damageDealt
        );
    }


    /*
     * =====================================================
     * LIVING TICK
     * =====================================================
     */

    @SubscribeEvent
    public static void onLivingTick(
            LivingEvent.LivingTickEvent event
    ) {
        if (
                !(event.getEntity() instanceof Mob elite)
        ) {
            return;
        }

        if (
                elite.level().isClientSide()
                || !EliteData.isElite(
                        elite
                )
        ) {
            return;
        }

        /*
        * Tick every Special Attribute owned by this Elite.
        *
        * Radioactive, and any future continuously-running
        * Special Attributes, are dispatched from here.
        */
        EliteAttributes.tickSpecialAttributes(
                elite
        );

        List<EliteAttributes.RolledAttribute> attributes =
                getAttributes(
                        elite
                );

        if (attributes.isEmpty()) {
            return;
        }

        long gameTime =
                elite.level()
                        .getGameTime();

        for (
                EliteAttributes.RolledAttribute attribute :
                attributes
        ) {
            switch (attribute.id()) {

                case "regenerative" ->
                        tickRegenerative(
                                elite,
                                attribute.level(),
                                gameTime
                        );

                case "fleet" ->
                        tickFleet(
                                elite,
                                attribute.level()
                        );

                case "berserk" ->
                        tickBerserk(
                                elite,
                                attribute.level()
                        );

                case "vengeful" ->
                        tickVengeful(
                                elite,
                                gameTime
                        );

                case "adrenalized" ->
                        tickAdrenalized(
                                elite,
                                gameTime
                        );

                case "unyielding" ->
                        tickUnyielding(
                                elite,
                                gameTime
                        );

                default -> {
                }
            }
        }
    }


    /*
     * =====================================================
     * DAMAGE EVENTS
     * =====================================================
     */

    @SubscribeEvent
    public static void onLivingHurt(
            LivingHurtEvent event
    ) {
        if (
                event.getEntity()
                        .level()
                        .isClientSide()
        ) {
            return;
        }

        /*
         * Elite is the victim.
         */
        if (
                event.getEntity() instanceof Mob elite
                && EliteData.isElite(
                        elite
                )
        ) {
            handleEliteDamaged(
                    event,
                    elite
            );

            return;
        }

        /*
         * Player is the victim.
         */
        if (
                event.getEntity() instanceof ServerPlayer player
        ) {
            Mob attacker =
                    findResponsibleMob(
                            event.getSource()
                    );

            if (
                    attacker == null
                    || !EliteData.isElite(
                            attacker
                    )
            ) {
                return;
            }

            handleEliteDamagingPlayer(
                    event,
                    attacker,
                    player
            );
        }
    }


    /*
     * =====================================================
     * REGENERATIVE
     * =====================================================
     */

    private static void tickRegenerative(
            Mob elite,
            int level,
            long gameTime
    ) {
        if (
                elite.getHealth()
                        >= elite.getMaxHealth()
        ) {
            return;
        }

        long lastHurtTime =
                elite.getPersistentData()
                        .getLong(
                                LAST_HURT_TIME_KEY
                        );

        if (
                gameTime - lastHurtTime
                        < REGEN_DELAY_TICKS
        ) {
            return;
        }

        float healPerTick =
                switch (level) {
                    case 2 -> 0.15F;
                    case 3 -> 0.25F;
                    default -> 0.08F;
                };

        elite.heal(
                healPerTick
        );
    }


    /*
     * =====================================================
     * FLEET
     * =====================================================
     *
     * Faster only while actively pursuing a player.
     */

    private static void tickFleet(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.25D;
                    case 3 -> 0.40D;
                    default -> 0.15D;
                };

        if (
                elite.getTarget() instanceof ServerPlayer
        ) {
            ensureTransientModifier(
                    elite,
                    Attributes.MOVEMENT_SPEED,
                    FleetHolder.ID,
                    "The Flood Fleet",
                    bonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
        } else {
            removeModifier(
                    elite,
                    Attributes.MOVEMENT_SPEED,
                    FleetHolder.ID
            );
        }
    }

    private static final class FleetHolder {
        private static final UUID ID =
                UUID.fromString(
                        "3582f4ad-cf43-4994-b431-e24950af35d2"
                );
    }


    /*
     * =====================================================
     * BERSERK
     * =====================================================
     *
     * Damage rises as HP falls.
     */

    private static void tickBerserk(
            Mob elite,
            int level
    ) {
        float healthPercent =
                elite.getHealth()
                        / elite.getMaxHealth();

        double maximumBonus =
                switch (level) {
                    case 2 -> 0.50D;
                    case 3 -> 0.75D;
                    default -> 0.30D;
                };

        double bonus =
                maximumBonus
                        * (
                        1.0D
                                - healthPercent
                );

        replaceTransientModifier(
                elite,
                Attributes.ATTACK_DAMAGE,
                BerserkHolder.ID,
                "The Flood Berserk",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    private static final class BerserkHolder {
        private static final UUID ID =
                UUID.fromString(
                        "28b8f6eb-e57a-4470-959e-c97a423116ac"
                );
    }


    /*
     * =====================================================
     * EXECUTIONER
     * =====================================================
     *
     * Deals more damage to injured players.
     */

    private static void applyExecutioner(
            LivingHurtEvent event,
            ServerPlayer player,
            int level
    ) {
        float healthPercent =
                player.getHealth()
                        / player.getMaxHealth();

        if (
                healthPercent
                        > 0.40F
        ) {
            return;
        }

        double multiplier =
                switch (level) {
                    case 2 -> 1.35D;
                    case 3 -> 1.50D;
                    default -> 1.20D;
                };

        event.setAmount(
                (float) (
                        event.getAmount()
                                * multiplier
                )
        );
    }


    /*
     * =====================================================
     * VENGEFUL
     * =====================================================
     *
     * Taking damage grants temporary attack damage.
     */

    private static void triggerVengeful(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.30D;
                    case 3 -> 0.45D;
                    default -> 0.20D;
                };

        replaceTransientModifier(
                elite,
                Attributes.ATTACK_DAMAGE,
                VENGEFUL_DAMAGE_MODIFIER_ID,
                "The Flood Vengeful",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        elite.getPersistentData()
                .putLong(
                        VENGEFUL_END_KEY,
                        elite.level()
                                .getGameTime()
                                + TEMPORARY_BUFF_DURATION_TICKS
                );
    }

    private static void tickVengeful(
            Mob elite,
            long gameTime
    ) {
        long end =
                elite.getPersistentData()
                        .getLong(
                                VENGEFUL_END_KEY
                        );

        if (
                end > 0L
                && gameTime >= end
        ) {
            removeModifier(
                    elite,
                    Attributes.ATTACK_DAMAGE,
                    VENGEFUL_DAMAGE_MODIFIER_ID
            );

            elite.getPersistentData()
                    .remove(
                            VENGEFUL_END_KEY
                    );
        }
    }


    /*
     * =====================================================
     * ADRENALIZED
     * =====================================================
     *
     * Taking damage grants temporary movement speed.
     */

    private static void triggerAdrenalized(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.30D;
                    case 3 -> 0.45D;
                    default -> 0.20D;
                };

        replaceTransientModifier(
                elite,
                Attributes.MOVEMENT_SPEED,
                ADRENALIZED_SPEED_MODIFIER_ID,
                "The Flood Adrenalized",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        elite.getPersistentData()
                .putLong(
                        ADRENALIZED_END_KEY,
                        elite.level()
                                .getGameTime()
                                + TEMPORARY_BUFF_DURATION_TICKS
                );
    }

    private static void tickAdrenalized(
            Mob elite,
            long gameTime
    ) {
        long end =
                elite.getPersistentData()
                        .getLong(
                                ADRENALIZED_END_KEY
                        );

        if (
                end > 0L
                && gameTime >= end
        ) {
            removeModifier(
                    elite,
                    Attributes.MOVEMENT_SPEED,
                    ADRENALIZED_SPEED_MODIFIER_ID
            );

            elite.getPersistentData()
                    .remove(
                            ADRENALIZED_END_KEY
                    );
        }
    }


    /*
     * =====================================================
     * UNYIELDING
     * =====================================================
     *
     * Large hits trigger temporary armor.
     */

    private static void triggerUnyielding(
            Mob elite,
            int level,
            float incomingDamage
    ) {
        float threshold =
                elite.getMaxHealth()
                        * UNYIELDING_TRIGGER_PERCENT;

        if (
                incomingDamage
                        < threshold
        ) {
            return;
        }

        double armorBonus =
                switch (level) {
                    case 2 -> 8.0D;
                    case 3 -> 12.0D;
                    default -> 5.0D;
                };

        replaceTransientModifier(
                elite,
                Attributes.ARMOR,
                UNYIELDING_ARMOR_MODIFIER_ID,
                "The Flood Unyielding",
                armorBonus,
                AttributeModifier.Operation.ADDITION
        );

        elite.getPersistentData()
                .putLong(
                        UNYIELDING_END_KEY,
                        elite.level()
                                .getGameTime()
                                + TEMPORARY_BUFF_DURATION_TICKS
                );
    }

    private static void tickUnyielding(
            Mob elite,
            long gameTime
    ) {
        long end =
                elite.getPersistentData()
                        .getLong(
                                UNYIELDING_END_KEY
                        );

        if (
                end > 0L
                && gameTime >= end
        ) {
            removeModifier(
                    elite,
                    Attributes.ARMOR,
                    UNYIELDING_ARMOR_MODIFIER_ID
            );

            elite.getPersistentData()
                    .remove(
                            UNYIELDING_END_KEY
                    );
        }
    }


    /*
     * =====================================================
     * DAMAGE RESISTANCES
     * =====================================================
     */

    private static void applyFireproof(
            LivingHurtEvent event,
            DamageSource source,
            int level
    ) {
        if (
                !source.is(
                        DamageTypeTags.IS_FIRE
                )
        ) {
            return;
        }

        float multiplier =
                switch (level) {
                    case 2 -> 0.35F;
                    case 3 -> 0.10F;
                    default -> 0.60F;
                };

        event.setAmount(
                event.getAmount()
                        * multiplier
        );
    }

    private static void applyBlastproof(
            LivingHurtEvent event,
            DamageSource source,
            int level
    ) {
        if (
                !source.is(
                        DamageTypeTags.IS_EXPLOSION
                )
        ) {
            return;
        }

        float multiplier =
                switch (level) {
                    case 2 -> 0.50F;
                    case 3 -> 0.25F;
                    default -> 0.70F;
                };

        event.setAmount(
                event.getAmount()
                        * multiplier
        );
    }

    private static void applyDeflecting(
            LivingHurtEvent event,
            DamageSource source,
            int level
    ) {
        if (
                !(source.getDirectEntity()
                        instanceof Projectile)
        ) {
            return;
        }

        float multiplier =
                switch (level) {
                    case 2 -> 0.60F;
                    case 3 -> 0.40F;
                    default -> 0.80F;
                };

        event.setAmount(
                event.getAmount()
                        * multiplier
        );
    }


    /*
     * =====================================================
     * EVENT ROUTING
     * =====================================================
     */

    private static void handleEliteDamaged(
            LivingHurtEvent event,
            Mob elite
    ) {
        elite.getPersistentData()
                .putLong(
                        LAST_HURT_TIME_KEY,
                        elite.level()
                                .getGameTime()
                );

        EliteAttributes.onDamaged(
                elite,
                event.getSource(),
                event.getAmount()
        );

        List<EliteAttributes.RolledAttribute> attributes =
                getAttributes(
                        elite
                );

        for (
                EliteAttributes.RolledAttribute attribute :
                attributes
        ) {
            switch (attribute.id()) {

                case "vengeful" ->
                        triggerVengeful(
                                elite,
                                attribute.level()
                        );

                case "adrenalized" ->
                        triggerAdrenalized(
                                elite,
                                attribute.level()
                        );

                case "unyielding" ->
                        triggerUnyielding(
                                elite,
                                attribute.level(),
                                event.getAmount()
                        );

                case "fireproof" ->
                        applyFireproof(
                                event,
                                event.getSource(),
                                attribute.level()
                        );

                case "blastproof" ->
                        applyBlastproof(
                                event,
                                event.getSource(),
                                attribute.level()
                        );

                case "deflecting" ->
                        applyDeflecting(
                                event,
                                event.getSource(),
                                attribute.level()
                        );

                default -> {
                }
            }
        }
    }

    private static void handleEliteDamagingPlayer(
            LivingHurtEvent event,
            Mob attacker,
            ServerPlayer player
    ) {
        for (
                EliteAttributes.RolledAttribute attribute :
                getAttributes(
                        attacker
                )
        ) {
            if (
                    "executioner".equals(
                            attribute.id()
                    )
            ) {
                applyExecutioner(
                        event,
                        player,
                        attribute.level()
                );
            }
        }
    }


    /*
     * =====================================================
     * HELPERS
     * =====================================================
     */

    private static List<EliteAttributes.RolledAttribute> getAttributes(
            Mob elite
    ) {
        return EliteAttributes.deserialize(
                EliteData.getAttributes(
                        elite
                )
        );
    }

    private static Mob findResponsibleMob(
            DamageSource source
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

    private static void ensureTransientModifier(
            Mob elite,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            UUID id,
            String name,
            double amount,
            AttributeModifier.Operation operation
    ) {
        AttributeInstance instance =
                elite.getAttribute(
                        attribute
                );

        if (
                instance == null
                || instance.getModifier(
                        id
                ) != null
        ) {
            return;
        }

        instance.addTransientModifier(
                new AttributeModifier(
                        id,
                        name,
                        amount,
                        operation
                )
        );
    }

    private static void replaceTransientModifier(
            Mob elite,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            UUID id,
            String name,
            double amount,
            AttributeModifier.Operation operation
    ) {
        AttributeInstance instance =
                elite.getAttribute(
                        attribute
                );

        if (instance == null) {
            return;
        }

        instance.removeModifier(
                id
        );

        instance.addTransientModifier(
                new AttributeModifier(
                        id,
                        name,
                        amount,
                        operation
                )
        );
    }

    private static void removeModifier(
            Mob elite,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            UUID id
    ) {
        AttributeInstance instance =
                elite.getAttribute(
                        attribute
                );

        if (instance == null) {
            return;
        }

        instance.removeModifier(
                id
        );
    }
}