import java.util.*;

/**
 * The GameRules class contains the logic that enforces the rules of the game.
 * It determines whether actions such as buying cards, taking gems,
 * reserving cards, and winning conditions are valid.
 */
public class GameRules {
    private GameState gameState;

    /**
     * Constructs a GameRules object with the given game state.
     *
     * @param gameState the current state of the game
     */
    public GameRules(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Checks whether a player can afford a given card.
     * Takes into account bonuses and joker (gold) gems.
     *
     * @param player the player attempting to buy the card
     * @param card the card to be purchased
     * @return true if the player can afford the card, false otherwise
     */
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

    /**
     * Calculates the actual cost a player needs to pay for a card,
     * including the use of joker gems if necessary.
     *
     * @param player the player purchasing the card
     * @param card the card to be purchased
     * @return a GemCollection representing the actual cost to be paid
     */
    public GemCollection calculateActualCost(Player player, Card card) {
        Cost cost = card.getCost();
        Map<GemColor, Integer> bonus = player.calculateBonuses();
        cost = cost.afterBonuses(bonus);

        GemCollection required = cost.getCost();
        GemCollection playerGems = player.getGems();
        GemCollection actualCost = new GemCollection();

        for (GemColor color : required.getGems().keySet()) {
            int need = required.getCount(color);
            int have = playerGems.getCount(color);

            if (have >= need) {
                actualCost.add(color, need);
            } else {
                actualCost.add(color, have);
                actualCost.add(GemColor.GOLD_JOKER, need - have);
            }
        }

        return actualCost;
    }

    public Map<GemColor, Integer> getDiscountedCost(Player player, Card card) {
        Cost cost = card.getCost();
        Map<GemColor, Integer> bonus = player.calculateBonuses();
        cost = cost.afterBonuses(bonus);

        return new HashMap<>(cost.getCost().getGems());
    }

    /**
     * Checks if a player can take three different gems from the bank.
     *
     * @param requested the gems the player wants to take
     * @param gemBank the current gems available in the bank
     * @return true if the bank contains the requested gems, false otherwise
     */
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

    /**
     * Checks if a player can take two gems of the same color.
     * This is only allowed if there are at least 4 gems of that color in the bank.
     *
     * @param color the gem color requested
     * @param gemBank the current gems available in the bank
     * @return true if the player can take two gems of that color, false otherwise
     */
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

    /**
     * Returns a list of nobles that the player can claim
     * based on their current bonuses.
     *
     * @param player the player attempting to claim nobles
     * @param nobles the list of available nobles
     * @return a list of nobles that can be claimed
     */
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

    /**
     * Checks whether a player has reached the winning condition.
     *
     * @param player the player being checked
     * @param threshold the number of points required to win
     * @return true if the player has enough points, false otherwise
     */
    public boolean hasPlayerWon(Player player, int threshold) {
        if (player.getPoints() >= threshold) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether a player can reserve another card.
     * A player can only reserve up to 3 cards.
     *
     * @param player the player attempting to reserve a card
     * @return true if the player can reserve more cards, false otherwise
     */
    public boolean canReserveCard(Player player) {
        return player.getReservedCards().size() < 3;
    }

    /**
     * Determines the winner among players who have reached the winning condition.
     * If multiple players qualify, tie-breakers are applied:
     * 1. Highest points
     * 2. Fewest total cards (purchased + nobles)
     *
     * @param players the list of players in the game
     * @return the winning player
     */
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

    public int countMissingGems(Player player, Card card) {
        Cost cost = card.getCost();
        Map<GemColor, Integer> bonus = player.calculateBonuses();
        cost = cost.afterBonuses(bonus);

        GemCollection required = cost.getCost();
        int missing = 0;

        for (GemColor color : required.getGems().keySet()) {
            int need = required.getCount(color);
            int owned = player.getSpecificGem(color);

            if (owned < need) {
                missing += need - owned;
            }
        }

        missing -= player.getSpecificGem(GemColor.GOLD_JOKER);
        return Math.max(0, missing);
    }
}