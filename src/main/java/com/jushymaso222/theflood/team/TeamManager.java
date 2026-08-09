package com.jushymaso222.theflood.team;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.network.packet.TeamNetworkingPackets;

import net.minecraftforge.network.PacketDistributor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TeamManager {

    /*
     * Invited player UUID -> Set of Team UUIDs
     *
     * For now this is temporary server-memory state.
     * Later we can persist invites if we decide they should survive restarts.
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
                FloodTeamSavedData.get(owner.server);

        UUID ownerId = owner.getUUID();

        if (data.getPlayerTeams().containsKey(ownerId)) {
            owner.sendSystemMessage(
                    Component.literal(
                            "You are already in a team."
                    ).withStyle(ChatFormatting.RED)
            );

            return null;
        }

        String cleanName = name.trim();

        if (cleanName.isEmpty()) {
            owner.sendSystemMessage(
                    Component.literal(
                            "Team name cannot be empty."
                    ).withStyle(ChatFormatting.RED)
            );

            return null;
        }

        if (cleanName.length() > 24) {
            cleanName =
                    cleanName.substring(0, 24);
        }

        if (color == null) {
            color = DyeColor.WHITE;
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

        /*
         * Convenience reference stored on the online player.
         */
        PlayerTeamData.setTeam(
                owner,
                teamId
        );

        data.setDirty();

        owner.sendSystemMessage(
                Component.literal("Created team ")
                        .append(
                                Component.literal(cleanName)
                                        .withStyle(ChatFormatting.GREEN)
                        )
        );

        syncTeamStateToPlayer(owner);
        return team;
    }

    // ------------------------------------------------------------
    // TEAM LOOKUPS
    // ------------------------------------------------------------

    public static boolean isInTeam(
            ServerPlayer player
    ) {
        return getTeamForPlayer(player) != null;
    }

    public static FloodTeam getTeamForPlayer(
            ServerPlayer player
    ) {
        FloodTeamSavedData data =
                FloodTeamSavedData.get(player.server);

        UUID teamId =
                data.getPlayerTeams().get(
                        player.getUUID()
                );

        if (teamId == null) {
            return null;
        }

        FloodTeam team =
                data.getTeams().get(teamId);

        /*
         * Repair stale membership if the player map points
         * to a team that no longer exists.
         */
        if (team == null) {
            data.getPlayerTeams().remove(
                    player.getUUID()
            );

            PlayerTeamData.clearTeam(player);

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
        FloodTeamSavedData data =
                FloodTeamSavedData.get(player.server);

        return data.getPlayerTeams().get(
                player.getUUID()
        );
    }

    // ------------------------------------------------------------
    // INVITES
    // ------------------------------------------------------------

    public static boolean invitePlayer(
            ServerPlayer inviter,
            ServerPlayer invited
    ) {
        FloodTeam team =
                getTeamForPlayer(inviter);

        if (team == null) {
            inviter.sendSystemMessage(
                    Component.literal(
                            "You must be in a team to invite players."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        /*
         * For now only the owner may invite.
         *
         * We can loosen this later if you want normal members
         * to be allowed to invite too.
         */
        if (!team.isOwner(inviter.getUUID())) {
            inviter.sendSystemMessage(
                    Component.literal(
                            "Only the team owner can invite players."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        /*
         * Cannot invite yourself.
         */
        if (
                inviter.getUUID()
                        .equals(invited.getUUID())
        ) {
            inviter.sendSystemMessage(
                    Component.literal(
                            "You cannot invite yourself."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        /*
         * Critical rule:
         * a player already belonging to any team cannot
         * receive another team invite.
         */
        if (isInTeam(invited)) {
            inviter.sendSystemMessage(
                    Component.literal(
                            invited.getGameProfile()
                                    .getName()
                                    + " is already in a team."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        Set<UUID> invites =
                PENDING_INVITES.computeIfAbsent(
                        invited.getUUID(),
                        ignored -> new HashSet<>()
                );

        /*
         * Avoid duplicate invitations from the same team.
         */
        if (!invites.add(team.getTeamId())) {
            inviter.sendSystemMessage(
                    Component.literal(
                            invited.getGameProfile()
                                    .getName()
                                    + " already has an invite from your team."
                    ).withStyle(ChatFormatting.YELLOW)
            );

            return false;
        }

        inviter.sendSystemMessage(
                Component.literal(
                        "Invited "
                                + invited.getGameProfile().getName()
                                + " to "
                                + team.getName()
                                + "."
                ).withStyle(ChatFormatting.GREEN)
        );

        invited.sendSystemMessage(
                Component.literal(
                        inviter.getGameProfile().getName()
                                + " invited you to join "
                                + team.getName()
                                + "."
                ).withStyle(ChatFormatting.YELLOW)
        );

        return true;
    }

    public static boolean acceptInvite(
            ServerPlayer player,
            UUID teamId
    ) {
        /*
         * If they joined another team after receiving the invite,
         * they can no longer accept it.
         */
        if (isInTeam(player)) {
            player.sendSystemMessage(
                    Component.literal(
                            "You are already in a team."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        UUID playerId =
                player.getUUID();

        Set<UUID> invites =
                PENDING_INVITES.get(playerId);

        if (
                invites == null
                || !invites.contains(teamId)
        ) {
            player.sendSystemMessage(
                    Component.literal(
                            "That team invitation is no longer valid."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        FloodTeamSavedData data =
                FloodTeamSavedData.get(
                        player.server
                );

        FloodTeam team =
                data.getTeams().get(teamId);

        /*
         * Team may have been disbanded after the invite was sent.
         */
        if (team == null) {
            invites.remove(teamId);

            if (invites.isEmpty()) {
                PENDING_INVITES.remove(playerId);
            }

            player.sendSystemMessage(
                    Component.literal(
                            "That team no longer exists."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        team.addMember(playerId);

        data.getPlayerTeams().put(
                playerId,
                teamId
        );

        PlayerTeamData.setTeam(
                player,
                teamId
        );

        data.setDirty();

        /*
         * Once a player joins one team, every other pending
         * invite becomes invalid.
         */
        clearInvites(playerId);

        broadcastToTeam(
                player.server,
                team,
                Component.literal(
                        player.getGameProfile().getName()
                                + " joined the team."
                ).withStyle(ChatFormatting.GREEN)
        );

        syncTeamStateToPlayer(player);
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

        invites.remove(teamId);

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
                getTeamForPlayer(player);

        if (team == null) {
            player.sendSystemMessage(
                    Component.literal(
                            "You are not in a team."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        /*
         * Temporary behavior.
         *
         * Later we will change this so an owner may leave,
         * automatically transferring ownership to another
         * team member.
         */
        if (team.isOwner(player.getUUID())) {
            player.sendSystemMessage(
                    Component.literal(
                            "You are the team owner. Transfer ownership or disband the team before leaving."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        UUID playerId =
                player.getUUID();

        team.removeMember(playerId);

        data.getPlayerTeams().remove(
                playerId
        );

        PlayerTeamData.clearTeam(player);

        data.setDirty();

        player.sendSystemMessage(
                Component.literal(
                        "You left "
                                + team.getName()
                                + "."
                ).withStyle(ChatFormatting.YELLOW)
        );

        broadcastToTeam(
                player.server,
                team,
                Component.literal(
                        player.getGameProfile().getName()
                                + " left the team."
                ).withStyle(ChatFormatting.YELLOW)
        );

        syncTeamStateToPlayer(player);
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
                getTeamForPlayer(owner);

        if (
                team == null
                || !team.isOwner(owner.getUUID())
        ) {
            owner.sendSystemMessage(
                    Component.literal(
                            "Only the team owner can disband the team."
                    ).withStyle(ChatFormatting.RED)
            );

            return false;
        }

        /*
         * Copy first so modifications do not affect iteration.
         */
        Set<UUID> members =
                new HashSet<>(
                        team.getMembers()
                );

        for (UUID memberId : members) {
            data.getPlayerTeams().remove(
                    memberId
            );

            ServerPlayer member =
                    owner.server
                            .getPlayerList()
                            .getPlayer(memberId);

            /*
             * Offline players cannot have their entity data
             * modified right now. Their stale tag will be
             * repaired when they next join.
             */
            if (member != null) {
                PlayerTeamData.clearTeam(member);

                syncTeamStateToPlayer(member);

                member.sendSystemMessage(
                        Component.literal(
                                "Team "
                                        + team.getName()
                                        + " was disbanded."
                        ).withStyle(ChatFormatting.RED)
                );
            }
        }

        removeInvitesForTeam(
                team.getTeamId()
        );

        data.getTeams().remove(
                team.getTeamId()
        );

        data.setDirty();

        return true;
    }

    // ------------------------------------------------------------
    // PLAYER LOGIN / DATA REPAIR
    // ------------------------------------------------------------

    public static void syncPlayerTeamReference(
                ServerPlayer player
    ) {
        FloodTeamSavedData data =
                FloodTeamSavedData.get(player.server);

        UUID playerId = player.getUUID();

        UUID teamId =
                data.getPlayerTeams().get(playerId);

        if (teamId == null) {
                PlayerTeamData.clearTeam(player);

                syncTeamStateToPlayer(player);
                return;
        }

        FloodTeam team =
                data.getTeams().get(teamId);

        if (team == null) {
                data.getPlayerTeams().remove(playerId);

                PlayerTeamData.clearTeam(player);

                data.setDirty();

                syncTeamStateToPlayer(player);
                return;
        }

        if (!team.hasMember(playerId)) {
                team.addMember(playerId);
                data.setDirty();
        }

        PlayerTeamData.setTeam(
                player,
                teamId
        );

        syncTeamStateToPlayer(player);
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
                                invites.remove(teamId)
                );

        PENDING_INVITES.entrySet()
                .removeIf(
                        entry ->
                                entry.getValue().isEmpty()
                );
    }

    public static void syncTeamStateToPlayer(
                ServerPlayer player
    ) {
        FloodTeam team =
                getTeamForPlayer(player);

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

            if (player != null) {
                player.sendSystemMessage(
                        message
                );
            }
        }
    }
}