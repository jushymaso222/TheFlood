package com.jushymaso222.theflood;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(TheFlood.MOD_ID)
public class TheFlood {
    public static final String MOD_ID = "theflood";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TheFlood() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TheFloodConfig.SERVER_CONFIG);

        LOGGER.info("The Flood has loaded!");
    }
}