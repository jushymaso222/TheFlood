package com.jushymaso222.theflood.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class FloodTabCapabilityPanel {

    private static final int PANEL_WIDTH =
            150;

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
        // Intentionally empty for now.
        //
        // This panel will eventually display:
        //
        // THREAT ASSESSMENT
        //
        // OFFENSE      HIGH
        // DEFENSE      ???
        // SURVIVAL     MODERATE
        // MOBILITY     ???
        //
        // FLOOD RESPONSE
        // [=============     ]
        //
        // "The Flood learns..."
        //
        // Do not populate this with fake capability data.
    }
}