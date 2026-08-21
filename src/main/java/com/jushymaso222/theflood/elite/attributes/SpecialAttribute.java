package com.jushymaso222.theflood.elite.attributes;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public interface SpecialAttribute {

    String id();

    String displayName();

    /*
     * Called once when the attribute is applied.
     */
    default void onApplied(
            Mob elite,
            int level
    ) {
    }

    /*
     * Called every server tick.
     *
     * Radioactive will use this.
     */
    default void tick(
            Mob elite,
            int level
    ) {
    }

    /*
     * Modify damage this Elite receives.
     */
    default float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage,
            int level
    ) {
        return damage;
    }

    /*
     * Modify damage this Elite deals.
     */
    default float modifyOutgoingDamage(
            Mob elite,
            Player target,
            float damage,
            int level
    ) {
        return damage;
    }

    /*
     * Called after this Elite successfully damages
     * a player.
     *
     * Vampiric will probably use this rather than
     * merely modifying damage.
     */
    default void onDamageDealt(
            Mob elite,
            Player target,
            float damageDealt,
            int level
    ) {
    }

    /*
     * Called when this Elite is damaged.
     */
    default void onDamaged(
            Mob elite,
            DamageSource source,
            float damageTaken,
            int level
    ) {
    }
}