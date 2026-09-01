package com.jushymaso222.theflood.hud;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.jushymaso222.theflood.progression.HeatTier;
import com.jushymaso222.theflood.progression.client.ClientHeatData;
import com.jushymaso222.theflood.team.client.ClientTeamData;
import com.jushymaso222.theflood.team.client.ClientTeamHudData;
import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;
import com.jushymaso222.theflood.team.client.ClientInviteData;
import com.jushymaso222.theflood.milestone.network.RequestMilestoneSnapshotPacket;
import com.jushymaso222.theflood.progression.milestone.client.FloodMilestoneTabPanel;
import com.jushymaso222.theflood.team.client.ClientTeamChatData;
import com.jushymaso222.theflood.team.client.FloodCreateTeamScreen;
import com.jushymaso222.theflood.progression.capability.network.RequestCapabilitySnapshotPacket;
import com.jushymaso222.theflood.progression.milestone.client.FloodMilestoneScreen;

import net.minecraftforge.event.TickEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.jushymaso222.theflood.progression.client.FloodProgressOverlay;

public final class FloodTabOverlay {

    private FloodTabOverlay() {
    }

    private static boolean interactionMode = false;
    private static boolean altWasDown = false;
    private static boolean tabWasDown = false;

    private static int playerListX = 0;
    private static int playerListY = 0;
    private static int playerListWidth = 0;

    private static int mainPanelX = 0;
    private static int mainPanelY = 0;
    private static int mainPanelHeight = 0;

    private static int playerPreviousX = 0;
    private static int playerNextX = 0;
    private static int playerControlY = 0;

    private static int teamPreviousX = 0;
    private static int teamNextX = 0;
    private static int teamNearestX = 0;
    private static int teamFavoritesX = 0;
    private static int teamControlY = 0;

    private static int teamListX = 0;
    private static int teamListY = 0;
    private static int teamListWidth = 0;

    private static int teamChatX = 0;
    private static int teamChatY = 0;
    private static int teamChatWidth = 0;

    private static final int TAB_CONTROL_SIZE = 12;
    private static final int TAB_CONTROL_GAP = 3;

    private static final int PLAYER_ROW_HEIGHT = 12;
    private static final int PLAYER_MAX_ROWS = 7;
    private static int playerPage = 0;

    private static int teamLeaveX = 0;
    private static int teamLeaveY = 0;
    private static int teamLeaveWidth = 0;

    private static int teamDisbandX = 0;
    private static int teamDisbandY = 0;
    private static int teamDisbandWidth = 0;

    private static final int TEAM_ACTION_HEIGHT = 12;
    private static final int TEAM_ACTION_GAP = 5;
    private static boolean disbandArmed = false;

    private static int teamCreateX = 0;
    private static int teamCreateY = 0;
    private static int teamCreateWidth = 0;

    private static final int TAB_REFERENCE_WIDTH =
            854;

    private static final int TAB_REFERENCE_HEIGHT =
            480;

    private static float tabRenderScale =
            1.0F;

    private static float tabRenderOffsetX =
            0.0F;

    private static float tabRenderOffsetY =
            0.0F;

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

    public static boolean isInteractionMode() {
        return interactionMode;
    }

    private record TabPlayer(
            UUID id,
            String name,
            int latency,
            boolean dummy
    ) {
    }

