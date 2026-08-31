package com.jushymaso222.theflood.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.progression.HeatTier;
import com.jushymaso222.theflood.progression.client.ClientHeatData;
import com.jushymaso222.theflood.team.client.ClientTeamInviteData;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;
import com.jushymaso222.theflood.team.client.ClientTeamData;

import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class FloodTabInvitePanel {

    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_HEIGHT = 100;

    private static final int MAX_VISIBLE_INVITES = 2;

    private static final int ROW_HEIGHT = 34;
    private static final int BUTTON_HEIGHT = 12;
    private static final int BUTTON_GAP = 4;

    private static UUID armedInviteId = null;

    public static void resetConfirmation() {
        armedInviteId = null;
    }

    private static final List<InviteRowBounds> INVITE_BOUNDS =
            new ArrayList<>();

    private FloodTabInvitePanel() {
    }

    public static int getPanelWidth() {
        return PANEL_WIDTH;
    }

    public static int getPanelHeight() {
        return PANEL_HEIGHT;
    }

    public static boolean hasInvites() {
        return !ClientTeamInviteData
                .getInvites()
                .isEmpty();
    }

    public static void render(
            GuiGraphics graphics,
            Minecraft minecraft,
            int x,
            int y,
            double mouseX,
            double mouseY,
            boolean interactionMode
    ) {
        INVITE_BOUNDS.clear();

        int height =
                PANEL_HEIGHT;

        List<ClientTeamInviteData.Invite> invites =
                ClientTeamInviteData.getInvites();

        int accentColor =
                HeatTier.getColor(
                        ClientHeatData.getEffectiveHeat()
                );

        graphics.fill(
                x,
                y,
                x + PANEL_WIDTH,
                y + height,
                0xB0000000
        );

        graphics.fill(
                x,
                y,
                x + PANEL_WIDTH,
                y + 1,
                0xFFFFFFFF
        );

        graphics.fill(
                x,
                y + height - 1,
                x + PANEL_WIDTH,
                y + height,
                0xFFFFFFFF
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                0xFFFFFFFF
        );

        graphics.fill(
                x + PANEL_WIDTH - 1,
                y,
                x + PANEL_WIDTH,
                y + height,
                0xFFFFFFFF
        );

        String header =
                ClientTeamData.isInTeam()
                        ? "TEAM STATUS"
                        : "TEAM INVITES";

        graphics.drawString(
                minecraft.font,
                header,
                x + 8,
                y + 8,
                0xFFFFFFFF,
                true
        );

        graphics.fill(
                x + 8,
                y + 20,
                x + 70,
                y + 21,
                accentColor
        );

        if (invites.isEmpty()) {

            if (ClientTeamData.isInTeam()) {

                graphics.drawCenteredString(
                        minecraft.font,
                        "YOUR TEAM",
                        x + PANEL_WIDTH / 2,
                        y + 34,
                        0xFFFFFFFF
                );

                String teamName =
                        ClientTeamData.getTeamName();

                graphics.drawCenteredString(
                        minecraft.font,
                        teamName,
                        x + PANEL_WIDTH / 2,
                        y + 48,
                        accentColor
                );

                String roleText =
                        ClientTeamData.isOwner()
                                ? "OWNER"
                                : "MEMBER";

                graphics.drawCenteredString(
                        minecraft.font,
                        roleText,
                        x + PANEL_WIDTH / 2,
                        y + 61,
                        0xFFAAAAAA
                );

                String heatText =
                        "Team Heat: "
                                + ClientTeamData.getTeamHeat();

                graphics.drawCenteredString(
                        minecraft.font,
                        heatText,
                        x + PANEL_WIDTH / 2,
                        y + 75,
                        0xFFBBBBBB
                );

                return;
            }

            graphics.drawCenteredString(
                    minecraft.font,
                    "No pending invites",
                    x + PANEL_WIDTH / 2,
                    y + 44,
                    0xFF888888
            );

            return;
        }

        int visibleCount =
                Math.min(
                        invites.size(),
                        MAX_VISIBLE_INVITES
                );

    for (
            int i = 0;
            i < visibleCount;
            i++
    ) {
        ClientTeamInviteData.Invite invite =
                invites.get(i);

        int rowY =
                y
                        + 27
                        + (i * ROW_HEIGHT);

        int buttonY =
                rowY + 12;

        int innerX =
                x + 8;

        int innerWidth =
                PANEL_WIDTH - 16;

        int buttonWidth =
                (innerWidth - BUTTON_GAP) / 2;

        int acceptX =
                innerX;

        int declineX =
                acceptX
                        + buttonWidth
                        + BUTTON_GAP;


        /*
        * =================================================
        * CONFIRMATION STATE
        * =================================================
        */

        boolean armed =
                isInviteArmed(
                        invite.teamId()
                );

        String acceptText =
                armed
                        ? "CONFIRM?"
                        : "ACCEPT";


        /*
        * =================================================
        * HOVER STATE
        * =================================================
        */

        boolean acceptHovered =
                interactionMode
                        && isInside(
                                mouseX,
                                mouseY,
                                acceptX,
                                buttonY,
                                buttonWidth,
                                BUTTON_HEIGHT
                        );

        boolean declineHovered =
                interactionMode
                        && isInside(
                                mouseX,
                                mouseY,
                                declineX,
                                buttonY,
                                buttonWidth,
                                BUTTON_HEIGHT
                        );


        /*
        * =================================================
        * TEAM NAME
        * =================================================
        */

        graphics.drawString(
                minecraft.font,
                invite.teamName(),
                innerX,
                rowY,
                0xFFFFFFFF,
                true
        );


        /*
        * =================================================
        * ACCEPT / CONFIRM
        * =================================================
        */

        renderButton(
                graphics,
                minecraft,
                acceptText,
                acceptX,
                buttonY,
                buttonWidth,
                acceptHovered,
                0xFFFFAA00
        );


        /*
        * =================================================
        * DECLINE
        * =================================================
        */

        renderButton(
                graphics,
                minecraft,
                "DECLINE",
                declineX,
                buttonY,
                buttonWidth,
                declineHovered,
                0xFFFF5555
        );


        /*
        * =================================================
        * CLICK BOUNDS
        * =================================================
        */

        INVITE_BOUNDS.add(
                new InviteRowBounds(
                        invite.teamId(),
                        acceptX,
                        buttonY,
                        buttonWidth,
                        declineX
                )
        );


        /*
        * =================================================
        * HEAT CONFIRMATION TOOLTIP
        * =================================================
        */

        if (
                armed
                        && acceptHovered
        ) {
            graphics.renderTooltip(
                    minecraft.font,
                    List.of(
                            Component.literal(
                                    "Joining "
                                            + invite.teamName()
                                            + " will increase your Heat!"
                            ).withStyle(
                                    ChatFormatting.YELLOW
                            ),

                            Component.literal(
                                    "Current Heat: "
                                            + invite.currentHeat()
                            ).withStyle(
                                    ChatFormatting.GRAY
                            ),

                            Component.literal(
                                    "Heat after joining: "
                                            + invite.projectedHeat()
                            ).withStyle(
                                    ChatFormatting.RED
                            )
                    ),
                    Optional.empty(),
                    (int) mouseX,
                    (int) mouseY
            );
        }
    }
        if (invites.size() > MAX_VISIBLE_INVITES) {

            int remaining =
                    invites.size()
                            - MAX_VISIBLE_INVITES;

            graphics.drawString(
                    minecraft.font,
                    "+"
                            + remaining
                            + " more",
                    x + PANEL_WIDTH - 42,
                    y + PANEL_HEIGHT - 10,
                    0xFFAAAAAA,
                    false
            );
        }
    }

    public static boolean handleClick(
        double mouseX,
        double mouseY
) {
    for (
            InviteRowBounds bounds :
            INVITE_BOUNDS
    ) {

        /*
         * =================================================
         * ACCEPT / CONFIRM
         * =================================================
         */

        if (
                isInside(
                        mouseX,
                        mouseY,
                        bounds.acceptX(),
                        bounds.y(),
                        bounds.buttonWidth(),
                        BUTTON_HEIGHT
                )
        ) {

            ClientTeamInviteData.Invite invite =
                    findInvite(
                            bounds.teamId()
                    );

            if (invite == null) {
                armedInviteId = null;
                return true;
            }

            /*
             * No Heat increase.
             *
             * Accept immediately like normal.
             */
            if (!invite.increasesHeat()) {

                armedInviteId = null;

                FloodNetwork.CHANNEL.sendToServer(
                        new TeamNetworkingPackets
                                .AcceptTeamInvitePacket(
                                        invite.teamId()
                                )
                );

                ClientTeamInviteData.removeInvite(
                        invite.teamId()
                );

                return true;
            }

            /*
             * Heat WOULD increase.
             *
             * First click only arms the confirmation.
             *
             * We also send the normal accept request here
             * so the server creates its existing pending
             * Heat confirmation state.
             */
            if (
                    armedInviteId == null
                            || !armedInviteId.equals(
                                    invite.teamId()
                            )
            ) {

                armedInviteId =
                        invite.teamId();

                FloodNetwork.CHANNEL.sendToServer(
                        new TeamNetworkingPackets
                                .AcceptTeamInvitePacket(
                                        invite.teamId()
                                )
                );

                return true;
            }

            /*
             * Same invite clicked again while armed.
             *
             * Actually confirm the Heat increase.
             */
            armedInviteId = null;

            FloodNetwork.CHANNEL.sendToServer(
                    new TeamNetworkingPackets
                            .ConfirmTeamInvitePacket(
                                    invite.teamId()
                            )
            );

            ClientTeamInviteData.removeInvite(
                    invite.teamId()
            );

            return true;
        }


        /*
         * =================================================
         * DECLINE
         * =================================================
         */

        if (
                isInside(
                        mouseX,
                        mouseY,
                        bounds.declineX(),
                        bounds.y(),
                        bounds.buttonWidth(),
                        BUTTON_HEIGHT
                )
        ) {

            /*
             * Declining the currently armed invite also
             * cancels the local confirmation state.
             */
            if (
                    armedInviteId != null
                            && armedInviteId.equals(
                                    bounds.teamId()
                            )
            ) {
                armedInviteId = null;
            }

            FloodNetwork.CHANNEL.sendToServer(
                    new TeamNetworkingPackets
                            .DeclineTeamInvitePacket(
                                    bounds.teamId()
                            )
            );

            ClientTeamInviteData.removeInvite(
                    bounds.teamId()
            );

            return true;
        }
    }


    /*
     * Clicking anywhere outside the invite buttons
     * cancels an armed confirmation.
     *
     * Same behavior as the DISBAND confirmation.
     */
    armedInviteId =
            null;

    return false;
}

private static ClientTeamInviteData.Invite findInvite(
        UUID teamId
) {
    for (
            ClientTeamInviteData.Invite invite :
            ClientTeamInviteData.getInvites()
    ) {
        if (
                invite.teamId()
                        .equals(teamId)
        ) {
            return invite;
        }
    }

    return null;
}

private static boolean isInviteArmed(
        UUID teamId
) {
    return armedInviteId != null
            && armedInviteId.equals(
                    teamId
            );
}

    private static void renderButton(
            GuiGraphics graphics,
            Minecraft minecraft,
            String text,
            int x,
            int y,
            int width,
            boolean hovered,
            int color
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + BUTTON_HEIGHT,
                hovered
                        ? 0x55FFFFFF
                        : 0x33000000
        );

        graphics.fill(
                x,
                y + BUTTON_HEIGHT - 1,
                x + width,
                y + BUTTON_HEIGHT,
                hovered
                        ? color
                        : 0xFF555555
        );

        graphics.drawCenteredString(
                minecraft.font,
                text,
                x + width / 2,
                y + 2,
                color
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

    private record InviteRowBounds(
            UUID teamId,
            int acceptX,
            int y,
            int buttonWidth,
            int declineX
    ) {
    }
}