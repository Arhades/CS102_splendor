package splendor.entity;

/**
 * Represents the different types of gem colors available in the game.
 * Includes standard gem colors and the special GOLD_JOKER which can act as a wildcard (can b any color).
 */
public enum GemColor {
    /**
     * Diamond gem color.
     */
    DIAMOND,

    /**
     * Sapphire gem color.
     */
    SAPPHIRE,

    /**
     * Emerald gem color.
     */
    EMERALD,

    /**
     * Ruby gem color.
     */
    RUBY,

    /**
     * Onyx gem color.
     */
    ONYX,

    /**
     * Gold joker gem that can substitute for any color.
     */
    GOLD_JOKER;

    /**
     * Converts a string representation to its corresponding GemColor enum value.
     *
     * @param color the string name of the gem color (e.g. "DIAMOND", "RUBY")
     * @return the matching GemColor, or SAPPHIRE as the default
     */
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
