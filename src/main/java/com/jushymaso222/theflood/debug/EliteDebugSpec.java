package com.jushymaso222.theflood.elite.debug;

import java.util.ArrayList;
import java.util.List;

public record EliteDebugSpec(
        String mutation,
        List<String> attributes
) {

    public static EliteDebugSpec parse(
            String input
    ) {
        if (
                input == null
                || input.isBlank()
        ) {
            return null;
        }

        String cleaned =
                input.trim();

        if (
                cleaned.startsWith("{")
                && cleaned.endsWith("}")
        ) {
            cleaned =
                    cleaned.substring(
                            1,
                            cleaned.length() - 1
                    );
        }

        String mutation =
                null;

        List<String> attributes =
                new ArrayList<>();

        int mutationIndex =
                cleaned.indexOf(
                        "mutation:"
                );

        if (mutationIndex >= 0) {

            int start =
                    mutationIndex
                            + "mutation:".length();

            int end =
                    cleaned.indexOf(
                            ',',
                            start
                    );

            if (end < 0) {
                end =
                        cleaned.length();
            }

            mutation =
                    cleaned.substring(
                            start,
                            end
                    )
                    .trim()
                    .toLowerCase();
        }

        int attributesIndex =
                cleaned.indexOf(
                        "attributes:"
                );

        if (attributesIndex >= 0) {

            int start =
                    attributesIndex
                            + "attributes:".length();

            String attributePart =
                    cleaned.substring(
                            start
                    )
                    .trim();

            if (
                    attributePart.startsWith("[")
                    && attributePart.endsWith("]")
            ) {
                attributePart =
                        attributePart.substring(
                                1,
                                attributePart.length() - 1
                        );
            }

            if (!attributePart.isBlank()) {

                for (
                        String raw :
                        attributePart.split(",")
                ) {
                    String attribute =
                            raw.trim()
                                    .toLowerCase();

                    if (!attribute.isBlank()) {
                        attributes.add(
                                attribute
                        );
                    }
                }
            }
        }

        return new EliteDebugSpec(
                mutation,
                attributes
        );
    }
}