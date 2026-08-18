package com.jushymaso222.theflood.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TeamChatManager {

    public enum ChatMode {
        GLOBAL,
        TEAM
    }

    private static final Map<UUID, ChatMode> CHAT_MODES =
            new HashMap<>();

    private TeamChatManager() {
    }

    public static ChatMode getMode(
            UUID playerId
    ) {
        return CHAT_MODES.getOrDefault(
                playerId,
                ChatMode.GLOBAL
        );
    }

    public static void setMode(
            UUID playerId,
            ChatMode mode
    ) {
        CHAT_MODES.put(
                playerId,
                mode
        );
    }

    public static void clear(
            UUID playerId
    ) {
        CHAT_MODES.remove(
                playerId
        );
    }
}