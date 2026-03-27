import java.util.*;

public class BotGameRules extends GameRules {
    private BotGameState gameState;

    public BotGameRules(BotGameState gameState) {
        super(gameState);
        this.gameState = gameState;
    }

    public Map<GemColor, Integer> getDiscountedCost(Player player, Card card) {
        Map<GemColor, Integer> cost = new HashMap<>();
        Map<GemColor, Integer> bonus = player.calculateBonuses();

        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                cost.put(color, 0);
                continue;
            }
            int required = card.getCost().getRequired(color) - bonus.get(color);
            if (required < 0) {
                required = 0;
            }
            cost.put(color, required);
        }
        cost.put(GemColor.GOLD_JOKER, 0);
        return cost;
    }

    @Override
    public boolean canAffordCard(Player player, Card card) {
        Map<GemColor, Integer> cost = getDiscountedCost(player, card);
        int jokers = player.getSpecificGem(GemColor.GOLD_JOKER);

        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            int required = cost.get(color);
            int owned = player.getSpecificGem(color);
            if (owned < required) {
                jokers -= (required - owned);
            }
            if (jokers < 0) {
                return false;
            }
        }
        return true;
    }

    public int countMissingGems(Player player, Card card) {
        Map<GemColor, Integer> cost = getDiscountedCost(player, card);
        int missing = 0;

        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            int required = cost.get(color);
            int owned = player.getSpecificGem(color);
            if (owned < required) {
                missing += (required - owned);
            }
        }

        missing -= player.getSpecificGem(GemColor.GOLD_JOKER);
        if (missing < 0) {
            missing = 0;
        }
        return missing;
    }

    @Override
    public GemCollection calculateActualCost(Player player, Card card) {
        Map<GemColor, Integer> cost = getDiscountedCost(player, card);
        GemCollection spent = new GemCollection();
        int jokers = 0;

        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            int required = cost.get(color);
            int owned = player.getSpecificGem(color);
            int pay = Math.min(required, owned);
            for (int i = 0; i < pay; i++) {
                spent.add(color, 1);
            }
            jokers += required - pay;
        }

        for (int i = 0; i < jokers; i++) {
            spent.add(GemColor.GOLD_JOKER, 1);
        }
        return spent;
    }

    @Override
    public boolean hasPlayerWon(Player player, int threshold) {
        return player.getPoints() >= threshold;
    }

    @Override
    public Player getWinner(List<Player> players) {
        List<Player> winners = new ArrayList<>();
        for (Player player: players) {
            if (hasPlayerWon(player, gameState.getWinningThreshold())) {
                winners.add(player);
            }
        }

        if (winners.size() == 0) {
            return null;
        }
        if (winners.size() == 1) {
            return winners.get(0);
        }

        Player winner = winners.get(0);
        int mostPoints = winner.getPoints();
        int count = 1;
        for (int i = 1; i < winners.size(); i++) {
            Player player = winners.get(i);
            if (player.getPoints() > mostPoints) {
                winner = player;
                mostPoints = player.getPoints();
                count = 1;
            } else if (player.getPoints() == mostPoints) {
                count++;
            }
        }

        if (count == 1) {
            return winner;
        }

        int leastCards = winners.get(0).getPurchasedCards().size() + winners.get(0).getClaimedNobles().size();
        winner = winners.get(0);
        for (Player player: winners) {
            int totalCards = player.getPurchasedCards().size() + player.getClaimedNobles().size();
            if (totalCards < leastCards) {
                leastCards = totalCards;
                winner = player;
            }
        }
        return winner;
    }
}
