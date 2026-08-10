package com.jushymaso222.theflood.team;

import com.jushymaso222.theflood.debug.DummyPlayerManager;
import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.network.packet.TeamNetworkingPackets;
import com.jushymaso222.theflood.progression.HeatManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import com.jushymaso222.theflood.progression.PlayerFloodData;

import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TeamManager {

    /*
     * Invited player UUID -> Team UUIDs that invited them.
     *
     * Invitations are temporary server-memory data.
     */
    private static final Map<UUID, Set<UUID>> PENDING_INVITES =
            new HashMap<>();

    private TeamManager() {
    }

    // ------------------------------------------------------------
    // TEAM CREATION
    // ------------------------------------------------------------

    public static FloodTeam createTeam(
            ServerPlayer owner,
            String name,
            DyeColor color
    ) {
        FloodTeamSavedData data =
                FloodTeamSavedData.get(
                        owner.server
                );

        UUID ownerId =
                owner.getUUID();

        if (
                data.getPlayerTeams()
                        .containsKey(ownerId)
        ) {
            owner.sendSystemMessage(
                    Component.literal(
                            "You are already in a team."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return null;
        }

        String cleanName =
                name.trim();

        if (cleanName.isEmpty()) {
            owner.sendSystemMessage(
                    Component.literal(
                            "Team name cannot be empty."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return null;
        }

        if (cleanName.length() > 24) {
            cleanName =
                    cleanName.substring(
                            0,
                            24
                    );
        }

        if (color == null) {
            color =
                    DyeColor.WHITE;
        }

        UUID teamId =
                UUID.randomUUID();

        FloodTeam team =
                new FloodTeam(
                        teamId,
                        ownerId,
                        cleanName,
                        color
                );

        data.getTeams().put(
                teamId,
                team
        );

        data.getPlayerTeams().put(
                ownerId,
                teamId
        );

        PlayerTeamData.setTeam(
                owner,
                teamId
        );

        /*
        * Initialize the team's Heat immediately.
        *
        * A one-player team's Heat should equal
        * the owner's Solo Heat.
        */
        recalculateTeamHeat(
                owner.server,
                team
        );

        data.setDirty();

        owner.sendSystemMessage(
                Component.literal(
                        "Created team "
                ).append(
                        Component.literal(
                                cleanName
                        ).withStyle(
                                ChatFormatting.GREEN
                        )
                )
        );

        syncTeamStateToPlayer(
                owner
        );

        HeatManager.syncHeatToPlayer(
                owner
        );

        return team;
    }

    // ------------------------------------------------------------
    // LOOKUPS
    // ------------------------------------------------------------

    public static boolean isInTeam(
            ServerPlayer player
    ) {
        return getTeamForPlayer(
                player
        ) != null;
    }

    public static FloodTeam getTeamForPlayer(
            ServerPlayer player
    ) {
        FloodTeamSavedData data =
                FloodTeamSavedData.get(
                        player.server
                );

        UUID playerId =
                player.getUUID();

        UUID teamId =
                data.getPlayerTeams()
                        .get(playerId);

        if (teamId == null) {
            return null;
        }

        FloodTeam team =
                data.getTeams()
                        .get(teamId);

        /*
         * Repair stale SavedData references.
         */
        if (team == null) {
            data.getPlayerTeams()
                    .remove(playerId);

            PlayerTeamData.clearTeam(
                    player
            );

            data.setDirty();

            return null;
        }

        return team;
    }

    public static FloodTeam getTeam(
            MinecraftServer server,
            UUID teamId
    ) {
        return FloodTeamSavedData
                .get(server)
                .getTeams()
                .get(teamId);
    }

    public static UUID getTeamIdForPlayer(
            ServerPlayer player
    ) {
        return FloodTeamSavedData
                .get(player.server)
                .getPlayerTeams()
                .get(player.getUUID());
    }

    // ------------------------------------------------------------
    // INVITING
    // ------------------------------------------------------------

    public static boolean invitePlayer(
            ServerPlayer inviter,
            ServerPlayer invited
    ) {
        FloodTeam team =
                getTeamForPlayer(
                        inviter
                );

        if (team == null) {
            inviter.sendSystemMessage(
                    Component.literal(
                            "You must be in a team to invite players."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return false;
        }

        /*
         * Currently only owners can invite.
         */
        if (
                !team.isOwner(
                        inviter.getUUID()
                )
        ) {
            inviter.sendSystemMessage(
                    Component.literal(
                            "Only the team owner can invite players."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return false;
        }

        if (
                inviter.getUUID()
                        .equals(
                                invited.getUUID()
                        )
        ) {
            inviter.sendSystemMessage(
                    Component.literal(
                            "You cannot invite yourself."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return false;
        }

        /*
         * Players already belonging to another team
         * cannot receive invitations.
         */
        if (isInTeam(invited)) {
            inviter.sendSystemMessage(
                    Component.literal(
                            invited.getGameProfile()
                                    .getName()
                                    + " is already in a team."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return false;
        }

        Set<UUID> invites =
                PENDING_INVITES.computeIfAbsent(
                        invited.getUUID(),
                        ignored ->
                                new HashSet<>()
                );

        if (
                !invites.add(
                        team.getTeamId()
                )
        ) {
            inviter.sendSystemMessage(
                    Component.literal(
                            invited.getGameProfile()
                                    .getName()
                                    + " already has an invite from your team."
                    ).withStyle(
                            ChatFormatting.YELLOW
                    )
            );

            return false;
        }

        /*
         * Development dummies immediately accept invitations.
         */
        if (
                DummyPlayerManager.isDummy(
                        invited
                )
        ) {
            boolean joined =
                    acceptInvite(
                            invited,
                            team.getTeamId()
                    );

            if (joined) {
                inviter.sendSystemMessage(
                        Component.literal(
                                invited.getGameProfile()
                                        .getName()
                                        + " automatically joined "
                                        + team.getName()
                                        + "."
                        ).withStyle(
                                ChatFormatting.GREEN
                        )
                );
            }

            return joined;
        }

        inviter.sendSystemMessage(
                Component.literal(
                        "Invited "
                                + invited.getGameProfile()
                                        .getName()
                                + " to "
                                + team.getName()
                                + "."
                ).withStyle(
                        ChatFormatting.GREEN
                )
        );

        invited.sendSystemMessage(
                Component.literal(
                        inviter.getGameProfile()
                                .getName()
                                + " invited you to join "
                                + team.getName()
                                + "."
                ).withStyle(
                        ChatFormatting.YELLOW
                )
        );

        return true;
    }

    // ------------------------------------------------------------
    // ACCEPT / DECLINE
    // ------------------------------------------------------------

    public static boolean acceptInvite(
            ServerPlayer player,
            UUID teamId
    ) {
        if (isInTeam(player)) {
            if (
                    !DummyPlayerManager.isDummy(
                            player
                    )
            ) {
                player.sendSystemMessage(
                        Component.literal(
                                "You are already in a team."
                        ).withStyle(
                                ChatFormatting.RED
                        )
                );
            }

            return false;
        }

        UUID playerId =
                player.getUUID();

        Set<UUID> invites =
                PENDING_INVITES.get(
                        playerId
                );

        if (
                invites == null
                || !invites.contains(
                        teamId
                )
        ) {
            if (
                    !DummyPlayerManager.isDummy(
                            player
                    )
            ) {
                player.sendSystemMessage(
                        Component.literal(
                                "That team invitation is no longer valid."
                        ).withStyle(
                                ChatFormatting.RED
                        )
                );
            }

            return false;
        }

        FloodTeamSavedData data =
                FloodTeamSavedData.get(
                        player.server
                );

        FloodTeam team =
                data.getTeams()
                        .get(teamId);

        if (team == null) {
            invites.remove(
                    teamId
            );

            if (invites.isEmpty()) {
                PENDING_INVITES.remove(
                        playerId
                );
            }

            return false;
        }

        team.addMember(
                playerId
        );

        data.getPlayerTeams().put(
                playerId,
                teamId
        );

        PlayerTeamData.setTeam(
                player,
                teamId
        );

        clearInvites(
                playerId
        );

        /*
         * Team membership affects Team Heat,
         * so recalculate it now.
         *
         * This assumes you've already added the
         * recalculateTeamHeat helper we discussed.
         */
        recalculateTeamHeat(
                player.server,
                team
        );

        data.setDirty();

        broadcastToTeam(
                player.server,
                team,
                Component.literal(
                        player.getGameProfile()
                                .getName()
                                + " joined the team."
                ).withStyle(
                        ChatFormatting.GREEN
                )
        );

        /*
         * FakePlayers don't have a real connected client,
         * so don't try to send them GUI packets.
         */
        if (
                !DummyPlayerManager.isDummy(
                        player
                )
        ) {
            syncTeamStateToPlayer(
                    player
            );

            HeatManager.syncHeatToPlayer(
                    player
            );
        }

        /*
         * Every real team member may now have a new Team Heat.
         */
        syncTeamStateToAllMembers(
                player.server,
                team
        );

        return true;
    }

    public static void declineInvite(
            ServerPlayer player,
            UUID teamId
    ) {
        Set<UUID> invites =
                PENDING_INVITES.get(
                        player.getUUID()
                );

        if (invites == null) {
            return;
        }

        invites.remove(
                teamId
        );

        if (invites.isEmpty()) {
            PENDING_INVITES.remove(
                    player.getUUID()
            );
        }
    }

    public static Set<UUID> getPendingInvites(
            UUID playerId
    ) {
        return Set.copyOf(
                PENDING_INVITES.getOrDefault(
                        playerId,
                        Collections.emptySet()
                )
        );
    }

    // ------------------------------------------------------------
    // LEAVING
    // ------------------------------------------------------------

    public static boolean leaveTeam(
            ServerPlayer player
    ) {
        FloodTeamSavedData data =
                FloodTeamSavedData.get(
                        player.server
                );

        FloodTeam team =
                getTeamForPlayer(
                        player
                );

        if (team == null) {
            return false;
        }

        /*
         * Owner-leave transfer behavior still comes later.
         */
        if (
                team.isOwner(
                        player.getUUID()
                )
        ) {
            if (
                    !DummyPlayerManager.isDummy(
                            player
                    )
            ) {
                player.sendSystemMessage(
                        Component.literal(
                                "You are the team owner. Transfer ownership or disband the team before leaving."
                        ).withStyle(
                                ChatFormatting.RED
                        )
                );
            }

            return false;
        }

        UUID playerId =
                player.getUUID();

        team.removeMember(
                playerId
        );

        data.getPlayerTeams()
                .remove(playerId);

        PlayerTeamData.clearTeam(
                player
        );

        data.setDirty();

        recalculateTeamHeat(
                player.server,
                team
        );

        if (
                !DummyPlayerManager.isDummy(
                        player
                )
        ) {
            player.sendSystemMessage(
                    Component.literal(
                            "You left "
                                    + team.getName()
                                    + "."
                    ).withStyle(
                            ChatFormatting.YELLOW
                    )
            );

            syncTeamStateToPlayer(
                    player
            );

            HeatManager.syncHeatToPlayer(
                    player
            );
        }

        broadcastToTeam(
                player.server,
                team,
                Component.literal(
                        player.getGameProfile()
                                .getName()
                                + " left the team."
                ).withStyle(
                        ChatFormatting.YELLOW
                )
        );

        syncTeamStateToAllMembers(
                player.server,
                team
        );

        return true;
    }

    // ------------------------------------------------------------
    // DISBANDING
    // ------------------------------------------------------------

    public static boolean disbandTeam(
            ServerPlayer owner
    ) {
        FloodTeamSavedData data =
                FloodTeamSavedData.get(
                        owner.server
                );

        FloodTeam team =
                getTeamForPlayer(
                        owner
                );

        if (
                team == null
                || !team.isOwner(
                        owner.getUUID()
                )
        ) {
            return false;
        }

        Set<UUID> members =
                new HashSet<>(
                        team.getMembers()
                );

        for (UUID memberId :
                members) {

            data.getPlayerTeams()
                    .remove(memberId);

            ServerPlayer member =
                    owner.server
                            .getPlayerList()
                            .getPlayer(memberId);

            /*
             * FakePlayers may not exist in PlayerList,
             * so check our dummy registry too.
             */
            if (member == null) {
                member =
                        DummyPlayerManager.getDummy(
                                memberId
                        );
            }

            if (member != null) {
                PlayerTeamData.clearTeam(
                        member
                );

                if (
                        !DummyPlayerManager.isDummy(
                                member
                        )
                ) {
                    member.sendSystemMessage(
                            Component.literal(
                                    "Team "
                                            + team.getName()
                                            + " was disbanded."
                            ).withStyle(
                                    ChatFormatting.RED
                            )
                    );

                    syncTeamStateToPlayer(
                            member
                    );

                    HeatManager.syncHeatToPlayer(
                            member
                    );
                }
            }
        }

        removeInvitesForTeam(
                team.getTeamId()
        );

        data.getTeams()
                .remove(
                        team.getTeamId()
                );

        data.setDirty();

        return true;
    }

    // ------------------------------------------------------------
    // PLAYER LOGIN / REPAIR
    // ------------------------------------------------------------

    public static void syncPlayerTeamReference(
            ServerPlayer player
    ) {
        FloodTeamSavedData data =
                FloodTeamSavedData.get(
                        player.server
                );

        UUID playerId =
                player.getUUID();

        UUID teamId =
                data.getPlayerTeams()
                        .get(playerId);

        if (teamId == null) {
            PlayerTeamData.clearTeam(
                    player
            );

            syncTeamStateToPlayer(
                    player
            );

            return;
        }

        FloodTeam team =
                data.getTeams()
                        .get(teamId);

        if (team == null) {
            data.getPlayerTeams()
                    .remove(playerId);

            PlayerTeamData.clearTeam(
                    player
            );

            data.setDirty();

            syncTeamStateToPlayer(
                    player
            );

            return;
        }

        if (
                !team.hasMember(
                        playerId
                )
        ) {
            team.addMember(
                    playerId
            );

            data.setDirty();
        }

        PlayerTeamData.setTeam(
                player,
                teamId
        );

        syncTeamStateToPlayer(
                player
        );
    }

    // ------------------------------------------------------------
    // TEAM HEAT
    // ------------------------------------------------------------

    public static void recalculateTeamHeat(
        MinecraftServer server,
        FloodTeam team
    ) {
        int highestSoloHeat = 1;

        /*
        * Team progression is anchored to the most progressed
        * individual player in the team.
        */
        for (UUID memberId :
                team.getMembers()) {

                ServerPlayer member =
                        server.getPlayerList()
                                .getPlayer(memberId);

                /*
                * Development FakePlayers may not be present
                * in Minecraft's normal PlayerList.
                */
                if (member == null) {
                member =
                        DummyPlayerManager.getDummy(
                                memberId
                        );
                }

                if (member == null) {
                continue;
                }

                highestSoloHeat =
                        Math.max(
                                highestSoloHeat,
                                HeatManager.getSoloHeat(
                                        member
                                )
                        );
        }

        /*
        * Only additional members beyond the first
        * contribute a team-size bonus.
        */
        int additionalMembers =
                Math.max(
                        0,
                        team.getMemberCount() - 1
                );

        /*
        * Nonlinear group scaling.
        *
        * Team size:
        *
        * 1 -> +0
        * 2 -> +3
        * 3 -> +7
        * 4 -> +12
        * 5 -> +18
        * 6 -> +25
        *
        * Each additional teammate becomes slightly more
        * valuable than the previous one.
        */
        int baseBonus =
                additionalMembers * 3;

        int synergyBonus =
                additionalMembers
                        * (additionalMembers - 1)
                        / 2;

        int groupBonus =
                baseBonus + synergyBonus;

        int calculatedHeat =
                highestSoloHeat
                        + groupBonus;

        /*
        * Flood Heat can never exceed 100.
        */
        team.setTeamHeat(
                Math.min(
                        PlayerFloodData.MAX_HEAT,
                        Math.max(
                                1,
                                calculatedHeat
                        )
                )
        );

        FloodTeamSavedData
                .get(server)
                .setDirty();
    }

    // ------------------------------------------------------------
    // CLIENT SYNC
    // ------------------------------------------------------------

    public static void syncTeamStateToPlayer(
            ServerPlayer player
    ) {
        /*
         * FakePlayer has no real client connection.
         */
        if (
                DummyPlayerManager.isDummy(
                        player
                )
        ) {
            return;
        }

        FloodTeam team =
                getTeamForPlayer(
                        player
                );

        if (team == null) {
            FloodNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(
                            () -> player
                    ),
                    new TeamNetworkingPackets.SyncTeamStatePacket(
                            false,
                            false,
                            null,
                            "",
                            DyeColor.WHITE,
                            1
                    )
            );

            return;
        }

        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new TeamNetworkingPackets.SyncTeamStatePacket(
                        true,
                        team.isOwner(
                                player.getUUID()
                        ),
                        team.getTeamId(),
                        team.getName(),
                        team.getColor(),
                        team.getTeamHeat()
                )
        );
    }

    public static void syncTeamStateToAllMembers(
            MinecraftServer server,
            FloodTeam team
    ) {
        for (UUID memberId :
                team.getMembers()) {

            ServerPlayer member =
                    server.getPlayerList()
                            .getPlayer(memberId);

            /*
             * No need to sync FakePlayers because there
             * is no client HUD attached to them.
             */
            if (member == null) {
                continue;
            }

            syncTeamStateToPlayer(
                    member
            );

            HeatManager.syncHeatToPlayer(
                    member
            );
        }
    }

    // ------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------

    private static void clearInvites(
            UUID playerId
    ) {
        PENDING_INVITES.remove(
                playerId
        );
    }

    private static void removeInvitesForTeam(
            UUID teamId
    ) {
        PENDING_INVITES.values()
                .forEach(
                        invites ->
                                invites.remove(
                                        teamId
                                )
                );

        PENDING_INVITES.entrySet()
                .removeIf(
                        entry ->
                                entry.getValue()
                                        .isEmpty()
                );
    }

    public static List<ServerPlayer> getInvitablePlayers(
                ServerPlayer invitingPlayer
    ) {
        Map<UUID, ServerPlayer> candidates =
                new LinkedHashMap<>();

        /*
        * Real connected players.
        */
        for (ServerPlayer player :
                invitingPlayer.server
                        .getPlayerList()
                        .getPlayers()) {

                candidates.put(
                        player.getUUID(),
                        player
                );
        }

        /*
        * Development dummy players.
        */
        for (ServerPlayer dummy :
                DummyPlayerManager.getDummies()) {

                candidates.put(
                        dummy.getUUID(),
                        dummy
                );
        }

        List<ServerPlayer> result =
                new ArrayList<>();

        for (ServerPlayer candidate :
                candidates.values()) {

                /*
                * Don't include the inviting player.
                */
                if (
                        candidate.getUUID()
                                .equals(
                                        invitingPlayer.getUUID()
                                )
                ) {
                continue;
                }

                /*
                * Already-teamed players cannot be invited.
                */
                if (
                        getTeamForPlayer(candidate)
                        != null
                ) {
                continue;
                }

                result.add(candidate);
        }

        return result;
    }

    private static void broadcastToTeam(
            MinecraftServer server,
            FloodTeam team,
            Component message
    ) {
        for (UUID memberId :
                team.getMembers()) {

            ServerPlayer player =
                    server.getPlayerList()
                            .getPlayer(memberId);

            /*
             * Don't attempt chat/network traffic to our
             * development FakePlayers.
             */
            if (player != null) {
                player.sendSystemMessage(
                        message
                );
            }
        }
    }
}