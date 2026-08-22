package com.jushymaso222.theflood.elite.presentation.particle.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.presentation.particle.EliteParticles;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class EliteParticleClientEvents {

    private EliteParticleClientEvents() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(
            RegisterParticleProvidersEvent event
    ) {
        event.registerSpriteSet(
                EliteParticles.ELITE_ENERGY.get(),
                EliteEnergyParticleProvider::new
        );
    }
}