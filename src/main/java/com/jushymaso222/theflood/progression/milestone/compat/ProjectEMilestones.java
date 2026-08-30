package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class ProjectEMilestones {

    private ProjectEMilestones() {
    }

    public static void register() {

        MilestoneRegistry.registerCompat(
                "projecte",
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
                "projecte",
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
                "projecte",
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
                "projecte",
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
                "projecte",
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
                "projecte",
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