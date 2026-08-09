package com.jushymaso222.theflood.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
                        Component.literal("Done"),
                        button -> onClose()
                )
                .bounds(
                        centerX - 50,
                        height / 2 + 20,
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

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}