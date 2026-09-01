package com.jushymaso222.theflood.hud;

import com.jushymaso222.theflood.progression.capability.client.ClientCapabilityStatsData;

import com.jushymaso222.theflood.progression.HeatTier;
import com.jushymaso222.theflood.progression.client.ClientHeatData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class FloodTabCapabilityPanel {

    private static final int PANEL_WIDTH =
            150;

    private static final int PADDING =
            8;

    private static final double MIN_CONFIDENCE =
            0.20D;

    /*
     * Maximum response returned by CapabilityManager
     * for a single capability dimension.
     */
    private static final double MAX_RESPONSE =
            0.40D;

    private FloodTabCapabilityPanel() {
    }

    public static int getPanelWidth() {
        return PANEL_WIDTH;
    }

    public static void render(
            GuiGraphics graphics,
            Minecraft minecraft,
            int x,
            int y,
            int height
    ) {
        /*
        * Draw panel background and border first so
        * all content renders on top of it.
        */
        renderBackground(
                graphics,
                x,
                y,
                height
        );

        ClientCapabilityStatsData.CapabilityStats stats =
                ClientCapabilityStatsData.get();

        /*
         * If the client hasn't received a snapshot yet,
         * we still render the panel rather than making
         * the entire thing pop in and out.
         */
        int contentX =
                x + 8;

        int accentColor =
            HeatTier.getColor(
                    ClientHeatData.getEffectiveHeat()
            );

        graphics.drawString(
                minecraft.font,
                "THREAT ASSESSMENT",
                contentX,
                y + 8,
                0xFFFFFFFF,
                false
        );

        int underlineY =
                y + 21;

        graphics.fill(
                contentX,
                underlineY,
                contentX + 44,
                underlineY + 1,
                accentColor
        );

        /*
         * =================================================
         * HEADER
         * =================================================
         */

        if (stats == null) {
            renderNoData(
                    graphics,
                    minecraft,
                    x,
                    y,
                    height
            );

            return;
        }

        /*
         * =================================================
         * CAPABILITY ASSESSMENT
         * =================================================
         */

        int rowY =
                y + 38;

        renderAssessmentRow(
                graphics,
                minecraft,
                "OFFENSE",
                stats.effectiveOffense(),
                stats.offenseConfidence(),
                x + PADDING,
                rowY,
                PANEL_WIDTH - PADDING * 2
        );

        rowY += 18;

        renderAssessmentRow(
                graphics,
                minecraft,
                "DEFENSE",
                stats.effectiveDefense(),
                stats.defenseConfidence(),
                x + PADDING,
                rowY,
                PANEL_WIDTH - PADDING * 2
        );

        rowY += 18;

        renderAssessmentRow(
                graphics,
                minecraft,
                "SURVIVAL",
                stats.effectiveSurvival(),
                stats.survivalConfidence(),
                x + PADDING,
                rowY,
                PANEL_WIDTH - PADDING * 2
        );

        rowY += 18;

        renderAssessmentRow(
                graphics,
                minecraft,
                "MOBILITY",
                stats.effectiveMobility(),
                stats.mobilityConfidence(),
                x + PADDING,
                rowY,
                PANEL_WIDTH - PADDING * 2
        );


        /*
         * =================================================
         * FLOOD RESPONSE
         * =================================================
         */

        int responseSectionY =
                rowY + 28;

        graphics.drawString(
                minecraft.font,
                "FLOOD RESPONSE",
                x + PADDING,
                responseSectionY,
                0xFFFFFFFF,
                false
        );

        graphics.fill(
                x + PADDING,
                responseSectionY + 13,
                x + PADDING + 44,
                responseSectionY + 14,
                accentColor
        );

        double response =
                getDisplayedResponse(
                        stats
                );

        renderResponseBar(
                graphics,
                x + PADDING,
                responseSectionY + 22,
                PANEL_WIDTH - PADDING * 2,
                response
        );

        String responseText =
                response <= 0.0D
                        ? "NONE"
                        : String.format(
                                "%.1f%%",
                                response * 100.0D
                        );

        int responseColor =
                response <= 0.0D
                        ? 0xFF777777
                        : getResponseColor(
                                response
                        );

        graphics.drawCenteredString(
                minecraft.font,
                responseText,
                x + PANEL_WIDTH / 2,
                responseSectionY + 35,
                responseColor
        );


        /*
         * =================================================
         * STATUS MESSAGE
         * =================================================
         */

        String message =
                getResponseMessage(
                        stats,
                        response
                );

        graphics.drawString(
                minecraft.font,
                message,
                x + PADDING,
                responseSectionY + 53,
                0xFF888888,
                false
        );
    }


    /*
     * =====================================================
     * BACKGROUND
     * =====================================================
     */

    private static void renderBackground(
        GuiGraphics graphics,
        int x,
        int y,
        int height
) {
    /*
     * Panel background.
     */
    graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + height,
            0xB0000000
    );

    /*
     * Top border.
     */
    graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + 1,
            0xFFFFFFFF
    );

    /*
     * Bottom border.
     */
    graphics.fill(
            x,
            y + height - 1,
            x + PANEL_WIDTH,
            y + height,
            0xFFFFFFFF
    );

    /*
     * Left border.
     */
    graphics.fill(
            x,
            y,
            x + 1,
            y + height,
            0xFFFFFFFF
    );

    /*
     * Right border.
     */
    graphics.fill(
            x + PANEL_WIDTH - 1,
            y,
            x + PANEL_WIDTH,
            y + height,
            0xFFFFFFFF
    );
}


    /*
     * =====================================================
     * ASSESSMENT ROWS
     * =====================================================
     */

    private static void renderAssessmentRow(
            GuiGraphics graphics,
            Minecraft minecraft,
            String label,
            double capability,
            double confidence,
            int x,
            int y,
            int width
    ) {
        graphics.drawString(
                minecraft.font,
                label,
                x,
                y,
                0xFFAAAAAA,
                false
        );

        String assessment =
                getAssessment(
                        capability,
                        confidence
                );

        int assessmentColor =
                getAssessmentColor(
                        capability,
                        confidence
                );

        int textWidth =
                minecraft.font.width(
                        assessment
                );

        graphics.drawString(
                minecraft.font,
                assessment,
                x + width - textWidth,
                y,
                assessmentColor,
                false
        );
    }

    private static String getAssessment(
            double capability,
            double confidence
    ) {
        if (
                !Double.isFinite(capability)
                        || !Double.isFinite(confidence)
                        || confidence < MIN_CONFIDENCE
        ) {
            return "UNKNOWN";
        }

        if (capability < 20.0D) {
            return "LOW";
        }

        if (capability < 40.0D) {
            return "MODERATE";
        }

        if (capability < 60.0D) {
            return "HIGH";
        }

        if (capability < 80.0D) {
            return "VERY HIGH";
        }

        return "EXTREME";
    }

    private static int getAssessmentColor(
            double capability,
            double confidence
    ) {
        if (
                !Double.isFinite(capability)
                        || !Double.isFinite(confidence)
                        || confidence < MIN_CONFIDENCE
        ) {
            return 0xFF777777;
        }

        if (capability < 20.0D) {
            return 0xFF55FF55;
        }

        if (capability < 40.0D) {
            return 0xFFFFFF55;
        }

        if (capability < 60.0D) {
            return 0xFFFFAA00;
        }

        if (capability < 80.0D) {
            return 0xFFFF5555;
        }

        return 0xFFFF55FF;
    }


    /*
     * =====================================================
     * FLOOD RESPONSE
     * =====================================================
     *
     * This is NOT an aggregate Capability score.
     *
     * It represents the strongest adaptation currently
     * being applied by The Flood on any one axis.
     *
     * Keeping it this way avoids pretending that Offense,
     * Defense, Survival and Mobility are interchangeable.
     */

    private static double getDisplayedResponse(
            ClientCapabilityStatsData.CapabilityStats stats
    ) {
        double response =
                Math.max(
                        stats.offenseResponse(),
                        Math.max(
                                stats.defenseResponse(),
                                Math.max(
                                        stats.survivalResponse(),
                                        stats.mobilityResponse()
                                )
                        )
                );

        if (!Double.isFinite(response)) {
            return 0.0D;
        }

        return Math.max(
                0.0D,
                Math.min(
                        MAX_RESPONSE,
                        response
                )
        );
    }

    private static void renderResponseBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            double response
    ) {
        int height =
                7;

        /*
         * Background.
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0xFF222222
        );

        double progress =
                response
                        / MAX_RESPONSE;

        progress =
                Math.max(
                        0.0D,
                        Math.min(
                                1.0D,
                                progress
                        )
                );

        int filledWidth =
                (int) Math.round(
                        width
                                * progress
                );

        if (filledWidth > 0) {
            graphics.fill(
                    x + 1,
                    y + 1,
                    x + Math.max(
                            1,
                            filledWidth - 1
                    ),
                    y + height - 1,
                    getResponseColor(
                            response
                    )
            );
        }

        /*
         * Border.
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                0xFF555555
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                0xFF555555
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                0xFF555555
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                0xFF555555
        );
    }

    private static int getResponseColor(
            double response
    ) {
        double normalized =
                Math.max(
                        0.0D,
                        Math.min(
                                1.0D,
                                response
                                        / MAX_RESPONSE
                        )
                );

        if (normalized < 0.25D) {
            return 0xFFFFFF55;
        }

        if (normalized < 0.50D) {
            return 0xFFFFAA00;
        }

        if (normalized < 0.75D) {
            return 0xFFFF5555;
        }

        return 0xFFFF55FF;
    }


    /*
     * =====================================================
     * FLAVOR / STATUS
     * =====================================================
     */

    private static String getResponseMessage(
            ClientCapabilityStatsData.CapabilityStats stats,
            double response
    ) {
        boolean stillLearning =
                stats.offenseConfidence() < MIN_CONFIDENCE
                        || stats.defenseConfidence() < MIN_CONFIDENCE
                        || stats.survivalConfidence() < MIN_CONFIDENCE
                        || stats.mobilityConfidence() < MIN_CONFIDENCE;

        if (stillLearning) {
            return "The Flood is learning...";
        }

        if (response <= 0.0D) {
            return "The Flood is watching.";
        }

        if (response < 0.10D) {
            return "The Flood is adapting.";
        }

        if (response < 0.20D) {
            return "Resistance is increasing.";
        }

        if (response < 0.30D) {
            return "The Flood knows you.";
        }

        return "You have its attention.";
    }


    /*
     * =====================================================
     * NO DATA
     * =====================================================
     */

    private static void renderNoData(
            GuiGraphics graphics,
            Minecraft minecraft,
            int x,
            int y,
            int height
    ) {
        graphics.drawCenteredString(
                minecraft.font,
                "ASSESSING...",
                x + PANEL_WIDTH / 2,
                y + 48,
                0xFF777777
        );

        graphics.drawString(
                minecraft.font,
                "The Flood is learning...",
                x + PADDING,
                y + 70,
                0xFF777777,
                false
        );
    }
}