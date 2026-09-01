package com.jushymaso222.theflood.progression.client;

import com.jushymaso222.theflood.TheFlood;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.resources.ResourceLocation;

import com.jushymaso222.theflood.debug.DummyPlayerManager;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.common.util.FakePlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.jushymaso222.theflood.progression.HeatManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraft.network.chat.Component;

import org.joml.Matrix4f;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PlayerHeatNameplateRenderer {

    private static final ResourceLocation FLAME_1 =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame1.png"
            );

    private static final ResourceLocation FLAME_2 =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame2.png"
            );

    private static final ResourceLocation FLAME_3 =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame3.png"
            );

    private static final ResourceLocation FLAME_4 =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame4.png"
            );

    private static final ResourceLocation FLAME_5 =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame5.png"
            );

    private static final ResourceLocation FLAME_6 =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame6.png"
            );

    private PlayerHeatNameplateRenderer() {
    }

    @SubscribeEvent
    public static void onRenderNameTag(
            RenderNameTagEvent event
    ) {
        Entity entity =
        event.getEntity();

Minecraft minecraft =
        Minecraft.getInstance();

if (minecraft.player == null) {
    return;
}

int heat;

/*
 * NORMAL REAL PLAYER
 */
if (entity instanceof Player player) {

    /*
     * We normally don't render our own
     * overhead nametag.
     */
    if (player == minecraft.player) {
        return;
    }

    heat =
            ClientPlayerHeatData.getHeat(
                    player.getUUID()
            );
}

/*
 * DEVELOPMENT DUMMY
 *
 * Dummies are displayed using an ArmorStand,
 * so resolve that ArmorStand back to its
 * underlying FakePlayer.
 */
else if (entity instanceof ArmorStand armorStand) {

            FakePlayer dummy =
                    DummyPlayerManager.getDummyForVisual(
                            armorStand.getUUID()
                    );

            if (dummy == null) {
                return;
            }

            heat =
                    HeatManager.getEffectiveHeat(
                            dummy
                    );
        }

        /*
        * Nothing we're interested in.
        */
        else {
            return;
        }

        ResourceLocation texture =
                getHeatTexture(
                        heat
                );

        PoseStack poseStack =
                event.getPoseStack();

        MultiBufferSource bufferSource =
                event.getMultiBufferSource();

        Font font =
                minecraft.font;

        /*
        * Keep the formatted component so scoreboard prefixes
        * retain their team color while the username remains white.
        */
        Component name =
                event.getContent();

        int textWidth =
                font.width(
                        name
                );

        float nameplateScale =
                0.025F;

        float iconSize =
                8.0F;

        float iconGap =
                3.0F;

        /*
        * Treat the text and flame as one combined object
        * so the entire nameplate stays centered over the player.
        */
        float totalWidth =
                textWidth
                        + iconGap
                        + iconSize;

        float textX =
                -totalWidth / 2.0F;

        float textY =
                0.0F;

        float iconX =
                textX
                        + textWidth
                        + iconGap;

        float iconY =
                0.5F;

        poseStack.pushPose();

        /*
        * Move to the normal overhead nametag position.
        */
        poseStack.translate(
                0.0D,
                entity.getBbHeight()
                        + 0.5D,
                0.0D
        );

        /*
        * Always face the camera.
        */
        poseStack.mulPose(
                minecraft.getEntityRenderDispatcher()
                        .cameraOrientation()
        );

        poseStack.scale(
                -nameplateScale,
                -nameplateScale,
                nameplateScale
        );

        Matrix4f matrix =
                poseStack.last()
                        .pose();

        /*
        * Vanilla-style translucent background.
        *
        * Because the flame is part of our nameplate,
        * extend the background far enough to include it.
        */

        /*
        * Draw the formatted team prefix + username.
        */
        int backgroundOpacity =
                (int) (
                        minecraft.options
                                .getBackgroundOpacity(
                                        0.25F
                                )
                                * 255.0D
                );

        int backgroundColor =
                backgroundOpacity << 24;

        font.drawInBatch(
                name,
                textX,
                textY,
                0xFFFFFFFF,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                backgroundColor,
                event.getPackedLight()
        );

        /*
        * Heat flame.
        */
        renderFlame(
                poseStack,
                bufferSource,
                texture,
                iconX,
                iconY,
                iconSize,
                event.getPackedLight()
        );

        poseStack.popPose();

        /*
        * Stop vanilla from drawing a second nametag.
        */
        event.setResult(
                Event.Result.DENY
        );
    }

    private static void renderFlame(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation texture,
            float x,
            float y,
            float size,
            int packedLight
    ) {
        Matrix4f matrix =
                poseStack.last()
                        .pose();

        var consumer =
                bufferSource.getBuffer(
                        RenderType.entityCutoutNoCull(
                                texture
                        )
                );

        float z =
                -0.01F;

        /*
         * Top-left
         */
        consumer.vertex(
                        matrix,
                        x,
                        y,
                        z
                )
                .color(
                        255,
                        255,
                        255,
                        255
                )
                .uv(
                        0.0F,
                        0.0F
                )
                .overlayCoords(
                        OverlayTexture.NO_OVERLAY
                )
                .uv2(
                        packedLight
                )
                .normal(
                        0.0F,
                        0.0F,
                        1.0F
                )
                .endVertex();

        /*
         * Bottom-left
         */
        consumer.vertex(
                        matrix,
                        x,
                        y + size,
                        z
                )
                .color(
                        255,
                        255,
                        255,
                        255
                )
                .uv(
                        0.0F,
                        1.0F
                )
                .overlayCoords(
                        OverlayTexture.NO_OVERLAY
                )
                .uv2(
                        packedLight
                )
                .normal(
                        0.0F,
                        0.0F,
                        1.0F
                )
                .endVertex();

        /*
         * Bottom-right
         */
        consumer.vertex(
                        matrix,
                        x + size,
                        y + size,
                        z
                )
                .color(
                        255,
                        255,
                        255,
                        255
                )
                .uv(
                        1.0F,
                        1.0F
                )
                .overlayCoords(
                        OverlayTexture.NO_OVERLAY
                )
                .uv2(
                        packedLight
                )
                .normal(
                        0.0F,
                        0.0F,
                        1.0F
                )
                .endVertex();

        /*
         * Top-right
         */
        consumer.vertex(
                        matrix,
                        x + size,
                        y,
                        z
                )
                .color(
                        255,
                        255,
                        255,
                        255
                )
                .uv(
                        1.0F,
                        0.0F
                )
                .overlayCoords(
                        OverlayTexture.NO_OVERLAY
                )
                .uv2(
                        packedLight
                )
                .normal(
                        0.0F,
                        0.0F,
                        1.0F
                )
                .endVertex();
    }

    private static ResourceLocation getHeatTexture(
            int heat
    ) {
        if (heat < 8) {
            return FLAME_1;
        }

        if (heat < 16) {
            return FLAME_2;
        }

        if (heat < 25) {
            return FLAME_3;
        }

        if (heat < 35) {
            return FLAME_4;
        }

        if (heat < 50) {
            return FLAME_5;
        }

        return FLAME_6;
    }
}