import java.util.*;

public class GemCollection {
    private Map<GemColor, Integer> gems;

    public GemCollection() {
        this.gems = new HashMap<>();
        this.gems.put(GemColor.DIAMOND, 0);
        this.gems.put(GemColor.SAPPHIRE, 0);
        this.gems.put(GemColor.EMERALD, 0);
        this.gems.put(GemColor.RUBY, 0);
        this.gems.put(GemColor.ONYX, 0);
        this.gems.put(GemColor.GOLD_JOKER, 0);
    }

    public GemCollection(Map<GemColor, Integer> gems) {
        this.gems = gems;
    }

    public int getCount(GemColor color) {
        return gems.get(color);
    }

    public int getTotalCount() {
        int sum = 0;
        for (Integer count: gems.values()) {
            sum += count;
        }
        return sum;
    }

    public void add(GemColor color, int amount) {
        gems.put(color, gems.get(color) + 1);
    }

    public void add(GemCollection other) {
        for (GemColor color: gems.keySet()) {
            gems.put(color, gems.get(color) + other.get(color));
        }
    }

    public void subtract(GemColor color, int amount) {
        gems.put(color, gems.get(color) - amount);
    }

    public void subtract(GemCollection other) {
        for (GemColor color: gems.KeySet()) {
            gems.put(color, gems.get(color) - other.get(color));
        }
    }

    public boolean contains(GemCollection other) {
        for (GemColor color: other.KeySet()) {
            if (gems.get(color) < other.get(color)) {
                return false;
            }
        }
        return true;
    }
}