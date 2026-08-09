package com.jushymaso222.theflood.client;

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

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ClientScreenEvents {

    private ClientScreenEvents() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            addTeamButton(event, inventoryScreen);
        }

        if (event.getScreen() instanceof CreativeModeInventoryScreen creativeScreen) {
            addCreativeTeamButton(event, creativeScreen);
        }

        if (event.getScreen() instanceof PauseScreen pauseScreen) {
            addFloodSettingsButton(event, pauseScreen);
        }
    }

    private static void addTeamButton(
            ScreenEvent.Init.Post event,
            InventoryScreen screen
    ) {
        int buttonSize = 20;

        int x = screen.width / 2 + 90;
        int y = screen.height / 2 - 82;

        Button teamButton = Button.builder(
                Component.literal("T"),
                button -> Minecraft.getInstance().setScreen(
                        new FloodTeamScreen(screen)
                )
        )
        .bounds(x, y, buttonSize, buttonSize)
        .build();

        event.addListener(teamButton);
    }

    private static void addCreativeTeamButton(
            ScreenEvent.Init.Post event,
            CreativeModeInventoryScreen screen
    ) {
        int buttonSize = 20;

        int x = screen.width / 2 + 100;
        int y = screen.height / 2 - 95;

        Button teamButton = Button.builder(
                Component.literal("T"),
                button -> Minecraft.getInstance().setScreen(
                        new FloodTeamScreen(screen)
                )
        )
        .bounds(x, y, buttonSize, buttonSize)
        .build();

        event.addListener(teamButton);
    }

    private static void addFloodSettingsButton(
            ScreenEvent.Init.Post event,
            PauseScreen screen
    ) {
        int width = 100;
        int height = 20;

        int x = screen.width / 2 + 105;
        int y = screen.height / 2 - 100;

        Button settingsButton = Button.builder(
                Component.literal("Flood Settings"),
                button -> Minecraft.getInstance().setScreen(
                        new FloodHudSettingsScreen(screen)
                )
        )
        .bounds(x, y, width, height)
        .build();

        event.addListener(settingsButton);
    }
}