package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class ShiftingPhaseLayer<
        T extends LivingEntity,
        M extends EntityModel<T>
        > extends RenderLayer<T, M> {

    private static final float BASE_SEPARATION =
            0.010F;

    private static final float DRIFT_AMOUNT =
            0.003F;

    private static final float PHASE_ALPHA =
            0.18F;

    public ShiftingPhaseLayer(
            RenderLayerParent<T, M> parent
    ) {
        super(
                parent
        );
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (
                !(entity instanceof Mob mob)
        ) {
            return;
        }

        ClientEliteStateData.EliteState state =
                ClientEliteStateData.getState(
                        mob.getId()
                );

        if (
                state == null
                || !"shifting".equals(
                        state.mutationId()
                )
        ) {
            return;
        }

        /*
         * Two mismatched sine waves keep the phase
         * displacement from looking perfectly periodic.
         */
        float drift =
                (float) Math.sin(
                        ageInTicks
                                * 0.11F
                )
                        * DRIFT_AMOUNT
                        +
                (float) Math.sin(
                        ageInTicks
                                * 0.037F
                )
                        * DRIFT_AMOUNT
                        * 0.5F;

        float separation =
                BASE_SEPARATION
                        + drift;

        ResourceLocation texture =
                getTextureLocation(
                        entity
                );

        /*
         * Red displaced copy.
         */
                renderPhaseCopy(
                poseStack,
                bufferSource,
                texture,
                packedLight,
                separation,
                separation * 0.35F,
                1.0F,
                0.05F,
                0.05F,
                PHASE_ALPHA
        );

        renderPhaseCopy(
                poseStack,
                bufferSource,
                texture,
                packedLight,
                -separation,
                -separation * 0.35F,
                0.05F,
                1.0F,
                1.0F,
                PHASE_ALPHA
        );
    }

    private void renderPhaseCopy(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation texture,
            int packedLight,
            float offsetX,
            float offsetZ,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        poseStack.pushPose();

        /*
         * Tiny lateral displacement only.
         *
         * No scaling — we don't want the old
         * "ghost shell" look.
         */
        poseStack.translate(
                offsetX,
                0.0D,
                offsetZ
        );

        VertexConsumer buffer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                texture
                        )
                );

        getParentModel()
                .renderToBuffer(
                        poseStack,
                        buffer,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        red,
                        green,
                        blue,
                        alpha
                );

        poseStack.popPose();
    }
}