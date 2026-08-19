package com.jushymaso222.theflood.elite.behavior;

import com.jushymaso222.theflood.elite.behavior.mutations.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class EliteMutationRegistry {

    private static final Random RANDOM =
            new Random();

    private static final Map<String, EliteMutation> MUTATIONS =
            new LinkedHashMap<>();

    static {
        register(new BulwarkMutation());
        register(new PursuerMutation());
        register(new CommanderMutation());
        register(new FrenziedMutation());
        register(new SpikedMutation());
        register(new UndyingMutation());
        register(new InfestedMutation());
    }

    private EliteMutationRegistry() {
    }

    private static void register(
            EliteMutation mutation
    ) {
        MUTATIONS.put(
                mutation.id(),
                mutation
        );
    }

    public static EliteMutation get(
            String id
    ) {
        if (id == null) {
            return null;
        }

        return MUTATIONS.get(
                id.toLowerCase()
        );
    }

    public static EliteMutation random() {
        if (MUTATIONS.isEmpty()) {
            return null;
        }

        List<EliteMutation> available =
                new ArrayList<>(
                        MUTATIONS.values()
                );

        return available.get(
                RANDOM.nextInt(
                        available.size()
                )
        );
    }

    public static boolean exists(
            String id
    ) {
        return get(id) != null;
    }

    public static List<EliteMutation> getAll() {
        return List.copyOf(
                MUTATIONS.values()
        );
    }
}