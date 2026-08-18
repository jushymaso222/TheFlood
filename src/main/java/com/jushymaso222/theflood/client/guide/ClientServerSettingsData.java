package com.jushymaso222.theflood.client.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientServerSettingsData {

    private static final List<ServerSettingEntry> SETTINGS =
            new ArrayList<>();

    private static boolean received =
            false;

    private ClientServerSettingsData() {
    }

    public static void setSettings(
            List<ServerSettingEntry> settings
    ) {
        SETTINGS.clear();

        SETTINGS.addAll(
                settings
        );

        received =
                true;
    }

    public static List<ServerSettingEntry> getSettings() {
        return Collections.unmodifiableList(
                SETTINGS
        );
    }

    public static boolean hasReceivedSettings() {
        return received;
    }

    public static int getModifiedCount() {
        int count =
                0;

        for (
                ServerSettingEntry entry :
                SETTINGS
        ) {
            if (entry.isModified()) {
                count++;
            }
        }

        return count;
    }

    public static List<ServerSettingsPage> getPages() {

        List<ServerSettingsPage> pages =
                new ArrayList<>();

        /*
        * First page is a special overview.
        */
        pages.add(
                new ServerSettingsPage(
                        "Server Overview",
                        "Current Flood configuration",
                        List.of()
                )
        );

        /*
        * Preserve the order sent by the server.
        *
        * section -> group -> entries
        */
        Map<
                String,
                Map<
                        String,
                        List<ServerSettingEntry>
                >
        > grouped =
                new LinkedHashMap<>();

        for (
                ServerSettingEntry entry :
                SETTINGS
        ) {
            grouped
                    .computeIfAbsent(
                            entry.section(),
                            ignored ->
                                    new LinkedHashMap<>()
                    )
                    .computeIfAbsent(
                            entry.group(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            entry
                    );
        }

        for (
                Map.Entry<
                        String,
                        Map<
                                String,
                                List<ServerSettingEntry>
                        >
                > sectionEntry :
                grouped.entrySet()
        ) {
            String section =
                    sectionEntry.getKey();

            for (
                    Map.Entry<
                            String,
                            List<ServerSettingEntry>
                    > groupEntry :
                    sectionEntry.getValue()
                            .entrySet()
            ) {
                addGroupPages(
                        pages,
                        section,
                        groupEntry.getKey(),
                        groupEntry.getValue()
                );
            }
        }

        return pages;
    }

    private static void addGroupPages(
            List<ServerSettingsPage> pages,
            String section,
            String group,
            List<ServerSettingEntry> entries
    ) {

        /*
        * Mob pages have enough information that they should
        * always be divided into progression/combat pages.
        */
        if (
                section.equals(
                        "Mobs"
                )
        ) {
            addMobPages(
                    pages,
                    group,
                    entries
            );

            return;
        }

        if (
                section.equals("Spawning")
                && group.equals("Population")
        ) {
            addSplitPages(
                    pages,
                    section,
                    group,
                    entries,
                    4
            );

            return;
        }

        if (
                section.equals("Hordes")
                && group.equals("Mini-Hordes")
        ) {
            addSplitPages(
                    pages,
                    section,
                    group,
                    entries,
                    3
            );

            return;
        }

        /*
        * Everything else currently fits comfortably.
        */
        pages.add(
                new ServerSettingsPage(
                        group,
                        section,
                        entries
                )
        );
    }

    private static void addSplitPages(
            List<ServerSettingsPage> pages,
            String section,
            String group,
            List<ServerSettingEntry> entries,
            int entriesPerPage
    ) {

        int pageCount =
                (int) Math.ceil(
                        entries.size()
                                / (double) entriesPerPage
                );

        for (
                int pageIndex = 0;
                pageIndex < pageCount;
                pageIndex++
        ) {

            int fromIndex =
                    pageIndex
                            * entriesPerPage;

            int toIndex =
                    Math.min(
                            fromIndex
                                    + entriesPerPage,
                            entries.size()
                    );

            List<ServerSettingEntry> pageEntries =
                    new ArrayList<>(
                            entries.subList(
                                    fromIndex,
                                    toIndex
                            )
                    );

            String title =
                    pageCount > 1
                            ? group
                                    + " "
                                    + (pageIndex + 1)
                                    + "/"
                                    + pageCount
                            : group;

            pages.add(
                    new ServerSettingsPage(
                            title,
                            section,
                            pageEntries
                    )
            );
        }
    }

    private static void addMobPages(
            List<ServerSettingsPage> pages,
            String mobName,
            List<ServerSettingEntry> entries
    ) {

        List<ServerSettingEntry> progression =
                new ArrayList<>();

        List<ServerSettingEntry> combat =
                new ArrayList<>();

        for (
                ServerSettingEntry entry :
                entries
        ) {

            switch (
                    entry.name()
            ) {

                case "Unlock Heat",
                    "Base Spawn Weight",
                    "Health at Unlock",
                    "Health Growth" ->

                        progression.add(
                                entry
                        );

                default ->

                        combat.add(
                                entry
                        );
            }
        }

        if (!progression.isEmpty()) {

            pages.add(
                    new ServerSettingsPage(
                            mobName + " — Progression",
                            "Mobs",
                            progression
                    )
            );
        }

        if (!combat.isEmpty()) {

            pages.add(
                    new ServerSettingsPage(
                            mobName + " — Combat",
                            "Mobs",
                            combat
                    )
            );
        }
    }

    public static void clear() {
        SETTINGS.clear();

        received =
                false;
    }
}