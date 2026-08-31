package com.jushymaso222.theflood.team.network;

import com.jushymaso222.theflood.team.client.ClientInviteData;
import com.jushymaso222.theflood.team.TeamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.item.DyeColor;
import com.jushymaso222.theflood.team.client.ClientTeamData;
import net.minecraftforge.network.PacketDistributor;
import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.debug.DummyPlayerManager;
import com.jushymaso222.theflood.team.client.ClientTeamInviteData;
import com.jushymaso222.theflood.progression.client.ClientPlayerHeatData;
import com.jushymaso222.theflood.team.client.ClientTeamHudData;
import com.jushymaso222.theflood.team.client.ClientTeamChatData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.jushymaso222.theflood.team.FloodTeam;
import com.jushymaso222.theflood.team.TeamChatManager;
import com.jushymaso222.theflood.progression.HeatManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class TeamNetworkingPackets {

    private TeamNetworkingPackets() {
    }

    public static class SyncTeamChatModePacket {

    private final boolean teamChat;

    public SyncTeamChatModePacket(
            boolean teamChat
    ) {
        this.teamChat = teamChat;
    }

    public static void encode(
            SyncTeamChatModePacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBoolean(
                message.teamChat
        );
    }

    public static SyncTeamChatModePacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncTeamChatModePacket(
                buffer.readBoolean()
        );
    }

    public static void handle(
            SyncTeamChatModePacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() ->
                ClientTeamChatData.setMode(
                        message.teamChat
                                ? ClientTeamChatData.ChatMode.TEAM
                                : ClientTeamChatData.ChatMode.GLOBAL
                )
        );

        context.setPacketHandled(true);
    }
}

    public static class SyncTeamHudPacket {

    public record Entry(
            UUID playerId,
            String name,
            float health,
            float maxHealth,
            double x,
            double y,
            double z,
            String dimension
    ) {
    }

    private final List<Entry> entries;

    public SyncTeamHudPacket(
            List<Entry> entries
    ) {
        this.entries = entries;
    }

    public static void encode(
            SyncTeamHudPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeInt(
                message.entries.size()
        );

        for (Entry entry :
                message.entries) {

            buffer.writeUUID(
                    entry.playerId()
            );

            buffer.writeUtf(
                    entry.name(),
                    16
            );

            buffer.writeFloat(
                    entry.health()
            );

            buffer.writeFloat(
                    entry.maxHealth()
            );

            buffer.writeDouble(
                    entry.x()
            );

            buffer.writeDouble(
                    entry.y()
            );

            buffer.writeDouble(
                    entry.z()
            );

            buffer.writeUtf(
                    entry.dimension()
            );
        }
    }

    public static SyncTeamHudPacket decode(
            FriendlyByteBuf buffer
    ) {
        int size =
                buffer.readInt();

        List<Entry> entries =
                new ArrayList<>();

        for (int i = 0; i < size; i++) {
            entries.add(
                    new Entry(
                            buffer.readUUID(),
                            buffer.readUtf(16),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readUtf()
                    )
            );
        }

        return new SyncTeamHudPacket(
                entries
        );
    }

    public static void handle(
            SyncTeamHudPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {

            List<ClientTeamHudData.Teammate> teammates =
                    new ArrayList<>();

            for (Entry entry :
                    message.entries) {

                teammates.add(
                        new ClientTeamHudData.Teammate(
                                entry.playerId(),
                                entry.name(),
                                entry.health(),
                                entry.maxHealth(),
                                entry.x(),
                                entry.y(),
                                entry.z(),
                                entry.dimension()
                        )
                );
            }

            ClientTeamHudData.setTeammates(
                    teammates
            );
        });

        context.setPacketHandled(true);
    }
}

    public static class SyncInviteCandidatesPacket {

        public record Entry(
                UUID id,
                String name
        ) {
        }

        private final List<Entry> entries;

        public SyncInviteCandidatesPacket(
                List<Entry> entries
        ) {
                this.entries = entries;
        }

        public static void encode(
                SyncInviteCandidatesPacket message,
                FriendlyByteBuf buffer
        ) {
                buffer.writeInt(message.entries.size());

                for (Entry entry : message.entries) {
                buffer.writeUUID(entry.id());
                buffer.writeUtf(entry.name());
                }
        }

        public static SyncInviteCandidatesPacket decode(
                FriendlyByteBuf buffer
        ) {
                int size =
                        buffer.readInt();

                List<Entry> entries =
                        new ArrayList<>();

                for (int i = 0; i < size; i++) {
                entries.add(
                        new Entry(
                                buffer.readUUID(),
                                buffer.readUtf()
                        )
                );
                }

                return new SyncInviteCandidatesPacket(
                        entries
                );
        }

        public static void handle(
                SyncInviteCandidatesPacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
                NetworkEvent.Context context =
                        contextSupplier.get();

                context.enqueueWork(() -> {
                List<ClientInviteData.Candidate> candidates =
                        new ArrayList<>();

                for (Entry entry : message.entries) {
                        candidates.add(
                                new ClientInviteData.Candidate(
                                        entry.id(),
                                        entry.name()
                                )
                        );
                }

                ClientInviteData.setCandidates(
                        candidates
                );
                });

                context.setPacketHandled(true);
        }
        }

    public static class RequestInviteCandidatesPacket {

        public static void encode(
                RequestInviteCandidatesPacket message,
                FriendlyByteBuf buffer
        ) {
        }

        public static RequestInviteCandidatesPacket decode(
                FriendlyByteBuf buffer
        ) {
                return new RequestInviteCandidatesPacket();
        }

        public static void handle(
                RequestInviteCandidatesPacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {

                ServerPlayer player =
                        context.getSender();

                if (player == null) {
                return;
                }

                /*
                * Normal connected players that can
                * currently be invited.
                */
                List<ServerPlayer> candidates =
                        TeamManager.getInvitablePlayers(
                                player
                        );

                List<SyncInviteCandidatesPacket.Entry> entries =
                        new ArrayList<>();

                for (
                        ServerPlayer candidate :
                        candidates
                ) {
                entries.add(
                        new SyncInviteCandidatesPacket.Entry(
                                candidate.getUUID(),
                                candidate.getGameProfile()
                                        .getName()
                        )
                );
                }

                /*
                * Development FakePlayers are not necessarily
                * in Minecraft's connected-player list, so add
                * eligible dummies explicitly.
                */
                for (
                        ServerPlayer dummy :
                        DummyPlayerManager.getDummies()
                ) {
                /*
                * Avoid duplicates just in case a dummy
                * somehow already appeared above.
                */
                boolean alreadyPresent =
                        entries.stream()
                                .anyMatch(
                                        entry ->
                                                entry.id()
                                                        .equals(
                                                                dummy.getUUID()
                                                        )
                                );

                if (alreadyPresent) {
                        continue;
                }

                /*
                * Don't include ourselves.
                */
                if (
                        dummy.getUUID()
                                .equals(
                                        player.getUUID()
                                )
                ) {
                        continue;
                }

                /*
                * Don't offer a dummy that's already
                * part of a team.
                */
                if (
                        TeamManager.getTeamForPlayer(
                                dummy
                        ) != null
                ) {
                        continue;
                }

                entries.add(
                        new SyncInviteCandidatesPacket.Entry(
                                dummy.getUUID(),
                                dummy.getGameProfile()
                                        .getName()
                        )
                );
                }

                FloodNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(
                                () -> player
                        ),
                        new SyncInviteCandidatesPacket(
                                entries
                        )
                );
        });

        context.setPacketHandled(
                true
        );
        }
        }

    public static class CreateTeamPacket {

        private final String teamName;
        private final DyeColor color;

        public CreateTeamPacket(
                String teamName,
                DyeColor color
        ) {
            this.teamName = teamName;
            this.color = color;
        }

        public static void encode(
                CreateTeamPacket message,
                FriendlyByteBuf buffer
        ) {
            buffer.writeUtf(
                    message.teamName,
                    24
            );

            buffer.writeEnum(
                    message.color
            );
        }

        public static CreateTeamPacket decode(
                FriendlyByteBuf buffer
        ) {
            return new CreateTeamPacket(
                    buffer.readUtf(24),
                    buffer.readEnum(DyeColor.class)
            );
        }

        public static void handle(
                CreateTeamPacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
            NetworkEvent.Context context =
                    contextSupplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player =
                        context.getSender();

                if (player == null) {
                    return;
                }

                TeamManager.createTeam(
                        player,
                        message.teamName,
                        message.color
                );
            });

            context.setPacketHandled(true);
        }
    }

    public static class SyncTeamStatePacket {

    private final boolean inTeam;
    private final boolean owner;

    private final UUID teamId;

    private final String teamName;
    private final DyeColor teamColor;

    private final int teamHeat;

    public SyncTeamStatePacket(
            boolean inTeam,
            boolean owner,
            UUID teamId,
            String teamName,
            DyeColor teamColor,
            int teamHeat
    ) {
        this.inTeam = inTeam;
        this.owner = owner;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.teamHeat = teamHeat;
    }

    public static void encode(
            SyncTeamStatePacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBoolean(
                message.inTeam
        );

        buffer.writeBoolean(
                message.owner
        );

        /*
         * UUID may be null when solo.
         */
        buffer.writeBoolean(
                message.teamId != null
        );

        if (message.teamId != null) {
            buffer.writeUUID(
                    message.teamId
            );
        }

        buffer.writeUtf(
                message.teamName,
                24
        );

        buffer.writeEnum(
                message.teamColor
        );

        buffer.writeInt(
                message.teamHeat
        );
    }

    public static SyncTeamStatePacket decode(
                FriendlyByteBuf buffer
        ) {
            boolean inTeam =
                    buffer.readBoolean();

            boolean owner =
                    buffer.readBoolean();

            UUID teamId = null;

            if (buffer.readBoolean()) {
                teamId =
                        buffer.readUUID();
            }

            String teamName =
                    buffer.readUtf(24);

            DyeColor teamColor =
                    buffer.readEnum(
                            DyeColor.class
                    );

            int teamHeat =
                    buffer.readInt();

            return new SyncTeamStatePacket(
                    inTeam,
                    owner,
                    teamId,
                    teamName,
                    teamColor,
                    teamHeat
            );
        }

        public static void handle(
                SyncTeamStatePacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
            NetworkEvent.Context context =
                    contextSupplier.get();

            context.enqueueWork(() -> {
                ClientTeamData.update(
                        message.inTeam,
                        message.owner,
                        message.teamId,
                        message.teamName,
                        message.teamColor,
                        message.teamHeat
                );

                if (!message.inTeam) {
                    ClientTeamHudData.clearAllTeamHudData();
                }
            });


            context.setPacketHandled(true);
        }
    }

    public static class ToggleTeamChatPacket {

    public static void encode(
            ToggleTeamChatPacket message,
            FriendlyByteBuf buffer
    ) {
    }

    public static ToggleTeamChatPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new ToggleTeamChatPacket();
    }

    public static void handle(
            ToggleTeamChatPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {

            ServerPlayer player =
                    context.getSender();

            if (player == null) {
                return;
            }

            TeamChatManager.ChatMode current =
                    TeamChatManager.getMode(
                            player.getUUID()
                    );

            if (
                    current
                            == TeamChatManager.ChatMode.GLOBAL
            ) {
                FloodTeam team =
                        TeamManager.getTeamForPlayer(
                                player
                        );

                if (team == null) {
                    player.sendSystemMessage(
                            Component.literal(
                                    "You are not in a team."
                            ).withStyle(
                                    ChatFormatting.RED
                            )
                    );

                    /*
                     * Explicitly sync GLOBAL back to the
                     * client so the icon can never become
                     * desynchronized.
                     */
                    FloodNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(
                                    () -> player
                            ),
                            new SyncTeamChatModePacket(
                                    false
                            )
                    );

                    return;
                }

                TeamChatManager.setMode(
                        player.getUUID(),
                        TeamChatManager.ChatMode.TEAM
                );

                player.sendSystemMessage(
                        Component.literal(
                                "Chat mode set to Team."
                        ).withStyle(
                                ChatFormatting.AQUA
                        )
                );

                FloodNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(
                                () -> player
                        ),
                        new SyncTeamChatModePacket(
                                true
                        )
                );

                return;
            }

            TeamChatManager.setMode(
                    player.getUUID(),
                    TeamChatManager.ChatMode.GLOBAL
            );

            player.sendSystemMessage(
                    Component.literal(
                            "Chat mode set to Global."
                    ).withStyle(
                            ChatFormatting.GREEN
                    )
            );

            FloodNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(
                            () -> player
                    ),
                    new SyncTeamChatModePacket(
                            false
                    )
            );
        });

        context.setPacketHandled(true);
    }
}

    public static class RequestPendingTeamInvitesPacket {

    public static void encode(
            RequestPendingTeamInvitesPacket message,
            FriendlyByteBuf buffer
    ) {
    }

    public static RequestPendingTeamInvitesPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new RequestPendingTeamInvitesPacket();
    }

    public static void handle(
            RequestPendingTeamInvitesPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {

            ServerPlayer player =
                    context.getSender();

            if (player == null) {
                return;
            }

            List<SyncPendingTeamInvitesPacket.Entry> entries =
                    new ArrayList<>();

            for (UUID teamId :
                    TeamManager.getPendingInvites(
                            player.getUUID()
                    )) {

                com.jushymaso222.theflood.team.FloodTeam team =
                        TeamManager.getTeam(
                                player.server,
                                teamId
                        );

                if (team == null) {
                    continue;
                }

                int currentHeat =
                        HeatManager.getEffectiveHeat(
                                player
                        );

                int projectedHeat =
                        TeamManager.getProjectedJoinHeat(
                                player,
                                teamId
                        );

                entries.add(
                        new SyncPendingTeamInvitesPacket.Entry(
                                team.getTeamId(),
                                team.getName(),
                                currentHeat,
                                projectedHeat
                        )
                );
            }

            FloodNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(
                            () -> player
                    ),
                    new SyncPendingTeamInvitesPacket(
                            entries
                    )
            );
        });

        context.setPacketHandled(true);
    }
}

        public static class SyncPlayerHeatPacket {

    private final UUID playerId;
    private final int heat;

    public SyncPlayerHeatPacket(
            UUID playerId,
            int heat
    ) {
        this.playerId = playerId;
        this.heat = heat;
    }

    public static void encode(
            SyncPlayerHeatPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeUUID(
                message.playerId
        );

        buffer.writeInt(
                message.heat
        );
    }

    public static SyncPlayerHeatPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncPlayerHeatPacket(
                buffer.readUUID(),
                buffer.readInt()
        );
    }

    public static void handle(
            SyncPlayerHeatPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() ->
                ClientPlayerHeatData.setHeat(
                        message.playerId,
                        message.heat
                )
        );

        context.setPacketHandled(
                true
        );
    }
}

        public static class ConfirmTeamInvitePacket {

    private final UUID teamId;

    public ConfirmTeamInvitePacket(
            UUID teamId
    ) {
        this.teamId =
                teamId;
    }

    public static void encode(
            ConfirmTeamInvitePacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeUUID(
                message.teamId
        );
    }

    public static ConfirmTeamInvitePacket decode(
            FriendlyByteBuf buffer
    ) {
        return new ConfirmTeamInvitePacket(
                buffer.readUUID()
        );
    }

    public static void handle(
            ConfirmTeamInvitePacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {

            ServerPlayer player =
                    context.getSender();

            if (player == null) {
                return;
            }

            TeamManager.confirmHeatInvite(
                    player,
                    message.teamId
            );
        });

        context.setPacketHandled(
                true
        );
    }
}

    public static class InvitePlayerPacket {

        private final UUID playerId;

        public InvitePlayerPacket(UUID playerId) {
            this.playerId = playerId;
        }

        public static void encode(
                InvitePlayerPacket message,
                FriendlyByteBuf buffer
        ) {
            buffer.writeUUID(message.playerId);
        }

        public static InvitePlayerPacket decode(
                FriendlyByteBuf buffer
        ) {
            return new InvitePlayerPacket(
                    buffer.readUUID()
            );
        }

        public static void handle(
                InvitePlayerPacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
            NetworkEvent.Context context =
                    contextSupplier.get();

            context.enqueueWork(() -> {
                ServerPlayer inviter =
                        context.getSender();

                if (inviter == null) {
                    return;
                }

                ServerPlayer invited =
                        inviter.server
                                .getPlayerList()
                                .getPlayer(message.playerId);

                /*
                * FakePlayers aren't necessarily registered in
                * Minecraft's normal connected-player list.
                */
                if (invited == null) {
                invited =
                        DummyPlayerManager.getDummy(
                                message.playerId
                        );
                }

                if (invited == null) {
                return;
                }

                TeamManager.invitePlayer(
                        inviter,
                        invited
                );
            });

            context.setPacketHandled(true);
        }
    }

    public static class AcceptTeamInvitePacket {

        private final UUID teamId;

        public AcceptTeamInvitePacket(UUID teamId) {
            this.teamId = teamId;
        }

        public static void encode(
                AcceptTeamInvitePacket message,
                FriendlyByteBuf buffer
        ) {
            buffer.writeUUID(message.teamId);
        }

        public static AcceptTeamInvitePacket decode(
                FriendlyByteBuf buffer
        ) {
            return new AcceptTeamInvitePacket(
                    buffer.readUUID()
            );
        }

        public static void handle(
                AcceptTeamInvitePacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
            NetworkEvent.Context context =
                    contextSupplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null) {
                    return;
                }

                TeamManager.requestAcceptInvite(
                        player,
                        message.teamId
                );
            });

            context.setPacketHandled(true);
        }
    }

    public static class SyncPendingTeamInvitesPacket {

        public record Entry(
                UUID teamId,
                String teamName,
                int currentHeat,
                int projectedHeat
        ) {
        }

    private final List<Entry> entries;

    public SyncPendingTeamInvitesPacket(
            List<Entry> entries
    ) {
        this.entries = entries;
    }

    public static void encode(
            SyncPendingTeamInvitesPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeInt(
                message.entries.size()
        );

        for (Entry entry :
                message.entries) {

            buffer.writeUUID(
                        entry.teamId()
                );

                buffer.writeUtf(
                        entry.teamName(),
                        24
                );

                buffer.writeInt(
                        entry.currentHeat()
                );

                buffer.writeInt(
                        entry.projectedHeat()
                );
        }
    }

    public static SyncPendingTeamInvitesPacket decode(
            FriendlyByteBuf buffer
    ) {
        int size =
                buffer.readInt();

        List<Entry> entries =
                new ArrayList<>();

        for (int i = 0; i < size; i++) {

            entries.add(
                        new Entry(
                                buffer.readUUID(),
                                buffer.readUtf(24),
                                buffer.readInt(),
                                buffer.readInt()
                        )
                );
        }

        return new SyncPendingTeamInvitesPacket(
                entries
        );
    }

    public static void handle(
            SyncPendingTeamInvitesPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {

            List<ClientTeamInviteData.Invite> invites =
                    new ArrayList<>();

            for (Entry entry :
                    message.entries) {

                invites.add(
                        new ClientTeamInviteData.Invite(
                                entry.teamId(),
                                entry.teamName(),
                                entry.currentHeat(),
                                entry.projectedHeat()
                        )
                );
            }

            ClientTeamInviteData.setInvites(
                    invites
            );
        });

        context.setPacketHandled(
                true
        );
    }
}

    public static class DeclineTeamInvitePacket {

        private final UUID teamId;

        public DeclineTeamInvitePacket(UUID teamId) {
            this.teamId = teamId;
        }

        public static void encode(
                DeclineTeamInvitePacket message,
                FriendlyByteBuf buffer
        ) {
            buffer.writeUUID(message.teamId);
        }

        public static DeclineTeamInvitePacket decode(
                FriendlyByteBuf buffer
        ) {
            return new DeclineTeamInvitePacket(
                    buffer.readUUID()
            );
        }

        public static void handle(
                DeclineTeamInvitePacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
            NetworkEvent.Context context =
                    contextSupplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null) {
                    return;
                }

                TeamManager.declineInvite(
                        player,
                        message.teamId
                );
            });

            context.setPacketHandled(true);
        }
    }

    public static class LeaveTeamPacket {

        public static void encode(
                LeaveTeamPacket message,
                FriendlyByteBuf buffer
        ) {
        }

        public static LeaveTeamPacket decode(
                FriendlyByteBuf buffer
        ) {
            return new LeaveTeamPacket();
        }

        public static void handle(
                LeaveTeamPacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
            NetworkEvent.Context context =
                    contextSupplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null) {
                    return;
                }

                TeamManager.leaveTeam(player);
            });

            context.setPacketHandled(true);
        }
    }

    public static class DisbandTeamPacket {

        public static void encode(
                DisbandTeamPacket message,
                FriendlyByteBuf buffer
        ) {
        }

        public static DisbandTeamPacket decode(
                FriendlyByteBuf buffer
        ) {
            return new DisbandTeamPacket();
        }

        public static void handle(
                DisbandTeamPacket message,
                Supplier<NetworkEvent.Context> contextSupplier
        ) {
            NetworkEvent.Context context =
                    contextSupplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null) {
                    return;
                }

                TeamManager.disbandTeam(player);
            });

            context.setPacketHandled(true);
        }
    }
}