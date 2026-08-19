package com.jushymaso222.theflood.team.client;

public final class ClientTeamChatData {

    public enum ChatMode {
        GLOBAL,
        TEAM
    }

    private static ChatMode mode =
            ChatMode.GLOBAL;

    private ClientTeamChatData() {
    }

    public static ChatMode getMode() {
        return mode;
    }

    public static void setMode(
            ChatMode newMode
    ) {
        mode = newMode;
    }

    public static boolean isTeamChat() {
        return mode == ChatMode.TEAM;
    }

    public static void reset() {
        mode = ChatMode.GLOBAL;
    }
}