    private static int getPlayerPageCount(
        int playerCount
) {
    if (playerCount <= 0) {
        return 1;
    }

    return Math.max(
            1,
            (playerCount
                    + PLAYER_MAX_ROWS
                    - 1)
                    / PLAYER_MAX_ROWS
    );
}

private static void nextPlayerPage(
        int playerCount
) {
    int pageCount =
            getPlayerPageCount(
                    playerCount
            );

    playerPage =
            (playerPage + 1)
                    % pageCount;
}

private static void renderTabControl(
        GuiGraphics graphics,
        ResourceLocation texture,
        int x,
        int y,
        boolean active,
        boolean hovered
) {
    if (hovered) {
        graphics.fill(
                x - 1,
                y - 1,
                x + TAB_CONTROL_SIZE + 1,
                y + TAB_CONTROL_SIZE + 1,
                0x44FFFFFF
        );
    }

    graphics.setColor(
            active ? 1.0F : 0.55F,
            active ? 1.0F : 0.55F,
            active ? 1.0F : 0.55F,
            1.0F
    );

    graphics.blit(
            texture,
            x,
            y,
            0,
            0,
            TAB_CONTROL_SIZE,
            TAB_CONTROL_SIZE,
            TAB_CONTROL_SIZE,
            TAB_CONTROL_SIZE
    );

    graphics.setColor(
            1.0F,
            1.0F,
            1.0F,
            1.0F
    );
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

private static void previousPlayerPage(
        int playerCount
) {
    int pageCount =
            getPlayerPageCount(
                    playerCount
            );

    playerPage =
            Math.floorMod(
                    playerPage - 1,
                    pageCount
            );
}


    /*
     * =================================================
     * CUSTOM FLOOD TAB OVERLAY
     * =================================================
     */

    @Mod.EventBusSubscriber(
            modid = TheFlood.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static final class OverlayRegistration {

        @SubscribeEvent
        public static void registerOverlay(
                RegisterGuiOverlaysEvent event
        ) {
            event.registerAboveAll(
                    "flood_tab",
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
    }

    private static void updateTabInteraction(
        Minecraft minecraft,
        boolean tabDown
) {
    /*
     * Tab was just opened.
     *
     * Refresh the server-side player/candidate data now so
     * FakePlayers can appear in the player list even while
     * the overlay is still passive.
     */
    if (
            tabDown
            && !tabWasDown
            && minecraft.player != null
            && minecraft.getConnection() != null
    ) {
        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets
                        .RequestInviteCandidatesPacket()
        );

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets
                        .RequestPendingTeamInvitesPacket()
        );

        FloodNetwork.CHANNEL.sendToServer(
                new RequestMilestoneSnapshotPacket()
        );

        FloodNetwork.CHANNEL.sendToServer(
                new RequestCapabilitySnapshotPacket()
        );
    }
        if (
                !tabDown
                        && tabWasDown
        ) {
        FloodTabInvitePanel.resetConfirmation();

        if (
                minecraft.screen
                        instanceof FloodMilestoneScreen
        ) {
                minecraft.setScreen(
                        null
                );
        }
        }

    tabWasDown = tabDown;

    if (!tabDown) {
        interactionMode = false;
        altWasDown = false;

        if (
                minecraft.screen == null
                && !minecraft.mouseHandler.isMouseGrabbed()
        ) {
            minecraft.mouseHandler.grabMouse();
        }

        return;
    }

    long window =
            minecraft.getWindow()
                    .getWindow();

    boolean altDown =
            InputConstants.isKeyDown(
                    window,
                    GLFW.GLFW_KEY_LEFT_ALT
            );

    if (altDown && !altWasDown) {
        interactionMode =
                !interactionMode;

        if (interactionMode) {
                minecraft.mouseHandler.releaseMouse();
        } else if (minecraft.screen == null) {
                minecraft.mouseHandler.grabMouse();
        }
        }

        altWasDown = altDown;


        /*
        * Keep the cursor released for as long as
        * the interactive Tab overlay owns the mouse.
        *
        * Closing a normal Screen can cause Minecraft
        * to automatically grab the mouse again.
        */
        if (
                interactionMode
                        && minecraft.screen == null
                        && minecraft.mouseHandler.isMouseGrabbed()
        ) {
        minecraft.mouseHandler.releaseMouse();
        }
}


    /*
     * =================================================
     * VANILLA PLAYER LIST SUPPRESSION
     * =================================================
     */

    @Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public static final class ForgeEvents {

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        long window =
                minecraft.getWindow()
                        .getWindow();

        boolean tabDown =
                minecraft.player != null
                        && minecraft.level != null
                        && InputConstants.isKeyDown(
                                window,
                                GLFW.GLFW_KEY_TAB
                        );

        updateTabInteraction(
                minecraft,
                tabDown
        );
    }

    @SubscribeEvent
    public static void onMouseButton(
            net.minecraftforge.client.event.InputEvent.MouseButton.Pre event
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                !FloodTabOverlay.isTabOpen()
                        || !FloodTabOverlay.isInteractionMode()
                        || minecraft.screen != null
        ) {
        return;
        }

        /*
        * Prevent vanilla from handling ANY mouse
        * buttons while the Tab cursor is active.
        *
        * This prevents:
        * - mouse being re-grabbed
        * - attacking
        * - using items
        * - other gameplay mouse bindings
        */
        event.setCanceled(true);

        /*
        * Eventually our Tab UI clicks will be
        * handled here.
        *
        * For now, only recognize a left-click
        * press and swallow everything else.
        */
        if (
                event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT
                        || event.getAction() != GLFW.GLFW_PRESS
        ) {
            return;
        }

        double mouseX =
                getTabMouseX(
                        minecraft
                );

        double mouseY =
                getTabMouseY(
                        minecraft
                );

        /*
        * We'll enable this when we add
        * the actual clickable Tab controls.
        */
        handleTabClick(
                minecraft,
                mouseX,
                mouseY
        );
    }


    @SubscribeEvent
    public static void onRenderOverlay(
            RenderGuiOverlayEvent.Pre event
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.player == null
                        || minecraft.level == null
        ) {
            return;
        }

        /*
         * Suppress vanilla player list.
         */
        if (
                event.getOverlay()
                        .id()
                        .equals(
                                VanillaGuiOverlay
                                        .PLAYER_LIST
                                        .id()
                        )
        ) {
            event.setCanceled(true);
            return;
        }

        /*
         * Hide vanilla crosshair while
         * The Flood Tab overlay is open.
         */
        if (
                FloodTabOverlay.isTabOpen()
                        && event.getOverlay()
                        .id()
                        .equals(
                                VanillaGuiOverlay
                                        .CROSSHAIR
                                        .id()
                        )
        ) {
            event.setCanceled(true);
        }
    }
}

    private static void handleTabClick(
        Minecraft minecraft,
        double mouseX,
        double mouseY
) {
    if (
            FloodMilestoneTabPanel.handleClick(
                    minecraft,
                    mouseX,
                    mouseY,
                    mainPanelX,
                    mainPanelY,
                    mainPanelHeight
            )
    ) {
        return;
    }

    if (
            minecraft.player == null
                    || minecraft.getConnection() == null
    ) {
        return;
    }

    /*
        * =================================================
        * TEAM INVITES
        * =================================================
        */

        if (
                FloodTabInvitePanel.handleClick(
                        mouseX,
                        mouseY
                )
        ) {
        return;
        }


    /*
    * =================================================
    * CREATE TEAM
    * =================================================
    */

    if (
            !ClientTeamData.isInTeam()
                    && teamCreateWidth > 0
                    && isInside(
                            mouseX,
                            mouseY,
                            teamCreateX,
                            teamCreateY,
                            teamCreateWidth,
                            TEAM_ACTION_HEIGHT
                    )
    ) {

        interactionMode =
                false;

        disbandArmed =
                false;

        minecraft.setScreen(
                new FloodCreateTeamScreen(
                        null
                )
        );

        return;
    }


    /*
    * All remaining controls require a team.
    */

    if (!ClientTeamData.isInTeam()) {
        return;
    }

    List<TabPlayer> allPlayers =
            getDisplayedPlayers(
                    minecraft
            );

    int playerPageCount =
            getPlayerPageCount(
                    allPlayers.size()
            );

    List<ClientTeamHudData.Teammate> visibleTeammates =
        ClientTeamHudData.getVisibleTeammates(
                minecraft
        );

    /*
    * =================================================
    * TEAM ACTIONS
    * =================================================
    */

   /*
    * Chat mode.
    */
    if (
            teamChatWidth > 0
                    && isInside(
                            mouseX,
                            mouseY,
                            teamChatX,
                            teamChatY,
                            teamChatWidth,
                            TEAM_ACTION_HEIGHT
                    )
    ) {

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets.ToggleTeamChatPacket()
        );

        return;
    }

    if (
            isInside(
                    mouseX,
                    mouseY,
                    teamLeaveX,
                    teamLeaveY,
                    teamLeaveWidth,
                    TEAM_ACTION_HEIGHT
            )
    ) {
        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets.LeaveTeamPacket()
        );

        return;
    }

    if (
            ClientTeamData.isOwner()
            && teamDisbandWidth > 0
            && isInside(
                    mouseX,
                    mouseY,
                    teamDisbandX,
                    teamDisbandY,
                    teamDisbandWidth,
                    TEAM_ACTION_HEIGHT
            )
    ) {
        if (!disbandArmed) {
            disbandArmed =
                    true;

            return;
        }

        disbandArmed =
                false;

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets.DisbandTeamPacket()
        );

        return;
    }

    /*
    * Clicking anywhere else cancels confirmation.
    */
    disbandArmed =
            false;

    for (
            int i = 0;
            i < visibleTeammates.size();
            i++
    ) {
        int rowY =
                teamListY
                        + i * PLAYER_ROW_HEIGHT;

        boolean inside =
                mouseX >= teamListX
                        && mouseX < teamListX
                        + teamListWidth
                        && mouseY >= rowY
                        && mouseY < rowY
                        + PLAYER_ROW_HEIGHT;

        if (!inside) {
            continue;
        }

        ClientTeamHudData.Teammate teammate =
                visibleTeammates.get(i);

        if (
                ClientTeamHudData.isFavorite(
                        teammate.playerId()
                )
        ) {
            ClientTeamHudData.toggleFavorite(
                    teammate.playerId()
            );

            return;
        }

        if (
                ClientTeamHudData.getFavoriteCount()
                        >= ClientTeamHudData.MAX_FAVORITES
        ) {
            minecraft.player.displayClientMessage(
                    Component.literal(
                            "You can favorite up to 4 teammates."
                    ),
                    false
            );

            return;
        }

        ClientTeamHudData.toggleFavorite(
                teammate.playerId()
        );

        return;
    }

    /*
    * PLAYER PAGE CONTROLS
    */
    if (playerPageCount > 1) {

        if (
                isInside(
                        mouseX,
                        mouseY,
                        playerPreviousX,
                        playerControlY,
                        TAB_CONTROL_SIZE,
                        TAB_CONTROL_SIZE
                )
        ) {
            previousPlayerPage(
                    allPlayers.size()
            );

            return;
        }

        if (
                isInside(
                        mouseX,
                        mouseY,
                        playerNextX,
                        playerControlY,
                        TAB_CONTROL_SIZE,
                        TAB_CONTROL_SIZE
                )
        ) {
            nextPlayerPage(
                    allPlayers.size()
            );

            return;
        }
    }


    /*
    * TEAM PAGE CONTROLS
    */
    if (
            ClientTeamHudData.getPageCount() > 1
    ) {
        if (
                isInside(
                        mouseX,
                        mouseY,
                        teamPreviousX,
                        teamControlY,
                        TAB_CONTROL_SIZE,
                        TAB_CONTROL_SIZE
                )
        ) {
            ClientTeamHudData.previousPage();
            return;
        }

        if (
                isInside(
                        mouseX,
                        mouseY,
                        teamNextX,
                        teamControlY,
                        TAB_CONTROL_SIZE,
                        TAB_CONTROL_SIZE
                )
        ) {
            ClientTeamHudData.nextPage();
            return;
        }
    }


    /*
    * NEAREST MODE
    */
    if (
            isInside(
                    mouseX,
                    mouseY,
                    teamNearestX,
                    teamControlY,
                    TAB_CONTROL_SIZE,
                    TAB_CONTROL_SIZE
            )
    ) {
        ClientTeamHudData.setHudMode(
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.NEAREST
                        ? ClientTeamHudData.HudMode.MANUAL
                        : ClientTeamHudData.HudMode.NEAREST
        );

        return;
    }


    /*
    * FAVORITES MODE
    */
    if (
            isInside(
                    mouseX,
                    mouseY,
                    teamFavoritesX,
                    teamControlY,
                    TAB_CONTROL_SIZE,
                    TAB_CONTROL_SIZE
            )
    ) {
        ClientTeamHudData.setHudMode(
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.FAVORITES
                        ? ClientTeamHudData.HudMode.MANUAL
                        : ClientTeamHudData.HudMode.FAVORITES
        );

        return;
    }

    int pageCount =
            getPlayerPageCount(
                    allPlayers.size()
            );

    if (playerPage >= pageCount) {
        playerPage =
                Math.max(
                        0,
                        pageCount - 1
                );
    }

    int start =
            playerPage
                    * PLAYER_MAX_ROWS;

    int end =
            Math.min(
                    start + PLAYER_MAX_ROWS,
                    allPlayers.size()
            );

    List<TabPlayer> players =
            allPlayers.subList(
                    start,
                    end
            );

    int shown =
            players.size();

    for (
            int i = 0;
            i < shown;
            i++
    ) {
        int rowY =
                playerListY
                        + i * PLAYER_ROW_HEIGHT;

        boolean inside =
                mouseX >= playerListX
                        && mouseX < playerListX
                        + playerListWidth
                        && mouseY >= rowY
                        && mouseY < rowY
                        + PLAYER_ROW_HEIGHT;

        if (!inside) {
            continue;
        }

        TabPlayer playerInfo =
                players.get(i);

        UUID playerId =
                playerInfo.id();

        if (
                !canInvitePlayer(
                        minecraft,
                        playerId
                )
        ) {
            return;
        }

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets
                        .InvitePlayerPacket(
                                playerId
                        )
        );
        return;
    }
}

    private static int renderPlayerTeamSection(
        GuiGraphics graphics,
        Minecraft minecraft,
        int x,
        int y,
        int width
) {
    int heat =
            ClientHeatData.getEffectiveHeat();

    int accentColor =
            HeatTier.getColor(
                    heat
            );

    int centerX =
            x + width / 2;

    int gap =
            18;

    int columnWidth =
            (width - gap) / 2;

    int playerX =
            x;

    int teamX =
            centerX + gap / 2;


    /*
     * =================================================
     * TOP SEPARATOR
     * =================================================
     */

    graphics.fill(
            x,
            y,
            x + width,
            y + 1,
            0x66555555
    );

    int contentY =
            y + 12;


    /*
     * =================================================
     * HEADERS
     * =================================================
     */

    graphics.drawString(
            minecraft.font,
            "PLAYERS",
            playerX,
            contentY,
            0xFFFFFFFF,
            true
    );

    graphics.fill(
            playerX,
            contentY + 12,
            playerX + 34,
            contentY + 13,
            accentColor
    );


    graphics.drawString(
            minecraft.font,
            "TEAM",
            teamX,
            contentY,
            0xFFFFFFFF,
            true
    );

    graphics.fill(
            teamX,
            contentY + 12,
            teamX + 22,
            contentY + 13,
            accentColor
    );


    /*
     * Vertical divider.
     */
    graphics.fill(
            centerX,
            contentY,
            centerX + 1,
            contentY + 90,
            0x33555555
    );


    /*
     * =================================================
     * PLAYERS
     * =================================================
     */

    playerListX = playerX;
    playerListY = contentY + 22;
    playerListWidth = columnWidth;

    renderPlayerList(
            graphics,
            minecraft,
            playerListX,
            playerListY,
            playerListWidth
    );

    /*
     * =================================================
     * TEAM
     * =================================================
     */

    teamListX =
            teamX;

    teamListY =
            contentY + 22;

    teamListWidth =
            columnWidth;

    renderTeamList(
            graphics,
            minecraft,
            teamListX,
            teamListY,
            teamListWidth
    );


    /*
     * Reserved height for now.
     *
     * Later we can calculate this dynamically once
     * team controls live here too.
     */
    return contentY + 96;
}

