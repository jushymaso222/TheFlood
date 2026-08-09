package com.jushymaso222.theflood.client;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.network.packet.TeamNetworkingPackets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FloodInvitePlayerScreen extends Screen {

    private final Screen parent;

    private final List<PlayerInfo> players =
            new ArrayList<>();

    public FloodInvitePlayerScreen(Screen parent) {
        super(Component.literal("Invite Player"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        players.clear();

        Minecraft minecraft =
                Minecraft.getInstance();

        /*
         * Get every player currently connected
         * to this server/world.
         */
        if (minecraft.getConnection() != null) {

            for (PlayerInfo playerInfo :
                    minecraft.getConnection().getOnlinePlayers()) {

                /*
                 * Don't allow the player to invite themselves.
                 */
                if (
                        minecraft.player != null
                        && playerInfo.getProfile()
                                .getId()
                                .equals(minecraft.player.getUUID())
                ) {
                    continue;
                }

                players.add(playerInfo);
            }
        }

        createPlayerButtons();

        addRenderableWidget(
                Button.builder(
                        Component.literal("Back"),
                        button -> onClose()
                )
                .bounds(
                        width / 2 - 50,
                        height - 30,
                        100,
                        20
                )
                .build()
        );
    }

    private void createPlayerButtons() {

        int centerX = width / 2;

        int startY = 55;
        int spacing = 25;

        for (int i = 0; i < players.size(); i++) {

            PlayerInfo playerInfo =
                    players.get(i);

            int y =
                    startY + (i * spacing);

            String playerName =
                    playerInfo.getProfile().getName();

            /*
             * Player name
             */
            addRenderableWidget(
                    Button.builder(
                            Component.literal(
                                    "Invite " + playerName
                            ),
                            button -> invitePlayer(
                                    playerInfo
                            )
                    )
                    .bounds(
                            centerX - 90,
                            y,
                            180,
                            20
                    )
                    .build()
            );
        }
    }

    private void invitePlayer(
            PlayerInfo playerInfo
    ) {

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets.InvitePlayerPacket(
                        playerInfo.getProfile().getId()
                )
        );

        /*
         * For now, return to the team screen.
         *
         * The server still performs all validation,
         * so clicking this doesn't guarantee the
         * invite was accepted/sent.
         */
        onClose();
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        renderBackground(graphics);

        graphics.drawCenteredString(
                font,
                "INVITE PLAYER",
                width / 2,
                20,
                0xFFFFFFFF
        );

        if (players.isEmpty()) {

            graphics.drawCenteredString(
                    font,
                    "No other players are online.",
                    width / 2,
                    50,
                    0xFFAAAAAA
            );
        }

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}