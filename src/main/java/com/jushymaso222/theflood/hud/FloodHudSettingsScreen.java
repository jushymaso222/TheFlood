package com.jushymaso222.theflood.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.jushymaso222.theflood.config.TheFloodClientConfig;

public class FloodHudSettingsScreen extends Screen {

    private final Screen parent;

    public FloodHudSettingsScreen(Screen parent) {
        super(Component.literal("The Flood Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Edit HUD Layout"),
                        button -> minecraft.setScreen(
                                new FloodHudLayoutScreen(this)
                        )
                )
                .bounds(
                        centerX - 75,
                        height / 2 - 20,
                        150,
                        20
                )
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        getTeamHudButtonText(),
                        button -> {

                        boolean visible =
                                !TheFloodClientConfig
                                        .TEAM_HUD_VISIBLE
                                        .get();

                        TheFloodClientConfig
                                .TEAM_HUD_VISIBLE
                                .set(
                                        visible
                                );

                        button.setMessage(
                                getTeamHudButtonText()
                        );
                        }
                )
                .bounds(
                        centerX - 75,
                        height / 2 + 10,
                        150,
                        20
                )
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                )
                .bounds(
                        centerX - 50,
                        height / 2 + 45,
                        100,
                        20
                )
                .build()
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.drawCenteredString(
                font,
                "THE FLOOD",
                width / 2,
                35,
                0xFFFFFFFF
        );
    }

    private Component getTeamHudButtonText() {
        return Component.literal(
                "Team HUD: "
                        + (
                        TheFloodClientConfig
                                .TEAM_HUD_VISIBLE
                                .get()
                                ? "ON"
                                : "OFF"
                )
        );
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}