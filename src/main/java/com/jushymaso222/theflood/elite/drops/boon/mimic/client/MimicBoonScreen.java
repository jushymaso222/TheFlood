package com.jushymaso222.theflood.elite.drops.boon.mimic.client;

import com.jushymaso222.theflood.elite.drops.boon.mimic.PlayerMimicMutation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.resources.ResourceLocation;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.elite.drops.boon.mimic.network.SelectMimicMutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MimicBoonScreen
        extends Screen {

    /*
     * Main panel dimensions.
     *
     * The screen is intentionally compact and centered so
     * it still feels like the rest of The Flood's UI.
     */
    private static final int PANEL_WIDTH =
            390;

    private static final int PANEL_HEIGHT =
            250;

    private static final int SCREEN_MARGIN =
            16;

    private float uiScale =
            1.0F;

    private static final ResourceLocation ICON_SPIKED =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/boon/mimic/spiked.png"
            );

    private static final ResourceLocation ICON_SHIFTING =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/boon/mimic/shifting.png"
            );

    private static final ResourceLocation ICON_UNDYING =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/boon/mimic/undying.png"
            );

    private static final ResourceLocation ICON_FRENZIED =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/boon/mimic/frenzied.png"
            );

    private static final ResourceLocation ICON_COMMANDER =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/boon/mimic/commander.png"
            );


    /*
     * Card dimensions.
     */
    private static final int CARD_WIDTH =
            112;

    private static final int CARD_HEIGHT =
            68;

    private static final int CARD_GAP =
            8;


    /*
     * Colors.
     */
    private static final int BACKGROUND_OVERLAY =
            0x88000000;

    private static final int PANEL_BACKGROUND =
            0xDD090A0D;

    private static final int PANEL_BORDER =
            0xFF565B66;

    private static final int PANEL_INNER_BORDER =
            0xFF242730;

    private static final int TEXT_PRIMARY =
            0xFFF2F2F2;

    private static final int TEXT_SECONDARY =
            0xFF9A9DA6;

    private static final int MIMIC_PURPLE =
            0xFF9A63FF;

    private static final int CARD_BACKGROUND =
            0xCC111318;

    private static final int CARD_HOVER_BACKGROUND =
            0xEE181B22;


    /*
     * Mutation accents.
     */
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


    private final List<UiParticle> particles =
            new ArrayList<>();

    private final Random random =
            new Random();


    private int panelX;
    private int panelY;

    private PlayerMimicMutation hoveredMutation;


    public MimicBoonScreen() {
        super(
                Component.literal(
                        "Boon of the Mimic"
                )
        );
    }

    private static ResourceLocation getMutationIcon(
            PlayerMimicMutation mutation
    ) {
        return switch (mutation) {
            case SPIKED ->
                    ICON_SPIKED;

            case SHIFTING ->
                    ICON_SHIFTING;

            case UNDYING ->
                    ICON_UNDYING;

            case FRENZIED ->
                    ICON_FRENZIED;

            case COMMANDER ->
                    ICON_COMMANDER;
        };
    }


    @Override
    protected void init() {
        super.init();

        float availableWidth =
                width - SCREEN_MARGIN * 2.0F;

        float availableHeight =
                height - SCREEN_MARGIN * 2.0F;

        float widthScale =
                availableWidth / PANEL_WIDTH;

        float heightScale =
                availableHeight / PANEL_HEIGHT;

        uiScale =
                Math.min(
                        1.0F,
                        Math.min(
                                widthScale,
                                heightScale
                        )
                );

        panelX =
                (int) (
                        (width / uiScale - PANEL_WIDTH)
                                / 2.0F
                );

        panelY =
                (int) (
                        (height / uiScale - PANEL_HEIGHT)
                                / 2.0F
                );

        particles.clear();

        for (
                int i = 0;
                i < 24;
                i++
        ) {
            particles.add(
                    createParticle(
                            true
                    )
            );
        }
    }


    @Override
    public void tick() {
        super.tick();

        for (
                int i = 0;
                i < particles.size();
                i++
        ) {
            UiParticle particle =
                    particles.get(i);

            particle.x +=
                    particle.velocityX;

            particle.y +=
                    particle.velocityY;

            particle.life--;

            if (
                    particle.life <= 0
                    || particle.x < panelX - 18
                    || particle.x > panelX + PANEL_WIDTH + 18
                    || particle.y < panelY - 18
                    || particle.y > panelY + PANEL_HEIGHT + 18
            ) {
                particles.set(
                        i,
                        createParticle(
                                false
                        )
                );
            }
        }
    }


    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        /*
         * Leave the game visible behind the UI,
         * but heavily darken it.
         */
        graphics.fill(
                0,
                0,
                width,
                height,
                BACKGROUND_OVERLAY
        );

        graphics.pose().pushPose();

        graphics.pose().scale(
                uiScale,
                uiScale,
                1.0F
        );

        int scaledMouseX =
                (int) (mouseX / uiScale);

        int scaledMouseY =
                (int) (mouseY / uiScale);

        hoveredMutation =
                null;

        renderParticles(
                graphics
        );

        renderPanel(
                graphics
        );

        renderHeader(
                graphics
        );

        renderMutationCards(
                graphics,
                scaledMouseX,
                scaledMouseY
        );

        renderFooter(
                graphics,
                scaledMouseX,
                scaledMouseY
        );

        graphics.pose().popPose();

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }


    private void renderPanel(
            GuiGraphics graphics
    ) {
        /*
         * Very subtle outer glow.
         */
        graphics.fill(
                panelX - 2,
                panelY - 2,
                panelX + PANEL_WIDTH + 2,
                panelY + PANEL_HEIGHT + 2,
                0x229A63FF
        );

        /*
         * Main panel.
         */
        graphics.fill(
                panelX,
                panelY,
                panelX + PANEL_WIDTH,
                panelY + PANEL_HEIGHT,
                PANEL_BACKGROUND
        );

        drawBorder(
                graphics,
                panelX,
                panelY,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                PANEL_BORDER
        );

        drawBorder(
                graphics,
                panelX + 3,
                panelY + 3,
                PANEL_WIDTH - 6,
                PANEL_HEIGHT - 6,
                PANEL_INNER_BORDER
        );


        /*
         * Small purple corner accents.
         *
         * Enough to distinguish the Mimic screen without
         * turning it into a completely different UI style.
         */
        int accentLength =
                20;

        graphics.fill(
                panelX,
                panelY,
                panelX + accentLength,
                panelY + 1,
                MIMIC_PURPLE
        );

        graphics.fill(
                panelX,
                panelY,
                panelX + 1,
                panelY + accentLength,
                MIMIC_PURPLE
        );

        graphics.fill(
                panelX + PANEL_WIDTH - accentLength,
                panelY,
                panelX + PANEL_WIDTH,
                panelY + 1,
                MIMIC_PURPLE
        );

        graphics.fill(
                panelX + PANEL_WIDTH - 1,
                panelY,
                panelX + PANEL_WIDTH,
                panelY + accentLength,
                MIMIC_PURPLE
        );

        graphics.fill(
                panelX,
                panelY + PANEL_HEIGHT - 1,
                panelX + accentLength,
                panelY + PANEL_HEIGHT,
                MIMIC_PURPLE
        );

        graphics.fill(
                panelX,
                panelY + PANEL_HEIGHT - accentLength,
                panelX + 1,
                panelY + PANEL_HEIGHT,
                MIMIC_PURPLE
        );

        graphics.fill(
                panelX + PANEL_WIDTH - accentLength,
                panelY + PANEL_HEIGHT - 1,
                panelX + PANEL_WIDTH,
                panelY + PANEL_HEIGHT,
                MIMIC_PURPLE
        );

        graphics.fill(
                panelX + PANEL_WIDTH - 1,
                panelY + PANEL_HEIGHT - accentLength,
                panelX + PANEL_WIDTH,
                panelY + PANEL_HEIGHT,
                MIMIC_PURPLE
        );
    }


    private void renderHeader(
            GuiGraphics graphics
    ) {
        String title =
                "BOON OF THE MIMIC";

        int titleX =
                panelX
                        + (PANEL_WIDTH
                        - font.width(title))
                        / 2;

        int titleY =
                panelY + 16;

        /*
         * Tiny restrained glow behind the title.
         */
        graphics.drawString(
                font,
                title,
                titleX + 1,
                titleY + 1,
                0x559A63FF,
                false
        );

        graphics.drawString(
                font,
                title,
                titleX,
                titleY,
                TEXT_PRIMARY,
                false
        );


        String subtitle =
                "Choose the mutation you will inherit.";

        graphics.drawCenteredString(
                font,
                subtitle,
                panelX + PANEL_WIDTH / 2,
                titleY + 16,
                TEXT_SECONDARY
        );


        /*
         * Header divider.
         */
        int dividerWidth =
                190;

        int dividerX =
                panelX
                        + (PANEL_WIDTH - dividerWidth)
                        / 2;

        graphics.fill(
                dividerX,
                titleY + 30,
                dividerX + dividerWidth,
                titleY + 31,
                0x66565B66
        );

        graphics.fill(
                panelX + PANEL_WIDTH / 2 - 20,
                titleY + 30,
                panelX + PANEL_WIDTH / 2 + 20,
                titleY + 31,
                MIMIC_PURPLE
        );
    }


    private void renderMutationCards(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        int firstRowY =
                panelY + 64;

        int firstRowWidth =
                CARD_WIDTH * 3
                        + CARD_GAP * 2;

        int firstRowX =
                panelX
                        + (PANEL_WIDTH - firstRowWidth)
                        / 2;


        renderMutationCard(
                graphics,
                PlayerMimicMutation.SPIKED,
                firstRowX,
                firstRowY,
                SPIKED_COLOR,
                "Shield",
                mouseX,
                mouseY
        );

        renderMutationCard(
                graphics,
                PlayerMimicMutation.SHIFTING,
                firstRowX
                        + CARD_WIDTH
                        + CARD_GAP,
                firstRowY,
                SHIFTING_COLOR,
                "Mobility",
                mouseX,
                mouseY
        );

        renderMutationCard(
                graphics,
                PlayerMimicMutation.UNDYING,
                firstRowX
                        + (CARD_WIDTH + CARD_GAP) * 2,
                firstRowY,
                UNDYING_COLOR,
                "Rebirth",
                mouseX,
                mouseY
        );


        int secondRowY =
                firstRowY
                        + CARD_HEIGHT
                        + CARD_GAP;

        int secondRowWidth =
                CARD_WIDTH * 2
                        + CARD_GAP;

        int secondRowX =
                panelX
                        + (PANEL_WIDTH - secondRowWidth)
                        / 2;


        renderMutationCard(
                graphics,
                PlayerMimicMutation.FRENZIED,
                secondRowX,
                secondRowY,
                FRENZIED_COLOR,
                "Rage",
                mouseX,
                mouseY
        );

        renderMutationCard(
                graphics,
                PlayerMimicMutation.COMMANDER,
                secondRowX
                        + CARD_WIDTH
                        + CARD_GAP,
                secondRowY,
                COMMANDER_COLOR,
                "Control",
                mouseX,
                mouseY
        );
    }


    private void renderMutationCard(
            GuiGraphics graphics,
            PlayerMimicMutation mutation,
            int x,
            int y,
            int accentColor,
            String descriptor,
            int mouseX,
            int mouseY
    ) {
        boolean hovered =
                mouseX >= x
                        && mouseX < x + CARD_WIDTH
                        && mouseY >= y
                        && mouseY < y + CARD_HEIGHT;

        if (hovered) {
            hoveredMutation =
                    mutation;

            /*
             * Very small glow around hovered cards.
             */
            graphics.fill(
                    x - 2,
                    y - 2,
                    x + CARD_WIDTH + 2,
                    y + CARD_HEIGHT + 2,
                    withAlpha(
                            accentColor,
                            40
                    )
            );
        }


        graphics.fill(
                x,
                y,
                x + CARD_WIDTH,
                y + CARD_HEIGHT,
                hovered
                        ? CARD_HOVER_BACKGROUND
                        : CARD_BACKGROUND
        );

        drawBorder(
                graphics,
                x,
                y,
                CARD_WIDTH,
                CARD_HEIGHT,
                hovered
                        ? accentColor
                        : PANEL_BORDER
        );


        int iconSize =
                24;

        int iconX =
                x
                        + (CARD_WIDTH - iconSize)
                        / 2;

        int iconY =
                y + 7;

        int iconCenterX =
                x + CARD_WIDTH / 2;

        ResourceLocation icon =
                getMutationIcon(
                        mutation
                );

        float red =
                ((accentColor >> 16) & 0xFF)
                        / 255.0F;

        float green =
                ((accentColor >> 8) & 0xFF)
                        / 255.0F;

        float blue =
                (accentColor & 0xFF)
                        / 255.0F;

        graphics.setColor(
                red,
                green,
                blue,
                hovered
                        ? 1.0F
                        : 0.8F
        );

        graphics.blit(
                icon,
                iconX,
                iconY,
                0,
                0,
                iconSize,
                iconSize,
                iconSize,
                iconSize
        );

        graphics.setColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );


        graphics.drawCenteredString(
                font,
                mutation.displayName()
                        .toUpperCase(),
                iconCenterX,
                y + 34,
                hovered
                        ? accentColor
                        : TEXT_PRIMARY
        );

        graphics.drawCenteredString(
                font,
                descriptor,
                iconCenterX,
                y + 48,
                TEXT_SECONDARY
        );


        if (hovered) {
            graphics.fill(
                    x + 10,
                    y + CARD_HEIGHT - 5,
                    x + CARD_WIDTH - 10,
                    y + CARD_HEIGHT - 4,
                    accentColor
            );
        }
    }


    private void renderFooter(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        int footerY =
                panelY + PANEL_HEIGHT - 35;

        graphics.drawCenteredString(
                font,
                "Selecting a mutation consumes the Boon.",
                panelX + PANEL_WIDTH / 2,
                footerY,
                TEXT_SECONDARY
        );


        String cancel =
                "[ CANCEL ]";

        int cancelX =
                panelX
                        + (PANEL_WIDTH - font.width(cancel))
                        / 2;

        int cancelY =
                footerY + 16;

        boolean hovered =
                mouseX >= cancelX - 5
                        && mouseX <= cancelX
                        + font.width(cancel)
                        + 5
                        && mouseY >= cancelY - 3
                        && mouseY <= cancelY
                        + font.lineHeight
                        + 3;

        graphics.drawString(
                font,
                cancel,
                cancelX,
                cancelY,
                hovered
                        ? TEXT_PRIMARY
                        : TEXT_SECONDARY,
                false
        );
    }


    private void renderParticles(
            GuiGraphics graphics
    ) {
        for (
                UiParticle particle :
                particles
        ) {
            float lifeProgress =
                    particle.life
                            / (float) particle.maxLife;

            int alpha =
                    (int) (
                            110.0F
                                    * Mth.clamp(
                                            lifeProgress,
                                            0.0F,
                                            1.0F
                                    )
                    );

            int color =
                    withAlpha(
                            MIMIC_PURPLE,
                            alpha
                    );

            int size =
                    particle.size;

            graphics.fill(
                    (int) particle.x,
                    (int) particle.y,
                    (int) particle.x + size,
                    (int) particle.y + size,
                    color
            );
        }
    }


    private UiParticle createParticle(
            boolean randomLife
    ) {
        /*
         * Choose one of the four panel edges.
         */
        int edge =
                random.nextInt(
                        4
                );

        float x;
        float y;

        switch (edge) {
            case 0 -> {
                x =
                        panelX
                                + random.nextFloat()
                                * PANEL_WIDTH;

                y =
                        panelY
                                - 3
                                - random.nextFloat()
                                * 8;
            }

            case 1 -> {
                x =
                        panelX
                                + random.nextFloat()
                                * PANEL_WIDTH;

                y =
                        panelY
                                + PANEL_HEIGHT
                                + 3
                                + random.nextFloat()
                                * 8;
            }

            case 2 -> {
                x =
                        panelX
                                - 3
                                - random.nextFloat()
                                * 8;

                y =
                        panelY
                                + random.nextFloat()
                                * PANEL_HEIGHT;
            }

            default -> {
                x =
                        panelX
                                + PANEL_WIDTH
                                + 3
                                + random.nextFloat()
                                * 8;

                y =
                        panelY
                                + random.nextFloat()
                                * PANEL_HEIGHT;
            }
        }


        int maxLife =
                30
                        + random.nextInt(
                                50
                        );

        int life =
                randomLife
                        ? 1
                        + random.nextInt(
                                maxLife
                        )
                        : maxLife;


        return new UiParticle(
                x,
                y,
                (random.nextFloat() - 0.5F)
                        * 0.12F,
                -0.04F
                        - random.nextFloat()
                        * 0.08F,
                random.nextBoolean()
                        ? 1
                        : 2,
                life,
                maxLife
        );
    }


    private static void drawBorder(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                color
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                color
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                color
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                color
        );
    }


    private static int withAlpha(
            int color,
            int alpha
    ) {
        return (Mth.clamp(
                alpha,
                0,
                255
        ) << 24)
                | (color & 0x00FFFFFF);
    }


    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        double scaledMouseX =
                mouseX / uiScale;

        double scaledMouseY =
                mouseY / uiScale;

        /*
        * Cancel button.
        */
        int footerY =
                panelY + PANEL_HEIGHT - 35;

        String cancel =
                "[ CANCEL ]";

        int cancelX =
                panelX
                        + (PANEL_WIDTH - font.width(cancel))
                        / 2;

        int cancelY =
                footerY + 16;

        boolean cancelHovered =
                scaledMouseX >= cancelX - 5
                        && scaledMouseX <= cancelX
                        + font.width(cancel)
                        + 5
                        && scaledMouseY >= cancelY - 3
                        && scaledMouseY <= cancelY
                        + font.lineHeight
                        + 3;

        if (
                button == 0
                && cancelHovered
        ) {
            onClose();

            return true;
        }

        /*
        * hoveredMutation was determined using the same
        * virtual coordinate system during render.
        */
        if (
                button == 0
                && hoveredMutation != null
        ) {
            FloodNetwork.CHANNEL.sendToServer(
                    new SelectMimicMutation(
                            hoveredMutation
                    )
            );

            onClose();

            return true;
        }

        return super.mouseClicked(
                scaledMouseX,
                scaledMouseY,
                button
        );
    }


    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        /*
         * ESC cancels without consuming anything.
         */
        if (keyCode == 256) {
            onClose();

            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }


    private static final class UiParticle {

        private float x;
        private float y;

        private final float velocityX;
        private final float velocityY;

        private final int size;

        private int life;
        private final int maxLife;


        private UiParticle(
                float x,
                float y,
                float velocityX,
                float velocityY,
                int size,
                int life,
                int maxLife
        ) {
            this.x =
                    x;

            this.y =
                    y;

            this.velocityX =
                    velocityX;

            this.velocityY =
                    velocityY;

            this.size =
                    size;

            this.life =
                    life;

            this.maxLife =
                    maxLife;
        }
    }
}