package com.jushymaso222.theflood.debug.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ClientDummyPlayerData {

    public record Dummy(
            UUID id,
            String name
    ) {}

    private static final List<Dummy> DUMMIES =
            new ArrayList<>();

    private ClientDummyPlayerData() {}

    public static void setDummies(
            List<Dummy> dummies
    ) {
        DUMMIES.clear();
        DUMMIES.addAll(dummies);
    }

    public static List<Dummy> getDummies() {
        return Collections.unmodifiableList(
                DUMMIES
        );
    }

    public static void clear() {
        DUMMIES.clear();
    }
}