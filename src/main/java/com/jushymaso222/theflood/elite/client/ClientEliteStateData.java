package com.jushymaso222.theflood.elite.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientEliteStateData {

    public record EliteState(
            String mutationId,
            String copiedMutationId,
            boolean mimicRevealed,
            List<String> attributes,
            float statusProgress,
            float statusMax,
            boolean statusActive,
            int statusColor
    ) {
    }

    private static final Map<Integer, EliteState> STATES =
            new HashMap<>();

    private ClientEliteStateData() {
    }

    public static void setState(
            int entityId,
            String mutationId,
            String copiedMutationId,
            boolean mimicRevealed,
            List<String> attributes,
            float statusProgress,
            float statusMax,
            boolean statusActive,
            int statusColor
    ) {
        STATES.put(
                entityId,
                new EliteState(
                        mutationId,
                        copiedMutationId,
                        mimicRevealed,
                        attributes,
                        statusProgress,
                        statusMax,
                        statusActive,
                        statusColor
                )
        );
    }

    public static EliteState getState(
            int entityId
    ) {
        return STATES.get(
                entityId
        );
    }

    public static void remove(
            int entityId
    ) {
        STATES.remove(
                entityId
        );
    }

    public static void clear() {
        STATES.clear();
    }
}