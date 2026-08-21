package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;
import com.jushymaso222.theflood.elite.attributes.SpecialAttributeRegistry;
import com.jushymaso222.theflood.elite.attributes.StandardAttributes;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class EliteAttributes {

    private static final Random RANDOM =
            new Random();

    private static final double SPECIAL_ATTRIBUTE_CHANCE =
            0.12D;

    /*
     * Maximum number of UNIQUE attribute slots.
     *
     * Example:
     *
     * Swift++
     * Tough
     * Vampiric+
     *
     * = 3 slots.
     */
    private static final int MAX_ATTRIBUTE_SLOTS =
            3;

    /*
     * Maximum level of one attribute.
     *
     * 1 = normal
     * 2 = +
     * 3 = ++
     */
    private static final int MAX_ATTRIBUTE_LEVEL =
            3;

    private EliteAttributes() {
    }

    public record RolledAttribute(
            String id,
            int level
    ) {
    }

    /*
     * Build the complete available attribute pool.
     *
     * EliteAttributes itself does not know what the
     * individual attributes actually do.
     */
    private static List<String> getAttributePool() {
        List<String> pool =
                new ArrayList<>();

        pool.addAll(
                StandardAttributes.allIds()
        );

        pool.addAll(
                SpecialAttributeRegistry.allIds()
        );

        return pool;
    }

    private static String rollAttributeId() {
    List<String> standard =
            StandardAttributes.allIds();

    List<String> special =
            SpecialAttributeRegistry.allIds();

    /*
     * Specials are intentionally much rarer.
     */
    boolean rollSpecial =
            !special.isEmpty()
            && RANDOM.nextDouble()
                    < SPECIAL_ATTRIBUTE_CHANCE;

    if (rollSpecial) {
        return special.get(
                RANDOM.nextInt(
                        special.size()
                )
        );
    }

    if (!standard.isEmpty()) {
        return standard.get(
                RANDOM.nextInt(
                        standard.size()
                )
        );
    }

    /*
     * Fallback if somehow no Standards exist.
     */
    if (!special.isEmpty()) {
        return special.get(
                RANDOM.nextInt(
                        special.size()
                )
        );
    }

    return null;
}

    /*
     * Roll a fresh set of attributes for an Elite.
     *
     * Every Elite receives at least one roll.
     *
     * Heat increases the chance that rolling continues.
     */
    public static List<RolledAttribute> rollAttributes(
            int sourceHeat
    ) {
        Map<String, Integer> rolled =
                new LinkedHashMap<>();

        List<String> pool =
                getAttributePool();

        if (pool.isEmpty()) {
            return List.of();
        }

        /*
         * Every Elite ALWAYS gets its first attribute.
         */
        rollOne(
                rolled
        );

        /*
         * Continue rolling while RNG succeeds
         * and the Elite can still improve.
         */
        while (
                canRollAgain(
                        sourceHeat
                )
                && canStillImprove(
                        rolled
                )
        ) {
            rollOne(
                    rolled
            );
        }

        List<RolledAttribute> result =
                new ArrayList<>();

        for (
                Map.Entry<String, Integer> entry :
                rolled.entrySet()
        ) {
            result.add(
                    new RolledAttribute(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        return result;
    }

    /*
     * Perform one attribute roll.
     *
     * Rolling something already owned upgrades it:
     *
     * Swift
     *   ->
     * Swift+
     *   ->
     * Swift++
     */
    private static void rollOne(
            Map<String, Integer> rolled
    ) {
        /*
         * Once all three unique slots are occupied,
         * future successful rolls can only upgrade
         * existing attributes.
         */
        if (
                rolled.size()
                        >= MAX_ATTRIBUTE_SLOTS
        ) {
            upgradeExistingAttribute(
                    rolled
            );

            return;
        }

        String selected =
                rollAttributeId();

        if (selected == null) {
                return;
        }

        int currentLevel =
                rolled.getOrDefault(
                        selected,
                        0
                );

        if (currentLevel > 0) {

            if (
                    currentLevel
                            < MAX_ATTRIBUTE_LEVEL
            ) {
                rolled.put(
                        selected,
                        currentLevel + 1
                );

                return;
            }

            /*
             * Selected attribute is already ++.
             *
             * Try to find another valid result rather
             * than wasting the successful roll.
             */
            rerollOne(
                    rolled
            );

            return;
        }

        /*
         * Brand-new attribute.
         */
        rolled.put(
                selected,
                1
        );
    }

    /*
     * Upgrade one of the three occupied slots.
     */
    private static void upgradeExistingAttribute(
            Map<String, Integer> rolled
    ) {
        List<String> upgradeable =
                rolled.entrySet()
                        .stream()
                        .filter(
                                entry ->
                                        entry.getValue()
                                                < MAX_ATTRIBUTE_LEVEL
                        )
                        .map(
                                Map.Entry::getKey
                        )
                        .toList();

        if (upgradeable.isEmpty()) {
            return;
        }

        String selected =
                upgradeable.get(
                        RANDOM.nextInt(
                                upgradeable.size()
                        )
                );

        rolled.put(
                selected,
                rolled.get(
                        selected
                ) + 1
        );
    }

    /*
     * Used when RNG lands on an attribute that
     * has already reached ++.
     */
    private static void rerollOne(
            Map<String, Integer> rolled
    ) {
        if (
                !canStillImprove(
                        rolled
                )
        ) {
            return;
        }

        /*
         * Avoid an infinite loop caused by absurd RNG.
         */
        for (
                int attempt = 0;
                attempt < 32;
                attempt++
        ) {
            String selected =
                        rollAttributeId();

                if (selected == null) {
                        return;
                }

            int level =
                    rolled.getOrDefault(
                            selected,
                            0
                    );

            /*
             * Existing attribute that can be upgraded.
             */
            if (
                    level > 0
                    && level < MAX_ATTRIBUTE_LEVEL
            ) {
                rolled.put(
                        selected,
                        level + 1
                );

                return;
            }

            /*
             * Brand-new attribute and there is still
             * an available slot.
             */
            if (
                    level == 0
                    && rolled.size()
                            < MAX_ATTRIBUTE_SLOTS
            ) {
                rolled.put(
                        selected,
                        1
                );

                return;
            }
        }

        /*
         * Fallback:
         *
         * If random rerolls somehow failed repeatedly
         * while all slots are occupied, directly upgrade
         * something valid.
         */
        if (
                rolled.size()
                        >= MAX_ATTRIBUTE_SLOTS
        ) {
            upgradeExistingAttribute(
                    rolled
            );
        }
    }

    /*
     * Heat-driven continuation chance.
     *
     * Heat 0   = 10%
     * Heat 10  = 17%
     * Heat 20  = 24%
     * Heat 40  = 38%
     * Heat 60  = 52%
     * Heat 80  = 66%
     * Heat 100 = 80%
     *
     * TEMPORARY BALANCE VALUES.
     */
    private static boolean canRollAgain(
            int heat
    ) {
        double chance =
                getAdditionalRollChance(
                        heat
                );

        return RANDOM.nextDouble()
                < chance;
    }

    private static double getAdditionalRollChance(
            int heat
    ) {
        double chance =
                0.10D
                        + (
                        heat * 0.007D
                );

        return Math.min(
                0.80D,
                chance
        );
    }

    /*
     * Determine whether another successful roll
     * could possibly improve this Elite.
     */
    private static boolean canStillImprove(
            Map<String, Integer> rolled
    ) {
        /*
         * Fewer than three unique attributes means
         * another attribute can still be added.
         */
        if (
                rolled.size()
                        < MAX_ATTRIBUTE_SLOTS
        ) {
            return true;
        }

        /*
         * Three slots are occupied.
         *
         * Continue as long as something is below ++.
         */
        for (
                int level :
                rolled.values()
        ) {
            if (
                    level
                            < MAX_ATTRIBUTE_LEVEL
            ) {
                return true;
            }
        }

        /*
         * Absolute maximum:
         *
         * Attribute A++
         * Attribute B++
         * Attribute C++
         */
        return false;
    }

    /*
     * Apply a freshly rolled list directly.
     */
    public static void applyAll(
            Mob elite,
            List<RolledAttribute> attributes
    ) {
        for (
                RolledAttribute attribute :
                attributes
        ) {
            apply(
                    elite,
                    attribute.id(),
                    attribute.level()
            );
        }
    }

    /*
     * Apply the attributes already stored in EliteData.
     */
    public static void applyAll(
            Mob elite
    ) {
        applyAll(
                elite,
                deserialize(
                        EliteData.getAttributes(
                                elite
                        )
                )
        );
    }

    /*
     * Route an attribute to the system that owns it.
     *
     * EliteAttributes contains ZERO actual attribute
     * implementation.
     */
    public static void apply(
            Mob elite,
            String attribute,
            int level
    ) {
        if (
                StandardAttributes.contains(
                        attribute
                )
        ) {
            StandardAttributes.apply(
                    elite,
                    attribute,
                    level
            );

            return;
        }

        SpecialAttribute special =
                SpecialAttributeRegistry.get(
                        attribute
                );

        if (special != null) {
            special.onApplied(
                    elite,
                    level
            );
        }
    }

    /*
     * Convert command-friendly / stored values into
     * the canonical:
     *
     * attribute:level
     *
     * format.
     *
     * "swift"   -> "swift:1"
     * "swift:2" -> "swift:2"
     */
    public static List<String> normalizeStoredAttributes(
        List<String> attributes
) {
    List<String> result =
            new ArrayList<>();

    if (attributes == null) {
        return result;
    }

    for (String attribute : attributes) {
        if (
                attribute == null
                || attribute.isBlank()
        ) {
            continue;
        }

        String[] parts =
                attribute.split(
                        ":",
                        2
                );

        String id =
                parts[0]
                        .trim()
                        .toLowerCase();

        /*
         * Command supplied an attribute that
         * doesn't actually exist.
         *
         * Ignore it completely.
         */
        if (
                !isRegistered(
                        id
                )
        ) {
            continue;
        }

        int level =
                1;

        if (parts.length > 1) {
            try {
                level =
                        Integer.parseInt(
                                parts[1]
                        );
            } catch (NumberFormatException ignored) {
                level =
                        1;
            }
        }

        /*
         * Clamp command input to:
         *
         * Attribute
         * Attribute+
         * Attribute++
         */
        level =
                Math.max(
                        1,
                        Math.min(
                                MAX_ATTRIBUTE_LEVEL,
                                level
                        )
                );

        result.add(
                id
                        + ":"
                        + level
        );
    }

    return result;
}

    /*
     * Convert rolled attributes into persistent
     * EliteData strings.
     */
    public static List<String> serialize(
            List<RolledAttribute> attributes
    ) {
        List<String> result =
                new ArrayList<>();

        for (
                RolledAttribute attribute :
                attributes
        ) {
            result.add(
                    attribute.id()
                            + ":"
                            + attribute.level()
            );
        }

        return result;
    }

    /*
     * Convert persistent EliteData strings back into
     * strongly structured rolled attributes.
     */
    public static List<RolledAttribute> deserialize(
            List<String> stored
    ) {
        List<RolledAttribute> result =
                new ArrayList<>();

        for (String entry : stored) {

            if (
                    entry == null
                    || entry.isBlank()
            ) {
                continue;
            }

            String[] parts =
                    entry.split(
                            ":",
                            2
                    );

            String id =
                    parts[0];

            int level =
                    1;

            if (parts.length > 1) {
                try {
                    level =
                            Integer.parseInt(
                                    parts[1]
                            );
                } catch (NumberFormatException ignored) {
                    level =
                            1;
                }
            }

            level =
                    Math.max(
                            1,
                            Math.min(
                                    MAX_ATTRIBUTE_LEVEL,
                                    level
                            )
                    );

            result.add(
                    new RolledAttribute(
                            id,
                            level
                    )
            );
        }

        return result;
    }

    /*
     * =====================================================
     * SPECIAL ATTRIBUTE DISPATCH
     * =====================================================
     *
     * Everything below forwards Elite events to whatever
     * SpecialAttributes the entity actually owns.
     */

    public static void tickSpecialAttributes(
            Mob elite
    ) {
        for (
                RolledAttribute rolled :
                getStoredAttributes(
                        elite
                )
        ) {
            SpecialAttribute special =
                    SpecialAttributeRegistry.get(
                            rolled.id()
                    );

            if (special == null) {
                continue;
            }

            special.tick(
                    elite,
                    rolled.level()
            );
        }
    }

    public static float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        float modifiedDamage =
                damage;

        for (
                RolledAttribute rolled :
                getStoredAttributes(
                        elite
                )
        ) {
            SpecialAttribute special =
                    SpecialAttributeRegistry.get(
                            rolled.id()
                    );

            if (special == null) {
                continue;
            }

            modifiedDamage =
                    special.modifyIncomingDamage(
                            elite,
                            source,
                            modifiedDamage,
                            rolled.level()
                    );
        }

        return modifiedDamage;
    }

    public static float modifyOutgoingDamage(
            Mob elite,
            Player target,
            float damage
    ) {
        float modifiedDamage =
                damage;

        for (
                RolledAttribute rolled :
                getStoredAttributes(
                        elite
                )
        ) {
            SpecialAttribute special =
                    SpecialAttributeRegistry.get(
                            rolled.id()
                    );

            if (special == null) {
                continue;
            }

            modifiedDamage =
                    special.modifyOutgoingDamage(
                            elite,
                            target,
                            modifiedDamage,
                            rolled.level()
                    );
        }

        return modifiedDamage;
    }

    public static void onDamageDealt(
            Mob elite,
            Player target,
            float damageDealt
    ) {
        for (
                RolledAttribute rolled :
                getStoredAttributes(
                        elite
                )
        ) {
            SpecialAttribute special =
                    SpecialAttributeRegistry.get(
                            rolled.id()
                    );

            if (special == null) {
                continue;
            }

            special.onDamageDealt(
                    elite,
                    target,
                    damageDealt,
                    rolled.level()
            );
        }
    }

    public static void onDamaged(
            Mob elite,
            DamageSource source,
            float damageTaken
    ) {
        for (
                RolledAttribute rolled :
                getStoredAttributes(
                        elite
                )
        ) {
            SpecialAttribute special =
                    SpecialAttributeRegistry.get(
                            rolled.id()
                    );

            if (special == null) {
                continue;
            }

            special.onDamaged(
                    elite,
                    source,
                    damageTaken,
                    rolled.level()
            );
        }
    }

    /*
     * Shared helper for SpecialAttribute dispatch.
     */
    private static List<RolledAttribute> getStoredAttributes(
            Mob elite
    ) {
        return deserialize(
                EliteData.getAttributes(
                        elite
                )
        );
    }

    public static boolean isRegistered(
                String id
        ) {
        if (
                id == null
                || id.isBlank()
        ) {
                return false;
        }

        return StandardAttributes.contains(
                id
        )
                || SpecialAttributeRegistry.contains(
                        id
                );
        }

}