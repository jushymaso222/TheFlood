package com.jushymaso222.theflood.team;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FloodTeamSavedData extends SavedData {

    private static final String DATA_NAME = "theflood_teams";

    private final Map<UUID, FloodTeam> teams = new HashMap<>();
    private final Map<UUID, UUID> playerTeams = new HashMap<>();

    public FloodTeamSavedData() {
    }

    public static FloodTeamSavedData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        FloodTeamSavedData::load,
                        FloodTeamSavedData::new,
                        DATA_NAME
                );
    }

    public Map<UUID, FloodTeam> getTeams() {
        return teams;
    }

    public Map<UUID, UUID> getPlayerTeams() {
        return playerTeams;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag teamList = new ListTag();

        for (FloodTeam team : teams.values()) {
            CompoundTag teamTag = new CompoundTag();

            teamTag.putUUID(
                    "TeamId",
                    team.getTeamId()
            );

            teamTag.putUUID(
                    "OwnerId",
                    team.getOwnerId()
            );

            teamTag.putString(
                    "Name",
                    team.getName()
            );

            teamTag.putString(
                    "Color",
                    team.getColor().getName()
            );

            teamTag.putInt(
                    "TeamHeat",
                    team.getTeamHeat()
            );

            ListTag members = new ListTag();

            for (UUID memberId : team.getMembers()) {
                CompoundTag memberTag = new CompoundTag();

                memberTag.putUUID(
                        "PlayerId",
                        memberId
                );

                members.add(memberTag);
            }

            teamTag.put(
                    "Members",
                    members
            );

            teamList.add(teamTag);
        }

        tag.put(
                "Teams",
                teamList
        );

        return tag;
    }

    public static FloodTeamSavedData load(CompoundTag tag) {
        FloodTeamSavedData data =
                new FloodTeamSavedData();

        ListTag teamList = tag.getList(
                "Teams",
                Tag.TAG_COMPOUND
        );

        for (int i = 0; i < teamList.size(); i++) {
            CompoundTag teamTag =
                    teamList.getCompound(i);

            UUID teamId =
                    teamTag.getUUID("TeamId");

            UUID ownerId =
                    teamTag.getUUID("OwnerId");

            String name =
                    teamTag.getString("Name");

            DyeColor color =
                    DyeColor.byName(
                            teamTag.getString("Color"),
                            DyeColor.WHITE
                    );

            FloodTeam team =
                    new FloodTeam(
                            teamId,
                            ownerId,
                            name,
                            color
                    );

            team.setTeamHeat(
                    teamTag.getInt("TeamHeat")
            );

            /*
             * Constructor already added the owner.
             */
            ListTag members = teamTag.getList(
                    "Members",
                    Tag.TAG_COMPOUND
            );

            for (int j = 0; j < members.size(); j++) {
                UUID memberId =
                        members.getCompound(j)
                                .getUUID("PlayerId");

                team.addMember(memberId);

                data.playerTeams.put(
                        memberId,
                        teamId
                );
            }

            data.teams.put(
                    teamId,
                    team
            );
        }

        return data;
    }
}