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

    public Map<GemColor, Integer> getGems() {
        return gems;
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

    public boolean isEmpty() {
        for (Integer count: gems.values()) {
            if (count > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmptyWithouJoker() {
        for (GemColor color: gems.keySet()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (gems.get(color) > 0) {
                return false;
            }
        }
        return true;
    }

    public void add(GemColor color, int amount) {
        gems.put(color, gems.get(color) + 1);
    }

    public void add(GemCollection other) {
        for (GemColor color: gems.keySet()) {
            gems.put(color, gems.get(color) + other.getCount(color));
        }
    }

    public void subtract(GemColor color, int amount) {
        gems.put(color, gems.get(color) - amount);
    }

    public void subtract(GemCollection other) {
        for (GemColor color: gems.keySet()) {
            gems.put(color, gems.get(color) - other.getCount(color));
        }
    }

    public boolean contains(GemCollection other) {
        for (GemColor color: other.getGems().keySet()) {
            if (gems.get(color) < other.getCount(color)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAfterJoker(GemCollection other) {
        int joker = gems.get(GemColor.GOLD_JOKER);
        for (GemColor color: other.getGems().keySet()) {
            if (gems.get(color) + joker < other.getCount(color)) {
                return false;
            } else if (gems.get(color) < other.getCount(color)) {
                joker -= (other.getCount(color) - gems.get(color));
            }
        }
        return true;
    }

    public int jokerNeeded(GemCollection other) {
        int joker = gems.get(GemColor.GOLD_JOKER);
        for (GemColor color: other.getGems().keySet()) {
            if (gems.get(color) < other.getCount(color)) {
                joker -= (other.getCount(color) - gems.get(color));
            }
        }
        return gems.get(GemColor.GOLD_JOKER) - joker;
    }
}