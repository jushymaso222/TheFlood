package com.jushymaso222.theflood.elite.drops;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.EliteManager;

import net.minecraft.world.entity.Mob;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteDeathEvents {

    private EliteDeathEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(
            LivingDeathEvent event
    ) {
        if (
                !(event.getEntity() instanceof Mob mob)
        ) {
            return;
        }

        EliteManager.handleEliteDeath(
                mob,
                event.getSource()
        );
    }
}