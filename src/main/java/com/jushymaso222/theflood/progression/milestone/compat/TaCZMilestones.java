package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class TaCZMilestones {

    private TaCZMilestones() {
    }

    public static void register() {

        MilestoneRegistry.registerCompat(
                "tacz",
                "firearms",
                "Firearms",
                "You've brought modern firepower into the fight.",
                20,
                0.04D,
                hasItemId(
                        "tacz:modern_kinetic_gun"
                )
        );
    }
}