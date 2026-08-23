package com.jushymaso222.theflood.elite.drops.boon;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BoonRegistry {

    private static final Map<String, Boon> BOONS =
            new LinkedHashMap<>();

    private BoonRegistry() {
    }

    public static void register(
            Boon boon
    ) {
        BOONS.put(
                boon.type()
                        .id(),
                boon
        );
    }

    public static Boon get(
            String id
    ) {
        return BOONS.get(
                id
        );
    }

    public static Collection<Boon> all() {
        return BOONS.values();
    }
}