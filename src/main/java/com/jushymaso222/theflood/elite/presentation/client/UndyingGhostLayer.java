package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class UndyingGhostLayer<T extends LivingEntity>
        extends RenderLayer<T, HumanoidModel<T>> {

    private static final float GHOST_ALPHA =
            0.28F;

    public UndyingGhostLayer(
            RenderLayerParent<T, HumanoidModel<T>> parent
    ) {
        super(parent);
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
                || !"undying".equals(
                        state.mutationId()
                )
                || !"UNDYING_DOWNED".equals(
                        state.poseId()
                )
        ) {
            return;
        }

        HumanoidModel<T> model =
                getParentModel();

        /*
         * Save scattered pose.
         */
        PartState head =
                PartState.capture(
                        model.head
                );

        PartState body =
                PartState.capture(
                        model.body
                );

        PartState leftArm =
                PartState.capture(
                        model.leftArm
                );

        PartState rightArm =
                PartState.capture(
                        model.rightArm
                );

        PartState leftLeg =
                PartState.capture(
                        model.leftLeg
                );

        PartState rightLeg =
                PartState.capture(
                        model.rightLeg
                );

        /*
         * Restore the model's normal pivots.
         *
         * We want the ghost standing where the entity's
         * hitbox actually is.
         */
        model.head.resetPose();
        model.body.resetPose();
        model.leftArm.resetPose();
        model.rightArm.resetPose();
        model.leftLeg.resetPose();
        model.rightLeg.resetPose();

        ResourceLocation texture =
                getTextureLocation(
                        entity
                );

        VertexConsumer buffer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                texture
                        )
                );

        model.renderToBuffer(
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0.65F,
                0.35F,
                1.0F,
                GHOST_ALPHA
        );

        /*
         * Restore the scattered corpse pose so we don't
         * affect the real model render state.
         */
        head.restore(model.head);
        body.restore(model.body);
        leftArm.restore(model.leftArm);
        rightArm.restore(model.rightArm);
        leftLeg.restore(model.leftLeg);
        rightLeg.restore(model.rightLeg);
    }

    private record PartState(
            float x,
            float y,
            float z,
            float xRot,
            float yRot,
            float zRot
    ) {

        private static PartState capture(
                net.minecraft.client.model.geom.ModelPart part
        ) {
            return new PartState(
                    part.x,
                    part.y,
                    part.z,
                    part.xRot,
                    part.yRot,
                    part.zRot
            );
        }

        private void restore(
                net.minecraft.client.model.geom.ModelPart part
        ) {
            part.x =
                    x;

            part.y =
                    y;

            part.z =
                    z;

            part.xRot =
                    xRot;

            part.yRot =
                    yRot;

            part.zRot =
                    zRot;
        }
    }
}