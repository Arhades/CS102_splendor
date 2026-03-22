/**
 * Represents a development card in the game.
 * 
 * A card has a level, a point value, a bonus gem color, and a cost required to purchase it.
 * Cards can be bought by players to gain permanent bonuses, which reduces the required cost,
 * and victory points.
 * 
 */
public class Card {

    /**
     * The level of the card.
     */
    private int level;

    /**
     * The number of victory points this card gives.
     */
    private int points;

    /**
     * The bonus gem color of the card.
     */
    private GemColor bonus;

    /**
     * The cost required to purchase this card.
     */
    private Cost cost;

    /**
     * Constructs a card with the specified level, points, bonus, and cost.
     * 
     * @param level the level of the card
     * @param points the number of victory points the card gives
     * @param bonus the bonus gem color provided by the card
     * @param cost the cost required to purchase the card
     */
    public Card(int level, int points, GemColor bonus, Cost cost) {
        this.level = level;
        this.points = points;
        this.bonus = bonus;
        this.cost = cost;
    }

    /**
     * Returns the level of the card.
     * 
     * @return the card level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the number of victory points provided by the card.
     * 
     * @return the card's point value
     */
    public int getPoints() {
        return points;
    }

    /**
     * Returns the bonus gem color of the card.
     * 
     * @return the bonus gem color
     */
    public GemColor getBonus() {
        return bonus;
    }

    /**
     * Returns the cost required to purchase a card.
     * 
     * @return the card cost
     */
    public Cost getCost() {
        return cost;
    }

    /**
     * Compares this card with another object.
     * 
     * If the object is a card, both cards are considered to be equal if they
     * have the same level, points, bonus gem color, and cost.
     * 
     * @param obj the object to compare with this card
     * @return true if the given object is equal to this card, and false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        
        Card other = (Card) obj;
        
        return this.level == other.level &&
               this.points == other.points &&
               this.bonus == other.bonus && 
               this.cost.equals(other.cost); 
    }

    /**
     * Returns a hash code value for this card.
     * 
     * The hash code is computed based on the card's level, points, bonus gem color, and cost.
     * 
     * @return the hash code value for this card
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(level, points, bonus, cost);
    }

    /**
     * Returns a string representation of the card.
     * 
     * @return a string containing the card's details
     */
    public String toString() {
        return String.format("Level: %d, Points: %d, Bonus: %s, Cost: %s", level, points, bonus.name(), cost.getCost().getGems().toString());
    }
}