package com.jushymaso222.theflood.elite.presentation.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class EliteEnergyParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    private final float baseScale;

    private final double swayOffset;

    protected EliteEnergyParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            SpriteSet sprites,
            float red,
            float green,
            float blue
    ) {
        super(
                level,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ
        );

        this.sprites =
                sprites;

        this.baseScale =
                0.12F
                        + level.random.nextFloat()
                        * 0.08F;

        this.swayOffset =
                level.random.nextDouble()
                        * Math.PI
                        * 2.0D;

        /*
         * Lifetime:
         *
         * Roughly 0.7 - 1.2 seconds.
         */
        this.lifetime =
                14
                        + level.random.nextInt(
                        11
                );

        /*
         * Slightly randomized size.
         */
        this.quadSize =
                baseScale;

        /*
         * Energy color.
         */
        this.rCol =
                red;

        this.gCol =
                green;

        this.bCol =
                blue;

        /*
         * Start mostly transparent.
         *
         * We'll fade it in and then back out.
         */
        this.alpha =
                0.0F;

        /*
         * Ignore gravity.
         */
        this.gravity =
                0.0F;

        /*
         * Small amount of drag.
         */
        this.friction =
                0.94F;

        /*
         * Spawn with the first sprite.
         */
        this.pickSprite(
                sprites
        );
    }


    @Override
    public void tick() {
        super.tick();

        if (removed) {
            return;
        }


        /*
         * =================================================
         * AGE PROGRESS
         * =================================================
         */

        float progress =
                (float) age
                        / (float) lifetime;


        /*
         * =================================================
         * FADE
         * =================================================
         *
         * Fade in quickly, then slowly disappear.
         */

        if (progress < 0.20F) {

            alpha =
                    progress
                            / 0.20F
                            * 0.55F;

        }
        else {

            float fadeProgress =
                    (progress - 0.20F)
                            / 0.80F;

            alpha =
                    0.55F
                            * (
                            1.0F
                                    - fadeProgress
                    );
        }


        /*
         * =================================================
         * SHRINK
         * =================================================
         */

        quadSize =
                baseScale
                        * (
                        1.0F
                                - progress
                                * 0.35F
                );


        /*
         * =================================================
         * UPWARD DRIFT
         * =================================================
         *
         * This keeps the energy gently rising away
         * from the mob.
         */

        yd +=
                0.0025D;


        /*
         * =================================================
         * SIDE-TO-SIDE CURL
         * =================================================
         *
         * Instead of moving in a perfectly straight line,
         * the particle subtly snakes through the air.
         */

        double sway =
                Math.sin(
                        age
                                * 0.35D
                                + swayOffset
                )
                        * 0.0025D;

        xd +=
                sway;

        zd +=
                Math.cos(
                        age
                                * 0.30D
                                + swayOffset
                )
                        * 0.0025D;


        /*
         * =================================================
         * ANIMATED SPRITE
         * =================================================
         *
         * If we eventually give the particle multiple
         * frames, this automatically progresses through
         * them.
         */

        setSpriteFromAge(
                sprites
        );
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}