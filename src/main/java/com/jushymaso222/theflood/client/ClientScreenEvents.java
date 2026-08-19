package com.jushymaso222.theflood.client;

import com.jushymaso222.theflood.hud.FloodSettingsIconButton;
import com.jushymaso222.theflood.hud.FloodHudSettingsScreen;
import com.jushymaso222.theflood.team.client.FloodTeamScreen;
import com.jushymaso222.theflood.team.client.TeamTabButton;

import com.jushymaso222.theflood.TheFlood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.components.AbstractWidget;

import com.jushymaso222.theflood.guide.client.FloodGuideScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ClientScreenEvents {

    private static final ResourceLocation TEAM_ICON =
        new ResourceLocation(
                TheFlood.MOD_ID,
                "textures/gui/team/team_button.png"
    );

    private static final ResourceLocation WIKI_ICON =
        new ResourceLocation(
                TheFlood.MOD_ID,
                "textures/gui/team/wiki.png"
        );

    private ClientScreenEvents() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            addTeamButton(event, inventoryScreen);
            addWikiButton(event, inventoryScreen);
        }

        if (event.getScreen() instanceof CreativeModeInventoryScreen creativeScreen) {
            addCreativeTeamButton(event, creativeScreen);
            addCreativeWikiButton(event, creativeScreen);
        }

        if (event.getScreen() instanceof PauseScreen pauseScreen) {
                addFloodSettingsButton(event, pauseScreen);
        }
    }

    private static void addTeamButton(
        ScreenEvent.Init.Post event,
        InventoryScreen screen
) {
    int buttonSize =
            16;

    int tabWidth =
        22;

        int guiLeft =
                (screen.width - 176) / 2;

        int guiTop =
                (screen.height - 166) / 2;

        int x =
                guiLeft
                        - tabWidth
                        + 2;

        int y =
                guiTop+2;

     TeamTabButton teamButton =
        new TeamTabButton(
                x,
                y,
                22,
                22,
                Component.literal("Teams"),
                TEAM_ICON,
                button ->
                        Minecraft.getInstance()
                                .setScreen(
                                        new FloodTeamScreen(
                                                screen
                                        )
                                )
        );

    event.addListener(
            teamButton
    );
}

    private static void addCreativeTeamButton(
        ScreenEvent.Init.Post event,
        CreativeModeInventoryScreen screen
) {
    int buttonSize =
            16;

    /*
     * Creative inventory is wider than survival.
     */
    int guiWidth =
            195;

    int guiHeight =
            135;

    int tabWidth =
        22;

        int guiLeft =
                (screen.width - 176) / 2;

        int guiTop =
                (screen.height - guiHeight) / 2;

        int x =
                guiLeft
                        - tabWidth
                        - 8;

        int y =
                guiTop+1;

     TeamTabButton teamButton =
        new TeamTabButton(
                x,
                y,
                22,
                22,
                Component.literal("Teams"),
                TEAM_ICON,
                button ->
                        Minecraft.getInstance()
                                .setScreen(
                                        new FloodTeamScreen(
                                                screen
                                        )
                                )
        );

    event.addListener(
            teamButton
    );
}

        @SubscribeEvent
