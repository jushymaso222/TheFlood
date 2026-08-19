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

    private final Screen parent;

    private EditBox teamNameBox;

    private DyeColor selectedColor =
            DyeColor.RED;

    private Button colorButton;
    private Button createButton;

    public FloodCreateTeamScreen(Screen parent) {
        super(Component.literal("Create Flood Team"));

        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        teamNameBox = new EditBox(
                font,
                centerX - 100,
                65,
                200,
                20,
                Component.literal("Team Name")
        );

        teamNameBox.setMaxLength(24);

        teamNameBox.setHint(
                Component.literal("Enter team name...")
        );

        addRenderableWidget(teamNameBox);

        colorButton = Button.builder(
                getColorButtonText(),
                button -> cycleColor()
        )
        .bounds(
                centerX - 100,
                100,
                200,
                20
        )
        .build();

        addRenderableWidget(colorButton);

        createButton = Button.builder(
                Component.literal("Create Team"),
                button -> createTeam()
        )
        .bounds(
                centerX - 100,
                140,
                95,
                20
        )
        .build();

        addRenderableWidget(createButton);

        addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        button -> onClose()
                )
                .bounds(
                        centerX + 5,
                        140,
                        95,
                        20
                )
                .build()
        );

        setInitialFocus(teamNameBox);
    }

    @Override
    public void tick() {
        super.tick();

        teamNameBox.tick();

        createButton.active =
                !teamNameBox.getValue()
                        .trim()
                        .isEmpty();
    }

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
                "Team Color: "
                        + formatColorName(
                                selectedColor
                        )
        );
    }

    private String formatColorName(DyeColor color) {
        String name =
                color.getName()
                        .replace('_', ' ');

        String[] words =
                name.split(" ");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(' ');
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

    private void createTeam() {
        String teamName =
                teamNameBox.getValue().trim();

        if (teamName.isEmpty()) {
            return;
        }

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets.CreateTeamPacket(
                        teamName,
                        selectedColor
                )
        );

        /*
         * Until we have server -> client team sync,
         * return to the parent after sending.
         */
        onClose();
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);

        graphics.drawCenteredString(
                font,
                "CREATE TEAM",
                width / 2,
                25,
                0xFFFFFFFF
        );

        graphics.drawCenteredString(
                font,
                "Team Name",
                width / 2,
                52,
                0xFFAAAAAA
        );

        drawColorPreview(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    private void drawColorPreview(
            GuiGraphics graphics
    ) {
        int previewX =
                width / 2 - 110;

        int previewY = 105;

        graphics.fill(
                previewX,
                previewY,
                previewX + 10,
                previewY + 10,
                getDyeColorRgb(selectedColor)
        );
    }

    private int getDyeColorRgb(
            DyeColor color
    ) {
        float[] rgb =
                color.getTextureDiffuseColors();

        int red =
                (int) (rgb[0] * 255);

        int green =
                (int) (rgb[1] * 255);

        int blue =
                (int) (rgb[2] * 255);

        return 0xFF000000
                | (red << 16)
                | (green << 8)
                | blue;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}