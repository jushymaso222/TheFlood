package com.jushymaso222.theflood.progression.milestone.compat;

import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class PneumaticCraftMilestones {

    private PneumaticCraftMilestones() {
    }

    public static void register() {

        MilestoneRegistry.registerCompat(
                "pneumaticcraft",
                "compressed_iron",
                "Compressed Iron",
                "You've begun mastering the power of pressure.",
                20,
                0.03D,
                hasItemId(
                        "pneumaticcraft:ingot_iron_compressed"
                )
        );

        MilestoneRegistry.registerCompat(
                "pneumaticcraft",
                "pressure_chamber",
                "Under Pressure",
                "You've built the heart of pneumatic technology.",
                25,
                0.04D,
                hasAnyItemId(
                        "pneumaticcraft:pressure_chamber_wall",
                        "pneumaticcraft:pressure_chamber_valve",
                        "pneumaticcraft:pressure_chamber_interface"
                )
        );

        MilestoneRegistry.registerCompat(
                "pneumaticcraft",
                "advanced_processing",
                "Advanced Processing",
                "Pressure now drives your industrial processing.",
                40,
                0.05D,
                hasAnyItemId(
                        "pneumaticcraft:refinery",
                        "pneumaticcraft:thermopneumatic_processing_plant"
                )
        );

        MilestoneRegistry.registerCompat(
                "pneumaticcraft",
                "printed_circuit_board",
                "Precision Engineering",
                "You've entered the age of pneumatic electronics.",
                45,
                0.06D,
                hasItemId(
                        "pneumaticcraft:printed_circuit_board"
                )
        );

        MilestoneRegistry.registerCompat(
                "pneumaticcraft",
                "drone",
                "Autonomous Machines",
                "Your machines can now work without you.",
                50,
                0.07D,
                hasAnyItemId(
                        "pneumaticcraft:drone",
                        "pneumaticcraft:logistics_drone",
                        "pneumaticcraft:harvesting_drone",
                        "pneumaticcraft:collector_drone"
                )
        );

        MilestoneRegistry.registerCompat(
                "pneumaticcraft",
                "pneumatic_armor",
                "Pneumatic Armor",
                "You've turned compressed air into protection.",
                60,
                0.10D,
                hasFullArmorSetIds(
                        "pneumaticcraft:pneumatic_helmet",
                        "pneumaticcraft:pneumatic_chestplate",
                        "pneumaticcraft:pneumatic_leggings",
                        "pneumaticcraft:pneumatic_boots"
                )
        );
    }
}