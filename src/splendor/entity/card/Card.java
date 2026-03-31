package splendor.entity.card;

import splendor.valueobjects.*;

/**
 * Abstract base class for all cards in the game.
 */
public abstract class Card {
    private int points;
    private Cost cost;

    /**
     * Constructs a card with the given points and cost.
     *
     * @param points the prestige points awarded by this card
     * @param cost   the gem cost required to purchase this card
     */
    public Card(int points, Cost cost) {
        this.points = points;
        this.cost = cost;
    }

    /**
     * Returns the prestige points of this card.
     *
     * @return the point value
     */
    public int getPoints() {
        return points;
    }

    /**
     * Returns the cost of this card.
     *
     * @return the cost object
     */
    public Cost getCost() {
        return cost;
    }

    /**
     * Returns a string representation of this card.
     *
     * @return a string describing the card
     */
    public abstract String toString();
}
