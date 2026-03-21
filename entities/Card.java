public class Card {
    private int level;
    private int points;
    private GemColor bonus;
    private Cost cost;

    public Card(int level, int points, GemColor bonus, Cost cost) {
        this.level = level;
        this.points = points;
        this.bonus = bonus;
        this.cost = cost;
    }

    public int getLevel() {
        return level;
    }

    public int getPoints() {
        return points;
    }

    public GemColor getBonus() {
        return bonus;
    }

    public Cost getCost() {
        return cost;
    }
    
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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(level, points, bonus, cost);
    }
}