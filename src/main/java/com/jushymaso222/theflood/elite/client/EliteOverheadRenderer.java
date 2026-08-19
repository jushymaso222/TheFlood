package com.jushymaso222.theflood.elite.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.elite.behavior.EliteMutationRegistry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Mob;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteOverheadRenderer {

    private static final int BAR_WIDTH =
            40;

    private static final int BAR_HEIGHT =
            4;

    private EliteOverheadRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLiving(
            RenderLivingEvent.Post<?, ?> event
    ) {
        if (
                !(event.getEntity() instanceof Mob mob)
        ) {
            return;
        }

        ClientEliteStateData.EliteState state =
                ClientEliteStateData.getState(
                        mob.getId()
                );

        if (state == null) {
            return;
        }

        renderEliteInfo(
                mob,
                state,
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }

    private static void renderEliteInfo(
            Mob mob,
            ClientEliteStateData.EliteState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        Font font =
                minecraft.font;

        poseStack.pushPose();

        /*
         * Position the entire Elite UI above
         * the mob's head.
         */
        poseStack.translate(
                0.0D,
                mob.getBbHeight()
                        + 0.55D,
                0.0D
        );

        /*
         * Billboard the UI toward the player's camera.
         */
        poseStack.mulPose(
                minecraft.getEntityRenderDispatcher()
                        .cameraOrientation()
        );

        poseStack.scale(
                -0.025F,
                -0.025F,
                0.025F
        );

        /*
         * Mutation / behavior name.
         */
        String behaviorName =
                getBehaviorDisplayName(
                        state.mutationId()
                );

        float textX =
                -font.width(
                        behaviorName
                ) / 2.0F;

        font.drawInBatch(
                behaviorName,
                textX,
                0.0F,
                0xFFFFD54F,
                false,
                poseStack.last()
                        .pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0x80000000,
                packedLight
        );

        /*
         * Generic mutation status bar.
         *
         * Any mutation can use this for whatever
         * mechanic it needs:
         *
         * Bulwark  -> stagger
         * Infested -> spawn timer
         * Undying  -> regeneration
         * etc.
         */
        if (
                state.statusActive()
                && state.statusMax() > 0.0F
        ) {
            renderStatusBar(
                    state,
                    poseStack,
                    12
            );
        }

        poseStack.popPose();
    }

    private static String getBehaviorDisplayName(
            String mutationId
    ) {
        EliteMutation mutation =
                EliteMutationRegistry.get(
                        mutationId
                );

        if (mutation == null) {
            return "ELITE";
        }

        return mutation.displayName()
                .toUpperCase();
    }

    private static void renderStatusBar(
            ClientEliteStateData.EliteState state,
            PoseStack poseStack,
            int y
    ) {
        float progress =
                state.statusProgress()
                        / state.statusMax();

        progress =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                progress
                        )
                );

        int left =
                -BAR_WIDTH / 2;

        int filledWidth =
                Math.round(
                        BAR_WIDTH
                                * progress
                );

        /*
         * Outer black border.
         */
        drawRect(
                poseStack,
                left - 1,
                y - 1,
                BAR_WIDTH + 2,
                BAR_HEIGHT + 2,
                0xCC000000
        );

        /*
         * Empty/dark portion of the bar.
         */
        drawRect(
                poseStack,
                left,
                y,
                BAR_WIDTH,
                BAR_HEIGHT,
                0xFF2A2A2A
        );

        /*
         * Mutation-defined fill color.
         */
        if (filledWidth > 0) {
            drawRect(
                    poseStack,
                    left,
                    y,
                    filledWidth,
                    BAR_HEIGHT,
                    state.statusColor()
            );
        }
    }

    private static void drawRect(
            PoseStack poseStack,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        float alpha =
                (float) (
                        color >> 24
                        & 255
                ) / 255.0F;

        float red =
                (float) (
                        color >> 16
                        & 255
                ) / 255.0F;

        float green =
                (float) (
                        color >> 8
                        & 255
                ) / 255.0F;

        float blue =
                (float) (
                        color
                        & 255
                ) / 255.0F;

        float x1 =
                x;

        float x2 =
                x + width;

        float y1 =
                y;

        float y2 =
                y + height;

        RenderSystem.enableBlend();

        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(
                GameRenderer::getPositionColorShader
        );

        Tesselator tesselator =
                Tesselator.getInstance();

        BufferBuilder builder =
                tesselator.getBuilder();

        builder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );

        builder.vertex(
                        poseStack.last()
                                .pose(),
                        x1,
                        y2,
                        0.0F
                )
                .color(
                        red,
                        green,
                        blue,
                        alpha
                )
                .endVertex();

        builder.vertex(
                        poseStack.last()
                                .pose(),
                        x2,
                        y2,
                        0.0F
                )
                .color(
                        red,
                        green,
                        blue,
                        alpha
                )
                .endVertex();

        builder.vertex(
                        poseStack.last()
                                .pose(),
                        x2,
                        y1,
                        0.0F
                )
                .color(
                        red,
                        green,
                        blue,
                        alpha
                )
                .endVertex();

        builder.vertex(
                        poseStack.last()
                                .pose(),
                        x1,
                        y1,
                        0.0F
                )
                .color(
                        red,
                        green,
                        blue,
                        alpha
                )
                .endVertex();

        tesselator.end();

        RenderSystem.disableBlend();
    }
}