private static List<TabPlayer> getDisplayedPlayers(
        Minecraft minecraft
) {
    List<TabPlayer> players =
            new ArrayList<>();

    /*
     * Real connected players.
     */
    if (minecraft.getConnection() != null) {

        for (
                PlayerInfo playerInfo :
                minecraft.getConnection()
                        .getOnlinePlayers()
        ) {
            players.add(
                    new TabPlayer(
                            playerInfo
                                    .getProfile()
                                    .getId(),
                            playerInfo
                                    .getProfile()
                                    .getName(),
                            playerInfo.getLatency(),
                            false
                    )
            );
        }
    }

    /*
     * Invite candidates can contain our debug
     * FakePlayers, which are not represented by
     * vanilla PlayerInfo objects.
     */
    for (
            ClientInviteData.Candidate candidate :
            ClientInviteData.getCandidates()
    ) {
        boolean alreadyPresent =
                players.stream()
                        .anyMatch(
                                player ->
                                        player.id()
                                                .equals(
                                                        candidate.id()
                                                )
                        );

        if (alreadyPresent) {
            continue;
        }

        players.add(
                new TabPlayer(
                        candidate.id(),
                        candidate.name(),
                        -1,
                        true
                )
        );
    }

    players.sort(
            Comparator.comparing(
                    TabPlayer::name,
                    String.CASE_INSENSITIVE_ORDER
            )
    );

    return players;
}

