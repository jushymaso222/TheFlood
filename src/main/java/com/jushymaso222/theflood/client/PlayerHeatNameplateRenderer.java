package com.jushymaso222.theflood.client;

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

        String name =
                event.getContent()
                        .getString();

        int textWidth =
                font.width(
                        name
                );

        /*
         * Nameplates are rendered using this tiny
         * world-space scale in vanilla.
         */
        float nameplateScale =
                0.025F;

        /*
         * Flame dimensions in "font pixels".
         *
         * 8x8 keeps it visually close to the text
         * without overpowering the nametag.
         */
        float iconSize =
                8.0F;

        /*
         * Position just after the right edge of
         * the player's rendered name.
         *
         * Nametag text is centered around X=0.
         */
        float iconX =
                (textWidth / 2.0F)
                        + 3.0F;

        float iconY =
                0.0F;

        poseStack.pushPose();

        /*
         * Recreate the same camera-facing transform
         * used by vanilla nametags.
         */
        double nameHeight;

        if (entity instanceof ArmorStand) {
            nameHeight =
                    entity.getBbHeight()
                            + 0.5D;
        } else {
            nameHeight =
                    entity.getBbHeight()
                            + 0.5D;
        }

        poseStack.translate(
                0.0D,
                nameHeight,
                0.0D
        );

        poseStack.mulPose(
                minecraft.getEntityRenderDispatcher()
                        .cameraOrientation()
        );

        poseStack.scale(
                -nameplateScale,
                -nameplateScale,
                nameplateScale
        );

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