public final class Card {
    private final int id;
    private final int level;
    private final GemColour bonus;
    private final int prestigePoints;
    private final GemCollection cost;

    public Card(int id, int level, GemColor bonus, int prestigePoints, GemCollection cost) {
        this.id = id;
        this.level = level;
        this.bonus = bonus;
        this.prestigePoints = prestigePoints;
        this.cost = cost;
    }

    public int getLevel() {
        return level;
    }

    public GemColour getBonus() {
        return bonus;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public GemCollection getCost() {
        return cost;
    }
}
