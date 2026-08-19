package com.jushymaso222.theflood.team.client;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ClientInviteData {

    public record Candidate(
            UUID id,
            String name
    ) {
    }

    private static final List<Candidate> CANDIDATES =
            new ArrayList<>();

    private ClientInviteData() {
    }

    public static void setCandidates(
            List<Candidate> candidates
    ) {
        /*
        * Store the new data FIRST.
        */
        CANDIDATES.clear();
        CANDIDATES.addAll(candidates);

        /*
        * Then refresh the screen so it reads
        * the newly updated candidate list.
        */
        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.screen
                instanceof FloodInvitePlayerScreen screen
        ) {
            screen.refreshCandidates();
        }
    }

    public static List<Candidate> getCandidates() {
        return Collections.unmodifiableList(
                CANDIDATES
        );
    }

    public static void clear() {
        CANDIDATES.clear();
    }
}