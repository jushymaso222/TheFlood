package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;
import net.minecraft.network.chat.Component;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class MekanismMilestones {

    private MekanismMilestones() {
    }

    public static final String MOD_ID =
            "mekanism";

    public static final String CATEGORY_ID =
            "mekanism";

    public static void register() {

        MilestoneCompatRegistry.register(
                new MilestoneCompatDefinition(
                        CATEGORY_ID,
                        MOD_ID,
                        Component.literal(
                                "Mekanism"
                        ),
                        30
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "basic_technology",
                "Basic Technology",
                "You've begun harnessing advanced machinery.",
                20,
                0.03D,
                hasItemId(
                        "mekanism:metallurgic_infuser"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "factory",
                "Industrialization",
                "Your machines are becoming an industry.",
                30,
                0.04D,
                hasAnyItemId(
                        "mekanism:basic_enriching_factory",
                        "mekanism:advanced_enriching_factory",
                        "mekanism:elite_enriching_factory",
                        "mekanism:ultimate_enriching_factory",
                        "mekanism:basic_infusing_factory",
                        "mekanism:advanced_infusing_factory",
                        "mekanism:elite_infusing_factory",
                        "mekanism:ultimate_infusing_factory"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "digital_miner",
                "Automated Extraction",
                "The earth now gives up its resources automatically.",
                45,
                0.05D,
                hasItemId(
                        "mekanism:digital_miner"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "atomic_disassembler",
                "Atomic Tools",
                "You've harnessed atomic power in your hands.",
                55,
                0.06D,
                hasItemId(
                        "mekanism:atomic_disassembler"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "fission",
                "Fission Power",
                "You've begun harnessing nuclear energy.",
                65,
                0.08D,
                hasAnyItemId(
                        "mekanism:fission_reactor_casing",
                        "mekanism:fission_reactor_port",
                        "mekanism:fission_fuel_assembly"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "fusion",
                "Fusion Power",
                "You've brought the power of the stars under control.",
                75,
                0.10D,
                hasAnyItemId(
                        "mekanism:fusion_reactor_controller",
                        "mekanism:fusion_reactor_frame",
                        "mekanism:fusion_reactor_port"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "antimatter",
                "Antimatter",
                "You've reached the frontier of matter itself.",
                85,
                0.12D,
                hasItemId(
                        "mekanism:pellet_antimatter"
                )
        );

        MilestoneRegistry.registerCompat(
                CATEGORY_ID,
                "mekasuit",
                "MekaSuit",
                "You've assembled Mekanism's ultimate armor.",
                90,
                0.15D,
                hasFullArmorSetIds(
                        "mekanism:mekasuit_helmet",
                        "mekanism:mekasuit_bodyarmor",
                        "mekanism:mekasuit_pants",
                        "mekanism:mekasuit_boots"
                )
        );
    }
}