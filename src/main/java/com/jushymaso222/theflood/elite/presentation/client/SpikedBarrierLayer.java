package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SpikedBarrierLayer<
        T extends LivingEntity,
        M extends EntityModel<T>
        > extends RenderLayer<T, M> {

    /*
     * Geometry quality.
     *
     * This is deliberately low-poly enough to fit
     * Minecraft's visual style while still reading
     * clearly as a barrier.
     */
    private static final int LATITUDE_SEGMENTS =
            20;

    private static final int LONGITUDE_SEGMENTS =
            32;

    /*
     * Barrier transparency.
     */
    private static final float BASE_ALPHA =
            0.10F;

    private static final float PULSE_ALPHA =
            0.04F;

    /*
     * Subtle expansion/contraction.
     */
    private static final float PULSE_SPEED =
            0.06F;

    private static final float PULSE_SCALE =
            0.015F;

    /*
     * Untextured white texture.
     *
     * Use the same solid-white texture we made earlier
     * if you still have it.
     */
    private static final ResourceLocation WHITE_TEXTURE =
            new ResourceLocation(
                    "theflood",
                    "textures/entity/elite_glow.png"
            );


    public SpikedBarrierLayer(
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
                || !"spiked".equals(
                        state.mutationId()
                )
        ) {
            return;
        }

        /*
         * =================================================
         * COLOR
         * =================================================
         *
         * SpikedMutation already syncs BLUE_COLOR or
         * ORANGE_COLOR as statusColor.
         */

        int color =
                state.statusColor();

        float red =
                ((color >> 16) & 0xFF)
                        / 255.0F;

        float green =
                ((color >> 8) & 0xFF)
                        / 255.0F;

        float blue =
                (color & 0xFF)
                        / 255.0F;


        /*
         * =================================================
         * PULSE
         * =================================================
         */

        float rawPulse =
                (float) Math.sin(
                        ageInTicks
                                * PULSE_SPEED
                );

        float normalizedPulse =
                (
                        rawPulse
                                + 1.0F
                )
                        * 0.5F;

        float alpha =
                BASE_ALPHA
                        + normalizedPulse
                        * PULSE_ALPHA;

        float pulseScale =
                1.0F
                        + rawPulse
                        * PULSE_SCALE;


        SpikedBarrierDimensions.Dimensions dimensions =
                SpikedBarrierDimensions.get(
                        mob
                );

        if (dimensions == null) {
                return;
        }

        float radiusX =
                dimensions.radiusX()
                        * pulseScale;

        float radiusY =
                dimensions.radiusY()
                        * pulseScale;

        float radiusZ =
                dimensions.radiusZ()
                        * pulseScale;

        /*
        * =================================================
        * RENDER
        * =================================================
        */

        VertexConsumer buffer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucentEmissive(
                                WHITE_TEXTURE
                        )
                );

        poseStack.pushPose();

        /*
        * The LivingEntityRenderer has already transformed
        * us into the entity's model space.
        *
        * Do not translate by half the world-space bounding
        * box height here.
        */
        poseStack.translate(
                0.0D,
                dimensions.centerYOffset(),
                0.0D
        );

        renderEllipsoid(
                poseStack,
                buffer,
                radiusX,
                radiusY,
                radiusZ,
                red,
                green,
                blue,
                alpha,
                packedLight
        );

        poseStack.popPose();
    }


    private static void renderEllipsoid(
            PoseStack poseStack,
            VertexConsumer buffer,
            float radiusX,
            float radiusY,
            float radiusZ,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight
    ) {
        PoseStack.Pose pose =
                poseStack.last();

        Matrix4f matrix =
                pose.pose();

        Matrix3f normalMatrix =
                pose.normal();


        for (
                int latitude = 0;
                latitude < LATITUDE_SEGMENTS;
                latitude++
        ) {
            float theta1 =
                    (float) Math.PI
                            * latitude
                            / LATITUDE_SEGMENTS;

            float theta2 =
                    (float) Math.PI
                            * (latitude + 1)
                            / LATITUDE_SEGMENTS;


            for (
                    int longitude = 0;
                    longitude < LONGITUDE_SEGMENTS;
                    longitude++
            ) {
                float phi1 =
                        (float) (
                                Math.PI
                                        * 2.0D
                                        * longitude
                                        / LONGITUDE_SEGMENTS
                        );

                float phi2 =
                        (float) (
                                Math.PI
                                        * 2.0D
                                        * (longitude + 1)
                                        / LONGITUDE_SEGMENTS
                        );


                /*
                 * Four corners of one sphere quad.
                 */
                emitVertex(
                        buffer,
                        matrix,
                        normalMatrix,
                        theta1,
                        phi1,
                        radiusX,
                        radiusY,
                        radiusZ,
                        red,
                        green,
                        blue,
                        alpha,
                        packedLight
                );

                emitVertex(
                        buffer,
                        matrix,
                        normalMatrix,
                        theta2,
                        phi1,
                        radiusX,
                        radiusY,
                        radiusZ,
                        red,
                        green,
                        blue,
                        alpha,
                        packedLight
                );

                emitVertex(
                        buffer,
                        matrix,
                        normalMatrix,
                        theta2,
                        phi2,
                        radiusX,
                        radiusY,
                        radiusZ,
                        red,
                        green,
                        blue,
                        alpha,
                        packedLight
                );

                emitVertex(
                        buffer,
                        matrix,
                        normalMatrix,
                        theta1,
                        phi2,
                        radiusX,
                        radiusY,
                        radiusZ,
                        red,
                        green,
                        blue,
                        alpha,
                        packedLight
                );
            }
        }
    }


    private static void emitVertex(
            VertexConsumer buffer,
            Matrix4f matrix,
            Matrix3f normalMatrix,
            float theta,
            float phi,
            float radiusX,
            float radiusY,
            float radiusZ,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight
    ) {
        float sinTheta =
                (float) Math.sin(
                        theta
                );

        float cosTheta =
                (float) Math.cos(
                        theta
                );

        float cosPhi =
                (float) Math.cos(
                        phi
                );

        float sinPhi =
                (float) Math.sin(
                        phi
                );


        float normalX =
                sinTheta
                        * cosPhi;

        float normalY =
                cosTheta;

        float normalZ =
                sinTheta
                        * sinPhi;


        float x =
                normalX
                        * radiusX;

        float y =
                normalY
                        * radiusY;

        float z =
                normalZ
                        * radiusZ;


        /*
         * Basic spherical UV projection.
         *
         * Since we're using a solid white texture,
         * the actual UV values barely matter.
         */
        float u =
                phi
                        / (
                        (float) Math.PI
                                * 2.0F
                );

        float v =
                theta
                        / (float) Math.PI;


        buffer.vertex(
                        matrix,
                        x,
                        y,
                        z
                )
                .color(
                        red,
                        green,
                        blue,
                        alpha
                )
                .uv(
                        u,
                        v
                )
                .overlayCoords(
                        0
                )
                .uv2(
                        packedLight
                )
                .normal(
                        normalMatrix,
                        normalX,
                        normalY,
                        normalZ
                )
                .endVertex();
    }
}