private static void renderPlayerList(
        GuiGraphics graphics,
        Minecraft minecraft,
        int x,
        int y,
        int width
) {
    if (minecraft.getConnection() == null) {
        return;
    }

    List<TabPlayer> players =
            getDisplayedPlayers(
                    minecraft
            );

    int pageCount =
            getPlayerPageCount(
                    players.size()
            );

    if (playerPage >= pageCount) {
        playerPage =
                Math.max(
                        0,
                        pageCount - 1
                );
    }

    int start =
            playerPage
                    * PLAYER_MAX_ROWS;

    int end =
            Math.min(
                    start + PLAYER_MAX_ROWS,
                    players.size()
            );

    List<TabPlayer> visiblePlayers =
            players.subList(
                    start,
                    end
            );

    double mouseX =
            getTabMouseX(
                    minecraft
            );

    double mouseY =
            getTabMouseY(
                    minecraft
            );

    int rowHeight =
            PLAYER_ROW_HEIGHT;

    int shown =
            visiblePlayers.size();

    for (
            int i = 0;
            i < shown;
            i++
    ) {
        TabPlayer playerInfo =
                visiblePlayers.get(i);

        int rowY =
                y + i * rowHeight;

        UUID playerId =
            playerInfo.id();

        boolean clickable =
                interactionMode
                        && canInvitePlayer(
                                minecraft,
                                playerId
                        );

        boolean hovered =
                clickable
                        && mouseX >= x
                        && mouseX < x + width
                        && mouseY >= rowY
                        && mouseY < rowY + rowHeight;

        if (hovered) {
            graphics.fill(
                    x,
                    rowY,
                    x + width,
                    rowY + rowHeight,
                    0x44FFFFFF
            );
        }

        String name =
                playerInfo.name();

        int latency =
                playerInfo.latency();

        String latencyText =
                playerInfo.dummy()
                        ? "DUMMY"
                        : latency + "ms";

        /*
         * Local player gets a subtle marker.
         */
        boolean localPlayer =
            minecraft.player != null
                    && minecraft.player
                            .getUUID()
                            .equals(
                                    playerInfo.id()
                            );

        int nameColor =
                localPlayer
                        ? 0xFFFFFFFF
                        : 0xFFDDDDDD;

        if (localPlayer) {
            graphics.fill(
                    x,
                    rowY + 4,
                    x + 2,
                    rowY + 8,
                    HeatTier.getColor(
                            ClientHeatData
                                    .getEffectiveHeat()
                    )
            );
        }

        graphics.drawString(
                minecraft.font,
                name,
                x + 6,
                rowY,
                nameColor,
                true
        );

        int latencyWidth =
                minecraft.font.width(
                        latencyText
                );

        int rightTextColor =
            playerInfo.dummy()
                    ? 0xFF888888
                    : getLatencyColor(latency);

        if (!hovered) {
            graphics.drawString(
                    minecraft.font,
                    latencyText,
                    x + width - latencyWidth - 4,
                    rowY,
                    rightTextColor,
                    true
            );
        }

        if (hovered) {
            String inviteText =
                    "INVITE";

            graphics.drawString(
                    minecraft.font,
                    inviteText,
                    x
                            + width
                            - minecraft.font.width(inviteText)
                            - 4,
                    rowY,
                    0xFFFFFFFF,
                    true
            );
        }
    }

    if (pageCount <= 1) {
        return;
    }

    playerControlY =
            y
                    + visiblePlayers.size()
                    * PLAYER_ROW_HEIGHT
                    + 4;

    int totalWidth =
            TAB_CONTROL_SIZE * 2
                    + TAB_CONTROL_GAP;

    int controlX =
            x
                    + width / 2
                    - totalWidth / 2;

    playerPreviousX =
            controlX;

    playerNextX =
            playerPreviousX
                    + TAB_CONTROL_SIZE
                    + TAB_CONTROL_GAP;

    boolean previousHovered =
            interactionMode
                    && isInside(
                            mouseX,
                            mouseY,
                            playerPreviousX,
                            playerControlY,
                            TAB_CONTROL_SIZE,
                            TAB_CONTROL_SIZE
                    );

    boolean nextHovered =
            interactionMode
                    && isInside(
                            mouseX,
                            mouseY,
                            playerNextX,
                            playerControlY,
                            TAB_CONTROL_SIZE,
                            TAB_CONTROL_SIZE
                    );

    renderTabControl(
            graphics,
            PREVIOUS_PAGE,
            playerPreviousX,
            playerControlY,
            true,
            previousHovered
    );

    renderTabControl(
            graphics,
            NEXT_PAGE,
            playerNextX,
            playerControlY,
            true,
            nextHovered
    );

    String pageText =
            (playerPage + 1)
                    + "/"
                    + pageCount;

    graphics.drawCenteredString(
            minecraft.font,
            pageText,
            x + width / 2,
            playerControlY + TAB_CONTROL_SIZE + 1,
            0xFF888888
    );
}

