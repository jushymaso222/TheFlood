package com.jushymaso222.theflood.team.client;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

public class FloodTeamScreen extends Screen {

    private final Screen parent;

    private Button createTeamButton;
    private Button invitePlayerButton;
    private Button invitesButton;
    private Button leaveTeamButton;
    private Button disbandTeamButton;

    public FloodTeamScreen(Screen parent) {
        super(Component.literal("Flood Teams"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        createTeamButton = Button.builder(
                Component.literal("Create Team"),
                button -> minecraft.setScreen(
                        new FloodCreateTeamScreen(this)
                )
        )
        .bounds(
                centerX - 75,
                height / 2 - 50,
                150,
                20
        )
        .build();

        addRenderableWidget(createTeamButton);


        invitePlayerButton = Button.builder(
                Component.literal("Invite Player"),
                button -> minecraft.setScreen(
                        new FloodInvitePlayerScreen(this)
                )
        )
        .bounds(
                centerX - 75,
                height / 2 - 25,
                150,
                20
        )
        .build();

        addRenderableWidget(invitePlayerButton);


        invitesButton = Button.builder(
                Component.literal("Invites"),
                button ->
                        minecraft.setScreen(
                                new FloodTeamInvitesScreen(
                                        this
                                )
                        )
        )
        .bounds(
                centerX - 75,
                height / 2,
                150,
                20
        )
        .build();

        addRenderableWidget(
                invitesButton
        );


        leaveTeamButton = Button.builder(
                Component.literal("Leave Team"),
                button -> FloodNetwork.CHANNEL.sendToServer(
                        new TeamNetworkingPackets.LeaveTeamPacket()
                )
        )
        .bounds(
                centerX - 75,
                height / 2 + 25,
                150,
                20
        )
        .build();

        addRenderableWidget(leaveTeamButton);


        disbandTeamButton = Button.builder(
                Component.literal("Disband Team"),
                button -> FloodNetwork.CHANNEL.sendToServer(
                        new TeamNetworkingPackets.DisbandTeamPacket()
                )
        )
        .bounds(
                centerX - 75,
                height / 2 + 50,
                150,
                20
        )
        .build();

        addRenderableWidget(disbandTeamButton);


        addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                )
                .bounds(
                        centerX - 50,
                        height / 2 + 85,
                        100,
                        20
                )
                .build()
        );

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean inTeam =
                ClientTeamData.isInTeam();

        boolean owner =
                ClientTeamData.isOwner();

        createTeamButton.active =
                !inTeam;

        invitePlayerButton.active =
                inTeam;

        /*
        * You can always view invitations while solo.
        *
        * Once you're in a team, existing invitations are
        * irrelevant because one player cannot join two teams.
        */
        invitesButton.active =
                !inTeam;

        leaveTeamButton.active =
                inTeam;

        disbandTeamButton.active =
                inTeam && owner;
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
                "FLOOD TEAM",
                width / 2,
                25,
                0xFFFFFFFF
        );

        graphics.drawCenteredString(
                font,
                "Team management",
                width / 2,
                40,
                0xFFAAAAAA
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        if (ClientTeamData.isInTeam()) {

                String teamName =
                        ClientTeamData.getTeamName();

                int teamColor =
                        getDyeTextColor(
                                ClientTeamData.getTeamColor()
                        );

                graphics.drawCenteredString(
                        font,
                        teamName,
                        width / 2,
                        50,
                        teamColor
                );

                } else {

                graphics.drawCenteredString(
                        font,
                        "No Team",
                        width / 2,
                        50,
                        0xFFAAAAAA
                );
        }
    }

    private int getDyeTextColor(
                DyeColor color
    ) {
        float[] rgb =
                color.getTextureDiffuseColors();

        int red =
                (int) (rgb[0] * 255);

        int green =
                (int) (rgb[1] * 255);

        int blue =
                (int) (rgb[2] * 255);

        return 0xFF000000
                | (red << 16)
                | (green << 8)
                | blue;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void tick() {
        super.tick();

        updateButtonStates();
    }
}