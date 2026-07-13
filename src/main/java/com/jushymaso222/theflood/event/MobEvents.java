package com.jushymaso222.theflood.event;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class MobEvents {

    @SubscribeEvent
    public static void onMobFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster monster)) return;
        if (!TheFloodConfig.SPAWNING.disableVanillaHostileSpawns.get()) return;

        MobSpawnType spawnType = event.getSpawnType();

        if (spawnType == MobSpawnType.COMMAND || spawnType == MobSpawnType.SPAWN_EGG) {
            return;
        }

        int day = getCurrentDay(event.getLevel().getLevel().getDayTime());

        if (!isMobUnlocked(monster, day)) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;

        if (event.getSource().is(DamageTypes.ON_FIRE)
                || event.getSource().is(DamageTypes.IN_FIRE)
                || event.getSource().is(DamageTypes.LAVA)) {
            event.setCanceled(true);
            event.getEntity().clearFire();
        }
    }

    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Monster monster)) return;

        if (monster.isOnFire()) {
            monster.clearFire();
        }
    }

    private static boolean isMobUnlocked(Monster monster, int day) {
        if (monster instanceof Zombie) {
            return day >= TheFloodConfig.MOBS.zombie.unlockDay.get();
        }

        if (monster instanceof Skeleton) {
            return day >= TheFloodConfig.MOBS.skeleton.unlockDay.get();
        }

        if (monster instanceof Spider) {
            return day >= TheFloodConfig.MOBS.spider.unlockDay.get();
        }

        return false;
    }

    private static int getCurrentDay(long dayTime) {
        return (int) (dayTime / 24000L) + 1;
    }
}