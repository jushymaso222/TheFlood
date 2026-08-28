package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.elite.attributes.SpecialAttributeRegistry;

import java.util.List;

public final class EliteDanger {

    private static final double MAX_HEAT_SCORE =
            40.0D;

    private static final double MAX_ATTRIBUTE_SCORE =
            45.0D;

    private static final double SPECIAL_BONUS =
            15.0D;

    private static final int MAX_ATTRIBUTE_SLOTS =
            3;

    private static final int MAX_ATTRIBUTE_LEVEL =
            3;

    private static final int MAX_TOTAL_ATTRIBUTE_LEVELS =
            MAX_ATTRIBUTE_SLOTS
                    * MAX_ATTRIBUTE_LEVEL;

    private EliteDanger() {
    }

    public static int calculate(
            int sourceHeat,
            List<EliteAttributes.RolledAttribute> attributes
    ) {
        int clampedHeat =
                Math.max(
                        0,
                        Math.min(
                                100,
                                sourceHeat
                        )
                );

        double heatScore =
                (
                        clampedHeat / 100.0D
                )
                        * MAX_HEAT_SCORE;

        int totalAttributeLevels =
                0;

        boolean hasSpecial =
                false;

        if (attributes != null) {
            for (
                    EliteAttributes.RolledAttribute attribute :
                    attributes
            ) {
                if (attribute == null) {
                    continue;
                }

                int level =
                        Math.max(
                                1,
                                Math.min(
                                        MAX_ATTRIBUTE_LEVEL,
                                        attribute.level()
                                )
                        );

                totalAttributeLevels +=
                        level;

                if (
                        SpecialAttributeRegistry.contains(
                                attribute.id()
                        )
                ) {
                    hasSpecial =
                            true;
                }
            }
        }

        totalAttributeLevels =
                Math.min(
                        MAX_TOTAL_ATTRIBUTE_LEVELS,
                        totalAttributeLevels
                );

        double attributeScore =
                (
                        totalAttributeLevels
                                / (double) MAX_TOTAL_ATTRIBUTE_LEVELS
                )
                        * MAX_ATTRIBUTE_SCORE;

        double specialScore =
                hasSpecial
                        ? SPECIAL_BONUS
                        : 0.0D;

        int danger =
                (int) Math.round(
                        heatScore
                                + attributeScore
                                + specialScore
                );

        return Math.max(
                1,
                Math.min(
                        100,
                        danger
                )
        );
    }
}