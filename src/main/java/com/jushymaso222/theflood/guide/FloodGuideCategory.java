package com.jushymaso222.theflood.guide;

public enum FloodGuideCategory {

    GETTING_STARTED(
            "Getting Started"
    ),

    HEAT(
            "Heat"
    ),

    TEAMS(
            "Teams"
    ),

    SPAWNING(
            "Spawning"
    ),

    HORDES(
            "Hordes"
    ),

    BLOOD_MOONS(
            "Blood Moons"
    ),

    DAY_NIGHT(
            "Day & Night"
    ),

    COMMANDS(
            "Commands"
    ),

    SERVER_SETTINGS(
            "Server Settings"
    );

    private final String displayName;

    FloodGuideCategory(
            String displayName
    ) {
        this.displayName =
                displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}