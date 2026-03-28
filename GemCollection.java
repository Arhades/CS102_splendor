import java.util.*;
/**
 * This represents a collection of gems and their quantities.
 * stores the no of gems for each color
 */

public class GemCollection {
    private Map<GemColor, Integer> gems;
    /**
     * Constructs an empty gem collection with all gem colors initialized to 0.
     */
    public GemCollection() {
        this.gems = new HashMap<>();
        this.gems.put(GemColor.DIAMOND, 0);
        this.gems.put(GemColor.SAPPHIRE, 0);
        this.gems.put(GemColor.EMERALD, 0);
        this.gems.put(GemColor.RUBY, 0);
        this.gems.put(GemColor.ONYX, 0);
        this.gems.put(GemColor.GOLD_JOKER, 0);
    }

    /**
     * Constructs a gem collection using the given map of gem counts.
     *
     * @param gems a map containing gem colors and their corresponding quantities
     */
    
    public GemCollection(Map<GemColor, Integer> gems) {
        this.gems = new HashMap<>();
        for (GemColor color : gems.keySet()) {
            this.gems.put(color, gems.get(color));
        }
    }
    /**
     * Returns the map of gem counts in this collection.
     *
     * @return the map storing gem colors and their quantities
     */

    public Map<GemColor, Integer> getGems() {
        return gems;
    }

    /**
     * Returns the number of gems of the specified color.
     * If the color is not present in the map, 0 is returned.
     *
     * @param color the gem color to check
     * @return the number of gems of the specified color, or 0 if absent
     */
    
    public int getCount(GemColor color) {
        return gems.get(color) == null? 0: gems.get(color);
    }

    
    /**
     * Returns the total number of gems in this collection.
     *
     * @return the sum of all gem counts in the collection
     */
    public int getTotalCount() {
        int sum = 0;
        for (Integer count: gems.values()) {
            sum += count;
        }
        return sum;
    }
    /**
     * Checks whether this gem collection contains no gems at all.
     *
     * @return true if all gem counts are 0, otherwise false
     */
    public boolean isEmpty() {
        for (Integer count: gems.values()) {
            if (count > 0) {
                return false;
            }
        }
        return true;
    }
    /**
     * Checks whether this gem collection contains no non-joker gems.
     * Gold jokers are ignored in this check.
     *
     * @return true if all non-joker gem counts are 0, otherwise false
     */
    public boolean isEmptyWithoutJoker() {
        for (GemColor color: gems.keySet()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (gems.get(color) > 0) {
                return false;
            }
        }
        return true;
    }
    /**
     * Adds the specified amount of a gem color to this collection.
     *
     * @param color the gem color to add
     * @param amount the number of gems to add
     */
    public void add(GemColor color, int amount) {
        gems.put(color, gems.get(color) + amount);
    }
    /**
     * Adds the contents of another gem collection to this collection.
     *
     * @param other the gem collection whose gem counts that are to be added
     */
    
    public void add(GemCollection other) {
        for (GemColor color: gems.keySet()) {
            gems.put(color, gems.get(color) + other.getCount(color));
        }
    }
    /**
     * Subtracts the specified amount of a gem color from this collection.
     *
     * @param color the gem color to subtract
     * @param amount the number of gems to subtract
     */
    public void subtract(GemColor color, int amount) {
        gems.put(color, gems.get(color) - amount);
    }

    /**
     * Subtracts the contents of another gem collection from this collection.
     *
     * @param other the gem collection whose gem counts are to be subtracted
     */

    public void subtract(GemCollection other) {
        for (GemColor color: other.getGems().keySet()) {
            gems.put(color, gems.getOrDefault(color, 0) - other.getCount(color));
        }
    }
    /**
     * Checks whether this collection contains at least as many gems as another collection
     * for every gem color in the other collection.
     *
     * @param other the gem collection to compare against
     * @return true if this collection contains all required gems, otherwise false
     */
    public boolean contains(GemCollection other) {
        for (GemColor color: other.getGems().keySet()) {
            if (gems.get(color) < other.getCount(color)) {
                return false;
            }
        }
        return true;
    }
    /**
     * Checks whether this collection can satisfy another collection's required gems
     * when gold jokers may be used to cover any shortage.
     *
     * @param other the gem collection representing the required gems
     * @return true if this collection can satisfy the requirement using jokers if needed,
     *         otherwise false
     */
    public boolean containsAfterJoker(GemCollection other) {
        int joker = gems.get(GemColor.GOLD_JOKER);
        for (GemColor color: other.getGems().keySet()) {
            if (gems.get(color) + joker < other.getCount(color)) {
                return false;
            } else if (gems.get(color) < other.getCount(color)) {
                joker -= (other.getCount(color) - gems.get(color));
            }
        }
        return true;
    }

        /**
     * Returns the number of gold jokers needed for this collection
     * to satisfy another collection's required gems.
     *
     * @param other the gem collection representing the required gems
     * @return the number of gold jokers required to make up the shortage
     */


    public int jokerNeeded(GemCollection other) {
        int joker = gems.get(GemColor.GOLD_JOKER);
        for (GemColor color: other.getGems().keySet()) {
            if (gems.get(color) < other.getCount(color)) {
                joker -= (other.getCount(color) - gems.get(color));
            }
        }
        return gems.get(GemColor.GOLD_JOKER) - joker;
    }
        /**
     * Returns a string representation of this gem collection.
     *
     * @return a string representation of the gem collection map
     */

    public String toString() {
        return gems.toString();
    }

    /**
     * Converts the current gem counts into a network-friendly string.
     * Example output: "DIAMOND-5,ONYX-5,EMERALD-4,RUBY-5,SAPPHIRE-5,GOLD-5"
     */
    public String getBankAsString() {
        // NOTE: Change these variable names if your class uses 
        // a Map or different integer names (like 'diamondCount')
        return "DIAMOND-" + GemColor.DIAMOND + "," +
               "ONYX-" + GemColor.ONYX + "," +
               "EMERALD-" + GemColor.EMERALD + "," +
               "RUBY-" + GemColor.RUBY + "," +
               "SAPPHIRE-" + GemColor.SAPPHIRE + "," +
               "GOLD_JOKER-" + GemColor.GOLD_JOKER; 
    }
}
