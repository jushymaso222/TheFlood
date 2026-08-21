package com.jushymaso222.theflood.elite.debug.network;

import com.jushymaso222.theflood.elite.EliteData;
import com.jushymaso222.theflood.elite.debug.client.ClientMobStatsData;
import com.jushymaso222.theflood.network.FloodNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public final class SyncMobStatsPacket {

    private final boolean open;

    private final int entityId;

    private final String entityName;
    private final String entityType;

    private final boolean elite;

    private final String mutation;
    private final int heat;
    private final List<String> attributes;

    private final float health;
    private final float maxHealth;

    private final double baseAttackDamage;
    private final double attackDamage;

    private final double baseArmor;
    private final double armor;

    private final double baseArmorToughness;
    private final double armorToughness;

    private final double baseAttackKnockback;
    private final double attackKnockback;

    private final double baseKnockbackResistance;
    private final double knockbackResistance;

    private final double baseMovementSpeed;
    private final double movementSpeed;

    private final double baseFollowRange;
    private final double followRange;

    private final String targetName;

    private final boolean noAi;
    private final boolean invulnerable;
    private final boolean onGround;
    private final boolean onFire;

    private final double x;
    private final double y;
    private final double z;


    public SyncMobStatsPacket(
            boolean open,

            int entityId,

            String entityName,
            String entityType,

            boolean elite,

            String mutation,
            int heat,
            List<String> attributes,

            float health,
            float maxHealth,

            double baseAttackDamage,
            double attackDamage,

            double baseArmor,
            double armor,

            double baseArmorToughness,
            double armorToughness,

            double baseAttackKnockback,
            double attackKnockback,

            double baseKnockbackResistance,
            double knockbackResistance,

            double baseMovementSpeed,
            double movementSpeed,

            double baseFollowRange,
            double followRange,

            String targetName,

            boolean noAi,
            boolean invulnerable,
            boolean onGround,
            boolean onFire,

            double x,
            double y,
            double z
    ) {
        this.open =
                open;

        this.entityId =
                entityId;

        this.entityName =
                entityName;

        this.entityType =
                entityType;

        this.elite =
                elite;

        this.mutation =
                mutation;

        this.heat =
                heat;

        this.attributes =
                attributes;

        this.health =
                health;

        this.maxHealth =
                maxHealth;

        this.baseAttackDamage =
                baseAttackDamage;

        this.attackDamage =
                attackDamage;

        this.baseArmor =
                baseArmor;

        this.armor =
                armor;

        this.baseArmorToughness =
                baseArmorToughness;

        this.armorToughness =
                armorToughness;

        this.baseAttackKnockback =
                baseAttackKnockback;

        this.attackKnockback =
                attackKnockback;

        this.baseKnockbackResistance =
                baseKnockbackResistance;

        this.knockbackResistance =
                knockbackResistance;

        this.baseMovementSpeed =
                baseMovementSpeed;

        this.movementSpeed =
                movementSpeed;

        this.baseFollowRange =
                baseFollowRange;

        this.followRange =
                followRange;

        this.targetName =
                targetName;

        this.noAi =
                noAi;

        this.invulnerable =
                invulnerable;

        this.onGround =
                onGround;

        this.onFire =
                onFire;

        this.x =
                x;

        this.y =
                y;

        this.z =
                z;
    }


    public static void sendSnapshot(
            ServerPlayer player,
            Mob mob
    ) {
        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                createSnapshot(
                        mob
                )
        );
    }


    public static void sendClosed(
            ServerPlayer player
    ) {
        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new SyncMobStatsPacket(
                        false,

                        0,

                        "",
                        "",

                        false,

                        "",
                        0,
                        List.of(),

                        0.0F,
                        0.0F,

                        0.0D,
                        0.0D,

                        0.0D,
                        0.0D,

                        0.0D,
                        0.0D,

                        0.0D,
                        0.0D,

                        0.0D,
                        0.0D,

                        0.0D,
                        0.0D,

                        0.0D,
                        0.0D,

                        "",

                        false,
                        false,
                        false,
                        false,

                        0.0D,
                        0.0D,
                        0.0D
                )
        );
    }


    private static SyncMobStatsPacket createSnapshot(
            Mob mob
    ) {
        boolean elite =
                EliteData.isElite(
                        mob
                );

        String mutation =
                elite
                        ? EliteData.getMutation(
                        mob
                )
                        : "";

        int heat =
                elite
                        ? EliteData.getSourceHeat(
                        mob
                )
                        : 0;

        List<String> attributes =
                elite
                        ? EliteData.getAttributes(
                        mob
                )
                        : List.of();

        String targetName =
                mob.getTarget() != null
                        ? mob.getTarget()
                        .getName()
                        .getString()
                        : "NONE";

        ResourceLocation typeId =
                net.minecraft.core.registries.BuiltInRegistries
                        .ENTITY_TYPE
                        .getKey(
                                mob.getType()
                        );

        return new SyncMobStatsPacket(
                true,

                mob.getId(),

                mob.getName()
                        .getString(),

                typeId != null
                        ? typeId.toString()
                        : "unknown",

                elite,

                mutation,
                heat,
                attributes,

                mob.getHealth(),
                mob.getMaxHealth(),

                getBaseValue(
                        mob,
                        Attributes.ATTACK_DAMAGE
                ),

                mob.getAttributeValue(
                        Attributes.ATTACK_DAMAGE
                ),

                getBaseValue(
                        mob,
                        Attributes.ARMOR
                ),

                mob.getAttributeValue(
                        Attributes.ARMOR
                ),

                getBaseValue(
                        mob,
                        Attributes.ARMOR_TOUGHNESS
                ),

                mob.getAttributeValue(
                        Attributes.ARMOR_TOUGHNESS
                ),

                getBaseValue(
                        mob,
                        Attributes.ATTACK_KNOCKBACK
                ),

                mob.getAttributeValue(
                        Attributes.ATTACK_KNOCKBACK
                ),

                getBaseValue(
                        mob,
                        Attributes.KNOCKBACK_RESISTANCE
                ),

                mob.getAttributeValue(
                        Attributes.KNOCKBACK_RESISTANCE
                ),

                getBaseValue(
                        mob,
                        Attributes.MOVEMENT_SPEED
                ),

                mob.getAttributeValue(
                        Attributes.MOVEMENT_SPEED
                ),

                getBaseValue(
                        mob,
                        Attributes.FOLLOW_RANGE
                ),

                mob.getAttributeValue(
                        Attributes.FOLLOW_RANGE
                ),

                targetName,

                mob.isNoAi(),
                mob.isInvulnerable(),
                mob.onGround(),
                mob.isOnFire(),

                mob.getX(),
                mob.getY(),
                mob.getZ()
        );
    }


    private static double getBaseValue(
            Mob mob,
            net.minecraft.world.entity.ai.attributes.Attribute attribute
    ) {
        AttributeInstance instance =
                mob.getAttribute(
                        attribute
                );

        if (instance == null) {
            return 0.0D;
        }

        return instance.getBaseValue();
    }


    public static void encode(
            SyncMobStatsPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBoolean(
                message.open
        );

        buffer.writeInt(
                message.entityId
        );

        buffer.writeUtf(
                message.entityName
        );

        buffer.writeUtf(
                message.entityType
        );

        buffer.writeBoolean(
                message.elite
        );

        buffer.writeUtf(
                message.mutation
        );

        buffer.writeInt(
                message.heat
        );

        buffer.writeCollection(
                message.attributes,
                FriendlyByteBuf::writeUtf
        );

        buffer.writeFloat(
                message.health
        );

        buffer.writeFloat(
                message.maxHealth
        );

        buffer.writeDouble(
                message.baseAttackDamage
        );

        buffer.writeDouble(
                message.attackDamage
        );

        buffer.writeDouble(
                message.baseArmor
        );

        buffer.writeDouble(
                message.armor
        );

        buffer.writeDouble(
                message.baseArmorToughness
        );

        buffer.writeDouble(
                message.armorToughness
        );

        buffer.writeDouble(
                message.baseAttackKnockback
        );

        buffer.writeDouble(
                message.attackKnockback
        );

        buffer.writeDouble(
                message.baseKnockbackResistance
        );

        buffer.writeDouble(
                message.knockbackResistance
        );

        buffer.writeDouble(
                message.baseMovementSpeed
        );

        buffer.writeDouble(
                message.movementSpeed
        );

        buffer.writeDouble(
                message.baseFollowRange
        );

        buffer.writeDouble(
                message.followRange
        );

        buffer.writeUtf(
                message.targetName
        );

        buffer.writeBoolean(
                message.noAi
        );

        buffer.writeBoolean(
                message.invulnerable
        );

        buffer.writeBoolean(
                message.onGround
        );

        buffer.writeBoolean(
                message.onFire
        );

        buffer.writeDouble(
                message.x
        );

        buffer.writeDouble(
                message.y
        );

        buffer.writeDouble(
                message.z
        );
    }


    public static SyncMobStatsPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncMobStatsPacket(
                buffer.readBoolean(),

                buffer.readInt(),

                buffer.readUtf(),
                buffer.readUtf(),

                buffer.readBoolean(),

                buffer.readUtf(),
                buffer.readInt(),
                buffer.readList(
                        FriendlyByteBuf::readUtf
                ),

                buffer.readFloat(),
                buffer.readFloat(),

                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readUtf(),

                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),

                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }


    public static void handle(
            SyncMobStatsPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> {
                    if (!packet.open) {
                        ClientMobStatsData.clear();

                        return;
                    }

                    ClientMobStatsData.set(
                            new ClientMobStatsData.MobStats(
                                    packet.entityId,

                                    packet.entityName,
                                    packet.entityType,

                                    packet.elite,

                                    packet.mutation,
                                    packet.heat,
                                    packet.attributes,

                                    packet.health,
                                    packet.maxHealth,

                                    packet.baseAttackDamage,
                                    packet.attackDamage,

                                    packet.baseArmor,
                                    packet.armor,

                                    packet.baseArmorToughness,
                                    packet.armorToughness,

                                    packet.baseAttackKnockback,
                                    packet.attackKnockback,

                                    packet.baseKnockbackResistance,
                                    packet.knockbackResistance,

                                    packet.baseMovementSpeed,
                                    packet.movementSpeed,

                                    packet.baseFollowRange,
                                    packet.followRange,

                                    packet.targetName,

                                    packet.noAi,
                                    packet.invulnerable,
                                    packet.onGround,
                                    packet.onFire,

                                    packet.x,
                                    packet.y,
                                    packet.z
                            )
                    );
                }
        );

        context.setPacketHandled(
                true
        );
    }
}