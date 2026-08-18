package com.jushymaso222.theflood.client;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ClientTeamInviteData {

    public record Invite(
            UUID teamId,
            String teamName
    ) {
    }

    private static final List<Invite> INVITES =
            new ArrayList<>();

    private ClientTeamInviteData() {
    }

    public static void setInvites(
            List<Invite> invites
    ) {
        INVITES.clear();
        INVITES.addAll(invites);

        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.screen
                        instanceof FloodTeamInvitesScreen screen
        ) {
            screen.refreshInvites();
        }
    }

    public static List<Invite> getInvites() {
        return Collections.unmodifiableList(
                INVITES
        );
    }

    public static void clear() {
        INVITES.clear();
    }
}