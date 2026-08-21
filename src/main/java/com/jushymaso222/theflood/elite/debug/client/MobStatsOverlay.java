package com.jushymaso222.theflood.elite.debug.client;

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
public final class MobStatsOverlay {

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

    private static final int MUTATION =
            0xFFFFAA00;

    private static final int STANDARD_ATTRIBUTE =
            0xFFFFFFFF;

    private static final int SPECIAL_ATTRIBUTE =
            0xFFFF66FF;


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


    private MobStatsOverlay() {
    }


    @SubscribeEvent
    public static void onRenderGui(
            RenderGuiOverlayEvent.Post event
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        /*
         * Don't render if the game isn't actually
         * in a playable world.
         */
        if (
                minecraft.player == null
                || minecraft.level == null
        ) {
            return;
        }

        ClientMobStatsData.MobStats stats =
                ClientMobStatsData.get();

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
         * Simple border.
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
            ClientMobStatsData.MobStats stats
    ) {
        List<Line> lines =
                new ArrayList<>();

        /*
         * HEADER
         */

        lines.add(
                new Line(
                        "THE FLOOD - MOB INSPECTOR",
                        TITLE
                )
        );

        lines.add(
                new Line(
                        stats.entityName()
                                + "  ["
                                + stats.entityId()
                                + "]",
                        VALUE
                )
        );

        lines.add(
                new Line(
                        stats.entityType(),
                        LABEL
                )
        );

        blank(
                lines
        );


        /*
         * ELITE
         */

        section(
                lines,
                "ELITE"
        );

        value(
                lines,
                "Elite",
                stats.elite()
                        ? "YES"
                        : "NO",
                stats.elite()
                        ? POSITIVE
                        : LABEL
        );

        if (stats.elite()) {

            value(
                    lines,
                    "Mutation",
                    stats.mutation(),
                    MUTATION
            );

            value(
                    lines,
                    "Heat",
                    Integer.toString(
                            stats.heat()
                    ),
                    VALUE
            );

            /*
             * We'll improve this shortly so individual
             * Special attributes can be magenta inside
             * the same line.
             *
             * For now this displays the stored list.
             */
            value(
                    lines,
                    "Attributes",
                    formatAttributes(
                            stats.attributes()
                    ),
                    VALUE
            );
        }

        blank(
                lines
        );


        /*
         * HEALTH
         */

        section(
                lines,
                "HEALTH"
        );

        value(
                lines,
                "Health",
                format(
                        stats.health()
                )
                        + " / "
                        + format(
                        stats.maxHealth()
                ),
                VALUE
        );

        float healthPercent =
                stats.maxHealth() > 0.0F
                        ? stats.health()
                        / stats.maxHealth()
                        * 100.0F
                        : 0.0F;

        value(
                lines,
                "Percent",
                format(
                        healthPercent
                )
                        + "%",
                getHealthColor(
                        healthPercent
                )
        );

        blank(
                lines
        );


        /*
         * COMBAT
         */

        section(
                lines,
                "COMBAT"
        );

        attributeValue(
                lines,
                "Attack Damage",
                stats.baseAttackDamage(),
                stats.attackDamage()
        );

        attributeValue(
                lines,
                "Armor",
                stats.baseArmor(),
                stats.armor()
        );

        attributeValue(
                lines,
                "Armor Toughness",
                stats.baseArmorToughness(),
                stats.armorToughness()
        );

        attributeValue(
                lines,
                "Attack Knockback",
                stats.baseAttackKnockback(),
                stats.attackKnockback()
        );

        attributeValue(
                lines,
                "KB Resistance",
                stats.baseKnockbackResistance(),
                stats.knockbackResistance()
        );

        blank(
                lines
        );


        /*
         * MOVEMENT / AI
         */

        section(
                lines,
                "MOVEMENT / AI"
        );

        attributeValue(
                lines,
                "Movement Speed",
                stats.baseMovementSpeed(),
                stats.movementSpeed()
        );

        attributeValue(
                lines,
                "Follow Range",
                stats.baseFollowRange(),
                stats.followRange()
        );

        value(
                lines,
                "Target",
                stats.targetName(),
                VALUE
        );

        value(
                lines,
                "On Ground",
                Boolean.toString(
                        stats.onGround()
                ),
                VALUE
        );

        value(
                lines,
                "On Fire",
                Boolean.toString(
                        stats.onFire()
                ),
                stats.onFire()
                        ? NEGATIVE
                        : VALUE
        );

        blank(
                lines
        );


        /*
         * POSITION
         */

        section(
                lines,
                "POSITION"
        );

        value(
                lines,
                "X",
                format(
                        stats.x()
                ),
                VALUE
        );

        value(
                lines,
                "Y",
                format(
                        stats.y()
                ),
                VALUE
        );

        value(
                lines,
                "Z",
                format(
                        stats.z()
                ),
                VALUE
        );

        return lines;
    }

    private static String formatAttributes(
            List<String> attributes
    ) {
        if (
                attributes == null
                || attributes.isEmpty()
        ) {
            return "NONE";
        }

        List<String> display =
                new ArrayList<>();

        for (String stored : attributes) {
            String[] parts =
                    stored.split(
                            ":",
                            2
                    );

            String name =
                    parts[0]
                            .replace(
                                    "_",
                                    " "
                            )
                            .toUpperCase();

            int level =
                    1;

            if (parts.length > 1) {
                try {
                    level =
                            Integer.parseInt(
                                    parts[1]
                            );
                } catch (NumberFormatException ignored) {
                }
            }

            if (level == 2) {
                name += "+";
            } else if (level >= 3) {
                name += "++";
            }

            display.add(
                    name
            );
        }

        return String.join(
                " • ",
                display
        );
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

    private static void attributeValue(
            List<Line> lines,
            String label,
            double base,
            double current
    ) {
        int color =
                getModifierColor(
                        base,
                        current
                );

        String text =
                format(
                        current
                )
                        + "   [base "
                        + format(
                        base
                )
                        + "]";

        value(
                lines,
                label,
                text,
                color
        );
    }

    private static int getModifierColor(
            double base,
            double current
    ) {
        double difference =
                current - base;

        if (
                Math.abs(
                        difference
                ) < 0.0001D
        ) {
            return VALUE;
        }

        return difference > 0.0D
                ? POSITIVE
                : NEGATIVE;
    }

    private static int getHealthColor(
            float percent
    ) {
        if (percent <= 25.0F) {
            return NEGATIVE;
        }

        if (percent <= 50.0F) {
            return TITLE;
        }

        return POSITIVE;
    }

    private static String format(
            double value
    ) {
        return String.format(
                "%.3f",
                value
        );
    }


    private record Line(
            String text,
            int color
    ) {
    }
}