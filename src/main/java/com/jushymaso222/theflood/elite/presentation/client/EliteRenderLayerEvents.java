package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class EliteRenderLayerEvents {

    private EliteRenderLayerEvents() {
    }

    @SubscribeEvent
    public static void addLayers(
            EntityRenderersEvent.AddLayers event
    ) {
        addLayer(
                event,
                EntityType.ZOMBIE
        );

        addLayer(
                event,
                EntityType.SKELETON
        );

        addLayer(
                event,
                EntityType.ENDERMAN
        );

        addLayer(
                event,
                EntityType.WARDEN
        );
    }


    private static <
            T extends LivingEntity,
            M extends EntityModel<T>
            > void addLayer(
            EntityRenderersEvent.AddLayers event,
            EntityType<T> type
    ) {
        LivingEntityRenderer<T, M> renderer =
                getLivingRenderer(
                        event,
                        type
                );

        if (renderer == null) {
            return;
        }

        // renderer.addLayer(
        //         new EliteGlowLayer<T, M>(
        //                 renderer
        //         )
        // );
    }


    @SuppressWarnings("unchecked")
    private static <
            T extends LivingEntity,
            M extends EntityModel<T>
            > LivingEntityRenderer<T, M> getLivingRenderer(
            EntityRenderersEvent.AddLayers event,
            EntityType<T> type
    ) {
        return (LivingEntityRenderer<T, M>)
                event.getRenderer(
                        type
                );
    }
}