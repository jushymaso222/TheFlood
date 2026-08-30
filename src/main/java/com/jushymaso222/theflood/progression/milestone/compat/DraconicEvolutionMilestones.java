package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class DraconicEvolutionMilestones {

    private DraconicEvolutionMilestones() {
    }

    public static void register() {

        MilestoneRegistry.registerCompat(
                "draconicevolution",
                "draconium",
                "Draconium",
                "You've begun working with draconic energy.",
                35,
                0.04D,
                hasAnyItemId(
                        "draconicevolution:draconium_ingot",
                        "draconicevolution:draconium_core"
                )
        );

        MilestoneRegistry.registerCompat(
                "draconicevolution",
                "wyvern",
                "Wyvern Technology",
                "Draconic power is becoming a serious weapon.",
                60,
                0.07D,
                hasAnyItemId(
                        "draconicevolution:wyvern_core",
                        "draconicevolution:wyvern_sword",
                        "draconicevolution:wyvern_bow",
                        "draconicevolution:wyvern_chestpiece"
                )
        );

        MilestoneRegistry.registerCompat(
                "draconicevolution",
                "awakened_draconium",
                "Awakened Draconium",
                "You've awakened the true potential of draconium.",
                75,
                0.10D,
                hasAnyItemId(
                        "draconicevolution:awakened_draconium_ingot",
                        "draconicevolution:awakened_core"
                )
        );

        MilestoneRegistry.registerCompat(
                "draconicevolution",
                "draconic",
                "Draconic Power",
                "You've reached an extraordinary level of power.",
                85,
                0.12D,
                hasAnyItemId(
                        "draconicevolution:draconic_sword",
                        "draconicevolution:draconic_bow",
                        "draconicevolution:draconic_staff",
                        "draconicevolution:draconic_chestpiece"
                )
        );

        MilestoneRegistry.registerCompat(
                "draconicevolution",
                "chaotic",
                "Chaotic Power",
                "You've reached the limits of draconic technology.",
                100,
                0.15D,
                hasAnyItemId(
                        "draconicevolution:chaos_shard",
                        "draconicevolution:chaotic_core",
                        "draconicevolution:chaotic_sword",
                        "draconicevolution:chaotic_staff",
                        "draconicevolution:chaotic_chestpiece"
                )
        );
    }
}