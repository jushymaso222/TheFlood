package com.jushymaso222.theflood.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TheFloodClientConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.BooleanValue SHOW_HEAT_HUD;
    public static final ForgeConfigSpec.EnumValue<HudCorner> HEAT_HUD_CORNER;
    public static final ForgeConfigSpec.IntValue HEAT_HUD_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HEAT_HUD_Y_OFFSET;

    public static final ForgeConfigSpec.DoubleValue HEAT_HUD_X;
    public static final ForgeConfigSpec.DoubleValue HEAT_HUD_Y;

    public static final ForgeConfigSpec.DoubleValue DAY_HUD_X;
    public static final ForgeConfigSpec.DoubleValue DAY_HUD_Y;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("heatHud");

        HEAT_HUD_X = builder
                .comment("Normalized horizontal position of the Heat HUD. 0.0 = left, 1.0 = right.")
                .defineInRange("heatX", 0.95, 0.0, 1.0);

        HEAT_HUD_Y = builder
                .comment("Normalized vertical position of the Heat HUD. 0.0 = top, 1.0 = bottom.")
                .defineInRange("heatY", 0.06, 0.0, 1.0);

        DAY_HUD_X = builder
                .comment("Normalized horizontal position of the Day HUD.")
                .defineInRange("dayX", 0.50, 0.0, 1.0);

        DAY_HUD_Y = builder
                .comment("Normalized vertical position of the Day HUD.")
                .defineInRange("dayY", 0.08, 0.0, 1.0);

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