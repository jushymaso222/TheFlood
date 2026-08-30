package com.jushymaso222.theflood.network;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import com.jushymaso222.theflood.progression.network.SyncHeatPacket;
import com.jushymaso222.theflood.guide.network.SyncServerSettingsPacket;
import com.jushymaso222.theflood.elite.network.SyncEliteStatePacket;
import com.jushymaso222.theflood.elite.debug.network.SyncMobStatsPacket;
import com.jushymaso222.theflood.progression.network.FloodXpGainPacket;
import com.jushymaso222.theflood.milestone.network.MilestoneUnlockedPacket;
import com.jushymaso222.theflood.progression.capability.network.SyncCapabilityStatsPacket;

public final class FloodNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(
                            TheFlood.MOD_ID,
                            "main"
                    ),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    private static int id = 0;

    private FloodNetwork() {
    }

    public static void register() {

        CHANNEL.registerMessage(
                id++,
                SyncCapabilityStatsPacket.class,
                SyncCapabilityStatsPacket::encode,
                SyncCapabilityStatsPacket::decode,
                SyncCapabilityStatsPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                MilestoneUnlockedPacket.class,
                MilestoneUnlockedPacket::encode,
                MilestoneUnlockedPacket::decode,
                MilestoneUnlockedPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                FloodXpGainPacket.class,
                FloodXpGainPacket::encode,
                FloodXpGainPacket::decode,
                FloodXpGainPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SyncEliteStatePacket.class,
                SyncEliteStatePacket::encode,
                SyncEliteStatePacket::decode,
                SyncEliteStatePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SyncMobStatsPacket.class,
                SyncMobStatsPacket::encode,
                SyncMobStatsPacket::decode,
                SyncMobStatsPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SyncServerSettingsPacket.class,
                SyncServerSettingsPacket::encode,
                SyncServerSettingsPacket::decode,
                SyncServerSettingsPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.ToggleTeamChatPacket.class,
                TeamNetworkingPackets.ToggleTeamChatPacket::encode,
                TeamNetworkingPackets.ToggleTeamChatPacket::decode,
                TeamNetworkingPackets.ToggleTeamChatPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.SyncTeamChatModePacket.class,
                TeamNetworkingPackets.SyncTeamChatModePacket::encode,
                TeamNetworkingPackets.SyncTeamChatModePacket::decode,
                TeamNetworkingPackets.SyncTeamChatModePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.SyncTeamHudPacket.class,
                TeamNetworkingPackets.SyncTeamHudPacket::encode,
                TeamNetworkingPackets.SyncTeamHudPacket::decode,
                TeamNetworkingPackets.SyncTeamHudPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.SyncPlayerHeatPacket.class,
                TeamNetworkingPackets.SyncPlayerHeatPacket::encode,
                TeamNetworkingPackets.SyncPlayerHeatPacket::decode,
                TeamNetworkingPackets.SyncPlayerHeatPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.RequestPendingTeamInvitesPacket.class,
                TeamNetworkingPackets.RequestPendingTeamInvitesPacket::encode,
                TeamNetworkingPackets.RequestPendingTeamInvitesPacket::decode,
                TeamNetworkingPackets.RequestPendingTeamInvitesPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.SyncPendingTeamInvitesPacket.class,
                TeamNetworkingPackets.SyncPendingTeamInvitesPacket::encode,
                TeamNetworkingPackets.SyncPendingTeamInvitesPacket::decode,
                TeamNetworkingPackets.SyncPendingTeamInvitesPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.RequestInviteCandidatesPacket.class,
                TeamNetworkingPackets.RequestInviteCandidatesPacket::encode,
                TeamNetworkingPackets.RequestInviteCandidatesPacket::decode,
                TeamNetworkingPackets.RequestInviteCandidatesPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.SyncInviteCandidatesPacket.class,
                TeamNetworkingPackets.SyncInviteCandidatesPacket::encode,
                TeamNetworkingPackets.SyncInviteCandidatesPacket::decode,
                TeamNetworkingPackets.SyncInviteCandidatesPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SyncHeatPacket.class,
                SyncHeatPacket::encode,
                SyncHeatPacket::decode,
                SyncHeatPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.SyncTeamStatePacket.class,
                TeamNetworkingPackets.SyncTeamStatePacket::encode,
                TeamNetworkingPackets.SyncTeamStatePacket::decode,
                TeamNetworkingPackets.SyncTeamStatePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.CreateTeamPacket.class,
                TeamNetworkingPackets.CreateTeamPacket::encode,
                TeamNetworkingPackets.CreateTeamPacket::decode,
                TeamNetworkingPackets.CreateTeamPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.InvitePlayerPacket.class,
                TeamNetworkingPackets.InvitePlayerPacket::encode,
                TeamNetworkingPackets.InvitePlayerPacket::decode,
                TeamNetworkingPackets.InvitePlayerPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.AcceptTeamInvitePacket.class,
                TeamNetworkingPackets.AcceptTeamInvitePacket::encode,
                TeamNetworkingPackets.AcceptTeamInvitePacket::decode,
                TeamNetworkingPackets.AcceptTeamInvitePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.DeclineTeamInvitePacket.class,
                TeamNetworkingPackets.DeclineTeamInvitePacket::encode,
                TeamNetworkingPackets.DeclineTeamInvitePacket::decode,
                TeamNetworkingPackets.DeclineTeamInvitePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.LeaveTeamPacket.class,
                TeamNetworkingPackets.LeaveTeamPacket::encode,
                TeamNetworkingPackets.LeaveTeamPacket::decode,
                TeamNetworkingPackets.LeaveTeamPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                TeamNetworkingPackets.DisbandTeamPacket.class,
                TeamNetworkingPackets.DisbandTeamPacket::encode,
                TeamNetworkingPackets.DisbandTeamPacket::decode,
                TeamNetworkingPackets.DisbandTeamPacket::handle
        );
    }
}