private static boolean canInvitePlayer(
        Minecraft minecraft,
        UUID playerId
) {
    if (
            minecraft.player == null
            || !ClientTeamData.isInTeam()
    ) {
        return false;
    }

    /*
     * Can't invite ourselves.
     */
    if (
            minecraft.player
                    .getUUID()
                    .equals(playerId)
    ) {
        return false;
    }

    /*
     * Can't invite somebody already on our team.
     */
    for (
            ClientTeamHudData.Teammate teammate :
            ClientTeamHudData.getTeammates()
    ) {
        if (
                teammate.playerId()
                        .equals(playerId)
        ) {
            return false;
        }
    }

    return true;
}

private static int getLatencyColor(
        int latency
) {
    if (latency < 0) {
        return 0xFF777777;
    }

    if (latency < 100) {
        return 0xFF66CC66;
    }

    if (latency < 200) {
        return 0xFFFFCC55;
    }

    return 0xFFFF6666;
}

private static void renderTeamList(
        GuiGraphics graphics,
        Minecraft minecraft,
        int x,
        int y,
        int width
) {
    if (!ClientTeamData.isInTeam()) {

        /*
        * Clear team-only clickable bounds so nothing
        * stale remains after leaving/disbanding.
        */
        teamLeaveWidth = 0;
        teamDisbandWidth = 0;
        teamChatWidth = 0;


        graphics.drawString(
                minecraft.font,
                "Not currently in a team",
                x,
                y,
                0xFF888888,
                true
        );


        /*
        * CREATE TEAM
        */

        teamCreateWidth =
                86;

        teamCreateX =
                x
                        + width / 2
                        - teamCreateWidth / 2;

        teamCreateY =
                y + 20;


        double mouseX =
                getTabMouseX(
                        minecraft
                );

        double mouseY =
                getTabMouseY(
                        minecraft
                );


        boolean createHovered =
                interactionMode
                        && isInside(
                                mouseX,
                                mouseY,
                                teamCreateX,
                                teamCreateY,
                                teamCreateWidth,
                                TEAM_ACTION_HEIGHT
                        );


        renderTeamActionButton(
                graphics,
                minecraft,
                "CREATE TEAM",
                teamCreateX,
                teamCreateY,
                teamCreateWidth,
                createHovered,
                HeatTier.getColor(
                        ClientHeatData.getEffectiveHeat()
                )
        );


        return;
    }

    teamCreateWidth = 0;

    var teammates =
            ClientTeamHudData
                    .getVisibleTeammates(
                            minecraft
                    );

    double mouseX =
            getTabMouseX(
                    minecraft
            );

    double mouseY =
            getTabMouseY(
                    minecraft
            );

    if (teammates.isEmpty()) {

        graphics.drawString(
                minecraft.font,
                "No teammates online",
                x,
                y,
                0xFF888888,
                true
        );
    }

    int rowHeight =
            12;

    int maxRows =
            7;

    int shown =
            Math.min(
                    teammates.size(),
                    maxRows
            );

    for (
            int i = 0;
            i < shown;
            i++
    ) {
        ClientTeamHudData.Teammate teammate =
                teammates.get(i);

        int rowY =
                y + i * rowHeight;

        boolean hovered =
                interactionMode
                        && mouseX >= x
                        && mouseX < x + width
                        && mouseY >= rowY
                        && mouseY < rowY + rowHeight;
        
        boolean favorite =
            ClientTeamHudData.isFavorite(
                    teammate.playerId()
            );

        if (hovered) {
            graphics.fill(
                    x,
                    rowY,
                    x + width,
                    rowY + rowHeight,
                    0x44FFFFFF
            );
        }

        String name =
                teammate.name();

        String distance =
                getTabDistanceText(
                        minecraft,
                        teammate
                );


        /*
         * Tiny health indicator.
         */
        float healthPercent =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                teammate.health()
                                        / Math.max(
                                        1.0F,
                                        teammate.maxHealth()
                                )
                        )
                );

        int healthBarWidth =
                22;

        int healthX =
                x;

        int healthY =
                rowY + 3;

        graphics.fill(
                healthX,
                healthY,
                healthX + healthBarWidth,
                healthY + 2,
                0xFF292929
        );

        int filled =
                Math.round(
                        healthBarWidth
                                * healthPercent
                );

        if (filled > 0) {
            graphics.fill(
                    healthX,
                    healthY,
                    healthX + filled,
                    healthY + 2,
                    getTabHealthColor(
                            healthPercent
                    )
            );
        }


        /*
         * Name.
         */
        graphics.drawString(
                minecraft.font,
                name,
                x + 28,
                rowY,
                0xFFFFFFFF,
                true
        );

        if (
                ClientTeamHudData.isFavorite(
                        teammate.playerId()
                )
                && !hovered
        ) {
            String favoriteMarker =
                    "★";

            graphics.drawString(
                    minecraft.font,
                    favoriteMarker,
                    x + width
                            - minecraft.font.width(distance)
                            - 14,
                    rowY,
                    0xFFFFDD55,
                    true
            );
        }


        /*
         * Distance.
         */
        String rightText =
                hovered
                        ? (
                                favorite
                                        ? "Unfavorite"
                                        : "Favorite"
                        )
                        : distance;

        int rightTextWidth =
                minecraft.font.width(
                        rightText
                );

        graphics.drawString(
                minecraft.font,
                rightText,
                x + width
                        - rightTextWidth
                        - 4,
                rowY,
                hovered
                        ? 0xFFFFFFFF
                        : 0xFFAAAAAA,
                true
        );
    }

    int renderedTeamRows =
            teammates.isEmpty()
                    ? 2
                    : teammates.size();

    teamControlY =
            y
                    + renderedTeamRows
                    * PLAYER_ROW_HEIGHT
                    + 4;

    int pageCount =
            ClientTeamHudData.getPageCount();

    boolean hasPages =
            pageCount > 1;

    int buttonCount =
            hasPages
                    ? 4
                    : 2;

    int controlTotalWidth =
            buttonCount * TAB_CONTROL_SIZE
                    + (buttonCount - 1)
                    * TAB_CONTROL_GAP;

    int controlX =
            x
                    + width / 2
                    - controlTotalWidth / 2;

    if (hasPages) {
        teamPreviousX =
                controlX;

        teamNextX =
                teamPreviousX
                        + TAB_CONTROL_SIZE
                        + TAB_CONTROL_GAP;

        teamNearestX =
                teamNextX
                        + TAB_CONTROL_SIZE
                        + TAB_CONTROL_GAP;

        teamFavoritesX =
                teamNearestX
                        + TAB_CONTROL_SIZE
                        + TAB_CONTROL_GAP;
    } else {
        teamPreviousX = -1;
        teamNextX = -1;

        teamNearestX =
                controlX;

        teamFavoritesX =
                teamNearestX
                        + TAB_CONTROL_SIZE
                        + TAB_CONTROL_GAP;
    }

    if (hasPages) {
        renderTabControl(
                graphics,
                PREVIOUS_PAGE,
                teamPreviousX,
                teamControlY,
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.MANUAL,
                interactionMode
                        && isInside(
                                mouseX,
                                mouseY,
                                teamPreviousX,
                                teamControlY,
                                TAB_CONTROL_SIZE,
                                TAB_CONTROL_SIZE
                        )
        );

        renderTabControl(
                graphics,
                NEXT_PAGE,
                teamNextX,
                teamControlY,
                ClientTeamHudData.getHudMode()
                        == ClientTeamHudData.HudMode.MANUAL,
                interactionMode
                        && isInside(
                                mouseX,
                                mouseY,
                                teamNextX,
                                teamControlY,
                                TAB_CONTROL_SIZE,
                                TAB_CONTROL_SIZE
                        )
        );
    }

    renderTabControl(
            graphics,
            FILTER,
            teamNearestX,
            teamControlY,
            ClientTeamHudData.getHudMode()
                    == ClientTeamHudData.HudMode.NEAREST,
            interactionMode
                    && isInside(
                            mouseX,
                            mouseY,
                            teamNearestX,
                            teamControlY,
                            TAB_CONTROL_SIZE,
                            TAB_CONTROL_SIZE
                    )
    );

    renderTabControl(
            graphics,
            FAVORITE,
            teamFavoritesX,
            teamControlY,
            ClientTeamHudData.getHudMode()
                    == ClientTeamHudData.HudMode.FAVORITES,
            interactionMode
                    && isInside(
                            mouseX,
                            mouseY,
                            teamFavoritesX,
                            teamControlY,
                            TAB_CONTROL_SIZE,
                            TAB_CONTROL_SIZE
                    )
    );

    if (hasPages) {
        String pageText =
                (ClientTeamHudData.getCurrentPage() + 1)
                        + "/"
                        + pageCount;

        graphics.drawCenteredString(
                minecraft.font,
                pageText,
                x + width / 2,
                teamControlY + TAB_CONTROL_SIZE + 2,
                0xFF888888
        );
    }

    int actionY =
            teamControlY
                    + TAB_CONTROL_SIZE
                    + 14;

    boolean owner =
            ClientTeamData.isOwner();

    int leaveWidth =
            48;

    int disbandWidth =
            52;

    if (owner) {

        int actionTotalWidth =
                leaveWidth
                        + TEAM_ACTION_GAP
                        + disbandWidth;

        int startX =
                x
                        + width / 2
                        - actionTotalWidth / 2;

        teamLeaveX =
                startX;

        teamLeaveY =
                actionY;

        teamLeaveWidth =
                leaveWidth;

        teamDisbandX =
                teamLeaveX
                        + teamLeaveWidth
                        + TEAM_ACTION_GAP;

        teamDisbandY =
                actionY;

        teamDisbandWidth =
                disbandWidth;

    } else {

        teamLeaveWidth =
                leaveWidth;

        teamLeaveX =
                x
                        + width / 2
                        - teamLeaveWidth / 2;

        teamLeaveY =
                actionY;

        /*
        * Owner-only control does not exist for
        * regular members.
        */
        teamDisbandX = -1;
        teamDisbandY = -1;
        teamDisbandWidth = 0;
    }

    boolean leaveHovered =
            interactionMode
                    && isInside(
                            mouseX,
                            mouseY,
                            teamLeaveX,
                            teamLeaveY,
                            teamLeaveWidth,
                            TEAM_ACTION_HEIGHT
                    );

    boolean disbandHovered =
            owner
                    && interactionMode
                    && isInside(
                            mouseX,
                            mouseY,
                            teamDisbandX,
                            teamDisbandY,
                            teamDisbandWidth,
                            TEAM_ACTION_HEIGHT
                    );

    renderTeamActionButton(
            graphics,
            minecraft,
            "LEAVE",
            teamLeaveX,
            teamLeaveY,
            teamLeaveWidth,
            leaveHovered,
            0xFFFFAA55
    );

    String disbandText =
        disbandArmed
                ? "CONFIRM?"
                : "DISBAND";

    if (owner) {
        renderTeamActionButton(
                graphics,
                minecraft,
                disbandText,
                teamDisbandX,
                teamDisbandY,
                teamDisbandWidth,
                disbandHovered,
                0xFFFF5555
        );
    }

    /*
    * =================================================
    * CHAT MODE
    * =================================================
    */

    int chatY =
            actionY
                    + TEAM_ACTION_HEIGHT
                    + 6;

    teamChatWidth =
            105;

    teamChatX =
            x
                    + width / 2
                    - teamChatWidth / 2;

    teamChatY =
            chatY;

    boolean teamChat =
            ClientTeamChatData.isTeamChat();

    boolean chatHovered =
            interactionMode
                    && isInside(
                            mouseX,
                            mouseY,
                            teamChatX,
                            teamChatY,
                            teamChatWidth,
                            TEAM_ACTION_HEIGHT
                    );

    int accentColor =
            HeatTier.getColor(
                    ClientHeatData.getEffectiveHeat()
            );

    String chatText =
            teamChat
                    ? "CHAT: TEAM"
                    : "CHAT: GLOBAL";

    renderTeamActionButton(
            graphics,
            minecraft,
            chatText,
            teamChatX,
            teamChatY,
            teamChatWidth,
            chatHovered,
            teamChat
                    ? accentColor
                    : 0xFFAAAAAA
    );
}

