package com.jushymaso222.theflood.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TheFloodClientConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.BooleanValue SHOW_HEAT_HUD;
    public static final ForgeConfigSpec.EnumValue<HudCorner> HEAT_HUD_CORNER;
    public static final ForgeConfigSpec.IntValue HEAT_HUD_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HEAT_HUD_Y_OFFSET;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("heatHud");

        SHOW_HEAT_HUD = builder
                .comment("Whether the Heat HUD is visible.")
                .define("enabled", true);

        HEAT_HUD_CORNER = builder
                .comment(
                        "Corner where the Heat HUD is displayed.",
                        "Valid values: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT"
                )
                .defineEnum("corner", HudCorner.TOP_RIGHT);

        HEAT_HUD_X_OFFSET = builder
                .comment(
                        "Horizontal distance from the selected screen corner.",
                        "Increase this to move the HUD farther inward."
                )
                .defineInRange("xOffset", 10, 0, 1000);

        HEAT_HUD_Y_OFFSET = builder
                .comment(
                        "Vertical distance from the selected screen corner.",
                        "Increase this to move the HUD farther inward."
                )
                .defineInRange("yOffset", 10, 0, 1000);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

    private TheFloodClientConfig() {
    }

    public enum HudCorner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}