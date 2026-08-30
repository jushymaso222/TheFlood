package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;
import net.minecraft.network.chat.Component;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class TwilightForestMilestones {

    public static final String MOD_ID =
            "twilightforest";

    public static final String CATEGORY_ID =
            "twilightforest";

    private TwilightForestMilestones() {
    }

    public static void register() {

        MilestoneCompatRegistry.register(
                new MilestoneCompatDefinition(
                        CATEGORY_ID,
                        MOD_ID,
                        Component.literal(
                                "The Twilight Forest"
                        ),
                        20
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "naga",
                "Naga Defeated",
                "You've overcome the first great beast of the Twilight.",
                20,
                0.03D,
                hasAdvancement(
                        "twilightforest:progress_naga"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "lich",
                "Twilight Lich Defeated",
                "The Lich's reign has come to an end.",
                30,
                0.04D,
                hasAdvancement(
                        "twilightforest:progress_lich"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "labyrinth",
                "The Labyrinth",
                "You've conquered the depths of the Labyrinth.",
                35,
                0.05D,
                hasAdvancement(
                        "twilightforest:progress_labyrinth"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "hydra",
                "Hydra Defeated",
                "The many-headed beast has fallen.",
                40,
                0.06D,
                hasAdvancement(
                        "twilightforest:progress_hydra"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "knights",
                "Knightly Stronghold",
                "You've broken through the Dark Forest's stronghold.",
                45,
                0.06D,
                hasAdvancement(
                        "twilightforest:progress_knights"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "ur_ghast",
                "Ur-Ghast Defeated",
                "The tower's monstrous guardian has fallen.",
                50,
                0.07D,
                hasAdvancement(
                        "twilightforest:progress_ur_ghast"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "yeti",
                "Alpha Yeti Defeated",
                "You've conquered the frozen forest's great beast.",
                55,
                0.08D,
                hasAdvancement(
                        "twilightforest:progress_yeti"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "glacier",
                "Snow Queen Defeated",
                "The ruler of the Aurora Palace has fallen.",
                60,
                0.09D,
                hasAdvancement(
                        "twilightforest:progress_glacier"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "finale",
                "Twilight Conquered",
                "You've reached the end of the Twilight's progression.",
                65,
                0.12D,
                hasAdvancement(
                        "twilightforest:progress_merge"
                )
        );
    }
}