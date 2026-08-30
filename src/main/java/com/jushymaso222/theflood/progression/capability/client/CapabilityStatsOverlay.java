package com.jushymaso222.theflood.progression.capability.client;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class CapabilityStatsOverlay {

    /*
     * =====================================================
     * COLORS
     * =====================================================
     */

    private static final int BACKGROUND =
            0xB0000000;

    private static final int BORDER =
            0xAA666666;

    private static final int TITLE =
            0xFFFFAA00;

    private static final int SECTION =
            0xFFFFD966;

    private static final int LABEL =
            0xFFAAAAAA;

    private static final int VALUE =
            0xFFFFFFFF;

    private static final int POSITIVE =
            0xFF55FF55;

    private static final int NEGATIVE =
            0xFFFF5555;

    private static final int LEARNING =
            0xFFFFFF55;


    /*
     * =====================================================
     * LAYOUT
     * =====================================================
     */

    private static final int X =
            8;

    private static final int Y =
            8;

    private static final int WIDTH =
            245;

    private static final int PADDING =
            6;

    private static final int LINE_HEIGHT =
            10;


    private CapabilityStatsOverlay() {
    }


    @SubscribeEvent
    public static void onRenderGui(
            RenderGuiOverlayEvent.Post event
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.player == null
                || minecraft.level == null
        ) {
            return;
        }


        ClientCapabilityStatsData.CapabilityStats stats =
                ClientCapabilityStatsData.get();

        if (stats == null) {
            return;
        }


        GuiGraphics graphics =
                event.getGuiGraphics();

        Font font =
                minecraft.font;

        List<Line> lines =
                buildLines(
                        stats
                );

        if (lines.isEmpty()) {
            return;
        }


        int height =
                PADDING * 2
                        + lines.size()
                        * LINE_HEIGHT;


        /*
         * Background.
         */
        graphics.fill(
                X,
                Y,
                X + WIDTH,
                Y + height,
                BACKGROUND
        );


        /*
         * Border.
         */
        graphics.fill(
                X,
                Y,
                X + WIDTH,
                Y + 1,
                BORDER
        );

        graphics.fill(
                X,
                Y + height - 1,
                X + WIDTH,
                Y + height,
                BORDER
        );

        graphics.fill(
                X,
                Y,
                X + 1,
                Y + height,
                BORDER
        );

        graphics.fill(
                X + WIDTH - 1,
                Y,
                X + WIDTH,
                Y + height,
                BORDER
        );


        int drawY =
                Y + PADDING;

        for (Line line : lines) {

            graphics.drawString(
                    font,
                    line.text(),
                    X + PADDING,
                    drawY,
                    line.color(),
                    false
            );

            drawY +=
                    LINE_HEIGHT;
        }
    }


    /*
     * =====================================================
     * CONTENT
     * =====================================================
     */

    private static List<Line> buildLines(
            ClientCapabilityStatsData.CapabilityStats stats
    ) {
        List<Line> lines =
                new ArrayList<>();


        /*
         * HEADER
         */

        lines.add(
                new Line(
                        "THE FLOOD - CAPABILITY INSPECTOR",
                        TITLE
                )
        );

        lines.add(
                new Line(
                        stats.playerName(),
                        VALUE
                )
        );

        blank(
                lines
        );


        /*
         * OFFENSE
         */

        section(
                lines,
                "OFFENSE"
        );

        value(
                lines,
                "Current",
                format(stats.offenseScore()),
                VALUE
        );

        value(
                lines,
                "Effective",
                format(stats.effectiveOffense()),
                VALUE
        );

        value(
                lines,
                "Peak",
                format(stats.peakOffense()),
                VALUE
        );

        value(
                lines,
                "Confidence",
                percent(stats.offenseConfidence()),
                confidenceColor(
                        stats.offenseConfidence()
                )
        );

        value(
                lines,
                "Samples",
                Long.toString(
                        stats.offenseSamples()
                ),
                VALUE
        );

        blank(
                lines
        );


        /*
         * OFFENSE OBSERVATIONS
         */

        section(
                lines,
                "OFFENSE OBSERVATIONS"
        );

        value(
                lines,
                "Last Raw Damage",
                format(
                        stats.lastOffensiveDamage()
                ),
                VALUE
        );

        value(
                lines,
                "Last Observation",
                format(
                        stats.lastOffenseObservation()
                ),
                VALUE
        );

        value(
                lines,
                "Average Damage",
                format(
                        stats.averageOffensiveDamage()
                ),
                VALUE
        );

        value(
                lines,
                "Peak Damage",
                format(
                        stats.peakObservedOffensiveDamage()
                ),
                VALUE
        );

        blank(
                lines
        );


        /*
         * DEFENSE
         */

        section(
                lines,
                "DEFENSE"
        );

        value(
                lines,
                "Current",
                format(
                        stats.defenseScore()
                ),
                VALUE
        );

        value(
                lines,
                "Effective",
                format(
                        stats.effectiveDefense()
                ),
                VALUE
        );

        value(
                lines,
                "Peak",
                format(
                        stats.peakDefense()
                ),
                VALUE
        );

        value(
                lines,
                "Confidence",
                percent(
                        stats.defenseConfidence()
                ),
                confidenceColor(
                        stats.defenseConfidence()
                )
        );

        value(
                lines,
                "Sensor",
                "NOT IMPLEMENTED",
                NEGATIVE
        );

        blank(
                lines
        );


        /*
         * SURVIVAL
         */

        section(
                lines,
                "SURVIVAL"
        );

        value(
                lines,
                "Current",
                format(
                        stats.survivalScore()
                ),
                VALUE
        );

        value(
                lines,
                "Effective",
                format(
                        stats.effectiveSurvival()
                ),
                VALUE
        );

        value(
                lines,
                "Peak",
                format(
                        stats.peakSurvival()
                ),
                VALUE
        );

        value(
                lines,
                "Confidence",
                percent(
                        stats.survivalConfidence()
                ),
                confidenceColor(
                        stats.survivalConfidence()
                )
        );

        value(
                lines,
                "Sensor",
                "NOT IMPLEMENTED",
                NEGATIVE
        );

        blank(
                lines
        );


        /*
         * PROGRESSION
         */

        section(
                lines,
                "KNOWN PROGRESSION"
        );

        value(
                lines,
                "Milestone Value",
                Integer.toString(
                        stats.milestoneProgression()
                ),
                VALUE
        );

        blank(
                lines
        );


        /*
         * SYSTEM
         *
         * These are deliberately explicit because
         * Capability does NOT affect Flood difficulty yet.
         */

        section(
                lines,
                "SYSTEM"
        );

        value(
                lines,
                "Difficulty Influence",
                "DISABLED",
                NEGATIVE
        );

        value(
                lines,
                "Aggregate Score",
                "DISABLED",
                NEGATIVE
        );

        value(
                lines,
                "Offense State",
                capabilityState(
                        stats.effectiveOffense(),
                        stats.offenseConfidence()
                ),
                confidenceColor(
                        stats.offenseConfidence()
                )
        );


        return lines;
    }


    /*
     * =====================================================
     * LINE HELPERS
     * =====================================================
     */

    private static void section(
            List<Line> lines,
            String name
    ) {
        lines.add(
                new Line(
                        name,
                        SECTION
                )
        );
    }


    private static void blank(
            List<Line> lines
    ) {
        lines.add(
                new Line(
                        "",
                        VALUE
                )
        );
    }


    private static void value(
            List<Line> lines,
            String label,
            String value,
            int color
    ) {
        lines.add(
                new Line(
                        String.format(
                                "%-18s %s",
                                label + ":",
                                value
                        ),
                        color
                )
        );
    }


    /*
     * =====================================================
     * FORMATTING
     * =====================================================
     */

    private static String format(
            double value
    ) {
        return String.format(
                "%.3f",
                value
        );
    }


    private static String percent(
            double value
    ) {
        return String.format(
                "%.1f%%",
                value * 100.0D
        );
    }


    private static int confidenceColor(
            double confidence
    ) {
        if (confidence <= 0.0D) {
            return LABEL;
        }

        if (confidence < 0.40D) {
            return LEARNING;
        }

        return POSITIVE;
    }


    private static String capabilityState(
            double score,
            double confidence
    ) {
        if (confidence <= 0.0D) {
            return "UNKNOWN";
        }

        if (confidence < 0.20D) {
            return "LEARNING";
        }

        if (score < 20.0D) {
            return "LOW";
        }

        if (score < 40.0D) {
            return "MODERATE";
        }

        if (score < 60.0D) {
            return "STRONG";
        }

        if (score < 80.0D) {
            return "VERY STRONG";
        }

        return "EXTREME";
    }


    private record Line(
            String text,
            int color
    ) {
    }
}