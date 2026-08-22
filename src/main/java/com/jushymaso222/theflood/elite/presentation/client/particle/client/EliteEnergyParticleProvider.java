package com.jushymaso222.theflood.elite.presentation.particle.client;

import com.jushymaso222.theflood.elite.presentation.particle.EliteEnergyParticleOptions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class EliteEnergyParticleProvider
        implements ParticleProvider<EliteEnergyParticleOptions> {

    private final SpriteSet sprites;

    public EliteEnergyParticleProvider(
            SpriteSet sprites
    ) {
        this.sprites =
                sprites;
    }

    @Override
    public Particle createParticle(
            EliteEnergyParticleOptions options,
            ClientLevel level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ
    ) {
        return new EliteEnergyParticle(
                level,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ,
                sprites,
                options.red(),
                options.green(),
                options.blue()
        );
    }
}