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

    public Cost afterBonuses(GemCollection bonus) {
        GemCollection newCost = new GemCollection<>(cost);
        newCost.subtract(bonus);
    }
}