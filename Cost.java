import java.util.*;

public class Cost{
    private GemCollection cost;

    public Cost(Map<GemColor, Integer> cost) {
        this.cost = new GemCollection(cost);
    }

    public int getRequired(GemColor color) {
        return cost.getCount(color);
    }

    public GemCollection getCost() {
        return cost;
    }

    public Cost afterBonuses(Map<GemColor, Integer> bonus) {
        GemCollection newCost = new GemCollection(cost.getGems());
        GemCollection bonusWithoutJoker = new GemCollection(bonus);
        bonus.remove(GemColor.GOLD_JOKER);
        newCost.subtract(bonusWithoutJoker);
        for (GemColor color: newCost.getGems().keySet()) {
            if (newCost.getCount(color) < 0) {
                newCost.add(color, -1 * newCost.getCount(color));
            }
        }
        return new Cost(newCost.getGems());
    }
}