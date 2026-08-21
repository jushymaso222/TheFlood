package com.jushymaso222.theflood.elite.attributes;

import com.jushymaso222.theflood.elite.attributes.specials.RadioactiveAttribute;
import com.jushymaso222.theflood.elite.attributes.specials.VampiricAttribute;
import com.jushymaso222.theflood.elite.attributes.specials.VolatileAttribute;
import com.jushymaso222.theflood.elite.attributes.specials.StaticAttribute;
import com.jushymaso222.theflood.elite.attributes.specials.CorrosiveAttribute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpecialAttributeRegistry {

    private static final Map<String, SpecialAttribute> ATTRIBUTES =
            new LinkedHashMap<>();

    static {
        register(new VampiricAttribute());
        register(new RadioactiveAttribute());
        register(new VolatileAttribute());
        register(new StaticAttribute());
        register(new CorrosiveAttribute());
    }

    private SpecialAttributeRegistry() {
    }

    private static void register(
            SpecialAttribute attribute
    ) {
        ATTRIBUTES.put(
                attribute.id(),
                attribute
        );
    }

    public static SpecialAttribute get(
            String id
    ) {
        return ATTRIBUTES.get(
                id
        );
    }

    public static boolean contains(
            String id
    ) {
        return ATTRIBUTES.containsKey(
                id
        );
    }

    public static List<SpecialAttribute> all() {
        return new ArrayList<>(
                ATTRIBUTES.values()
        );
    }

    public static List<String> allIds() {
        return new ArrayList<>(
                ATTRIBUTES.keySet()
        );
    }
}