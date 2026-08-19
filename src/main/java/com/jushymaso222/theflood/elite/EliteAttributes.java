public final class EliteAttributes {

    private EliteAttributes() {
    }

    public static void apply(
            Mob elite,
            String attribute
    ) {
        switch (attribute) {

            case "swift" ->
                    applySwift(elite);

            case "deadly" ->
                    applyDeadly(elite);

            case "tough" ->
                    applyTough(elite);

            case "resilient" ->
                    applyResilient(elite);

            case "giant" ->
                    applyGiant(elite);

            default -> {
            }
        }
    }

    private static void applySwift(
            Mob elite
    ) {
        // Movement speed increase.
    }

    private static void applyDeadly(
            Mob elite
    ) {
        // Attack damage increase.
    }

    private static void applyTough(
            Mob elite
    ) {
        // Maximum health increase.
    }

    private static void applyResilient(
            Mob elite
    ) {
        // Armor / knockback resistance.
    }
}