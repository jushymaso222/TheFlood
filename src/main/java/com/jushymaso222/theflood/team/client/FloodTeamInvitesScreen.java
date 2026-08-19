package com.jushymaso222.theflood.team.client;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FloodTeamInvitesScreen extends Screen {

    private final Screen parent;

    private final List<ClientTeamInviteData.Invite> invites =
            new ArrayList<>();

    public FloodTeamInvitesScreen(
            Screen parent
    ) {
        super(
                Component.literal(
                        "Team Invites"
                )
        );

        this.parent = parent;
    }

    public void refreshInvites() {
        clearWidgets();
        init();
    }

    @Override
    protected void init() {
        invites.clear();

        /*
         * Ask the server for the player's
         * current pending team invitations.
         */
        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets
                        .RequestPendingTeamInvitesPacket()
        );

        invites.addAll(
                ClientTeamInviteData.getInvites()
        );

        createInviteButtons();

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

    private void createInviteButtons() {

        int centerX =
                width / 2;

        int startY =
                55;

        int spacing =
                28;

        for (
                int i = 0;
                i < invites.size();
                i++
        ) {
            ClientTeamInviteData.Invite invite =
                    invites.get(i);

            int y =
                    startY
                            + (i * spacing);

            /*
             * Team name
             */
            addRenderableWidget(
                    Button.builder(
                            Component.literal(
                                    "Join "
                                            + invite.teamName()
                            ),
                            button ->
                                    acceptInvite(
                                            invite
                                    )
                    )
                    .bounds(
                            centerX - 105,
                            y,
                            140,
                            20
                    )
                    .build()
            );

            /*
             * Decline
             */
            addRenderableWidget(
                    Button.builder(
                            Component.literal("X"),
                            button ->
                                    declineInvite(
                                            invite
                                    )
                    )
                    .bounds(
                            centerX + 40,
                            y,
                            25,
                            20
                    )
                    .build()
            );
        }
    }

    private void acceptInvite(
            ClientTeamInviteData.Invite invite
    ) {
        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets
                        .AcceptTeamInvitePacket(
                                invite.teamId()
                        )
        );

        /*
         * Don't close immediately.
         *
         * If Heat confirmation is required,
         * the player will receive the warning.
         */
        onClose();
    }

    private void declineInvite(
            ClientTeamInviteData.Invite invite
    ) {
        FloodNetwork.CHANNEL.sendToServer(
                new TeamNetworkingPackets
                        .DeclineTeamInvitePacket(
                                invite.teamId()
                        )
        );

        /*
         * Remove immediately client-side so
         * the screen feels responsive.
         */
        invites.remove(invite);

        refreshInvites();
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
                "TEAM INVITES",
                width / 2,
                20,
                0xFFFFFFFF
        );

        if (invites.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    "You have no pending team invites.",
                    width / 2,
                    55,
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
        minecraft.setScreen(
                parent
        );
    }
}