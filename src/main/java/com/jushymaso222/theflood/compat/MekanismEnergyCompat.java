package com.jushymaso222.theflood.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public final class MekanismEnergyCompat {

    private static boolean initialized = false;
    private static boolean available = false;

    private static Method getEnergyContainerMethod;
    private static Method floatingLongCreateMethod;
    private static Method extractMethod;
    private static Method getValueMethod;

    private static Method getEnergyMethod;
    private static Method getMaxEnergyMethod;

    private static Object actionExecute;
    private static Object automationManual;

    public record EnergyInfo(
            long stored,
            long capacity
    ) {
    }

    /*
    * Reads the current and maximum native Mekanism
    * energy stored in an ItemStack.
    *
    * Returns null when Mekanism isn't available or
    * the stack has no Mekanism energy container.
    */
    public static EnergyInfo getEnergyInfo(
            ItemStack stack
    ) {
        initialize();

        if (!available || stack.isEmpty()) {
            return null;
        }

        try {
            Object energyContainer =
                    getEnergyContainerMethod.invoke(
                            null,
                            stack,
                            0
                    );

            if (energyContainer == null) {
                return null;
            }

            Object storedEnergy =
                    getEnergyMethod.invoke(
                            energyContainer
                    );

            Object maxEnergy =
                    getMaxEnergyMethod.invoke(
                            energyContainer
                    );

            if (
                    storedEnergy == null
                    || maxEnergy == null
            ) {
                return null;
            }

            /*
            * Both values are FloatingLongs.
            */
            Object storedValue =
                    getValueMethod.invoke(
                            storedEnergy
                    );

            Object capacityValue =
                    getValueMethod.invoke(
                            maxEnergy
                    );

            if (
                    storedValue instanceof Long stored
                    && capacityValue instanceof Long capacity
            ) {
                return new EnergyInfo(
                        stored,
                        capacity
                );
            }

            return null;

        } catch (ReflectiveOperationException exception) {
            System.err.println(
                    "[The Flood] Failed to read MekaSuit energy."
            );

            exception.printStackTrace();

            return null;
        }
    }

    private MekanismEnergyCompat() {
    }

    /*
     * Mekanism remains completely optional.
     *
     * If Mekanism isn't installed, this class simply
     * reports unavailable and nothing special happens.
     */
    private static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        if (!ModList.get().isLoaded("mekanism")) {
            available = false;
            return;
        }

        try {
            Class<?> storageUtilsClass =
                    Class.forName(
                            "mekanism.common.util.StorageUtils"
                    );

            Class<?> energyContainerClass =
                    Class.forName(
                            "mekanism.api.energy.IEnergyContainer"
                    );

            Class<?> floatingLongClass =
                    Class.forName(
                            "mekanism.api.math.FloatingLong"
                    );

            Class<?> actionClass =
                    Class.forName(
                            "mekanism.api.Action"
                    );

            Class<?> automationTypeClass =
                    Class.forName(
                            "mekanism.api.AutomationType"
                    );

            /*
             * StorageUtils.getEnergyContainer(
             *     ItemStack,
             *     int
             * )
             */
            getEnergyContainerMethod =
                    storageUtilsClass.getMethod(
                            "getEnergyContainer",
                            ItemStack.class,
                            int.class
                    );
            /*
            * IEnergyContainer.getEnergy()
            * IEnergyContainer.getMaxEnergy()
            */
            getEnergyMethod =
                    energyContainerClass.getMethod(
                            "getEnergy"
                    );

            getMaxEnergyMethod =
                    energyContainerClass.getMethod(
                            "getMaxEnergy"
                    );

            /*
             * FloatingLong.create(long)
             */
            floatingLongCreateMethod =
                    floatingLongClass.getMethod(
                            "create",
                            long.class
                    );

            /*
             * IEnergyContainer.extract(
             *     FloatingLong,
             *     Action,
             *     AutomationType
             * )
             */
            extractMethod =
                    energyContainerClass.getMethod(
                            "extract",
                            floatingLongClass,
                            actionClass,
                            automationTypeClass
                    );

            /*
             * FloatingLong.getValue()
             *
             * Used to determine how much energy
             * Mekanism actually removed.
             */
            getValueMethod =
                    floatingLongClass.getMethod(
                            "getValue"
                    );

            /*
             * Equivalent to:
             *
             * Action.EXECUTE
             * AutomationType.MANUAL
             */
            @SuppressWarnings("unchecked")
            Class<? extends Enum> actionEnum =
                    (Class<? extends Enum>) actionClass;

            @SuppressWarnings("unchecked")
            Class<? extends Enum> automationEnum =
                    (Class<? extends Enum>) automationTypeClass;

            actionExecute =
                    Enum.valueOf(
                            actionEnum,
                            "EXECUTE"
                    );

            automationManual =
                    Enum.valueOf(
                            automationEnum,
                            "MANUAL"
                    );

            available = true;

        } catch (ReflectiveOperationException exception) {
            available = false;

            System.err.println(
                    "[The Flood] Failed to initialize optional Mekanism energy integration."
            );

            exception.printStackTrace();
        }
    }

    public static boolean isAvailable() {
        initialize();
        return available;
    }

    /*
     * Removes energy directly through Mekanism's
     * own IEnergyContainer API.
     *
     * Returns the amount actually extracted.
     */
    public static long extractEnergy(
            ItemStack stack,
            long amount
    ) {
        if (amount <= 0) {
            return 0;
        }

        initialize();

        if (!available) {
            return 0;
        }

        try {
            Object energyContainer =
                    getEnergyContainerMethod.invoke(
                            null,
                            stack,
                            0
                    );

            if (energyContainer == null) {
                return 0;
            }

            Object requestedEnergy =
                    floatingLongCreateMethod.invoke(
                            null,
                            amount
                    );

            Object extractedEnergy =
                    extractMethod.invoke(
                            energyContainer,
                            requestedEnergy,
                            actionExecute,
                            automationManual
                    );

            if (extractedEnergy == null) {
                return 0;
            }

            Object extractedValue =
                    getValueMethod.invoke(
                            extractedEnergy
                    );

            if (extractedValue instanceof Long value) {
                return value;
            }

            return 0;

        } catch (ReflectiveOperationException exception) {
            System.err.println(
                    "[The Flood] Failed to drain MekaSuit energy."
            );

            exception.printStackTrace();

            return 0;
        }
    }
}