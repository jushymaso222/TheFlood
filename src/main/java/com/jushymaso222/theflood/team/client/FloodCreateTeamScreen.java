package com.jushymaso222.theflood.team.client;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

public class FloodCreateTeamScreen extends Screen {

    /*
     * =================================================
     * PANEL LAYOUT
     * =================================================
     */

    private static final int PANEL_WIDTH =
            260;

    private static final int PANEL_HEIGHT =
            190;

    private static final int CONTENT_PADDING =
            18;

    /*
     * Colors intentionally match the simple dark style
     * used throughout the Flood Tab.
     */
    private static final int PANEL_BACKGROUND =
            0xE8101010;

    private static final int PANEL_BORDER =
            0xFF555555;

    private static final int PANEL_INNER_BORDER =
            0xFF282828;

    private static final int DIVIDER_COLOR =
            0xFF3A3A3A;

    private static final int TITLE_COLOR =
            0xFFFFFFFF;

    private static final int LABEL_COLOR =
            0xFFAAAAAA;

    private static final int SUBTITLE_COLOR =
            0xFF777777;


    /*
     * =================================================
     * STATE
     * =================================================
     */

    private final Screen parent;

    private EditBox teamNameBox;

    private DyeColor selectedColor =
            DyeColor.RED;

    private Button colorButton;
    private Button createButton;


    public FloodCreateTeamScreen(
            Screen parent
    ) {
        super(
                Component.literal(
                        "Create Flood Team"
                )
        );

        this.parent =
                parent;
    }


    /*
     * =================================================
     * INITIALIZATION
     * =================================================
     */

