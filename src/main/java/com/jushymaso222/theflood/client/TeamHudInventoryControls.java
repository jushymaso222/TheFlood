package com.jushymaso222.theflood.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.network.packet.TeamNetworkingPackets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class TeamHudInventoryControls {

    private static final ResourceLocation PREVIOUS_PAGE =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/team/previous_page.png"
            );

    private static final ResourceLocation NEXT_PAGE =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/team/next_page.png"
            );

    private static final ResourceLocation FILTER =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/team/filter.png"
            );

    private static final ResourceLocation FAVORITE =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/team/favorite.png"
            );

    private static final ResourceLocation CHAT_GLOBAL =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/team/global_chat.png"
            );

    private static final ResourceLocation CHAT_TEAM =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/team/team_chat.png"
            );

    private static final int BUTTON_SIZE =
            16;

    private static final int BUTTON_GAP =
            3;

    private TeamHudInventoryControls() {
    }

    @SubscribeEvent
    public static void onRenderInventory(
            ScreenEvent.Render.Post event
    ) {
        if (!isPlayerInventoryScreen(
                event.getScreen()
        )) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.player == null
                || !ClientTeamData.isInTeam()
                || !TheFloodClientConfig
                        .TEAM_HUD_VISIBLE
                        .get()
        ) {
            return;
        }

        List<ClientTeamHudData.Teammate> visible =
                ClientTeamHudData.getVisibleTeammates(
                        minecraft
                );

        if (visible.isEmpty()) {
            return;
        }

        int hudX =
                (int) Math.round(
                        TheFloodClientConfig
                                .TEAM_HUD_X
                                .get()
                                * event.getScreen().width
                );

        int hudY =
                (int) Math.round(
                        TheFloodClientConfig
                                .TEAM_HUD_Y
                                .get()
                                * event.getScreen().height
                );

        GuiGraphics graphics =
                event.getGuiGraphics();

        int chatButtonX =
                8;

        int chatButtonY =
                event.getScreen().height - 24;

        double mouseX =
                event.getMouseX();

        double mouseY =
                event.getMouseY();


        ResourceLocation chatTexture =
                ClientTeamChatData.isTeamChat()
                        ? CHAT_TEAM
                        : CHAT_GLOBAL;

        drawButton(
                graphics,
                chatTexture,
                chatButtonX,
                chatButtonY,
                ClientTeamChatData.isTeamChat()
        );

        if (
                isInside(
                        mouseX,
                        mouseY,
                        chatButtonX,
                        chatButtonY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            Component tooltip =
                    ClientTeamChatData.isTeamChat()
                            ? Component.literal(
                                    "Team Chat - Click for Global"
                            )
                            : Component.literal(
                                    "Global Chat - Click for Team"
                            );

            graphics.renderTooltip(
                    minecraft.font,
                    tooltip,
                    (int) mouseX,
                    (int) mouseY
            );
        }

        /*
        * Render the teammate cards ABOVE the inventory.
        */
        for (
                int i = 0;
                i < visible.size();
                i++
        ) {
            int cardY =
                    hudY
                            + i
                            * (
                            TeamHudOverlay.CARD_HEIGHT
                                    + TeamHudOverlay.CARD_SPACING
                    );

            TeamHudOverlay.renderTeammate(
                    graphics,
                    minecraft,
                    visible.get(i),
                    hudX,
                    cardY,
                    true
            );
        }


        /*
        * Controls go directly underneath the cards.
        */
        int controlY =
                hudY
                        + visible.size()
                        * (
                        TeamHudOverlay.CARD_HEIGHT
                                + TeamHudOverlay.CARD_SPACING
                )
                        + 2;

        int totalWidth =
                BUTTON_SIZE * 4
                        + BUTTON_GAP * 3;

        int controlX =
                hudX
                        + TeamHudOverlay.CARD_WIDTH / 2
                        - totalWidth / 2;

        int previousX =
                controlX;

        int nextX =
                previousX
                        + BUTTON_SIZE
                        + BUTTON_GAP;

        int filterX =
                nextX
                        + BUTTON_SIZE
                        + BUTTON_GAP;

        int favoriteX =
                filterX
                        + BUTTON_SIZE
                        + BUTTON_GAP;


        drawButton(
                graphics,
                PREVIOUS_PAGE,
                previousX,
                controlY,
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.MANUAL
        );

        drawButton(
                graphics,
                NEXT_PAGE,
                nextX,
                controlY,
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.MANUAL
        );

        drawButton(
                graphics,
                FILTER,
                filterX,
                controlY,
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.NEAREST
        );

        drawButton(
                graphics,
                FAVORITE,
                favoriteX,
                controlY,
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.FAVORITES
        );


        /*
        * Tooltips.
        */

        if (
                isInside(
                        mouseX,
                        mouseY,
                        previousX,
                        controlY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            graphics.renderTooltip(
                    minecraft.font,
                    Component.literal("Previous Page"),
                    (int) mouseX,
                    (int) mouseY
            );

        } else if (
                isInside(
                        mouseX,
                        mouseY,
                        nextX,
                        controlY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            graphics.renderTooltip(
                    minecraft.font,
                    Component.literal("Next Page"),
                    (int) mouseX,
                    (int) mouseY
            );

        } else if (
                isInside(
                        mouseX,
                        mouseY,
                        filterX,
                        controlY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            graphics.renderTooltip(
                    minecraft.font,
                    Component.literal("Nearest Players"),
                    (int) mouseX,
                    (int) mouseY
            );

        } else if (
                isInside(
                        mouseX,
                        mouseY,
                        favoriteX,
                        controlY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            graphics.renderTooltip(
                    minecraft.font,
                    Component.literal("Favorites"),
                    (int) mouseX,
                    (int) mouseY
            );

        } else {
            renderCardTooltip(
                    graphics,
                    minecraft,
                    visible,
                    hudX,
                    hudY,
                    mouseX,
                    mouseY
            );
        }
    }

    @SubscribeEvent
    public static void onInventoryMouseClicked(
            ScreenEvent.MouseButtonPressed.Pre event
    ) {
        if (!isPlayerInventoryScreen(
                event.getScreen()
        )) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.player == null
                || !ClientTeamData.isInTeam()
                || !TheFloodClientConfig
                        .TEAM_HUD_VISIBLE
                        .get()
        ) {
            return;
        }

        /*
         * Left click only.
         */
        if (event.getButton() != 0) {
            return;
        }

        List<ClientTeamHudData.Teammate> visible =
                ClientTeamHudData.getVisibleTeammates(
                        minecraft
                );

        if (visible.isEmpty()) {
            return;
        }

        int hudX =
                (int) Math.round(
                        TheFloodClientConfig
                                .TEAM_HUD_X
                                .get()
                                * event.getScreen().width
                );

        int hudY =
                (int) Math.round(
                        TheFloodClientConfig
                                .TEAM_HUD_Y
                                .get()
                                * event.getScreen().height
                );

        int controlY =
                hudY
                        + visible.size()
                        * (TeamHudOverlay.CARD_HEIGHT + TeamHudOverlay.CARD_SPACING)
                        + 2;

        int totalWidth =
                BUTTON_SIZE * 4
                        + BUTTON_GAP * 3;

        int controlX =
                hudX
                        + TeamHudOverlay.CARD_WIDTH / 2
                        - totalWidth / 2;

        int previousX =
                controlX;

        int nextX =
                previousX
                        + BUTTON_SIZE
                        + BUTTON_GAP;

        int filterX =
                nextX
                        + BUTTON_SIZE
                        + BUTTON_GAP;

        int favoriteX =
                filterX
                        + BUTTON_SIZE
                        + BUTTON_GAP;

        double mouseX =
                event.getMouseX();

        double mouseY =
                event.getMouseY();

        int chatButtonX =
                8;

        int chatButtonY =
                event.getScreen().height - 24;

        /*
         * Previous page.
         */
        if (
                        isInside(
                                mouseX,
                                mouseY,
                                previousX,
                                controlY,
                                BUTTON_SIZE,
                                BUTTON_SIZE
                        )
                ) {
                    ClientTeamHudData.setHudMode(
                            ClientTeamHudData.HudMode.MANUAL
                    );

                    ClientTeamHudData.previousPage();

                    event.setCanceled(true);
                    return;
                }

                if (
                isInside(
                        mouseX,
                        mouseY,
                        chatButtonX,
                        chatButtonY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            FloodNetwork.CHANNEL.sendToServer(
                    new TeamNetworkingPackets.ToggleTeamChatPacket()
            );

            event.setCanceled(true);
            return;
        }

        /*
         * Next page.
         */
        if (
                isInside(
                        mouseX,
                        mouseY,
                        nextX,
                        controlY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            ClientTeamHudData.setHudMode(
                    ClientTeamHudData.HudMode.MANUAL
            );

            ClientTeamHudData.nextPage();

            event.setCanceled(true);
            return;
        }

        /*
         * Nearest-player filter.
         */
        if (
                isInside(
                        mouseX,
                        mouseY,
                        filterX,
                        controlY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            ClientTeamHudData.setHudMode(
                    ClientTeamHudData.getHudMode()
                            == ClientTeamHudData.HudMode.NEAREST
                            ? ClientTeamHudData.HudMode.MANUAL
                            : ClientTeamHudData.HudMode.NEAREST
            );

            event.setCanceled(true);
            return;
        }

        /*
         * Favorite-mode filter.
         */
        if (
                isInside(
                        mouseX,
                        mouseY,
                        favoriteX,
                        controlY,
                        BUTTON_SIZE,
                        BUTTON_SIZE
                )
        ) {
            ClientTeamHudData.setHudMode(
                    ClientTeamHudData.getHudMode()
                            == ClientTeamHudData.HudMode.FAVORITES
                            ? ClientTeamHudData.HudMode.MANUAL
                            : ClientTeamHudData.HudMode.FAVORITES
            );

            event.setCanceled(true);
            return;
        }

        /*
         * Clicking a teammate card favorites/unfavorites
         * that specific teammate.
         */
        for (int i = 0;
             i < visible.size();
             i++) {

            int cardY =
                    hudY
                            + i
                            * (
                            TeamHudOverlay.CARD_HEIGHT
                                    + TeamHudOverlay.CARD_SPACING
                    );

            if (
                    !isInside(
                            mouseX,
                            mouseY,
                            hudX,
                            cardY,
                            TeamHudOverlay.CARD_WIDTH,
                            TeamHudOverlay.CARD_HEIGHT
                    )
            ) {
                continue;
            }

            ClientTeamHudData.Teammate teammate =
                    visible.get(i);

            /*
             * If already favorited, always allow removal.
             */
            if (
                    ClientTeamHudData.isFavorite(
                            teammate.playerId()
                    )
            ) {
                ClientTeamHudData.toggleFavorite(
                        teammate.playerId()
                );

                event.setCanceled(true);
                return;
            }

            /*
             * Otherwise don't allow a fifth favorite.
             */
            if (
                    ClientTeamHudData.getFavoriteCount()
                            >= ClientTeamHudData.MAX_FAVORITES
            ) {
                minecraft.player.displayClientMessage(
                        Component.literal(
                                "You can favorite up to 4 teammates."
                        ),
                        true
                );

                event.setCanceled(true);
                return;
            }

            ClientTeamHudData.toggleFavorite(
                    teammate.playerId()
            );

            event.setCanceled(true);
            return;
        }
    }

    private static void drawButton(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            boolean active
    ) {
        /*
         * Small background so the icon remains visible
         * against bright inventory screens.
         */
        graphics.fill(
                x - 1,
                y - 1,
                x + BUTTON_SIZE + 1,
                y + BUTTON_SIZE + 1,
                active
                        ? 0xCC47556F
                        : 0x99000000
        );

        graphics.blit(
                texture,
                x,
                y,
                0,
                0,
                BUTTON_SIZE,
                BUTTON_SIZE,
                16,
                16
        );
    }

    private static void renderCardTooltip(
            GuiGraphics graphics,
            Minecraft minecraft,
            List<ClientTeamHudData.Teammate> visible,
            int hudX,
            int hudY,
            double mouseX,
            double mouseY
    ) {
        for (int i = 0;
             i < visible.size();
             i++) {

            int cardY =
                    hudY
                            + i
                            * (
                            TeamHudOverlay.CARD_HEIGHT
                                    + TeamHudOverlay.CARD_SPACING
                    );

            if (
                    !isInside(
                            mouseX,
                            mouseY,
                            hudX,
                            cardY,
                            TeamHudOverlay.CARD_WIDTH,
                            TeamHudOverlay.CARD_HEIGHT
                    )
            ) {
                continue;
            }

            ClientTeamHudData.Teammate teammate =
                    visible.get(i);

            Component tooltip =
                    Component.literal(
                            ClientTeamHudData.isFavorite(
                                    teammate.playerId()
                            )
                                    ? "Click to unfavorite"
                                    : "Click to favorite"
                    );

            graphics.renderTooltip(
                    minecraft.font,
                    tooltip,
                    (int) mouseX,
                    (int) mouseY
            );

            return;
        }
    }

    private static boolean isPlayerInventoryScreen(
            Screen screen
    ) {
        return screen instanceof InventoryScreen
                || screen instanceof CreativeModeInventoryScreen;
    }

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
}