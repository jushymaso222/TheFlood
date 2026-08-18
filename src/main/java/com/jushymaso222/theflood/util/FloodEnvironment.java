package com.jushymaso222.theflood.util;

import net.minecraftforge.fml.loading.FMLEnvironment;

public final class FloodEnvironment {

    private FloodEnvironment() {
    }

    public static boolean isDevelopment() {
        return !FMLEnvironment.production;
    }
}