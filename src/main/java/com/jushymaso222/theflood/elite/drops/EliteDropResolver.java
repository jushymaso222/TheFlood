package com.jushymaso222.theflood.elite.drops;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import com.jushymaso222.theflood.elite.drops.boon.BoonData;
import com.jushymaso222.theflood.elite.drops.boon.BoonType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EliteDropResolver {

    private EliteDropResolver() {
    }


    public static void resolve(
            EliteDropContext context
    ) {
        if (context == null) {
            return;
        }

        RandomSource random =
            context.elite()
                    .getRandom();

        EliteDropTable table =
                EliteDropTableLoader.get();

        if (table == null) {
            TheFlood.LOGGER.warn(
                    "Skipping Elite bonus drops because no valid drop table is loaded."
            );

            return;
        }

        if (
                table.getItems() == null
                || table.getItems().isEmpty()
        ) {
            TheFlood.LOGGER.warn(
                    "Skipping Elite bonus drops because the item pool is empty."
            );

            return;
        }

        Integer rollCount =
            rollWeightedNumber(
                    table.getRolls(),
                    "rolls",
                    random
            );

        boolean hoardersActive =
                context.killer() != null
                && BoonData.hasBoon(
                        context.killer(),
                        BoonType.HOARDERS
                );

        if (
                hoardersActive
                && rollCount != null
        ) {
            rollCount +=
                    2;
        }

        if (
                rollCount == null
                || rollCount <= 0
        ) {
            return;
        }

        List<EliteDropEntry> available =
                new ArrayList<>();

        for (
                EliteDropEntry entry :
                table.getItems()
        ) {
            if (
                    isValidEntry(
                            entry
                    )
                    && entry.isAvailableAtHeat(
                            context.sourceHeat()
                    )
                    && entry.getEffectiveWeight(
                            context.sourceHeat()
                    ) > 0.0D
            ) {
                available.add(
                        entry
                );
            }
        }

        if (available.isEmpty()) {
            TheFlood.LOGGER.warn(
                    "Skipping Elite bonus drops because no valid item entries remain."
            );

            return;
        }

        /*
         * We select without replacement.
         *
         * If the table asks for more unique rewards than
         * actually exist, just cap it to the pool size.
         */
        int selections =
                Math.min(
                        rollCount,
                        available.size()
                );

        for (
                int i = 0;
                i < selections;
                i++
        ) {
            EliteDropEntry selected =
                rollWeightedEntry(
                        available,
                        context.sourceHeat(),
                        random
                );

            if (selected == null) {
                break;
            }

            /*
             * Remove it immediately so the same item cannot
             * consume multiple reward slots.
             */
            available.remove(
                    selected
            );

            if (
                    isBoonEntry(
                            selected
                    )
            ) {
                available.removeIf(
                        EliteDropResolver::isBoonEntry
                );
            }

            ItemStack stack =
                    resolveRewardStack(
                            selected,
                            context,
                            random
                    );

            if (
                    stack == null
                    || stack.isEmpty()
            ) {
                continue;
            }

            int amount =
                    resolveAmount(
                            selected,
                            table,
                            random
                    );

            if (
                    hoardersActive
                    && amount > 0
                    && !isBoonEntry(
                            selected
                    )
            ) {
                if (
                        random.nextFloat()
                                < 0.35F
                ) {
                    amount++;
                }
            }

            amount =
                Math.min(
                        amount,
                        4
                );

            if (amount <= 0) {
                continue;
            }

            spawnDrop(
                    context,
                    stack,
                    amount
            );
        }
    }

    private static boolean isBoonEntry(
            EliteDropEntry entry
    ) {
        String itemId =
                entry.getItem();

        return itemId != null
                && itemId.startsWith(
                        "theflood:boon_"
                );
    }


    /*
     * =====================================================
     * ENTRY VALIDATION
     * =====================================================
     */

    private static boolean isValidEntry(
            EliteDropEntry entry
    ) {
        if (entry == null) {
            return false;
        }

        if (
                entry.getSourceCount()
                        != 1
        ) {
            TheFlood.LOGGER.warn(
                    "Ignoring Elite drop entry because exactly one of item, tag, or generator must be defined."
            );

            return false;
        }

        if (entry.getWeight() <= 0) {
            TheFlood.LOGGER.warn(
                    "Ignoring Elite drop entry '{}' because weight must be greater than zero.",
                    entry.getItem()
            );

            return false;
        }

        Integer min =
                entry.getMin();

        Integer max =
                entry.getMax();

        if (
                min != null
                && min <= 0
        ) {
            TheFlood.LOGGER.warn(
                    "Ignoring Elite drop entry '{}' because min must be greater than zero.",
                    entry.getItem()
            );

            return false;
        }

        if (
                max != null
                && max <= 0
        ) {
            TheFlood.LOGGER.warn(
                    "Ignoring Elite drop entry '{}' because max must be greater than zero.",
                    entry.getItem()
            );

            return false;
        }

        if (
                min != null
                && max != null
                && min > max
        ) {
            TheFlood.LOGGER.warn(
                    "Ignoring Elite drop entry '{}' because min ({}) is greater than max ({}).",
                    entry.getItem(),
                    min,
                    max
            );

            return false;
        }

        return true;
    }


    /*
     * =====================================================
     * WEIGHTED ENTRY SELECTION
     * =====================================================
     */

    private static EliteDropEntry rollWeightedEntry(
        List<EliteDropEntry> entries,
        int heat,
        RandomSource random
) {
    double totalWeight =
            0.0D;

    /*
     * Add up the heat-adjusted weights of every
     * currently available reward.
     */
    for (
            EliteDropEntry entry :
            entries
    ) {
        if (entry == null) {
            continue;
        }

        double effectiveWeight =
                entry.getEffectiveWeight(
                        heat
                );

        if (effectiveWeight <= 0.0D) {
            continue;
        }

        totalWeight +=
                effectiveWeight;
    }

    if (totalWeight <= 0.0D) {
        return null;
    }

    /*
     * Roll anywhere inside the total weighted range.
     */
    double roll =
            random.nextDouble()
                    * totalWeight;

    double current =
            0.0D;

    for (
            EliteDropEntry entry :
            entries
    ) {
        if (entry == null) {
            continue;
        }

        double effectiveWeight =
                entry.getEffectiveWeight(
                        heat
                );

        if (effectiveWeight <= 0.0D) {
            continue;
        }

        current +=
                effectiveWeight;

        if (roll < current) {
            return entry;
        }
    }

    return null;
}


    /*
     * =====================================================
     * WEIGHTED INTEGER SELECTION
     * =====================================================
     *
     * Used for:
     *
     * rolls:
     * 5 -> 70
     * 6 -> 20
     *
     * amounts:
     * 1 -> 85
     * 2 -> 12
     */

    private static Integer rollWeightedNumber(
            Map<String, Integer> values,
            String tableName,
            RandomSource random
    ) {
        if (
                values == null
                || values.isEmpty()
        ) {
            TheFlood.LOGGER.warn(
                    "Elite drop table '{}' section is missing or empty.",
                    tableName
            );

            return null;
        }

        long totalWeight =
                0L;

        List<WeightedNumber> valid =
                new ArrayList<>();

        for (
                Map.Entry<String, Integer> entry :
                values.entrySet()
        ) {
            int value;

            try {
                value =
                        Integer.parseInt(
                                entry.getKey()
                        );
            }
            catch (NumberFormatException exception) {
                TheFlood.LOGGER.warn(
                        "Ignoring invalid value '{}' inside Elite drop table section '{}'.",
                        entry.getKey(),
                        tableName
                );

                continue;
            }

            Integer weight =
                    entry.getValue();

            if (
                    value <= 0
                    || weight == null
                    || weight <= 0
            ) {
                TheFlood.LOGGER.warn(
                        "Ignoring invalid Elite drop table entry '{}: {}' inside '{}'.",
                        entry.getKey(),
                        weight,
                        tableName
                );

                continue;
            }

            valid.add(
                    new WeightedNumber(
                            value,
                            weight
                    )
            );

            totalWeight +=
                    weight;
        }

        if (
                valid.isEmpty()
                || totalWeight <= 0L
        ) {
            TheFlood.LOGGER.warn(
                    "Elite drop table section '{}' contains no usable weighted values.",
                    tableName
            );

            return null;
        }

        long roll =
            Math.floorMod(
                    random.nextLong(),
                    totalWeight
            );

        long current =
                0L;

        for (
                WeightedNumber option :
                valid
        ) {
            current +=
                    option.weight();

            if (roll < current) {
                return option.value();
            }
        }

        return null;
    }


    /*
     * =====================================================
     * ITEM RESOLUTION
     * =====================================================
     */

    private static String describeEntry(
        EliteDropEntry entry
) {
    if (
            entry.getItem() != null
            && !entry.getItem().isBlank()
    ) {
        return "item:"
                + entry.getItem();
    }

    if (
            entry.getTag() != null
            && !entry.getTag().isBlank()
    ) {
        return "tag:"
                + entry.getTag();
    }

    if (
            entry.getGenerator() != null
            && !entry.getGenerator().isBlank()
    ) {
        return "generator:"
                + entry.getGenerator();
    }

    return "unknown";
}


    /*
     * =====================================================
     * QUANTITY
     * =====================================================
     */

    private static int resolveAmount(
            EliteDropEntry entry,
            EliteDropTable table,
            RandomSource random
    ) {
        if (
                isBoonEntry(
                        entry
                )
        ) {
            return 1;
        }

        Integer min =
                entry.getMin();

        Integer max =
                entry.getMax();

        /*
         * Entry-specific quantity overrides the global table.
         */
        if (
                min != null
                || max != null
        ) {
            int resolvedMin =
                    min != null
                            ? min
                            : max;

            int resolvedMax =
                    max != null
                            ? max
                            : min;

            if (
                    resolvedMin <= 0
                    || resolvedMax <= 0
                    || resolvedMin > resolvedMax
            ) {
                return 1;
            }

            if (resolvedMin == resolvedMax) {
                return resolvedMin;
            }

            return resolvedMin
                + random.nextInt(
                        resolvedMax
                                - resolvedMin
                                + 1
                );
        }

        Integer amount =
            rollWeightedNumber(
                    table.getAmounts(),
                    "amounts",
                    random
            );

        if (
                amount == null
                || amount <= 0
        ) {
            /*
             * Safe fallback:
             *
             * malformed amount distribution should never
             * delete an otherwise valid reward.
             */
            return 1;
        }

        return amount;
    }


    /*
     * =====================================================
     * ACTUAL DROP
     * =====================================================
     */

    private static void spawnDrop(
        EliteDropContext context,
        ItemStack baseStack,
        int amount
) {
    if (
            baseStack == null
            || baseStack.isEmpty()
            || amount <= 0
    ) {
        return;
    }

    int remaining =
            amount;

    int maxStackSize =
            baseStack.getMaxStackSize();

    while (remaining > 0) {
        int stackAmount =
                Math.min(
                        remaining,
                        maxStackSize
                );

        /*
         * copy() is IMPORTANT.
         *
         * Generated rewards can contain enchantments,
         * potion data, names, etc.
         */
        ItemStack stack =
                baseStack.copy();

        stack.setCount(
                stackAmount
        );

        ItemEntity drop =
                new ItemEntity(
                        context.level(),
                        context.elite()
                                .getX(),
                        context.elite()
                                .getY()
                                + 0.25D,
                        context.elite()
                                .getZ(),
                        stack
                );

        drop.setDefaultPickUpDelay();

        context.level()
                .addFreshEntity(
                        drop
                );

        remaining -=
                stackAmount;
    }
}

    private static ItemStack resolveRewardStack(
        EliteDropEntry entry,
        EliteDropContext context,
        RandomSource random
) {
    /*
     * =====================================================
     * EXACT ITEM
     * =====================================================
     */
    if (
            entry.getItem() != null
            && !entry.getItem().isBlank()
    ) {
        return resolveExactItem(
                entry.getItem()
        );
    }


    /*
     * =====================================================
     * TAG
     * =====================================================
     */
    if (
            entry.getTag() != null
            && !entry.getTag().isBlank()
    ) {
        return resolveTagItem(
                entry.getTag(),
                random
        );
    }


    /*
     * =====================================================
     * GENERATOR
     * =====================================================
     */
    if (
            entry.getGenerator() != null
            && !entry.getGenerator().isBlank()
    ) {
        return resolveGeneratedItem(
                entry.getGenerator(),
                context,
                random
        );
    }


    return ItemStack.EMPTY;
}

    private static ItemStack resolveExactItem(
        String itemId
) {
    ResourceLocation id;

    try {
        id =
                new ResourceLocation(
                        itemId
                );
    }
    catch (Exception exception) {
        TheFlood.LOGGER.warn(
                "Ignoring malformed Elite drop item ID '{}'.",
                itemId
        );

        return ItemStack.EMPTY;
    }

    Item item =
            BuiltInRegistries.ITEM.get(
                    id
            );

    if (item == Items.AIR) {
        TheFlood.LOGGER.warn(
                "Ignoring unknown Elite drop item '{}'.",
                itemId
        );

        return ItemStack.EMPTY;
    }

    return new ItemStack(
            item
    );
}

    private static ItemStack resolveTagItem(
        String tagId,
        RandomSource random
) {
    ResourceLocation id;

    try {
        id =
                new ResourceLocation(
                        tagId
                );
    }
    catch (Exception exception) {
        TheFlood.LOGGER.warn(
                "Ignoring malformed Elite drop tag '{}'.",
                tagId
        );

        return ItemStack.EMPTY;
    }

    TagKey<Item> tag =
            TagKey.create(
                    Registries.ITEM,
                    id
            );

    var holders =
            BuiltInRegistries.ITEM
                    .getTag(
                            tag
                    );

    if (
            holders.isEmpty()
            || holders.get()
                    .size()
                    <= 0
    ) {
        TheFlood.LOGGER.warn(
                "Elite drop tag '{}' does not exist or contains no items.",
                tagId
        );

        return ItemStack.EMPTY;
    }

    List<Holder<Item>> possibilities =
            holders.get()
                    .stream()
                    .toList();

    Holder<Item> selected =
            possibilities.get(
                    random.nextInt(
                            possibilities.size()
                    )
            );

    return new ItemStack(
            selected.value()
    );
}

    private static ItemStack resolveGeneratedItem(
        String generator,
        EliteDropContext context,
        RandomSource random
) {
    return switch (generator) {

        case "enchanted_book" ->
                generateEnchantedBook(
                        context,
                        random
                );

        case "potion" ->
                generatePotion(
                        context,
                        random
                );

        case "smithing_template" ->
                generateSmithingTemplate(
                        context,
                        random
                );

        default -> {
            TheFlood.LOGGER.warn(
                    "Unknown Elite drop generator '{}'.",
                    generator
            );

            yield ItemStack.EMPTY;
        }
    };
}

private static ItemStack generateEnchantedBook(
        EliteDropContext context,
        RandomSource random
) {
    ItemStack book =
            new ItemStack(
                    Items.BOOK
            );

    int heat =
            context.sourceHeat();

    /*
     * Heat controls enchantment strength.
     *
     * Early:
     * roughly low-level table enchantments
     *
     * Late:
     * significantly stronger rolls
     */
    int enchantmentLevel =
            5
                    + Math.min(
                    25,
                    heat / 3
            );

    return EnchantmentHelper.enchantItem(
            random,
            book,
            enchantmentLevel,
            true
    );
}

private static ItemStack generatePotion(
        EliteDropContext context,
        RandomSource random
) {
    int heat =
            context.sourceHeat();

    List<Potion> choices =
            new ArrayList<>();

    /*
     * Useful at basically any point.
     */
    choices.add(
            Potions.HEALING
    );

    choices.add(
            Potions.SWIFTNESS
    );

    choices.add(
            Potions.STRENGTH
    );

    choices.add(
            Potions.FIRE_RESISTANCE
    );

    /*
     * Better variants become possible later.
     */
    if (heat >= 25) {
        choices.add(
                Potions.STRONG_HEALING
        );

        choices.add(
                Potions.LONG_SWIFTNESS
        );

        choices.add(
                Potions.LONG_FIRE_RESISTANCE
        );
    }

    if (heat >= 45) {
        choices.add(
                Potions.STRONG_STRENGTH
        );

        choices.add(
                Potions.STRONG_SWIFTNESS
        );

        choices.add(
                Potions.REGENERATION
        );
    }

    if (heat >= 70) {
        choices.add(
                Potions.STRONG_REGENERATION
        );

        choices.add(
                Potions.LONG_REGENERATION
        );
    }

    if (choices.isEmpty()) {
        return ItemStack.EMPTY;
    }

    Potion selected =
            choices.get(
                    random.nextInt(
                            choices.size()
                    )
            );

    /*
     * Mostly normal potions.
     *
     * Occasionally make them splash potions.
     */
    ItemStack stack =
            new ItemStack(
                    random.nextFloat() < 0.20F
                            ? Items.SPLASH_POTION
                            : Items.POTION
            );

    PotionUtils.setPotion(
            stack,
            selected
    );

    return stack;
}

private static ItemStack generateSmithingTemplate(
        EliteDropContext context,
        RandomSource random
) {
    int heat =
            context.sourceHeat();

    List<Item> choices =
            new ArrayList<>();

    /*
     * Overworld-accessible trims.
     */
    choices.add(
            Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE
    );

    choices.add(
            Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE
    );

    choices.add(
            Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE
    );

    choices.add(
            Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE
    );

    choices.add(
            Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE
    );

    /*
     * Nether progression.
     */
    if (heat >= 35) {
        choices.add(
                Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE
        );

        choices.add(
                Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE
        );
    }

    /*
     * Netherite upgrade should remain substantially later.
     */
    if (heat >= 45) {
        choices.add(
                Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE
        );
    }

    /*
     * End progression.
     */
    if (heat >= 70) {
        choices.add(
                Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE
        );
    }

    if (choices.isEmpty()) {
        return ItemStack.EMPTY;
    }

    Item selected =
            choices.get(
                    random.nextInt(
                            choices.size()
                    )
            );

    return new ItemStack(
            selected
    );
}


    private record WeightedNumber(
            int value,
            int weight
    ) {
    }
}