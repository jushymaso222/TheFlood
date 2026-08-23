package com.jushymaso222.theflood.elite.drops.boon;

public enum BoonType {

    HOARDERS(
            "hoarders",
            "Boon of Hoarders",
            20 * 60 * 10
    ),

    ATTACK(
            "attack",
            "Boon of Attack",
            20 * 60 * 10
    ),

    DEFENSE(
            "defense",
            "Boon of Defense",
            20 * 60 * 10
    ),

    TRANQUILITY(
            "tranquility",
            "Boon of Tranquility",
            20 * 60 * 10
    );


    private final String id;

    private final String displayName;

    private final int durationTicks;


    BoonType(
            String id,
            String displayName,
            int durationTicks
    ) {
        this.id =
                id;

        this.displayName =
                displayName;

        this.durationTicks =
                durationTicks;
    }

    public static BoonType fromId(
            String id
    ) {
        if (
                id == null
                || id.isBlank()
        ) {
            return null;
        }

        for (
                BoonType type :
                values()
        ) {
            if (
                    type.id()
                            .equals(
                                    id
                            )
            ) {
                return type;
            }
        }

        return null;
    }


    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int durationTicks() {
        return durationTicks;
    }
}