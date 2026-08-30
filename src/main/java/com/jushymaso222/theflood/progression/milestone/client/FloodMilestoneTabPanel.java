package com.jushymaso222.theflood.progression.milestone.client;

import com.jushymaso222.theflood.progression.HeatTier;
import com.jushymaso222.theflood.progression.client.ClientHeatData;
import com.jushymaso222.theflood.progression.milestone.MilestoneDefinition;
import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class FloodMilestoneTabPanel {

    /*
     * =================================================
     * PANEL LAYOUT
     * =================================================
     */

    public static final int WIDTH =
            150;

    public static final int GAP =
            8;

    private static final int PADDING =
            10;

    private static final int ENTRY_GAP =
            6;

    private static final int ENTRY_HEIGHT =
            48;

    private static final int VIEW_ALL_HEIGHT =
            18;


    private FloodMilestoneTabPanel() {
    }


    /*
     * =================================================
     * RENDER
     * =================================================
     */

    public static void render(
            GuiGraphics graphics,
            Minecraft minecraft,
            int mainPanelX,
            int panelY,
            int panelHeight,
            double mouseX,
            double mouseY,
            boolean interactionMode
    ) {
        if (
                minecraft == null
                        || minecraft.player == null
        ) {
            return;
        }


        int x =
                mainPanelX
                        - GAP
                        - WIDTH;

        int y =
                panelY;


        /*
         * =================================================
         * BACKGROUND
         * =================================================
         */

        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + panelHeight,
                0xB0000000
        );


        /*
         * Border.
         */
        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + 1,
                0xFFFFFFFF
        );

        graphics.fill(
                x,
                y + panelHeight - 1,
                x + WIDTH,
                y + panelHeight,
                0xFFFFFFFF
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + panelHeight,
                0xFFFFFFFF
        );

        graphics.fill(
                x + WIDTH - 1,
                y,
                x + WIDTH,
                y + panelHeight,
                0xFFFFFFFF
        );


        int heat =
                ClientHeatData.getEffectiveHeat();

        int accentColor =
                HeatTier.getColor(
                        heat
                );


        /*
         * =================================================
         * HEADER
         * =================================================
         */

        graphics.drawCenteredString(
                minecraft.font,
                "MILESTONES",
                x + WIDTH / 2,
                y + 12,
                0xFFFFFFFF
        );


        graphics.fill(
                x + PADDING,
                y + 28,
                x + WIDTH - PADDING,
                y + 29,
                0x66555555
        );


        /*
         * =================================================
         * RECENT COMPLETIONS
         * =================================================
         */

        List<ResourceLocation> recent =
                ClientMilestoneData.getRecent();

        int contentY =
                y + 38;


        if (recent.isEmpty()) {

            graphics.drawCenteredString(
                    minecraft.font,
                    "No milestones",
                    x + WIDTH / 2,
                    contentY + 8,
                    0xFF888888
            );

            graphics.drawCenteredString(
                    minecraft.font,
                    "completed yet",
                    x + WIDTH / 2,
                    contentY + 20,
                    0xFF666666
            );
        }
        else {

            /*
             * Leave enough room at the bottom for
             * VIEW ALL regardless of panel height.
             */
            int availableBottom =
                    y
                            + panelHeight
                            - VIEW_ALL_HEIGHT
                            - 18;


            for (
                    ResourceLocation id :
                    recent
            ) {
                if (
                        contentY
                                + ENTRY_HEIGHT
                                > availableBottom
                ) {
                    break;
                }


                MilestoneDefinition milestone =
                        findMilestone(
                                id
                        );

                if (milestone == null) {
                    continue;
                }


                renderMilestoneEntry(
                        graphics,
                        minecraft,
                        milestone,
                        x + PADDING,
                        contentY,
                        WIDTH - PADDING * 2,
                        accentColor
                );


                contentY +=
                        ENTRY_HEIGHT
                                + ENTRY_GAP;
            }
        }


        /*
         * =================================================
         * VIEW ALL BUTTON
         * =================================================
         */

        int buttonX =
                x + PADDING;

        int buttonY =
                y
                        + panelHeight
                        - VIEW_ALL_HEIGHT
                        - PADDING;

        int buttonWidth =
                WIDTH
                        - PADDING * 2;


        boolean hovered =
                interactionMode
                        && isInside(
                                mouseX,
                                mouseY,
                                buttonX,
                                buttonY,
                                buttonWidth,
                                VIEW_ALL_HEIGHT
                        );


        graphics.fill(
                buttonX,
                buttonY,
                buttonX + buttonWidth,
                buttonY + VIEW_ALL_HEIGHT,
                hovered
                        ? 0x55333333
                        : 0x33222222
        );


        int buttonBorder =
                hovered
                        ? accentColor
                        : 0xFF555555;


        drawBorder(
                graphics,
                buttonX,
                buttonY,
                buttonWidth,
                VIEW_ALL_HEIGHT,
                buttonBorder
        );


        graphics.drawCenteredString(
                minecraft.font,
                "[ VIEW ALL ]",
                buttonX + buttonWidth / 2,
                buttonY + 5,
                hovered
                        ? accentColor
                        : 0xFFAAAAAA
        );
    }


    /*
     * =================================================
     * MILESTONE ENTRY
     * =================================================
     */

    private static void renderMilestoneEntry(
            GuiGraphics graphics,
            Minecraft minecraft,
            MilestoneDefinition milestone,
            int x,
            int y,
            int width,
            int accentColor
    ) {
        /*
         * Small quest-card background.
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + ENTRY_HEIGHT,
                0x38222222
        );


        /*
         * Accent strip.
         */
        graphics.fill(
                x,
                y,
                x + 2,
                y + ENTRY_HEIGHT,
                accentColor
        );


        int textX =
                x + 7;


        /*
         * Title.
         */
        graphics.drawString(
                minecraft.font,
                milestone.title(),
                textX,
                y + 5,
                0xFFFFFFFF,
                true
        );


        /*
         * Description.
         *
         * Only show as much as comfortably fits
         * inside this compact Tab panel.
         */
        List<FormattedCharSequence> descriptionLines =
                minecraft.font.split(
                        milestone.description(),
                        width - 12
                );


        int descriptionY =
                y + 17;

        int maxDescriptionLines =
                2;


        for (
                int i = 0;
                i < descriptionLines.size()
                        && i < maxDescriptionLines;
                i++
        ) {
            graphics.drawString(
                    minecraft.font,
                    descriptionLines.get(
                            i
                    ),
                    textX,
                    descriptionY
                            + i
                            * minecraft.font.lineHeight,
                    0xFFAAAAAA,
                    false
            );
        }


        /*
         * XP reward.
         */
        String reward =
                "+"
                        + Math.round(
                                milestone.floodXpReward()
                                        * 100.0D
                        )
                        + "% XP";


        int rewardWidth =
                minecraft.font.width(
                        reward
                );


        graphics.drawString(
                minecraft.font,
                reward,
                x + width - rewardWidth - 5,
                y + ENTRY_HEIGHT - 11,
                accentColor,
                false
        );
    }


    /*
     * =================================================
     * CLICK HANDLING
     * =================================================
     */

    public static boolean handleClick(
            Minecraft minecraft,
            double mouseX,
            double mouseY,
            int mainPanelX,
            int panelY,
            int panelHeight
    ) {
        if (minecraft == null) {
            return false;
        }


        int x =
                mainPanelX
                        - GAP
                        - WIDTH;


        int buttonX =
                x + PADDING;

        int buttonY =
                panelY
                        + panelHeight
                        - VIEW_ALL_HEIGHT
                        - PADDING;

        int buttonWidth =
                WIDTH
                        - PADDING * 2;


        if (
                !isInside(
                        mouseX,
                        mouseY,
                        buttonX,
                        buttonY,
                        buttonWidth,
                        VIEW_ALL_HEIGHT
                )
        ) {
            return false;
        }


        minecraft.setScreen(
                new FloodMilestoneScreen()
        );

        return true;
    }


    /*
     * =================================================
     * REGISTRY LOOKUP
     * =================================================
     */

    private static MilestoneDefinition findMilestone(
            ResourceLocation id
    ) {
        if (id == null) {
            return null;
        }


        for (
                MilestoneDefinition milestone :
                MilestoneRegistry.all()
        ) {
            if (
                    milestone.id()
                            .equals(
                                    id
                            )
            ) {
                return milestone;
            }
        }


        return null;
    }


    /*
     * =================================================
     * HELPERS
     * =================================================
     */

    private static boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }


    private static void drawBorder(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                color
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                color
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                color
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                color
        );
    }
}