package com.jushymaso222.theflood.network.packet;

import com.jushymaso222.theflood.client.ClientInviteData;
import com.jushymaso222.theflood.team.TeamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.item.DyeColor;
import com.jushymaso222.theflood.client.ClientTeamData;
import net.minecraftforge.network.PacketDistributor;
import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.debug.DummyPlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class TeamNetworkingPackets {

    private TeamNetworkingPackets() {
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

                List<ServerPlayer> candidates =
                        TeamManager.getInvitablePlayers(player);

                List<SyncInviteCandidatesPacket.Entry> entries =
                        new ArrayList<>();

                for (ServerPlayer candidate : candidates) {
                        entries.add(
                                new SyncInviteCandidatesPacket.Entry(
                                        candidate.getUUID(),
                                        candidate.getGameProfile().getName()
                                )
                        );
                }

                FloodNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncInviteCandidatesPacket(entries)
                );
                });

                context.setPacketHandled(true);
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
            });

            context.setPacketHandled(true);
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

                TeamManager.acceptInvite(
                        player,
                        message.teamId
                );
            });

            context.setPacketHandled(true);
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