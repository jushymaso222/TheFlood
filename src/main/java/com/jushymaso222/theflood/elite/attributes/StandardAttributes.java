package com.jushymaso222.theflood.elite.attributes;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.UUID;

public final class StandardAttributes {

    private static final List<String> ATTRIBUTE_IDS =
            List.of(
                    "swift",
                    "deadly",
                    "tough",
                    "resilient",

                    "stalwart",
                    "armored",
                    "brutal",
                    "tenacious",
                    "healthy",
                    "heavy",

                    "regenerative",
                    "fleet",
                    "berserk",
                    "executioner",
                    "vengeful",
                    "adrenalized",
                    "unyielding",
                    "fireproof",
                    "blastproof",
                    "deflecting"
            );

    /*
     * =====================================================
     * EXISTING ATTRIBUTE MODIFIERS
     * =====================================================
     */

    private static final UUID SWIFT_MODIFIER_ID =
            UUID.fromString(
                    "77e89260-03b1-4cb0-b8cf-9e911c015b1c"
            );

    private static final UUID DEADLY_MODIFIER_ID =
            UUID.fromString(
                    "cb8be29a-a336-45dc-b68e-b9407be10c97"
            );

    private static final UUID TOUGH_MODIFIER_ID =
            UUID.fromString(
                    "1fac732e-2dd8-4249-91ea-4bf51633d239"
            );

    private static final UUID RESILIENT_ARMOR_MODIFIER_ID =
            UUID.fromString(
                    "53d56979-a463-4801-957d-011594b4bc6d"
            );

    private static final UUID RESILIENT_KNOCKBACK_MODIFIER_ID =
            UUID.fromString(
                    "24be95ad-d9c7-47ea-b3ec-57ad18175541"
            );

    /*
     * =====================================================
     * NEW ATTRIBUTE MODIFIERS
     * =====================================================
     */

    private static final UUID STALWART_MODIFIER_ID =
            UUID.fromString(
                    "882c38ad-e954-4bb7-b895-d0ad22a6974a"
            );

    private static final UUID ARMORED_MODIFIER_ID =
            UUID.fromString(
                    "f5e77125-b492-4cc6-b67e-21ef781a117f"
            );

    private static final UUID BRUTAL_MODIFIER_ID =
            UUID.fromString(
                    "29a60214-9253-47e2-865f-2340943f1fe4"
            );

    private static final UUID TENACIOUS_MODIFIER_ID =
            UUID.fromString(
                    "5575d07e-156d-4e51-897a-fe469122f4b4"
            );

    private static final UUID HEALTHY_MODIFIER_ID =
            UUID.fromString(
                    "c29281de-545a-4a98-8c03-8ee35ccff26d"
            );

    private static final UUID HEAVY_HEALTH_MODIFIER_ID =
            UUID.fromString(
                    "894eca9e-9172-45cb-af3d-590e5893cf90"
            );

    private static final UUID HEAVY_ARMOR_MODIFIER_ID =
            UUID.fromString(
                    "f78505dc-ec2f-47aa-9c10-d5e488676005"
            );

    private static final UUID HEAVY_SPEED_MODIFIER_ID =
            UUID.fromString(
                    "422586dd-77d4-4cca-a6d6-a74de8215ee0"
            );

    private StandardAttributes() {
    }

    public static List<String> allIds() {
        return ATTRIBUTE_IDS;
    }

    public static boolean contains(
            String id
    ) {
        return ATTRIBUTE_IDS.contains(
                id
        );
    }

