package com.jushymaso222.theflood.team;

import net.minecraft.world.item.DyeColor;
import com.jushymaso222.theflood.progression.PlayerFloodData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FloodTeam {

    private final UUID teamId;
    private UUID ownerId;

    private String name;
    private DyeColor color;

    private final Set<UUID> members = new HashSet<>();

    // Placeholder for later.
    private int teamHeat = 1;

    public FloodTeam(
            UUID teamId,
            UUID ownerId,
            String name,
            DyeColor color
    ) {
        this.teamId = teamId;
        this.ownerId = ownerId;
        this.name = name;
        this.color = color;

        members.add(ownerId);
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwner(UUID playerId) {
        return ownerId.equals(playerId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DyeColor getColor() {
        return color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public Set<UUID> getMembers() {
        return Set.copyOf(members);
    }

    public boolean hasMember(UUID playerId) {
        return members.contains(playerId);
    }

    public void addMember(UUID playerId) {
        members.add(playerId);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getTeamHeat() {
        return teamHeat;
    }

    public void setTeamHeat(int teamHeat) {
        this.teamHeat =
                PlayerFloodData.clampHeat(teamHeat);
    }
}