import java.util.Map;
import java.util.Objects;

public class Noble {
    private String name;
    private int points;
    private Map<GemColor, Integer> requirements;

    public Noble(String name, Map<GemColor, Integer> requirements) {
        this.name = name;
        this.requirements = requirements;
        this.points = 3; 
    }

    public String getName() {
        return this.name;
    }

    public int getPoints() {
        return points;
    }

    public Map<GemColor, Integer> getRequirements() {
        return requirements;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String toString() {
        return String.format("Name: %s, Requirements: %s", name, requirements.toString());
    }
}