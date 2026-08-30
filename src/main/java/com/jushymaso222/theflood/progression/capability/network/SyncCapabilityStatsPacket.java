package com.jushymaso222.theflood.progression.capability.network;

import com.jushymaso222.theflood.network.FloodNetwork;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;
import com.jushymaso222.theflood.progression.capability.CapabilityProfile;
import com.jushymaso222.theflood.progression.capability.client.ClientCapabilityStatsData;

import com.jushymaso222.theflood.progression.milestone.MilestoneManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;


public final class SyncCapabilityStatsPacket {

    private final boolean open;

    private final String playerName;


    /*
     * =====================================================
     * OFFENSE
     * =====================================================
     */

    private final double offenseScore;
    private final double effectiveOffense;
    private final double peakOffense;
    private final double offenseConfidence;

    private final long offenseSamples;

    private final double lastOffensiveDamage;
    private final double lastOffenseObservation;
    private final double averageOffensiveDamage;
    private final double peakObservedOffensiveDamage;


    /*
     * =====================================================
     * DEFENSE
     * =====================================================
     */

    private final double defenseScore;
    private final double effectiveDefense;
    private final double peakDefense;
    private final double defenseConfidence;


    /*
     * =====================================================
     * SURVIVAL
     * =====================================================
     */

    private final double survivalScore;
    private final double effectiveSurvival;
    private final double peakSurvival;
    private final double survivalConfidence;


    /*
     * =====================================================
     * PROGRESSION
     * =====================================================
     */

    private final int milestoneProgression;


    public SyncCapabilityStatsPacket(
            boolean open,

            String playerName,

            double offenseScore,
            double effectiveOffense,
            double peakOffense,
            double offenseConfidence,

            long offenseSamples,

            double lastOffensiveDamage,
            double lastOffenseObservation,
            double averageOffensiveDamage,
            double peakObservedOffensiveDamage,

            double defenseScore,
            double effectiveDefense,
            double peakDefense,
            double defenseConfidence,

            double survivalScore,
            double effectiveSurvival,
            double peakSurvival,
            double survivalConfidence,

            int milestoneProgression
    ) {
        this.open =
                open;

        this.playerName =
                playerName;


        this.offenseScore =
                offenseScore;

        this.effectiveOffense =
                effectiveOffense;

        this.peakOffense =
                peakOffense;

        this.offenseConfidence =
                offenseConfidence;

        this.offenseSamples =
                offenseSamples;

        this.lastOffensiveDamage =
                lastOffensiveDamage;

        this.lastOffenseObservation =
                lastOffenseObservation;

        this.averageOffensiveDamage =
                averageOffensiveDamage;

        this.peakObservedOffensiveDamage =
                peakObservedOffensiveDamage;


        this.defenseScore =
                defenseScore;

        this.effectiveDefense =
                effectiveDefense;

        this.peakDefense =
                peakDefense;

        this.defenseConfidence =
                defenseConfidence;


        this.survivalScore =
                survivalScore;

        this.effectiveSurvival =
                effectiveSurvival;

        this.peakSurvival =
                peakSurvival;

        this.survivalConfidence =
                survivalConfidence;


        this.milestoneProgression =
                milestoneProgression;
    }


    /*
     * =====================================================
     * SERVER SEND HELPERS
     * =====================================================
     */

    public static void sendSnapshot(
            ServerPlayer player
    ) {
        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                createSnapshot(
                        player
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
                new SyncCapabilityStatsPacket(
                        false,

                        "",

                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D,

                        0L,

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

                        0
                )
        );
    }


