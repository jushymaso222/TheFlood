package com.jushymaso222.theflood.elite.behavior;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public interface EliteMutation {

    /*
     * Internal persistent ID.
     *
     * This is what gets stored on the Elite's NBT.
     */
    String id();

    /*
     * Player-facing name.
     *
     * We'll use this later for the text above
     * an Elite's head.
     */
    String displayName();

    /*
     * Called every tick for an Elite using
     * this mutation.
     */
    default void tick(
            Mob elite
    ) {
    }

    /*
     * Allows a mutation to modify damage
     * received by the Elite.
     */
    default float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        return damage;
    }

    /*
     * Called when this Elite damages a player.
     */
    default void onHurtPlayer(
            Mob elite,
            Player player,
            float damage
    ) {
    }

    default float modifyOutgoingDamage(
            Mob elite,
            float damage
    ) {
        return damage;
    }

    default boolean handleLethalDamage(
            Mob elite,
            float incomingDamage
    ) {
        return false;
    }

    /*
     * Called when this Elite dies.
     */
    default void onDeath(
            Mob elite
    ) {
    }
}