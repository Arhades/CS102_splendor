package splendor.entity;

/**
 * Represents the different types of gem colors available in the game.
 * Includes standard gem colors and the special GOLD_JOKER which can act as a wildcard (can b any color).
 */
public enum GemColor {
    DIAMOND,
    SAPPHIRE,
    EMERALD,
    RUBY,
    ONYX,
    GOLD_JOKER;

    public static GemColor convertToColor(String color) {
        switch (color) {
            case "DIAMOND":
                return GemColor.DIAMOND;
            case "ONYX":
                return GemColor.ONYX;
            case "EMERALD":
                return GemColor.EMERALD;
            case "RUBY":
                return GemColor.RUBY;
            default:
                return GemColor.SAPPHIRE;
        }
    }
}
