package splendor.entity.card;

import splendor.valueobjects.*;

public abstract class Card {
    private int points;
    private Cost cost;

    public Card(int points, Cost cost) {
        this.points = points;
        this.cost = cost;
    }

    public int getPoints() {
        return points;
    }

    public Cost getCost() {
        return cost;
    }

    public abstract String toString();
}