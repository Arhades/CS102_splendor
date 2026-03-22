import java.util.*;

public class GameRules {
    private GameState gameState;

    public GameRules(GameState gameState) {
        this.gameState = gameState;
    }

    public boolean canAffordCard(Player player, Card card) {
        Cost cost = card.getCost();
        Map<GemColor, Integer> bonus = player.calculateBonuses();
        cost = cost.afterBonuses(bonus);

        GemCollection gems = player.getGems();
        GemCollection costCollection = cost.getCost();
        if (gems.contains(costCollection)) {
            return true;
        } else if (gems.containsAfterJoker(costCollection)) {
            return true;
        }
        return false;
    }

    public GemCollection calculateActualCost(Player player, Card card) {
        Cost cost = card.getCost();
        Map<GemColor, Integer> bonus = player.calculateBonuses();
        cost = cost.afterBonuses(bonus);

        GemCollection gems = player.getGems();
        GemCollection costCollection = cost.getCost();
        if (gems.contains(costCollection)) {
            return costCollection;
        }
        GemCollection newCost = new GemCollection(cost.getCost().getGems());
        newCost.add(GemColor.GOLD_JOKER, gems.jokerNeeded(costCollection));
        return newCost;
    }

    public boolean canTakeThreeDifferentGems(GemCollection requested, GemCollection gemBank) {
        return gemBank.contains(requested);
    }

    public boolean canTakeThreeDifferentGems(GemCollection gemBank) {
        int sum = 0;
        for (GemColor color: gemBank.getGems().keySet()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            sum += gemBank.getCount(color) > 0? 1: 0;
        }
        if (sum >= 3) {
            return true;
        }
        return false;
    }

    public boolean canTakeTwoSameGems(GemColor color, GemCollection gemBank) {
        if (gemBank.getCount(color) >= 4) {
            return true;
        }
        return false;
    }

    public boolean canTakeTwoSameGems(GemCollection gemBank) {
        for (GemColor color: gemBank.getGems().keySet()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (gemBank.getCount(color) >= 4) {
                return true;
            }
        }
        return false;
    }

    public boolean mustReturnGems(Player player) {
        if (player.getGemCount() > 10) {
            return true;
        }
        return false;
    }

    public List<Noble> getClaimableNobles(Player player, List<Noble> nobles) {
        List<Noble> claimableNobles = new ArrayList<>();
        Map<GemColor, Integer> bonus = player.calculateBonuses();
        for (Noble noble: nobles) {
            if (Noble.canBeClaimed(noble, bonus)) {
                claimableNobles.add(noble);
            }
        }
        return claimableNobles;
    }

    public boolean hasPlayerWon(Player player, int threshold) {
        if (player.getPoints() >= 15) {
            return true;
        }
        return false;
    }

    public boolean canReserveCard(Player player) {
        return player.getReservedCards().size() < 3;
    }

    public Player getWinner(List<Player> players) {
        List<Player> winners = new ArrayList<>();
        for (Player player: players) {
            if (hasPlayerWon(player, gameState.getWinningThreshold())) {
                winners.add(player);
            }
        }
        if (winners.size() == 1) {
            return winners.get(0);
        }

        int mostPoints = 0;
        int count = 0;
        Player winner = winners.get(0);
        for (Player player: winners) {
            if (player.getPoints() == mostPoints) {
                count++;
                continue;
            }
            if (player.getPoints() > mostPoints) {
                mostPoints = player.getPoints();
                winner = player;
                count = 1;
            }
        }

        if (count == 1) {
            return winner;
        }
        
        int leastCards = Integer.MAX_VALUE;
        for (Player player: winners) {
            leastCards = Math.min(leastCards, player.getPurchasedCards().size() + player.getClaimedNobles().size());
        }

        winner = winners.get(0);
        for (Player player: winners) {
            if (player.getPurchasedCards().size() + player.getClaimedNobles().size() == leastCards) {
                winner = player;
            }
        }
        return winner;
    }

    public boolean validColor(String color) {
        color = color.toUpperCase();
        for (GemColor col: GemColor.values()) {
            if (col.name().equals(color)) {
                return true;
            }
        }
        return false;
    }
}