private static String getTabDistanceText(
        Minecraft minecraft,
        ClientTeamHudData.Teammate teammate
) {
    if (minecraft.player == null) {
        return "--";
    }

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
                    - minecraft.player.getX();

    double dy =
            teammate.y()
                    - minecraft.player.getY();

    double dz =
            teammate.z()
                    - minecraft.player.getZ();

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

private static int getTabHealthColor(
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

    public static boolean isTabOpen() {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.player == null
                        || minecraft.level == null
        ) {
                return false;
        }

        long window =
                minecraft.getWindow()
                        .getWindow();

        return InputConstants.isKeyDown(
                window,
                GLFW.GLFW_KEY_TAB
        );
        }


    /*
     * =================================================
     * RENDER
     * =================================================
     */

    private static void renderHeatProgressBar(
        GuiGraphics graphics,
        Minecraft minecraft,
        int centerX,
        int y,
        int width,
        int heat,
        float progress
) {
    int height = 8;

    progress =
            Math.max(
                    0.0F,
                    Math.min(
                            1.0F,
                            progress
                    )
            );

    int x =
            centerX
                    - width / 2;

    /*
     * Background.
     */
    graphics.fill(
            x,
            y,
            x + width,
            y + height,
            0xAA111111
    );

    /*
     * Filled portion.
     */
    int filledWidth =
            Math.round(
                    width
                            * progress
            );

    if (filledWidth > 0) {
        graphics.fill(
                x,
                y,
                x + filledWidth,
                y + height,
                HeatTier.getColor(
                        heat
                )
        );
    }

    /*
     * Border.
     */
    graphics.fill(
            x,
            y,
            x + width,
            y + 1,
            0xFF555555
    );

    graphics.fill(
            x,
            y + height - 1,
            x + width,
            y + height,
            0xFF555555
    );

    graphics.fill(
            x,
            y,
            x + 1,
            y + height,
            0xFF555555
    );

    graphics.fill(
            x + width - 1,
            y,
            x + width,
            y + height,
            0xFF555555
    );

    /*
     * Progress text.
     */
    int percent =
            Math.round(
                    progress
                            * 100.0F
            );

    String progressText;

    if (heat >= 100) {
        progressText =
                "MAXIMUM HEAT";
    }
    else {
        progressText =
                percent
                        + "% TO HEAT "
                        + (heat + 1);
    }

    graphics.drawCenteredString(
            minecraft.font,
            progressText,
            centerX,
            y + height + 4,
            0xFFBBBBBB
    );
}

    private static void render(
        GuiGraphics graphics,
        int screenWidth,
        int screenHeight
) {
    Minecraft minecraft =
            Minecraft.getInstance();

    if (
            minecraft.player == null
            || minecraft.level == null
            || minecraft.options.hideGui
            || !isTabOpen()
    ) {
        return;
    }

    /*
     * =================================================
     * REFERENCE-SPACE SCALING
     * =================================================
     *
     * The entire Tab UI is authored against an
     * 854 x 480 virtual GUI.
     *
     * Everything inside renderTabContents() therefore
     * uses one consistent coordinate system regardless
     * of Minecraft GUI scale or window size.
     */

    float scaleX =
            screenWidth
                    / (float) TAB_REFERENCE_WIDTH;

    float scaleY =
            screenHeight
                    / (float) TAB_REFERENCE_HEIGHT;

    float scale =
            Math.min(
                    scaleX,
                    scaleY
            );

    /*
     * Center the scaled reference canvas inside the
     * actual GUI area.
     */
    float scaledWidth =
            TAB_REFERENCE_WIDTH
                    * scale;

    float scaledHeight =
            TAB_REFERENCE_HEIGHT
                    * scale;

    float offsetX =
            (
                    screenWidth
                            - scaledWidth
            ) / 2.0F;

    float offsetY =
            (
                    screenHeight
                            - scaledHeight
            ) / 2.0F;

    /*
     * Save these for mouse-coordinate conversion.
     */
    tabRenderScale =
            scale;

    tabRenderOffsetX =
            offsetX;

    tabRenderOffsetY =
            offsetY;

    graphics.pose().pushPose();

    graphics.pose().translate(
            offsetX,
            offsetY,
            0.0F
    );

    graphics.pose().scale(
            scale,
            scale,
            1.0F
    );

    /*
     * From this point onward the renderer believes
     * that it always has an 854 x 480 GUI.
     */
    renderTabContents(
            graphics,
            minecraft,
            TAB_REFERENCE_WIDTH,
            TAB_REFERENCE_HEIGHT
    );

    graphics.pose().popPose();
}

    private static void renderTeamActionButton(
        GuiGraphics graphics,
        Minecraft minecraft,
        String text,
        int x,
        int y,
        int width,
        boolean hovered,
        int textColor
) {
    graphics.fill(
            x,
            y,
            x + width,
            y + TEAM_ACTION_HEIGHT,
            hovered
                    ? 0x55FFFFFF
                    : 0x33000000
    );

    graphics.fill(
            x,
            y + TEAM_ACTION_HEIGHT - 1,
            x + width,
            y + TEAM_ACTION_HEIGHT,
            hovered
                    ? textColor
                    : 0xFF555555
    );

    graphics.drawCenteredString(
            minecraft.font,
            text,
            x + width / 2,
            y + 2,
            textColor
    );
}

    private static void renderTabContents(
        GuiGraphics graphics,
        Minecraft minecraft,
        int screenWidth,
        int screenHeight
) {
    /*
     * Because we're now rendering in a fixed virtual
     * canvas, these dimensions stay consistent.
     */
    int panelWidth =
            420;

    int panelHeight =
            370;

    int x =
            screenWidth / 2
                    - panelWidth / 2;

    int y =
        screenHeight / 2
                - panelHeight / 2;

    mainPanelX =
            x;

    mainPanelY =
            y;

    mainPanelHeight =
            panelHeight;


    /*
     * =================================================
     * BACKGROUND
     * =================================================
     */

    graphics.fill(
            x,
            y,
            x + panelWidth,
            y + panelHeight,
            0xB0000000
    );


    /*
     * Border.
     */
    int borderColor =
            0xFFFFFFFF;

    graphics.fill(
            x,
            y,
            x + panelWidth,
            y + 1,
            borderColor
    );

    graphics.fill(
            x,
            y + panelHeight - 1,
            x + panelWidth,
            y + panelHeight,
            borderColor
    );

    graphics.fill(
            x,
            y,
            x + 1,
            y + panelHeight,
            borderColor
    );

    graphics.fill(
            x + panelWidth - 1,
            y,
            x + panelWidth,
            y + panelHeight,
            borderColor
    );

    FloodMilestoneTabPanel.render(
            graphics,
            minecraft,
            x,
            y,
            panelHeight,
            getTabMouseX(
                    minecraft
            ),
            getTabMouseY(
                    minecraft
            ),
            isInteractionMode()
    );

    /*
        * =================================================
        * TEAM INVITES
        * =================================================
        */

        int rightPanelGap =
                8;

        int rightPanelX =
                x
                        + panelWidth
                        + rightPanelGap;

        FloodTabInvitePanel.render(
                graphics,
                minecraft,
                rightPanelX,
                y,
                getTabMouseX(
                        minecraft
                ),
                getTabMouseY(
                        minecraft
                ),
                isInteractionMode()
        );

        /*
        * =================================================
        * CAPABILITY / THREAT ASSESSMENT
        * =================================================
        */

        int inviteSectionHeight =
                105;

        int capabilityGap =
                6;

        int capabilityY =
                y
                        + inviteSectionHeight
                        + capabilityGap;

        int capabilityHeight =
                panelHeight
                        - inviteSectionHeight
                        - capabilityGap;

        FloodTabCapabilityPanel.render(
                graphics,
                minecraft,
                x + panelWidth + 8,
                capabilityY,
                capabilityHeight
        );



    /*
     * =================================================
     * HEAT HEADER
     * =================================================
     */

    int heat =
            ClientHeatData.getEffectiveHeat();

    String tierName =
            HeatTier.getName(
                    heat
            );

    int tierColor =
            HeatTier.getColor(
                    heat
            );

    float heatProgress =
            ClientHeatData.getFloodXpProgress();

    graphics.drawCenteredString(
            minecraft.font,
            "THE FLOOD",
            screenWidth / 2,
            y + 12,
            0xFFFFFFFF
    );

    graphics.drawCenteredString(
            minecraft.font,
            "HEAT " + heat,
            screenWidth / 2,
            y + 28,
            0xFFFFFFFF
    );

    graphics.drawCenteredString(
            minecraft.font,
            tierName,
            screenWidth / 2,
            y + 40,
            tierColor
    );


    /*
     * =================================================
     * HEAT XP BAR
     * =================================================
     */

    renderHeatProgressBar(
            graphics,
            minecraft,
            screenWidth / 2,
            y + 56,
            260,
            heat,
            heatProgress
    );


    /*
     * =================================================
     * FLOOD PROGRESSION
     * =================================================
     */

    int progressionY =
            y + 92;

    int nextY =
            FloodProgressOverlay.renderSection(
                    graphics,
                    minecraft,
                    heat,
                    x + 16,
                    progressionY,
                    panelWidth - 32
            );


    /*
     * =================================================
     * PLAYERS / TEAM
     * =================================================
     */

    renderPlayerTeamSection(
            graphics,
            minecraft,
            x + 16,
            nextY + 4,
            panelWidth - 32
    );


    /*
     * =================================================
     * INTERACTION HINT
     * =================================================
     */

    String interactionText =
            isInteractionMode()
                    ? "LALT - RETURN TO GAME"
                    : "LALT - ENABLE CURSOR";

    int interactionColor =
            isInteractionMode()
                    ? tierColor
                    : 0xFF888888;

    graphics.drawCenteredString(
            minecraft.font,
            interactionText,
            x + panelWidth / 2,
            y + panelHeight - 14,
            interactionColor
    );
}

private static double getTabMouseX(
        Minecraft minecraft
) {
    double mouseX =
            minecraft.mouseHandler.xpos()
                    * minecraft.getWindow()
                            .getGuiScaledWidth()
                    / minecraft.getWindow()
                            .getScreenWidth();

    return (
            mouseX
                    - tabRenderOffsetX
    ) / tabRenderScale;
}

private static double getTabMouseY(
        Minecraft minecraft
) {
    double mouseY =
            minecraft.mouseHandler.ypos()
                    * minecraft.getWindow()
                            .getGuiScaledHeight()
                    / minecraft.getWindow()
                            .getScreenHeight();

    return (
            mouseY
                    - tabRenderOffsetY
    ) / tabRenderScale;
}
}