    public static void apply(
            Mob elite,
            String id,
            int level
    ) {
        switch (id) {

            case "swift" ->
                    applySwift(
                            elite,
                            level
                    );

            case "deadly" ->
                    applyDeadly(
                            elite,
                            level
                    );

            case "tough" ->
                    applyTough(
                            elite,
                            level
                    );

            case "resilient" ->
                    applyResilient(
                            elite,
                            level
                    );

            case "stalwart" ->
                    applyStalwart(
                            elite,
                            level
                    );

            case "armored" ->
                    applyArmored(
                            elite,
                            level
                    );

            case "brutal" ->
                    applyBrutal(
                            elite,
                            level
                    );

            case "tenacious" ->
                    applyTenacious(
                            elite,
                            level
                    );

            case "healthy" ->
                    applyHealthy(
                            elite,
                            level
                    );

            case "heavy" ->
                    applyHeavy(
                            elite,
                            level
                    );

            default -> {
            }
        }
    }

    /*
     * =====================================================
     * EXISTING ATTRIBUTES
     * =====================================================
     */

    private static void applySwift(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.35D;
                    case 3 -> 0.50D;
                    default -> 0.20D;
                };

        addModifier(
                elite,
                Attributes.MOVEMENT_SPEED,
                SWIFT_MODIFIER_ID,
                "The Flood Swift",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    private static void applyDeadly(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.35D;
                    case 3 -> 0.50D;
                    default -> 0.20D;
                };

        addModifier(
                elite,
                Attributes.ATTACK_DAMAGE,
                DEADLY_MODIFIER_ID,
                "The Flood Deadly",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    private static void applyTough(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.45D;
                    case 3 -> 0.70D;
                    default -> 0.25D;
                };

        addHealthModifier(
                elite,
                TOUGH_MODIFIER_ID,
                "The Flood Tough",
                bonus
        );
    }

    private static void applyResilient(
            Mob elite,
            int level
    ) {
        double armor =
                switch (level) {
                    case 2 -> 5.0D;
                    case 3 -> 8.0D;
                    default -> 3.0D;
                };

        double knockback =
                switch (level) {
                    case 2 -> 0.30D;
                    case 3 -> 0.45D;
                    default -> 0.15D;
                };

        addModifier(
                elite,
                Attributes.ARMOR,
                RESILIENT_ARMOR_MODIFIER_ID,
                "The Flood Resilient armor",
                armor,
                AttributeModifier.Operation.ADDITION
        );

        addModifier(
                elite,
                Attributes.KNOCKBACK_RESISTANCE,
                RESILIENT_KNOCKBACK_MODIFIER_ID,
                "The Flood Resilient knockback",
                knockback,
                AttributeModifier.Operation.ADDITION
        );
    }

    /*
     * =====================================================
     * NEW ATTRIBUTES
     * =====================================================
     */

    /*
     * STALWART
     *
     * Dedicated knockback resistance.
     *
     * Stalwart   = +20%
     * Stalwart+  = +35%
     * Stalwart++ = +50%
     */
    private static void applyStalwart(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.35D;
                    case 3 -> 0.50D;
                    default -> 0.20D;
                };

        addModifier(
                elite,
                Attributes.KNOCKBACK_RESISTANCE,
                STALWART_MODIFIER_ID,
                "The Flood Stalwart",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
    }

    /*
     * ARMORED
     *
     * Pure armor increase.
     *
     * Armored   = +3 armor
     * Armored+  = +6 armor
     * Armored++ = +10 armor
     */
    private static void applyArmored(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 6.0D;
                    case 3 -> 10.0D;
                    default -> 3.0D;
                };

        addModifier(
                elite,
                Attributes.ARMOR,
                ARMORED_MODIFIER_ID,
                "The Flood Armored",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
    }

    /*
     * BRUTAL
     *
     * Increased attack knockback.
     *
     * Brutal   = +0.5
     * Brutal+  = +1.0
     * Brutal++ = +1.5
     */
    private static void applyBrutal(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 1.0D;
                    case 3 -> 1.5D;
                    default -> 0.5D;
                };

        addModifier(
                elite,
                Attributes.ATTACK_KNOCKBACK,
                BRUTAL_MODIFIER_ID,
                "The Flood Brutal",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
    }

    /*
     * TENACIOUS
     *
     * Increased follow range.
     *
     * Tenacious   = +25%
     * Tenacious+  = +50%
     * Tenacious++ = +75%
     */
    private static void applyTenacious(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.50D;
                    case 3 -> 0.75D;
                    default -> 0.25D;
                };

        addModifier(
                elite,
                Attributes.FOLLOW_RANGE,
                TENACIOUS_MODIFIER_ID,
                "The Flood Tenacious",
                bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    /*
     * HEALTHY
     *
     * Smaller health increase than Tough.
     *
     * Healthy   = +15%
     * Healthy+  = +25%
     * Healthy++ = +40%
     */
    private static void applyHealthy(
            Mob elite,
            int level
    ) {
        double bonus =
                switch (level) {
                    case 2 -> 0.25D;
                    case 3 -> 0.40D;
                    default -> 0.15D;
                };

        addHealthModifier(
                elite,
                HEALTHY_MODIFIER_ID,
                "The Flood Healthy",
                bonus
        );
    }

    /*
     * HEAVY
     *
     * Health + armor in exchange for movement speed.
     *
     * Heavy
     *   +25% health
     *   +3 armor
     *   -10% speed
     *
     * Heavy+
     *   +40% health
     *   +5 armor
     *   -15% speed
     *
     * Heavy++
     *   +60% health
     *   +8 armor
     *   -20% speed
     */
    private static void applyHeavy(
            Mob elite,
            int level
    ) {
        double healthBonus =
                switch (level) {
                    case 2 -> 0.40D;
                    case 3 -> 0.60D;
                    default -> 0.25D;
                };

        double armorBonus =
                switch (level) {
                    case 2 -> 5.0D;
                    case 3 -> 8.0D;
                    default -> 3.0D;
                };

        double speedPenalty =
                switch (level) {
                    case 2 -> -0.15D;
                    case 3 -> -0.20D;
                    default -> -0.10D;
                };

        addHealthModifier(
                elite,
                HEAVY_HEALTH_MODIFIER_ID,
                "The Flood Heavy health",
                healthBonus
        );

        addModifier(
                elite,
                Attributes.ARMOR,
                HEAVY_ARMOR_MODIFIER_ID,
                "The Flood Heavy armor",
                armorBonus,
                AttributeModifier.Operation.ADDITION
        );

        addModifier(
                elite,
                Attributes.MOVEMENT_SPEED,
                HEAVY_SPEED_MODIFIER_ID,
                "The Flood Heavy speed",
                speedPenalty,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    /*
     * =====================================================
     * SHARED HELPERS
     * =====================================================
     */

    /*
     * Health modifiers get a special helper because
     * changing max health should preserve the Elite's
     * current health percentage.
     *
     * Example:
     *
     * 75 / 100 HP
     * + health modifier
     * 150 max HP
     *
     * becomes:
     * 112.5 / 150
     *
     * rather than remaining at 75 HP.
     */
    private static void addHealthModifier(
            Mob elite,
            UUID id,
            String name,
            double amount
    ) {
        AttributeInstance instance =
                elite.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (
                instance == null
                || instance.getModifier(
                        id
                ) != null
        ) {
            return;
        }

        float oldMaxHealth =
                elite.getMaxHealth();

        float healthPercent =
                oldMaxHealth > 0.0F
                        ? elite.getHealth()
                        / oldMaxHealth
                        : 1.0F;

        instance.addPermanentModifier(
                new AttributeModifier(
                        id,
                        name,
                        amount,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );

        elite.setHealth(
                Math.max(
                        1.0F,
                        elite.getMaxHealth()
                                * healthPercent
                )
        );
    }

    private static void addModifier(
            Mob elite,
            Attribute attribute,
            UUID id,
            String name,
            double amount,
            AttributeModifier.Operation operation
    ) {
        AttributeInstance instance =
                elite.getAttribute(
                        attribute
                );

        if (
                instance == null
                || instance.getModifier(
                        id
                ) != null
        ) {
            return;
        }

        instance.addPermanentModifier(
                new AttributeModifier(
                        id,
                        name,
                        amount,
                        operation
                )
        );
    }
}