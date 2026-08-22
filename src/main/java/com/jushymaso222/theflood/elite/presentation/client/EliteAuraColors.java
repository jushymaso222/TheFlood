package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;

public final class EliteAuraColors {

    private EliteAuraColors() {
    }

    public static AuraColor get(
            ClientEliteStateData.EliteState state
    ) {
        String mutationId =
                state.mutationId();

        if (
                "mimic".equals(
                        mutationId
                )
                && state.mimicRevealed()
                && state.copiedMutationId() != null
                && !state.copiedMutationId().isBlank()
        ) {
            mutationId =
                    state.copiedMutationId();
        }

        return switch (mutationId) {

            case "bulwark" ->
                    new AuraColor(
                            0.15F,
                            0.85F,
                            1.00F
                    );

            case "undying" ->
                    new AuraColor(
                            0.55F,
                            0.12F,
                            0.80F
                    );

            case "shifting" ->
                    new AuraColor(
                            0.65F,
                            0.20F,
                            1.00F
                    );

            case "commander" ->
                    new AuraColor(
                            1.00F,
                            0.60F,
                            0.08F
                    );

            case "infested" ->
                    new AuraColor(
                            0.35F,
                            0.90F,
                            0.18F
                    );

            case "pursuer" ->
                    new AuraColor(
                            1.00F,
                            0.15F,
                            0.08F
                    );

            case "spiked" ->
                    new AuraColor(
                            0.10F,
                            0.70F,
                            1.00F
                    );

            case "mimic" ->
                    new AuraColor(
                            0.80F,
                            0.12F,
                            0.90F
                    );

            default ->
                    new AuraColor(
                            0.90F,
                            0.08F,
                            0.10F
                    );
        };
    }

    public record AuraColor(
            float red,
            float green,
            float blue
    ) {
    }
}