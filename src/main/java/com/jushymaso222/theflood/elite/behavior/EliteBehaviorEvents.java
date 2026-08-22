package com.jushymaso222.theflood.elite.behavior;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.EliteData;
import com.jushymaso222.theflood.elite.presentation.ElitePoseController;

import net.minecraft.world.entity.Mob;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteBehaviorEvents {

    private EliteBehaviorEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(
            LivingEvent.LivingTickEvent event
    ) {
        if (
                !(event.getEntity() instanceof Mob mob)
        ) {
            return;
        }

        if (!EliteData.isElite(mob)) {
            return;
        }

        /*
         * Mutation behavior is authoritative server-side.
         */
        if (mob.level().isClientSide()) {
            return;
        }

        EliteBehaviorHooks.tick(
                mob
        );

        ElitePoseController.tick(
                mob
        );
    }
}