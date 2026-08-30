package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class BotaniaMilestones {

    private BotaniaMilestones() {
    }

    public static void register() {

        MilestoneRegistry.registerCompat(
                "botania",
                "mana",
                "Mana Manipulation",
                "You've begun harnessing the power of mana.",
                20,
                0.03D,
                hasAnyItemId(
                        "botania:mana_pool",
                        "botania:mana_spreader"
                )
        );

        MilestoneRegistry.registerCompat(
                "botania",
                "runic_altar",
                "Runic Magic",
                "You've learned to shape mana into runes.",
                30,
                0.04D,
                hasItemId(
                        "botania:runic_altar"
                )
        );

        MilestoneRegistry.registerCompat(
                "botania",
                "terrasteel",
                "Terrasteel",
                "You've forged mana into something far stronger.",
                50,
                0.06D,
                hasAnyItemId(
                        "botania:terrasteel_ingot",
                        "botania:terrasteel_helmet",
                        "botania:terrasteel_chestplate",
                        "botania:terrasteel_leggings",
                        "botania:terrasteel_boots"
                )
        );

        MilestoneRegistry.registerCompat(
                "botania",
                "alfheim",
                "Secrets of Alfheim",
                "You've gained access to elven knowledge.",
                55,
                0.07D,
                hasAnyItemId(
                        "botania:elf_glass",
                        "botania:dreamwood",
                        "botania:elementium_ingot",
                        "botania:pixie_dust",
                        "botania:dragonstone"
                )
        );

        MilestoneRegistry.registerCompat(
                "botania",
                "gaia",
                "Guardian of Gaia",
                "You've overcome the Guardian of Gaia.",
                65,
                0.10D,
                hasItemId(
                        "botania:life_essence"
                )
        );

        MilestoneRegistry.registerCompat(
                "botania",
                "gaia_2",
                "Gaia's Final Challenge",
                "You've conquered Botania's greatest challenge.",
                75,
                0.12D,
                hasAnyItemId(
                        "botania:gaia_head",
                        "botania:flugel_eye"
                )
        );
    }
}