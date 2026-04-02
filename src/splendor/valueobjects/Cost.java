package splendor.valueobjects;

import java.util.*;
import splendor.entity.*;

/**
 * Represents the gem cost required to obtain a card.
 *
 * This class stores the number of gems required for each gem color.
 * It also provides functionality to retrieve individual costs and
 * calculate the remaining cost after applying card bonuses.
 */

public class Cost{

    /**
     * The gem collection representing the cost.
     */
    private GemCollection cost;

    /**
     * Constructs a cost object from the given map of gem requirements.
     * 
     * @param cost a map where each gem color is associated with the number of gems required
     */
    public Cost(Map<GemColor, Integer> cost) {
        this.cost = new GemCollection(cost);
    }

    /**
     * Returns the number of gems required for the specified gem color.
     * 
     * @param color the gem color to check
     * @return the number of required gems of the specified color
     */
    public int getRequired(GemColor color) {
        return cost.getCount(color);
    }

    /**
     * Returns the full gem collection representing this cost.
     *
     * @return the gem collection for this cost
     */
    public GemCollection getCost() {
        return cost;
    }

    /**
     * Returns a new cost after applying the given bonuses.
     *
     * The bonuses reduce the required gem counts for matching colors.
     * The original cost object is not modified.
     *
     * @param bonus a map of gem colors to bonus amounts
     * @return a new Cost object representing the remaining cost after bonuses
     */
    public Cost afterBonuses(Map<GemColor, Integer> bonus) {
        GemCollection newCost = new GemCollection();
        newCost.add(cost);
        Map<GemColor, Integer> bonusCopy = new HashMap<>(bonus);
        bonusCopy.remove(GemColor.GOLD_JOKER);

        GemCollection bonusWithoutJoker = new GemCollection(bonusCopy);
        newCost.subtract(bonusWithoutJoker);
        for (GemColor color: newCost.getGems().keySet()) {
            if (newCost.getCount(color) < 0) {
                newCost.add(color, -1 * newCost.getCount(color));
            }
        }
        return new Cost(newCost.getGems());
    }
}
