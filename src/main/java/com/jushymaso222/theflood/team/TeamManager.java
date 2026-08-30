package com.jushymaso222.theflood.team;

import com.jushymaso222.theflood.debug.DummyPlayerManager;
import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;
import com.jushymaso222.theflood.progression.HeatManager;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

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

    /*
        * Player UUID -> pending Heat confirmation.
        *
        * A player is added here only after accepting an
        * invitation that would increase their Heat.
        */
        private static final Map<UUID, PendingHeatConfirmation>
                PENDING_HEAT_CONFIRMATIONS =
                new HashMap<>();

        private record PendingHeatConfirmation(
                UUID teamId,
                int warnedHeat
        ) {
        }

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
        team.setTeamHeat(
                HeatManager.getSoloHeat(
                        owner
                )
        );

        team.setFloodXp(
                PlayerFloodData.getHeatProgressTicks(
                        owner
                )
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

        private static void mergePlayerProgressionIntoTeam(
        ServerPlayer player,
        FloodTeam team
) {
    int playerHeat =
            HeatManager.getSoloHeat(
                    player
            );

    long playerXp =
            PlayerFloodData.getHeatProgressTicks(
                    player
            );

    int teamHeat =
            team.getTeamHeat();

    long teamXp =
            team.getFloodXp();


    /*
     * Player is further ahead by Heat level.
     *
     * Adopt their entire progression point.
     */
    if (playerHeat > teamHeat) {

        team.setTeamHeat(
                playerHeat
        );

        team.setFloodXp(
                playerXp
        );

        return;
    }


    /*
     * Same Heat level:
     *
     * Keep whichever progression bar is further ahead.
     */
    if (playerHeat == teamHeat) {

        team.setFloodXp(
                Math.max(
                        teamXp,
                        playerXp
                )
        );
    }
}

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

    private static void sendHeatJoinWarning(
        ServerPlayer player,
        FloodTeam team,
        int currentHeat,
        int projectedHeat
) {
    player.sendSystemMessage(
            Component.literal(
                    "\nJoining "
            ).withStyle(
                    ChatFormatting.YELLOW
            ).append(
                    Component.literal(
                            team.getName()
                    ).withStyle(
                            ChatFormatting.GOLD
                    )
            ).append(
                    Component.literal(
                            " will increase your Heat!"
                    ).withStyle(
                            ChatFormatting.YELLOW
                    )
            )
    );

    player.sendSystemMessage(
            Component.literal(
                    "Current Heat: "
            ).withStyle(
                    ChatFormatting.GRAY
            ).append(
                    Component.literal(
                            Integer.toString(
                                    currentHeat
                            )
                    ).withStyle(
                            ChatFormatting.GREEN
                    )
            )
    );

    player.sendSystemMessage(
            Component.literal(
                    "Heat after joining: "
            ).withStyle(
                    ChatFormatting.GRAY
            ).append(
                    Component.literal(
                            Integer.toString(
                                    projectedHeat
                            )
                    ).withStyle(
                            ChatFormatting.RED
                    )
            )
    );

    Component confirm =
            Component.literal(
                    "[CONFIRM]"
            ).withStyle(style ->
                    style
                            .withColor(
                                    ChatFormatting.GREEN
                            )
                            .withBold(true)
                            .withClickEvent(
                                    new ClickEvent(
                                            ClickEvent.Action.RUN_COMMAND,
                                            "/flood team confirm "
                                                    + team.getTeamId()
                                    )
                            )
                            .withHoverEvent(
                                    new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Component.literal(
                                                    "Join "
                                                            + team.getName()
                                                            + " at Heat "
                                                            + projectedHeat
                                            )
                                    )
                            )
            );

    Component cancel =
            Component.literal(
                    "[CANCEL]"
            ).withStyle(style ->
                    style
                            .withColor(
                                    ChatFormatting.RED
                            )
                            .withBold(true)
                            .withClickEvent(
                                    new ClickEvent(
                                            ClickEvent.Action.RUN_COMMAND,
                                            "/flood team cancel "
                                                    + team.getTeamId()
                                    )
                            )
                            .withHoverEvent(
                                    new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Component.literal(
                                                    "Do not join the team"
                                            )
                                    )
                            )
            );

    player.sendSystemMessage(
            Component.literal(" ")
                    .append(confirm)
                    .append(
                            Component.literal(
                                    "     "
                            )
                    )
                    .append(cancel)
    );
}

    public static boolean requestAcceptInvite(
        ServerPlayer player,
        UUID teamId
) {
    /*
     * Development dummies should continue
     * auto-accepting without confirmation.
     */
    if (DummyPlayerManager.isDummy(player)) {
        return acceptInvite(
                player,
                teamId
        );
    }

    if (isInTeam(player)) {
        player.sendSystemMessage(
                Component.literal(
                        "You are already in a team."
                ).withStyle(
                        ChatFormatting.RED
                )
        );

        return false;
    }

    Set<UUID> invites =
            PENDING_INVITES.get(
                    player.getUUID()
            );

    if (
            invites == null
            || !invites.contains(teamId)
    ) {
        player.sendSystemMessage(
                Component.literal(
                        "That team invitation is no longer valid."
                ).withStyle(
                        ChatFormatting.RED
                )
        );

        return false;
    }

    FloodTeam team =
            getTeam(
                    player.server,
                    teamId
            );

    if (team == null) {
        return false;
    }

    int currentHeat =
            HeatManager.getEffectiveHeat(
                    player
            );

    int projectedHeat =
            calculateProjectedTeamHeat(
                    player.server,
                    team,
                    player
            );

    /*
     * No warning is necessary if joining
     * wouldn't increase this player's Heat.
     */
    if (projectedHeat <= currentHeat) {
        return acceptInvite(
                player,
                teamId
        );
    }

    PENDING_HEAT_CONFIRMATIONS.put(
        player.getUUID(),
        new PendingHeatConfirmation(
                teamId,
                projectedHeat
        )
);

    sendHeatJoinWarning(
            player,
            team,
            currentHeat,
            projectedHeat
    );

    return true;
}

        public static boolean confirmHeatInvite(
        ServerPlayer player,
        UUID teamId
) {
    PendingHeatConfirmation confirmation =
            PENDING_HEAT_CONFIRMATIONS.get(
                    player.getUUID()
            );

    if (
            confirmation == null
            || !confirmation.teamId()
                    .equals(teamId)
    ) {
        player.sendSystemMessage(
                Component.literal(
                        "You do not have a pending confirmation for that team."
                ).withStyle(
                        ChatFormatting.RED
                )
        );

        return false;
    }

    FloodTeam team =
            getTeam(
                    player.server,
                    teamId
            );

    if (team == null) {
        PENDING_HEAT_CONFIRMATIONS.remove(
                player.getUUID()
        );

        player.sendSystemMessage(
                Component.literal(
                        "That team no longer exists."
                ).withStyle(
                        ChatFormatting.RED
                )
        );

        return false;
    }

    /*
     * Recalculate before joining.
     *
     * The team's membership or highest-Heat player may
     * have changed since the warning was displayed.
     */
    int projectedHeat =
            calculateProjectedTeamHeat(
                    player.server,
                    team,
                    player
            );

    /*
     * Never allow confirmation of a Heat level higher
     * than the one the player was originally warned about.
     */
    if (
            projectedHeat
            > confirmation.warnedHeat()
    ) {
        PENDING_HEAT_CONFIRMATIONS.put(
                player.getUUID(),
                new PendingHeatConfirmation(
                        teamId,
                        projectedHeat
                )
        );

        sendHeatJoinWarning(
                player,
                team,
                HeatManager.getEffectiveHeat(player),
                projectedHeat
        );

        return false;
    }

    PENDING_HEAT_CONFIRMATIONS.remove(
            player.getUUID()
    );

    return acceptInvite(
            player,
            teamId
    );
}

        public static void cancelHeatInvite(
        ServerPlayer player,
        UUID teamId
) {
    PendingHeatConfirmation confirmation =
            PENDING_HEAT_CONFIRMATIONS.get(
                    player.getUUID()
            );

    if (
            confirmation != null
            && confirmation.teamId()
                    .equals(teamId)
    ) {
        PENDING_HEAT_CONFIRMATIONS.remove(
                player.getUUID()
        );
    }

    declineInvite(
            player,
            teamId
    );
}

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

        mergePlayerProgressionIntoTeam(
                player,
                team
        );

        clearInvites(
                playerId
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

        TeamChatManager.setMode(
                player.getUUID(),
                TeamChatManager.ChatMode.GLOBAL
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

            TeamChatManager.setMode(
                memberId,
                TeamChatManager.ChatMode.GLOBAL
            );

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

    public static int calculateProjectedTeamHeat(
        MinecraftServer server,
        FloodTeam team,
        ServerPlayer joiningPlayer
) {
    return Math.max(
            team.getTeamHeat(),
            HeatManager.getSoloHeat(
                    joiningPlayer
            )
    );
}

    // ------------------------------------------------------------
    // CLIENT SYNC
    // ------------------------------------------------------------

    public static void syncTeamMemberHeats(
        MinecraftServer server,
        FloodTeam team
) {
    List<ServerPlayer> connectedMembers =
            new ArrayList<>();

    for (UUID memberId :
            team.getMembers()) {

        ServerPlayer member =
                server.getPlayerList()
                        .getPlayer(memberId);

        if (member != null) {
            connectedMembers.add(
                    member
            );
        }
    }

    for (ServerPlayer receiver :
            connectedMembers) {

        for (ServerPlayer member :
                connectedMembers) {

            FloodNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(
                            () -> receiver
                    ),
                    new TeamNetworkingPackets.SyncPlayerHeatPacket(
                            member.getUUID(),
                            HeatManager.getEffectiveHeat(
                                    member
                            )
                    )
            );
        }
    }
}

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

            syncTeamMemberHeats(
                        server,
                        team
                );
        }
    }

    public static void syncTeamHudToPlayer(
        ServerPlayer receiver
) {
    if (
            DummyPlayerManager.isDummy(
                    receiver
            )
    ) {
        return;
    }

    FloodTeam team =
            getTeamForPlayer(
                    receiver
            );

    if (team == null) {
        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> receiver
                ),
                new TeamNetworkingPackets.SyncTeamHudPacket(
                        List.of()
                )
        );

        return;
    }

    List<TeamNetworkingPackets.SyncTeamHudPacket.Entry> entries =
            new ArrayList<>();

    for (UUID memberId :
            team.getMembers()) {

        /*
         * Don't put yourself on your own teammate HUD.
         */
        if (
                memberId.equals(
                        receiver.getUUID()
                )
        ) {
            continue;
        }

        ServerPlayer member =
                receiver.server
                        .getPlayerList()
                        .getPlayer(
                                memberId
                        );

        /*
         * Development FakePlayers aren't necessarily
         * in Minecraft's normal player list.
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

        entries.add(
                new TeamNetworkingPackets.SyncTeamHudPacket.Entry(
                        member.getUUID(),
                        member.getGameProfile()
                                .getName(),
                        member.getHealth(),
                        member.getMaxHealth(),
                        member.getX(),
                        member.getY(),
                        member.getZ(),
                        member.level()
                                .dimension()
                                .location()
                                .toString()
                )
        );
    }

    FloodNetwork.CHANNEL.send(
            PacketDistributor.PLAYER.with(
                    () -> receiver
            ),
            new TeamNetworkingPackets.SyncTeamHudPacket(
                    entries
            )
    );
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