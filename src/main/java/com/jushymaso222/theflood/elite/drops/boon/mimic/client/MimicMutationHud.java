package com.jushymaso222.theflood.elite.drops.boon.mimic.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.drops.boon.mimic.PlayerMimicMutation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT
)
public final class MimicMutationHud {

    private static final int BAR_WIDTH =
            90;

    private static final int BAR_HEIGHT =
            7;

    private static final int SPIKED_COLOR =
            0xFF58A6FF;

    private static final int SHIFTING_COLOR =
            0xFFFFA24C;

    private static final int UNDYING_COLOR =
            0xFF62D68B;

    private static final int FRENZIED_COLOR =
            0xFFFF5A5A;

    private static final int COMMANDER_COLOR =
            0xFFB06CFF;


    private MimicMutationHud() {
    }


    @SubscribeEvent
    public static void onRenderGui(
            RenderGuiOverlayEvent.Post event
    ) {
        if (
                !ClientMimicMutationData.hasActiveMutation()
        ) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.player == null
                || minecraft.options.hideGui
        ) {
            return;
        }

        GuiGraphics graphics =
                event.getGuiGraphics();

        PlayerMimicMutation mutation =
                ClientMimicMutationData.getActiveMutation();

        int screenWidth =
                minecraft.getWindow()
                        .getGuiScaledWidth();

        int screenHeight =
                minecraft.getWindow()
                        .getGuiScaledHeight();


        /*
         * Vanilla hotbar is 22 pixels tall.
         *
         * Anchor the mutation HUD immediately above it.
         */

        int centerX =
                screenWidth / 2;

        int hotbarTop =
                screenHeight - 22;

        int barX =
                centerX - 91;

        int barY =
                hotbarTop - 40;


        renderMutationName(
                graphics,
                minecraft,
                mutation,
                barX,
                barY
        );

        renderPlaceholderBar(
                graphics,
                mutation,
                barX,
                barY + 10
        );

        Component primaryKey =
                MimicKeyMappings.EFFECT_PRIMARY
                        .getTranslatedKeyMessage();

        Component secondaryKey =
                MimicKeyMappings.EFFECT_SECONDARY
                        .getTranslatedKeyMessage();
    }


    private static void renderMutationName(
            GuiGraphics graphics,
            Minecraft minecraft,
            PlayerMimicMutation mutation,
            int x,
            int y
    ) {
        graphics.drawString(
                minecraft.font,
                "Mutation: " + mutation.displayName(),
                x,
                y,
                0xFFFFFFFF,
                true
        );
    }


    private static void renderPlaceholderBar(
            GuiGraphics graphics,
            PlayerMimicMutation mutation,
            int x,
            int y
    ) {
        int color =
                getMutationColor(
                        mutation
                );

        /*
         * Dark backing.
         */
        graphics.fill(
                x,
                y,
                x + BAR_WIDTH,
                y + BAR_HEIGHT,
                0xCC111318
        );


        /*
         * Temporary 75% fill so we can design the HUD
         * before real mutation state exists.
         */
        int fillWidth =
                (int) (
                        BAR_WIDTH * 0.75F
                );

        graphics.fill(
                x + 1,
                y + 1,
                x + fillWidth - 1,
                y + BAR_HEIGHT - 1,
                color
        );


        /*
         * Simple border.
         */
        graphics.fill(
                x,
                y,
                x + BAR_WIDTH,
                y + 1,
                0xFF777777
        );

        graphics.fill(
                x,
                y + BAR_HEIGHT - 1,
                x + BAR_WIDTH,
                y + BAR_HEIGHT,
                0xFF777777
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + BAR_HEIGHT,
                0xFF777777
        );

        graphics.fill(
                x + BAR_WIDTH - 1,
                y,
                x + BAR_WIDTH,
                y + BAR_HEIGHT,
                0xFF777777
        );
    }


    private static int getMutationColor(
            PlayerMimicMutation mutation
    ) {
        return switch (mutation) {
            case SPIKED ->
                    SPIKED_COLOR;

            case SHIFTING ->
                    SHIFTING_COLOR;

            case UNDYING ->
                    UNDYING_COLOR;

            case FRENZIED ->
                    FRENZIED_COLOR;

            case COMMANDER ->
                    COMMANDER_COLOR;
        };
    }
}