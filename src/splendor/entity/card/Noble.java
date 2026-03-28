package splendor.entity.card;

import java.util.Map;
import java.util.Objects;
import splendor.entity.*;
import splendor.valueobjects.*;

/**
 * Represents a noble title in the game.
 * 
 * A noble has a name, a point value, and a set of bonus card requirements
 * that a player must satisfy before they can claim the noble.
 * 
 * Nobles award points when claimed.
 * 
 */
public class Noble extends Card {

    /**
     * The name of the noble.
     */
    private String name;

    /**
     * Constructs a nobe with the specified name and requirements.
     * 
     * @param name the name of the noble
     * @param requirements the bonus card requirements needed to claim the noble
     */
    public Noble(String name, Map<GemColor, Integer> requirements) {
        super(3, new Cost(requirements));
        this.name = name;
    }

    /**
     * Returns the name of the noble.
     * 
     * @return the noble's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the requirements needed to claim this noble.
     * 
     * @return a map of gem colors to required bonus counts
     */
    public Map<GemColor, Integer> getRequirements() {
        return super.getCost().getCost().getGems();
    }

    /**
     * Determines whether a noble can be claimed based on the given bonuses.
     * 
     * A noble can be claimed if the provided bonus counts meet or exceed
     * all of the noble's required bonus counts.
     * 
     * @param noble the noble to check
     * @param bonuses the player's current bonus counts
     */
    public static boolean canBeClaimed(Noble noble, Map<GemColor, Integer> bonuses) {
        for (Map.Entry<GemColor, Integer> req : noble.getRequirements().entrySet()) {
            GemColor requiredColor = req.getKey();
            int requiredAmount = req.getValue();
            int playerAmount = bonuses.getOrDefault(requiredColor, 0);
        
            if (playerAmount < requiredAmount) {
                return false; 
            }
        }
        
        return true; 
    }

    /**
     * Compares the noble with another object for equality.
     * 
     * Two nobles are considered equal if they have the same name, point value, and requirements.
     * 
     * @param obj the object to compare with this role
     * @return true if the given object to this noble, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Noble)) {
            return false;
        }
        
        Noble other = (Noble) obj;
        return this.name.equals(other.getName());
    }

    /**
     * Returns a hash code value for this noble.
     * 
     * The hash code is computed based on the noble's name, points, and requirements.
     * 
     * @return the hash code value for this noble
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, getCost(), getPoints());
    }

    /**
     * Returns a string representation of the noble.
     * 
     * @return a string containing the noble's details
     */
    public String toString() {
        return String.format("Name: %s | Requirements: %s", name, getRequirements().toString());
    }
}