public static void onScreenRender(
        ScreenEvent.Render.Post event
) {
    if (
            !(event.getScreen() instanceof InventoryScreen)
            && !(event.getScreen()
                    instanceof CreativeModeInventoryScreen)
    ) {
        return;
    }

    int tabWidth =
            22;

    int tabHeight =
            22;

    int teamX;
    int teamY;

    if (
            event.getScreen()
                    instanceof InventoryScreen screen
    ) {
        int guiLeft =
                (screen.width - 176) / 2;

        int guiTop =
                (screen.height - 166) / 2;

        /*
         * Match the FINAL survival tab coordinates.
         */
        teamX =
                guiLeft
                        - tabWidth
                        + 2;

        teamY =
                guiTop
                        + 2;

    } else {
        CreativeModeInventoryScreen screen =
                (CreativeModeInventoryScreen)
                        event.getScreen();

        int guiHeight =
                135;

        int guiLeft =
                (screen.width - 176) / 2;

        int guiTop =
                (screen.height - guiHeight) / 2;

        /*
         * Match the FINAL creative tab coordinates.
         */
        teamX =
                guiLeft
                        - tabWidth
                        - 8;

        teamY =
                guiTop
                        + 1;
    }

    /*
     * Wiki is directly underneath Teams.
     */
    int wikiX =
            teamX;

    int wikiY =
            teamY
                    + tabHeight;

    double mouseX =
            event.getMouseX();

    double mouseY =
            event.getMouseY();

    /*
     * Teams tooltip.
     */
    if (
            mouseX >= teamX
            && mouseX < teamX + tabWidth
            && mouseY >= teamY
            && mouseY < teamY + tabHeight
    ) {
        event.getGuiGraphics()
                .renderTooltip(
                        Minecraft.getInstance().font,
                        Component.literal(
                                "Teams"
                        ),
                        (int) mouseX,
                        (int) mouseY
                );

        return;
    }

    /*
     * Wiki tooltip.
     */
    if (
            mouseX >= wikiX
            && mouseX < wikiX + tabWidth
            && mouseY >= wikiY
            && mouseY < wikiY + tabHeight
    ) {
        event.getGuiGraphics()
                .renderTooltip(
                        Minecraft.getInstance().font,
                        Component.literal(
                                "Flood Guide"
                        ),
                        (int) mouseX,
                        (int) mouseY
                );
    }
}

        private static void addWikiButton(
        ScreenEvent.Init.Post event,
        InventoryScreen screen
) {
    int tabWidth = 22;
    int tabHeight = 22;

    /*
     * IMPORTANT:
     * Use the exact guiLeft/guiTop and X positioning
     * values you already finalized for the Teams tab.
     */
    int guiLeft =
            (screen.width - 176) / 2;

    int guiTop =
            (screen.height - 166) / 2;

    int x =
            guiLeft
                    - tabWidth
                    + 2; // use YOUR final Teams value here

    int y =
            guiTop
                    + 2
                    + tabHeight;

    TeamTabButton wikiButton =
            new TeamTabButton(
                    x,
                    y,
                    tabWidth,
                    tabHeight,
                    Component.literal("Flood Guide"),
                    WIKI_ICON,
                    button ->
                            Minecraft.getInstance()
                                    .setScreen(
                                            new FloodGuideScreen(
                                                    screen
                                            )
                                    )
            );

    event.addListener(
            wikiButton
    );
}

        private static void addCreativeWikiButton(
        ScreenEvent.Init.Post event,
        CreativeModeInventoryScreen screen
) {
    int tabWidth =
            22;

    int tabHeight =
            22;

    /*
     * EXACT same calculations as the finalized
     * Creative Teams tab.
     */
    int guiHeight =
            135;

    int guiLeft =
            (screen.width - 176) / 2;

    int guiTop =
            (screen.height - guiHeight) / 2;

    /*
     * EXACT same X as Teams.
     */
    int x =
            guiLeft
                    - tabWidth
                    - 8;

    /*
     * Teams is guiTop + 1.
     * Wiki goes immediately underneath.
     */
    int y =
            guiTop
                    + 1
                    + tabHeight;

    TeamTabButton wikiButton =
            new TeamTabButton(
                    x,
                    y,
                    tabWidth,
                    tabHeight,
                    Component.literal("Flood Guide"),
                    WIKI_ICON,
                    button ->
                            Minecraft.getInstance()
                                    .setScreen(
                                            new FloodGuideScreen(
                                                    screen
                                            )
                                    )
            );

    event.addListener(
            wikiButton
    );
}

    private static void addFloodSettingsButton(
        ScreenEvent.Init.Post event,
        PauseScreen screen
) {
    int buttonSize = 20;

    /*
     * Vanilla pause menu is 204 pixels wide.
     * Put our button just to the left of it.
     */
    int menuLeft = screen.width / 2 - 102;

    int x = menuLeft - buttonSize - 4;
    int y = screen.height / 4 + 72;

    FloodSettingsIconButton settingsButton =
            new FloodSettingsIconButton(
                    x,
                    y,
                    buttonSize,
                    buttonSize,
                    button -> Minecraft.getInstance().setScreen(
                            new FloodHudSettingsScreen(screen)
                    )
            );

    event.addListener(settingsButton);
}
}