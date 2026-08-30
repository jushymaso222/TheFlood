package com.jushymaso222.theflood.progression.milestone.compat;

import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MilestoneCompatRegistry {

    private static final List<MilestoneCompatDefinition> DEFINITIONS =
            new ArrayList<>();

    private MilestoneCompatRegistry() {
    }


    public static void register(
            MilestoneCompatDefinition definition
    ) {
        if (definition == null) {
            return;
        }

        DEFINITIONS.removeIf(
                existing ->
                        existing.id()
                                .equals(
                                        definition.id()
                                )
        );

        DEFINITIONS.add(
                definition
        );
    }


    public static List<MilestoneCompatDefinition> all() {
        return DEFINITIONS.stream()
                .sorted(
                        Comparator.comparingInt(
                                MilestoneCompatDefinition::sortOrder
                        )
                )
                .toList();
    }


    public static List<MilestoneCompatDefinition> available() {
        return all()
                .stream()
                .filter(
                        MilestoneCompatRegistry::isAvailable
                )
                .toList();
    }


    private static boolean isAvailable(
            MilestoneCompatDefinition definition
    ) {
        if (definition.isAlwaysAvailable()) {
            return true;
        }

        return ModList.get()
                .isLoaded(
                        definition.requiredModId()
                );
    }
}