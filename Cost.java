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
        newCost.subtract(new GemCollection(bonus));
        return new Cost(newCost.getGems());
    }
}