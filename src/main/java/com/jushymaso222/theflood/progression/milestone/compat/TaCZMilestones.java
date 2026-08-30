package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;
import net.minecraft.network.chat.Component;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class TaCZMilestones {

    public static final String MOD_ID =
            "tacz";

    public static final String CATEGORY_ID =
            "tacz";

    private TaCZMilestones() {
    }

    public static void register() {

        MilestoneCompatRegistry.register(
                new MilestoneCompatDefinition(
                        CATEGORY_ID,
                        MOD_ID,
                        Component.literal(
                                "Timeless and Classics Zero"
                        ),
                        80
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
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