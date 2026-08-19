package com.jushymaso222.theflood.team.client;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.jushymaso222.theflood.config.TheFloodClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class TeamHudOverlay {

    public static final int CARD_WIDTH =
            110;

    public static final int CARD_HEIGHT =
            28;

    public static final int CARD_SPACING =
            4;


    private static final ResourceLocation DIRECTION_ARROW =
        new ResourceLocation(
                TheFlood.MOD_ID,
                "textures/gui/direction_arrow.png"
        );

    private TeamHudOverlay() {
    }

    @SubscribeEvent
    public static void registerHud(
            RegisterGuiOverlaysEvent event
    ) {
        event.registerAboveAll(
                "flood_team_hud",
                (
                        gui,
                        graphics,
                        partialTick,
                        screenWidth,
                        screenHeight
                ) -> render(
                        graphics,
                        screenWidth,
                        screenHeight
                )
        );
    }

    private static void drawDirectionArrow(
        GuiGraphics graphics,
        int centerX,
        int centerY,
        float direction
) {
    final int size = 8;

    graphics.pose().pushPose();

    /*
     * Rotate around the CENTER of the arrow,
     * not its top-left corner.
     */
    graphics.pose().translate(
            centerX,
            centerY,
            0
    );

    graphics.pose().mulPose(
            com.mojang.math.Axis.ZP.rotationDegrees(
                    direction
            )
    );

    /*
     * We're now drawing relative to the rotation
     * center, so offset by half the size.
     */
    graphics.blit(
            DIRECTION_ARROW,
            -4,
            -4,
            8,
            8,
            0.0F,
            0.0F,
            16,
            16,
            16,
            16
    );

    graphics.pose().popPose();
}

    private static void render(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        if (!TheFloodClientConfig.TEAM_HUD_VISIBLE.get()) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.screen instanceof InventoryScreen
                || minecraft.screen instanceof CreativeModeInventoryScreen
        ) {
            return;
        }

        if (
                minecraft.player == null
                || minecraft.level == null
                || !ClientTeamData.isInTeam()
        ) {
            return;
        }

        var teammates =
            ClientTeamHudData.getVisibleTeammates(
                    minecraft
            );

        if (teammates.isEmpty()) {
            return;
        }

        /*
         * Temporary position.
         *
         * We'll make this draggable immediately
         * after confirming the card itself looks right.
         */
        int startX =
                (int) Math.round(
                        TheFloodClientConfig.TEAM_HUD_X.get()
                                * screenWidth
                );

        int startY =
                (int) Math.round(
                        TheFloodClientConfig.TEAM_HUD_Y.get()
                                * screenHeight
                );

        int index = 0;

        for (ClientTeamHudData.Teammate teammate :
                teammates) {

            int y =
                    startY
                            + index
                            * (
                            CARD_HEIGHT
                                    + CARD_SPACING
                    );

            renderTeammate(
                    graphics,
                    minecraft,
                    teammate,
                    startX,
                    y,
                    false
            );

            index++;
        }

        if (
                ClientTeamHudData.getPageCount() > 1
                && ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.MANUAL
        ) {
            int pageTextY =
                    startY
                            + teammates.size()
                            * (
                            CARD_HEIGHT
                                    + CARD_SPACING
                    )
                            + 2;

            String pageText =
                    "Page "
                            + (
                            ClientTeamHudData.getCurrentPage()
                                    + 1
                    )
                            + " / "
                            + ClientTeamHudData.getPageCount();

            graphics.drawCenteredString(
                    minecraft.font,
                    pageText,
                    startX + CARD_WIDTH / 2,
                    pageTextY,
                    0xFFAAAAAA
            );
        }
    }

    public static void renderTeammate(
            GuiGraphics graphics,
            Minecraft minecraft,
            ClientTeamHudData.Teammate teammate,
            int x,
            int y,
            boolean inventoryMode
    ) {
        /*
         * Dark translucent card.
         */
        graphics.fill(
                x,
                y,
                x + CARD_WIDTH,
                y + CARD_HEIGHT,
                0x88000000
        );

       int playerColor =
        getPlayerColor(
                teammate.playerId()
        );

        int markerSize =
                7;

        int markerX =
                x + 5;

        int markerY =
                y + 5;

        graphics.fill(
                markerX,
                markerY,
                markerX + markerSize,
                markerY + markerSize,
                playerColor
        );

        String distanceText =
                getDistanceText(
                        minecraft,
                        teammate
                );

        int distanceWidth =
                minecraft.font.width(
                        distanceText
                );

        /*
        * Left side starts after the teammate color marker.
        */
        int nameX =
                x + 16;

        /*
        * Reserve the far-right side for:
        *
        * distance + gap + direction arrow
        */
        int arrowCenterX =
                x + CARD_WIDTH - 8;

        int arrowCenterY =
                y + 8;

        int distanceX =
                arrowCenterX
                        - 7
                        - distanceWidth;

        /*
        * Leave a small gap between the end of the
        * username and the beginning of the distance.
        */
        int maxNameWidth =
                Math.max(
                        0,
                        distanceX
                                - nameX
                                - 4
                );

        /*
        * Long names are shortened with "..."
        * instead of overlapping distance/arrow.
        */
        String displayName =
                trimToWidth(
                        minecraft,
                        teammate.name(),
                        maxNameWidth
                );

        /*
        * Favorites are shown in gold ONLY while
        * rendering the interactive inventory HUD.
        */
        int nameColor =
                inventoryMode
                && ClientTeamHudData.isFavorite(
                        teammate.playerId()
                )
                        ? 0xFFFFD700
                        : 0xFFFFFFFF;

        /*
        * Player name.
        */
        graphics.drawString(
                minecraft.font,
                displayName,
                nameX,
                y + 4,
                nameColor,
                true
        );

        /*
        * Distance.
        */
        graphics.drawString(
                minecraft.font,
                distanceText,
                distanceX,
                y + 4,
                0xFFBBBBBB,
                true
        );

        String currentDimension =
        minecraft.player
                .level()
                .dimension()
                .location()
                .toString();

        boolean sameDimension =
                currentDimension.equals(
                        teammate.dimension()
                );

        if (
                sameDimension
                && shouldShowDirectionArrow(
                        minecraft,
                        teammate
                )
        ) {
            float direction =
                    getDirectionAngle(
                            minecraft,
                            teammate
                    );

            drawDirectionArrow(
                    graphics,
                    arrowCenterX,
                    arrowCenterY,
                    direction
            );
        }

        /*
         * Health bar.
         */
        int barX =
                x + 5;

        int barY =
                y + 17;

        int barWidth =
                CARD_WIDTH - 10;

        int barHeight =
                6;

        graphics.fill(
                barX,
                barY,
                barX + barWidth,
                barY + barHeight,
                0xFF252525
        );

        float maxHealth =
                Math.max(
                        1.0F,
                        teammate.maxHealth()
                );

        float healthPercent =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                teammate.health()
                                        / maxHealth
                        )
                );

        int filled =
                Math.round(
                        barWidth
                                * healthPercent
                );

        int healthColor =
                getHealthColor(
                        healthPercent
                );

        graphics.fill(
                barX,
                barY,
                barX + filled,
                barY + barHeight,
                healthColor
        );
    }

    private static String getDistanceText(
            Minecraft minecraft,
            ClientTeamHudData.Teammate teammate
    ) {
        String currentDimension =
                minecraft.player
                        .level()
                        .dimension()
                        .location()
                        .toString();

        if (
                !currentDimension.equals(
                        teammate.dimension()
                )
        ) {
            return "--";
        }

        double dx =
                teammate.x()
                        - minecraft.player
                                .getX();

        double dy =
                teammate.y()
                        - minecraft.player
                                .getY();

        double dz =
                teammate.z()
                        - minecraft.player
                                .getZ();

        int blocks =
                (int) Math.round(
                        Math.sqrt(
                                dx * dx
                                        + dy * dy
                                        + dz * dz
                        )
                );

        return blocks + "m";
    }

    private static final int[] PLAYER_COLORS = {
            0xFFE57373, // red
            0xFF64B5F6, // blue
            0xFF81C784, // green
            0xFFFFD54F, // yellow
            0xFFBA68C8, // purple
            0xFF4DD0E1, // cyan
            0xFFFF8A65, // orange
            0xFFF06292  // pink
    };

    private static int getPlayerColor(
            java.util.UUID playerId
    ) {
        int index =
                Math.floorMod(
                        playerId.hashCode(),
                        PLAYER_COLORS.length
                );

        return PLAYER_COLORS[index];
    }

    private static float getDirectionAngle(
            Minecraft minecraft,
            ClientTeamHudData.Teammate teammate
    ) {
        double dx =
                teammate.x()
                        - minecraft.player.getX();

        double dz =
                teammate.z()
                        - minecraft.player.getZ();

        float targetYaw =
                (float) Math.toDegrees(
                        Math.atan2(
                                -dx,
                                dz
                        )
                );

        return Mth.wrapDegrees(
                targetYaw
                        - minecraft.player.getYRot()
        );
    }

    private static boolean shouldShowDirectionArrow(
            Minecraft minecraft,
            ClientTeamHudData.Teammate teammate
    ) {
        double dx =
                teammate.x()
                        - minecraft.player.getX();

        double dz =
                teammate.z()
                        - minecraft.player.getZ();

        /*
        * Don't show a direction arrow when the teammate
        * is practically standing on top of the player.
        *
        * 4.0 squared distance = 2 blocks.
        */
        return dx * dx + dz * dz >= 4.0;
    }

    private static String trimToWidth(
            Minecraft minecraft,
            String text,
            int maxWidth
    ) {
        if (
                minecraft.font.width(
                        text
                ) <= maxWidth
        ) {
            return text;
        }

        String ellipsis =
                "...";

        int ellipsisWidth =
                minecraft.font.width(
                        ellipsis
                );

        StringBuilder builder =
                new StringBuilder();

        for (
                int i = 0;
                i < text.length();
                i++
        ) {
            String candidate =
                    builder.toString()
                            + text.charAt(i);

            if (
                    minecraft.font.width(
                            candidate
                    )
                            + ellipsisWidth
                            > maxWidth
            ) {
                break;
            }

            builder.append(
                    text.charAt(i)
            );
        }

        return builder
                + ellipsis;
    }

    private static int getHealthColor(
            float percentage
    ) {
        if (percentage > 0.60F) {
            return 0xFF49C95A;
        }

        if (percentage > 0.30F) {
            return 0xFFE5B84C;
        }

        return 0xFFD84A4A;
    }
}