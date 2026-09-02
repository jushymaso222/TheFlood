package com.jushymaso222.theflood.elite.drops.boon.mimic;

public enum PlayerMimicMutation {

    SPIKED(
            "spiked",
            "Spiked"
    ),

    SHIFTING(
            "shifting",
            "Shifting"
    ),

    UNDYING(
            "undying",
            "Undying"
    ),

    FRENZIED(
            "frenzied",
            "Frenzied"
    ),

    COMMANDER(
            "commander",
            "Commander"
    );


    private final String id;
    private final String displayName;


    PlayerMimicMutation(
            String id,
            String displayName
    ) {
        this.id =
                id;

        this.displayName =
                displayName;
    }


    public String id() {
        return id;
    }


    public String displayName() {
        return displayName;
    }


    public static PlayerMimicMutation fromId(
            String id
    ) {
        if (
                id == null
                || id.isBlank()
        ) {
            return null;
        }

        for (
                PlayerMimicMutation mutation :
                values()
        ) {
            if (
                    mutation.id()
                            .equals(
                                    id
                            )
            ) {
                return mutation;
            }
        }

        return null;
    }
}