    @Override
    protected void init() {

        int panelX =
                (width - PANEL_WIDTH) / 2;

        int panelY =
                (height - PANEL_HEIGHT) / 2;

        int contentX =
                panelX + CONTENT_PADDING;

        int contentWidth =
                PANEL_WIDTH
                        - CONTENT_PADDING * 2;


        /*
         * Team name.
         */
        teamNameBox =
                new EditBox(
                        font,
                        contentX,
                        panelY + 65,
                        contentWidth,
                        20,
                        Component.literal(
                                "Team Name"
                        )
                );

        teamNameBox.setMaxLength(
                24
        );

        teamNameBox.setHint(
                Component.literal(
                        "Enter team name..."
                )
        );

        addRenderableWidget(
                teamNameBox
        );


        /*
         * Team color.
         *
         * Leave room on the left for the color swatch.
         */
        colorButton =
                Button.builder(
                        getColorButtonText(),
                        button ->
                                cycleColor()
                )
                .bounds(
                        contentX + 18,
                        panelY + 105,
                        contentWidth - 18,
                        20
                )
                .build();

        addRenderableWidget(
                colorButton
        );


        /*
         * Bottom actions.
         */
        int buttonGap =
                6;

        int buttonWidth =
                (contentWidth - buttonGap) / 2;

        int buttonY =
                panelY + 150;

        createButton =
                Button.builder(
                        Component.literal(
                                "Create Team"
                        ),
                        button ->
                                createTeam()
                )
                .bounds(
                        contentX,
                        buttonY,
                        buttonWidth,
                        20
                )
                .build();

        addRenderableWidget(
                createButton
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Cancel"
                        ),
                        button ->
                                onClose()
                )
                .bounds(
                        contentX
                                + buttonWidth
                                + buttonGap,
                        buttonY,
                        buttonWidth,
                        20
                )
                .build()
        );

        setInitialFocus(
                teamNameBox
        );
    }


    /*
     * =================================================
     * UPDATE
     * =================================================
     */

    @Override
    public void tick() {
        super.tick();

        teamNameBox.tick();

        createButton.active =
                !teamNameBox
                        .getValue()
                        .trim()
                        .isEmpty();
    }


    /*
     * =================================================
     * TEAM COLOR
     * =================================================
     */

    private void cycleColor() {

        DyeColor[] colors =
                DyeColor.values();

        int next =
                (selectedColor.ordinal() + 1)
                        % colors.length;

        selectedColor =
                colors[next];

        colorButton.setMessage(
                getColorButtonText()
        );
    }


    private Component getColorButtonText() {
        return Component.literal(
                formatColorName(
                        selectedColor
                )
        );
    }


    private String formatColorName(
            DyeColor color
    ) {
        String name =
                color.getName()
                        .replace(
                                '_',
                                ' '
                        );

        String[] words =
                name.split(
                        " "
                );

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {

            if (!result.isEmpty()) {
                result.append(
                        ' '
                );
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            result.append(
                    word.substring(1)
            );
        }

        return result.toString();
    }


    /*
     * =================================================
     * CREATE TEAM
     * =================================================
     */

    private void createTeam() {

        String teamName =
                teamNameBox
                        .getValue()
                        .trim();

        if (teamName.isEmpty()) {
            return;
        }

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets
                        .CreateTeamPacket(
                                teamName,
                                selectedColor
                        )
        );

        onClose();
    }


    /*
     * =================================================
     * RENDERING
     * =================================================
     */

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        /*
         * Keep Minecraft's normal darkened background.
         */
        renderBackground(
                graphics
        );

        int panelX =
                (width - PANEL_WIDTH) / 2;

        int panelY =
                (height - PANEL_HEIGHT) / 2;

        int panelRight =
                panelX + PANEL_WIDTH;

        int panelBottom =
                panelY + PANEL_HEIGHT;


        /*
         * Outer panel.
         */
        graphics.fill(
                panelX,
                panelY,
                panelRight,
                panelBottom,
                PANEL_BORDER
        );

        graphics.fill(
                panelX + 1,
                panelY + 1,
                panelRight - 1,
                panelBottom - 1,
                PANEL_BACKGROUND
        );

        /*
         * Subtle inner border.
         */
        graphics.fill(
                panelX + 2,
                panelY + 2,
                panelRight - 2,
                panelY + 3,
                PANEL_INNER_BORDER
        );

        graphics.fill(
                panelX + 2,
                panelBottom - 3,
                panelRight - 2,
                panelBottom - 2,
                PANEL_INNER_BORDER
        );


        /*
         * Header.
         */
        graphics.drawCenteredString(
                font,
                "CREATE TEAM",
                width / 2,
                panelY + 12,
                TITLE_COLOR
        );

        graphics.drawCenteredString(
                font,
                "Form a team against the Flood",
                width / 2,
                panelY + 25,
                SUBTITLE_COLOR
        );


        /*
         * Header divider.
         */
        graphics.fill(
                panelX + 10,
                panelY + 39,
                panelRight - 10,
                panelY + 40,
                DIVIDER_COLOR
        );


        /*
         * Field labels.
         */
        int contentX =
                panelX + CONTENT_PADDING;

        graphics.drawString(
                font,
                "TEAM NAME",
                contentX,
                panelY + 52,
                LABEL_COLOR,
                false
        );

        graphics.drawString(
                font,
                "TEAM COLOR",
                contentX,
                panelY + 92,
                LABEL_COLOR,
                false
        );


        /*
         * Selected team-color swatch.
         */
        drawColorPreview(
                graphics,
                contentX,
                panelY + 105
        );


        /*
         * Render the actual EditBox/buttons last so they
         * remain above the custom panel.
         */
        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }


    private void drawColorPreview(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        /*
         * Dark border.
         */
        graphics.fill(
                x,
                y,
                x + 14,
                y + 20,
                0xFF444444
        );

        /*
         * Actual selected team color.
         */
        graphics.fill(
                x + 2,
                y + 2,
                x + 12,
                y + 18,
                getDyeColorRgb(
                        selectedColor
                )
        );
    }


    private int getDyeColorRgb(
            DyeColor color
    ) {
        float[] rgb =
                color.getTextureDiffuseColors();

        int red =
                (int) (
                        rgb[0] * 255
                );

        int green =
                (int) (
                        rgb[1] * 255
                );

        int blue =
                (int) (
                        rgb[2] * 255
                );

        return 0xFF000000
                | (red << 16)
                | (green << 8)
                | blue;
    }


    /*
     * =================================================
     * NAVIGATION
     * =================================================
     */

    @Override
    public void onClose() {
        minecraft.setScreen(
                parent
        );
    }
}