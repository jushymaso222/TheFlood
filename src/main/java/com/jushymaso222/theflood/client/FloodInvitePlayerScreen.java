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
import java.util.UUID;

public class FloodInvitePlayerScreen extends Screen {

    private final Screen parent;

    private record InviteCandidate(
                UUID id,
                String name
        ) {
        }
    private final List<InviteCandidate> players =
        new ArrayList<>();

    public FloodInvitePlayerScreen(Screen parent) {
        super(Component.literal("Invite Player"));
        this.parent = parent;
    }

    public void refreshCandidates() {
        clearWidgets();
        init();
    }

    @Override
    protected void init() {
        players.clear();

        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets.RequestInviteCandidatesPacket()
        );

        for (ClientInviteData.Candidate candidate :
                ClientInviteData.getCandidates()) {

                players.add(
                        new InviteCandidate(
                                candidate.id(),
                                candidate.name()
                        )
                );
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

                InviteCandidate candidate =
                        players.get(i);

                int y =
                        startY + (i * spacing);

                addRenderableWidget(
                        Button.builder(
                                Component.literal(
                                        "Invite "
                                                + candidate.name()
                                ),
                                button ->
                                        invitePlayer(candidate)
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
        InviteCandidate candidate
    ) {
        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets.InvitePlayerPacket(
                        candidate.id()
                )
        );

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