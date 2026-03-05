public class Card {
    private int cardID;
    private int level;
    private GemColour bonus;
    private int prestigePoints;
    private GemCost cost;

    public int getLevel() {
        return level;
    }

    public GemColour getBonus() {
        return bonus;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public GemCost getCost() {
        return cost;
    }
}