    private static SyncCapabilityStatsPacket createSnapshot(
            ServerPlayer player
    ) {
        CapabilityProfile profile =
                CapabilityManager.get(
                        player
                );

        /*
         * This should normally never happen because
         * CapabilityManager.get() loads the profile,
         * but keep the packet safe regardless.
         */
        if (profile == null) {
            return new SyncCapabilityStatsPacket(
                    true,

                    player.getGameProfile()
                            .getName(),

                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D,

                    0L,

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

                    MilestoneManager.getProgressionValue(
                            player
                    )
            );
        }


        return new SyncCapabilityStatsPacket(
                true,

                player.getGameProfile()
                        .getName(),


                /*
                 * OFFENSE
                 */
                profile.getOffenseScore(),

                profile.getEffectiveOffense(),

                profile.getPeakOffense(),

                profile.getOffenseConfidence(),

                profile.getOffenseSamples(),

                profile.getLastOffensiveDamage(),

                profile.getLastOffenseObservation(),

                profile.getAverageOffensiveDamage(),

                profile.getPeakObservedOffensiveDamage(),


                /*
                 * DEFENSE
                 */
                profile.getDefenseScore(),

                profile.getEffectiveDefense(),

                profile.getPeakDefense(),

                profile.getDefenseConfidence(),


                /*
                 * SURVIVAL
                 */
                profile.getSurvivalScore(),

                profile.getEffectiveSurvival(),

                profile.getPeakSurvival(),

                profile.getSurvivalConfidence(),


                /*
                 * PROGRESSION
                 */
                MilestoneManager.getProgressionValue(
                        player
                )
        );
    }


    /*
     * =====================================================
     * ENCODE
     * =====================================================
     */

    public static void encode(
            SyncCapabilityStatsPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBoolean(
                message.open
        );

        buffer.writeUtf(
                message.playerName
        );


        /*
         * OFFENSE
         */
        buffer.writeDouble(
                message.offenseScore
        );

        buffer.writeDouble(
                message.effectiveOffense
        );

        buffer.writeDouble(
                message.peakOffense
        );

        buffer.writeDouble(
                message.offenseConfidence
        );

        buffer.writeLong(
                message.offenseSamples
        );

        buffer.writeDouble(
                message.lastOffensiveDamage
        );

        buffer.writeDouble(
                message.lastOffenseObservation
        );

        buffer.writeDouble(
                message.averageOffensiveDamage
        );

        buffer.writeDouble(
                message.peakObservedOffensiveDamage
        );


        /*
         * DEFENSE
         */
        buffer.writeDouble(
                message.defenseScore
        );

        buffer.writeDouble(
                message.effectiveDefense
        );

        buffer.writeDouble(
                message.peakDefense
        );

        buffer.writeDouble(
                message.defenseConfidence
        );


        /*
         * SURVIVAL
         */
        buffer.writeDouble(
                message.survivalScore
        );

        buffer.writeDouble(
                message.effectiveSurvival
        );

        buffer.writeDouble(
                message.peakSurvival
        );

        buffer.writeDouble(
                message.survivalConfidence
        );


        /*
         * PROGRESSION
         */
        buffer.writeInt(
                message.milestoneProgression
        );
    }


    /*
     * =====================================================
     * DECODE
     * =====================================================
     */

    public static SyncCapabilityStatsPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncCapabilityStatsPacket(
                buffer.readBoolean(),

                buffer.readUtf(),

                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),

                buffer.readLong(),

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

                buffer.readInt()
        );
    }


    /*
     * =====================================================
     * CLIENT HANDLE
     * =====================================================
     */

    public static void handle(
            SyncCapabilityStatsPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> {

                    if (!packet.open) {
                        ClientCapabilityStatsData.clear();

                        return;
                    }


                    ClientCapabilityStatsData.set(
                            new ClientCapabilityStatsData.CapabilityStats(

                                    packet.playerName,


                                    /*
                                     * OFFENSE
                                     */
                                    packet.offenseScore,
                                    packet.effectiveOffense,
                                    packet.peakOffense,
                                    packet.offenseConfidence,

                                    packet.offenseSamples,

                                    packet.lastOffensiveDamage,
                                    packet.lastOffenseObservation,
                                    packet.averageOffensiveDamage,
                                    packet.peakObservedOffensiveDamage,


                                    /*
                                     * DEFENSE
                                     */
                                    packet.defenseScore,
                                    packet.effectiveDefense,
                                    packet.peakDefense,
                                    packet.defenseConfidence,


                                    /*
                                     * SURVIVAL
                                     */
                                    packet.survivalScore,
                                    packet.effectiveSurvival,
                                    packet.peakSurvival,
                                    packet.survivalConfidence,


                                    /*
                                     * PROGRESSION
                                     */
                                    packet.milestoneProgression
                            )
                    );
                }
        );

        context.setPacketHandled(
                true
        );
    }
}