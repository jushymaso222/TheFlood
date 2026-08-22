package com.jushymaso222.theflood.elite.presentation.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

public class EliteEnergyParticleOptions
        implements ParticleOptions {

    private final float red;
    private final float green;
    private final float blue;


    public EliteEnergyParticleOptions(
            float red,
            float green,
            float blue
    ) {
        this.red =
                red;

        this.green =
                green;

        this.blue =
                blue;
    }


    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public static final Codec<
        EliteEnergyParticleOptions
        > CODEC =
        Codec.FLOAT
                .listOf()
                .xmap(
                        values ->
                                new EliteEnergyParticleOptions(
                                        values.get(0),
                                        values.get(1),
                                        values.get(2)
                                ),
                        options ->
                                java.util.List.of(
                                        options.red(),
                                        options.green(),
                                        options.blue()
                                )
                );


    /*
     * =====================================================
     * DESERIALIZER
     * =====================================================
     */

    public static final ParticleOptions.Deserializer<
            EliteEnergyParticleOptions
            > DESERIALIZER =
            new ParticleOptions.Deserializer<>() {

                @Override
                public EliteEnergyParticleOptions fromCommand(
                        ParticleType<EliteEnergyParticleOptions> type,
                        StringReader reader
                ) throws CommandSyntaxException {

                    reader.expect(' ');

                    float red =
                            reader.readFloat();

                    reader.expect(' ');

                    float green =
                            reader.readFloat();

                    reader.expect(' ');

                    float blue =
                            reader.readFloat();

                    return new EliteEnergyParticleOptions(
                            red,
                            green,
                            blue
                    );
                }


                @Override
                public EliteEnergyParticleOptions fromNetwork(
                        ParticleType<EliteEnergyParticleOptions> type,
                        FriendlyByteBuf buffer
                ) {
                    return new EliteEnergyParticleOptions(
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat()
                    );
                }
            };


    /*
     * =====================================================
     * NETWORK SERIALIZATION
     * =====================================================
     */

    @Override
    public void writeToNetwork(
            FriendlyByteBuf buffer
    ) {
        buffer.writeFloat(
                red
        );

        buffer.writeFloat(
                green
        );

        buffer.writeFloat(
                blue
        );
    }


    /*
     * =====================================================
     * COMMAND STRING
     * =====================================================
     */

    @Override
    public String writeToString() {
        return String.format(
                Locale.ROOT,
                "%.3f %.3f %.3f",
                red,
                green,
                blue
        );
    }


    /*
     * =====================================================
     * PARTICLE TYPE
     * =====================================================
     *
     * We'll wire this to our registered ParticleType
     * next.
     */

    @Override
    public ParticleType<?> getType() {
        return EliteParticles.ELITE_ENERGY.get();
    }
}