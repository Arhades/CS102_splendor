package splendor.entity.card;

import splendor.entity.*;
import splendor.valueobjects.*;

/**
 * Represents a development card in the game.
 */
public class DevelopmentCard extends Card {

    /**
     * The level of the card.
     */
    private int level;

    /**
     * The bonus gem color of the card.
     */
    private GemColor bonus;

    /**
     * Constructs a DevelopmentCard with the specified level, points, bonus, and cost.
     * 
     * @param level the level of the card
     * @param points the number of points the card gives
     * @param bonus the bonus gem color provided by the card
     * @param cost the cost required to purchase the card
     */
    public DevelopmentCard(int level, int points, GemColor bonus, Cost cost) {
        super(points, cost);
        this.level = level;
        this.bonus = bonus;
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
     * Returns the bonus gem color of the card.
     * 
     * @return the bonus gem color
     */
    public GemColor getBonus() {
        return bonus;
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
        if (!(obj instanceof DevelopmentCard)) return false;
        
        DevelopmentCard other = (DevelopmentCard) obj;
        
        return this.level == other.level &&
               this.getPoints() == other.getPoints() &&
               this.bonus == other.bonus && 
               this.getCost().equals(other.getCost()); 
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
        return java.util.Objects.hash(level, getPoints(), bonus, getCost());
    }

    /**
     * Returns a string representation of the card.
     * 
     * @return a string containing the card's details
     */
    @Override
    public String toString() {
        return String.format("Level: %d | Points: %d | Bonus: %s | Cost: %s", level, getPoints(), bonus.name(), super.getCost().getCost().getGems().toString());
    }
}