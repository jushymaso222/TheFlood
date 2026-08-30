package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;
import net.minecraft.network.chat.Component;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class ProjectEMilestones {

    public static final String MOD_ID =
            "projecte";

    public static final String CATEGORY_ID =
            "projecte";

    private ProjectEMilestones() {
    }

    public static void register() {

        MilestoneCompatRegistry.register(
                new MilestoneCompatDefinition(
                        CATEGORY_ID,
                        MOD_ID,
                        Component.literal(
                                "ProjectE"
                        ),
                        60
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "philosophers_stone",
                "Equivalent Exchange",
                "You've discovered the principles of transmutation.",
                20,
                0.03D,
                hasItemId(
                        "projecte:philosophers_stone"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "transmutation",
                "Transmutation",
                "Matter itself has become interchangeable.",
                35,
                0.05D,
                hasAnyItemId(
                        "projecte:transmutation_table",
                        "projecte:transmutation_tablet"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "energy_condenser",
                "Condensed Matter",
                "You've learned to manufacture matter from energy.",
                45,
                0.06D,
                hasAnyItemId(
                        "projecte:condenser_mk1",
                        "projecte:condenser_mk2"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "klein_star",
                "Stored Energy",
                "You've concentrated immense energy into a single object.",
                55,
                0.07D,
                hasAnyItemId(
                        "projecte:klein_star_ein",
                        "projecte:klein_star_zwei",
                        "projecte:klein_star_drei",
                        "projecte:klein_star_vier",
                        "projecte:klein_star_sphere",
                        "projecte:klein_star_omega"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "red_matter",
                "Red Matter",
                "You've reached the upper limits of transmutation.",
                70,
                0.10D,
                hasAnyItemId(
                        "projecte:red_matter",
                        "projecte:red_matter_sword",
                        "projecte:red_matter_pick",
                        "projecte:red_matter_axe",
                        "projecte:red_matter_shovel",
                        "projecte:red_matter_shears"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "gem_armor",
                "Gem Armor",
                "You've assembled ProjectE's ultimate protection.",
                85,
                0.15D,
                hasFullArmorSetIds(
                        "projecte:gem_helmet",
                        "projecte:gem_chestplate",
                        "projecte:gem_leggings",
                        "projecte:gem_boots"
                )
        );
    }
}