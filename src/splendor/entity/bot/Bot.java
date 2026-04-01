package splendor.entity.bot;

import java.util.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.exception.*;
import splendor.rules.*;
import splendor.valueobjects.*;

/**
 * Abstract base class for all bot players.
 * Provides shared helper methods for gem evaluation, card scoring,
 * noble tracking, and move execution.
 */
public abstract class Bot extends Player {
    private Random random;

    /**
     * Constructs a Bot with the given name and turn order.
     *
     * @param name      the bot's display name
     * @param turnOrder the bot's position in the turn order
     */
    public Bot(String name, int turnOrder) {
        super(name, turnOrder);
        this.random = new Random();
    }

    /**
     * Executes the bot's full turn, including choosing a move and returning extra gems.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @return a description of the move taken
     */
    public String takeTurn(GameState gameState, GameRules gameRules) {
        String move = chooseMove(gameState, gameRules);
        String returned = returnExtraGems(gameState, gameRules);
        if (!returned.equals("")) {
            move += " " + returned;
        }
        return move;
    }

    /**
     * Chooses and executes a move for this bot's turn.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @return a description of the move chosen
     */
    protected abstract String chooseMove(GameState gameState, GameRules gameRules);

    /**
     * Returns the bot's random number generator.
     *
     * @return the Random instance
     */
    protected Random getRandom() {
        return random;
    }

    /**
     * Checks whether the game is still in the early phase.
     *
     * @param gameState the current game state
     * @return true if it is still the early game
     */
    protected boolean isEarlyGame(GameState gameState) {
        return gameState.getTurnCount() < gameState.getPlayers().size() * 2;
    }

    /**
     * Returns all visible card choices from the card market.
     *
     * @param gameState the current game state
     * @return a list of CardChoice objects for visible market cards
     */
    protected List<CardChoice> getVisibleChoices(GameState gameState) {
        List<CardChoice> choices = new ArrayList<>();
        for (int level = 1; level <= 3; level++) {
            try {
                List<DevelopmentCard> cards = gameState.getCardMarket().getVisibleCards(level);
                for (int i = 0; i < cards.size(); i++) {
                    choices.add(CardChoice.createVisible(cards.get(i), level, i));
                }
            } catch (InvalidIndexException e) {
            }
        }
        return choices;
    }

    /**
     * Returns card choices from this bot's reserved hand.
     *
     * @return a list of CardChoice objects for reserved cards
     */
    protected List<CardChoice> getReservedChoices() {
        List<CardChoice> choices = new ArrayList<>();
        for (int i = 0; i < getReservedCards().size(); i++) {
            choices.add(CardChoice.createReserved(getReservedCards().get(i), i));
        }
        return choices;
    }

    /**
     * Returns all card choices, both visible and reserved.
     *
     * @param gameState the current game state
     * @return a combined list of all available card choices
     */
    protected List<CardChoice> getAllChoices(GameState gameState) {
        List<CardChoice> choices = getVisibleChoices(gameState);
        choices.addAll(getReservedChoices());
        return choices;
    }

    /**
     * Returns all card choices that the bot can currently afford.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for affordability checks
     * @return a list of affordable card choices
     */
    protected List<CardChoice> getAffordableChoices(GameState gameState, GameRules gameRules) {
        List<CardChoice> choices = new ArrayList<>();
        List<CardChoice> allChoices = getAllChoices(gameState);
        for (CardChoice choice: allChoices) {
            if (gameRules.canAffordCard(this, choice.getCard())) {
                choices.add(choice);
            }
        }
        return choices;
    }

    /**
     * Selects a random card choice from the given list.
     *
     * @param choices the list of card choices to pick from
     * @return a randomly selected CardChoice, or null if the list is empty
     */
    protected CardChoice chooseRandomChoice(List<CardChoice> choices) {
        if (choices.size() == 0) {
            return null;
        }
        return choices.get(random.nextInt(choices.size()));
    }

    /**
     * Returns the number of gems the bot is missing to afford a card.
     *
     * @param gameRules the game rules for cost calculation
     * @param card      the card to check
     * @return the number of missing gems
     */
    protected int getMissingGems(GameRules gameRules, Card card) {
        return gameRules.countMissingGems(this, card);
    }

    /**
     * Returns the total discounted cost of a card for this bot.
     *
     * @param gameRules the game rules for discount calculation
     * @param card      the card to evaluate
     * @return the total discounted gem cost
     */
    protected int getDiscountedCostTotal(GameRules gameRules, Card card) {
        Map<GemColor, Integer> cost = gameRules.getDiscountedCost(this, card);
        int total = 0;
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            total += cost.get(color);
        }
        return total;
    }

    /**
     * Counts how many of the bot's gems would be wasted (not needed) when buying a card.
     *
     * @param gameRules the game rules for cost calculation
     * @param card      the card to evaluate
     * @return the number of wasted gems
     */
    protected int getWasteCount(GameRules gameRules, Card card) {
        Map<GemColor, Integer> cost = gameRules.getDiscountedCost(this, card);
        int waste = 0;
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            int need = cost.get(color);
            int owned = getSpecificGem(color);
            if (need == 0) {
                waste += owned;
            } else if (owned > need) {
                waste += owned - need;
            }
        }
        return waste;
    }

    /**
     * Returns the total base (undiscounted) cost of a card.
     *
     * @param card the card to evaluate
     * @return the total base gem cost
     */
    protected int getBaseCostTotal(Card card) {
        int total = 0;
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            total += card.getCost().getRequired(color);
        }
        return total;
    }

    /**
     * Finds the noble closest to being claimable by this bot.
     *
     * @param gameState the current game state
     * @return the closest Noble, or null if none are available
     */
    protected Noble getClosestNoble(GameState gameState) {
        Noble best = null;
        int bestColors = Integer.MAX_VALUE;
        int bestBonuses = Integer.MAX_VALUE;

        for (Noble noble: gameState.getAvailableNobles()) {
            int remainingColors = countRemainingNobleColors(noble);
            int remainingBonuses = countRemainingNobleBonuses(noble);
            if (best == null || remainingColors < bestColors || (remainingColors == bestColors && remainingBonuses < bestBonuses)) {
                best = noble;
                bestColors = remainingColors;
                bestBonuses = remainingBonuses;
            }
        }
        return best;
    }

    /**
     * Counts how many bonus colors the bot still needs to claim a noble.
     *
     * @param noble the noble to check
     * @return the number of remaining colors needed
     */
    protected int countRemainingNobleColors(Noble noble) {
        int remaining = 0;
        Map<GemColor, Integer> bonus = calculateBonuses();
        for (Map.Entry<GemColor, Integer> entry: noble.getRequirements().entrySet()) {
            if (bonus.get(entry.getKey()) < entry.getValue()) {
                remaining++;
            }
        }
        return remaining;
    }

    /**
     * Counts the total number of individual bonuses the bot still needs for a noble.
     *
     * @param noble the noble to check
     * @return the total number of remaining bonus cards needed
     */
    protected int countRemainingNobleBonuses(Noble noble) {
        int remaining = 0;
        Map<GemColor, Integer> bonus = calculateBonuses();
        for (Map.Entry<GemColor, Integer> entry: noble.getRequirements().entrySet()) {
            if (bonus.get(entry.getKey()) < entry.getValue()) {
                remaining += entry.getValue() - bonus.get(entry.getKey());
            }
        }
        return remaining;
    }

    /**
     * Returns the gem colors still needed to claim a noble.
     *
     * @param noble the noble to check (may be null)
     * @return a list of GemColors still needed
     */
    protected List<GemColor> getNeededColorsForNoble(Noble noble) {
        List<GemColor> colors = new ArrayList<>();
        if (noble == null) {
            return colors;
        }

        Map<GemColor, Integer> bonus = calculateBonuses();
        for (Map.Entry<GemColor, Integer> entry: noble.getRequirements().entrySet()) {
            if (bonus.get(entry.getKey()) < entry.getValue()) {
                addColor(colors, entry.getKey());
            }
        }
        return colors;
    }

    /**
     * Returns the gem colors the bot still needs to buy a specific card.
     *
     * @param gameRules the game rules for discount calculation
     * @param card      the card to evaluate
     * @return a list of GemColors still needed
     */
    protected List<GemColor> getNeededColorsForCard(GameRules gameRules, Card card) {
        List<GemColor> colors = new ArrayList<>();
        Map<GemColor, Integer> cost = gameRules.getDiscountedCost(this, card);

        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (getSpecificGem(color) < cost.get(color)) {
                addColor(colors, color);
            }
        }

        if (colors.size() > 0) {
            return colors;
        }

        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (cost.get(color) > 0) {
                addColor(colors, color);
            }
        }
        return colors;
    }

    /**
     * Adds a non-joker color to a list if it is not already present.
     *
     * @param colors the list to add to
     * @param color  the GemColor to add
     */
    protected void addColor(List<GemColor> colors, GemColor color) {
        if (!colors.contains(color) && !color.equals(GemColor.GOLD_JOKER)) {
            colors.add(color);
        }
    }

    /**
     * Executes a purchase action for the given card choice.
     *
     * @param choice    the card choice to buy
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @return a description of the purchase result
     */
    protected String buyChoice(CardChoice choice, GameState gameState, GameRules gameRules) {
        boolean success;
        if (choice.isReserved()) {
            success = GameActions.purchaseReservedCard(this, gameState, gameRules, choice.getIndex());
        } else {
            success = GameActions.purchaseVisibleCard(this, gameState, gameRules, choice.getLevel(), choice.getIndex());
        }

        if (!success) {
            return getName() + " could not buy the target card.";
        }
        return getName() + " bought " + describeChoice(choice) + ".";
    }

    /**
     * Executes a reserve action for the given visible card choice.
     *
     * @param choice    the card choice to reserve
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @return a description of the reserve result
     */
    protected String reserveChoice(CardChoice choice, GameState gameState, GameRules gameRules) {
        boolean success = GameActions.reserveVisibleCard(this, gameState, gameRules, choice.getLevel(), choice.getIndex());
        if (!success) {
            return getName() + " could not reserve the target card.";
        }
        return getName() + " reserved " + describeChoice(choice) + ".";
    }

    /**
     * Reserves a hidden card from the top of a deck at the given level.
     *
     * @param level     the deck level (1, 2, or 3)
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @return a description of the reserve result
     */
    protected String reserveHidden(int level, GameState gameState, GameRules gameRules) {
        boolean success = GameActions.reserveHiddenCard(this, gameState, gameRules, level);
        if (!success) {
            return getName() + " could not reserve a hidden card.";
        }
        return getName() + " reserved a hidden level " + level + " card.";
    }

    /**
     * Returns the list of non-joker gem colors that have stock in the bank.
     *
     * @param gameState the current game state
     * @return a list of available GemColors
     */
    protected List<GemColor> getAvailableColors(GameState gameState) {
        List<GemColor> colors = new ArrayList<>();
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (gameState.getGemBank().getCount(color) > 0) {
                colors.add(color);
            }
        }
        return colors;
    }

    /**
     * Adds a weight value to a color in the weights map.
     *
     * @param weights the map of color weights
     * @param color   the GemColor to weight
     * @param amount  the weight to add
     */
    protected void addWeight(Map<GemColor, Integer> weights, GemColor color, int amount) {
        if (color == null || color.equals(GemColor.GOLD_JOKER)) {
            return;
        }
        int value = 0;
        if (weights.containsKey(color)) {
            value = weights.get(color);
        }
        weights.put(color, value + amount);
    }

    /**
     * Returns the weight of a color from the weights map.
     *
     * @param weights the map of color weights
     * @param color   the GemColor to look up
     * @return the weight, or 0 if absent
     */
    protected int getWeight(Map<GemColor, Integer> weights, GemColor color) {
        if (!weights.containsKey(color)) {
            return 0;
        }
        return weights.get(color);
    }

    /**
     * Returns the best gem colors sorted by weight, limited to available colors in the bank.
     *
     * @param weights   the map of color weights
     * @param gameState the current game state
     * @param limit     the maximum number of colors to return
     * @return a list of the best GemColors
     */
    protected List<GemColor> getBestColors(Map<GemColor, Integer> weights, GameState gameState, int limit) {
        List<GemColor> available = getAvailableColors(gameState);
        List<GemColor> best = new ArrayList<>();

        while (best.size() < limit && available.size() > 0) {
            GemColor chosen = available.get(0);
            for (GemColor color: available) {
                int chosenWeight = getWeight(weights, chosen);
                int colorWeight = getWeight(weights, color);
                if (colorWeight > chosenWeight) {
                    chosen = color;
                } else if (colorWeight == chosenWeight) {
                    if (gameState.getGemBank().getCount(color) > gameState.getGemBank().getCount(chosen)) {
                        chosen = color;
                    }
                }
            }
            best.add(chosen);
            available.remove(chosen);
        }
        return best;
    }

    /**
     * Returns the best color for a take-two-same action based on weights.
     *
     * @param weights   the map of color weights
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @return the best GemColor for doubling, or null if none qualify
     */
    protected GemColor getBestDoubleColor(Map<GemColor, Integer> weights, GameState gameState, GameRules gameRules) {
        GemColor chosen = null;
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (!gameRules.canTakeTwoSameGems(color, gameState.getGemBank())) {
                continue;
            }
            if (chosen == null) {
                chosen = color;
                continue;
            }
            int chosenWeight = getWeight(weights, chosen);
            int colorWeight = getWeight(weights, color);
            if (colorWeight > chosenWeight) {
                chosen = color;
            } else if (colorWeight == chosenWeight) {
                if (gameState.getGemBank().getCount(color) > gameState.getGemBank().getCount(chosen)) {
                    chosen = color;
                }
            }
        }
        return chosen;
    }

    /**
     * Takes gems using a weighted strategy, choosing between take-three and take-two.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param weights   the map of color weights guiding the decision
     * @return a description of the gem move taken
     */
    protected String takeWeightedGemMove(GameState gameState, GameRules gameRules, Map<GemColor, Integer> weights) {
        List<GemColor> best = getBestColors(weights, gameState, 3);
        if (best.size() == 3 && gameRules.canTakeThreeDifferentGems(gameState.getGemBank())) {
            if (GameActions.takeThreeDifferent(this, gameState, gameRules, best)) {
                return getName() + " took " + formatColors(best) + ".";
            }
        }

        GemColor doubleColor = getBestDoubleColor(weights, gameState, gameRules);
        if (doubleColor != null) {
            int doubleScore = getWeight(weights, doubleColor) * 2;
            int threeScore = 0;
            for (GemColor color: best) {
                threeScore += getWeight(weights, color);
            }
            if (best.size() < 3 || doubleScore >= threeScore) {
                if (GameActions.takeTwoSame(this, gameState, gameRules, doubleColor)) {
                    return getName() + " took 2 " + doubleColor.name() + ".";
                }
            }
        }

        return fallbackRandomGemMove(gameState, gameRules);
    }

    /**
     * Takes a random gem move as a fallback when no strategic option is available.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @return a description of the gem move taken
     */
    protected String fallbackRandomGemMove(GameState gameState, GameRules gameRules) {
        List<GemColor> available = getAvailableColors(gameState);
        Collections.shuffle(available, random);

        if (available.size() >= 3 && gameRules.canTakeThreeDifferentGems(gameState.getGemBank())) {
            List<GemColor> choice = new ArrayList<>();
            choice.add(available.get(0));
            choice.add(available.get(1));
            choice.add(available.get(2));
            if (GameActions.takeThreeDifferent(this, gameState, gameRules, choice)) {
                return getName() + " took " + formatColors(choice) + ".";
            }
        }

        List<GemColor> doubleChoices = new ArrayList<>();
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (gameRules.canTakeTwoSameGems(color, gameState.getGemBank())) {
                doubleChoices.add(color);
            }
        }

        if (doubleChoices.size() > 0) {
            GemColor chosen = doubleChoices.get(random.nextInt(doubleChoices.size()));
            if (GameActions.takeTwoSame(this, gameState, gameRules, chosen)) {
                return getName() + " took 2 " + chosen.name() + ".";
            }
        }

        return getName() + " had no useful move.";
    }

    /**
     * Returns the closest cards (by missing gems) from all available choices.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for cost calculation
     * @param limit     the maximum number of choices to return
     * @return a list of the closest CardChoices
     */
    protected List<CardChoice> getClosestChoices(GameState gameState, GameRules gameRules, int limit) {
        List<CardChoice> remaining = new ArrayList<>(getAllChoices(gameState));
        List<CardChoice> chosen = new ArrayList<>();

        while (chosen.size() < limit && remaining.size() > 0) {
            CardChoice best = remaining.get(0);
            for (CardChoice choice: remaining) {
                int bestMissing = getMissingGems(gameRules, best.getCard());
                int choiceMissing = getMissingGems(gameRules, choice.getCard());
                if (choiceMissing < bestMissing) {
                    best = choice;
                } else if (choiceMissing == bestMissing) {
                    if (getDiscountedCostTotal(gameRules, choice.getCard()) < getDiscountedCostTotal(gameRules, best.getCard())) {
                        best = choice;
                    }
                }
            }
            chosen.add(best);
            remaining.remove(best);
        }
        return chosen;
    }

    /**
     * Chooses which gem color to return when the bot exceeds the 10-gem limit.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for cost/noble checks
     * @return the GemColor to return, or null if no gems can be returned
     */
    protected GemColor chooseGemToReturn(GameState gameState, GameRules gameRules) {
        List<GemColor> important = new ArrayList<>();
        List<GemColor> nobleColors = getNeededColorsForNoble(getClosestNoble(gameState));
        for (GemColor color: nobleColors) {
            addColor(important, color);
        }

        List<CardChoice> closestChoices = getClosestChoices(gameState, gameRules, 3);
        for (CardChoice choice: closestChoices) {
            List<GemColor> colors = getNeededColorsForCard(gameRules, choice.getCard());
            for (GemColor color: colors) {
                addColor(important, color);
            }
        }

        Map<GemColor, Integer> bonus = calculateBonuses();
        GemColor chosen = null;
        int lowestScore = Integer.MAX_VALUE;
        for (GemColor color: GemColor.values()) {
            if (getSpecificGem(color) == 0) {
                continue;
            }
            int score = 0;
            if (color.equals(GemColor.GOLD_JOKER)) {
                score += 20;
            }
            if (important.contains(color)) {
                score += 8;
            }
            score += bonus.get(color) * 2;
            if (getSpecificGem(color) > 2) {
                score -= (getSpecificGem(color) - 2) * 4;
            }
            if (chosen == null || score < lowestScore) {
                chosen = color;
                lowestScore = score;
            }
        }
        return chosen;
    }

    /**
     * Returns extra gems to the bank until the bot is at or below 10 gems.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for gem limit checks
     * @return a description of the gems returned, or an empty string if none
     */
    protected String returnExtraGems(GameState gameState, GameRules gameRules) {
        List<GemColor> returned = new ArrayList<>();
        while (gameRules.mustReturnGems(this)) {
            GemColor color = chooseGemToReturn(gameState, gameRules);
            if (color == null) {
                break;
            }
            GemCollection gems = new GemCollection();
            gems.add(color, 1);
            deductGems(gems);
            gameState.addGemsToBank(gems);
            returned.add(color);
        }

        if (returned.size() == 0) {
            return "";
        }
        return "Returned " + formatColors(returned) + ".";
    }

    /**
     * Formats a list of gem colors as a comma-separated string.
     *
     * @param colors the list of GemColors to format
     * @return a formatted string like "DIAMOND, RUBY, ONYX"
     */
    protected String formatColors(List<GemColor> colors) {
        String result = "";
        for (int i = 0; i < colors.size(); i++) {
            if (i > 0) {
                result += ", ";
            }
            result += colors.get(i).name();
        }
        return result;
    }

    /**
     * Returns a human-readable description of a card choice.
     *
     * @param choice the card choice to describe
     * @return a string describing the choice
     */
    protected String describeChoice(CardChoice choice) {
        if (choice.isReserved()) {
            return "reserved[" + choice.getIndex() + "] " + choice.getCard().toString();
        }
        return "level " + choice.getLevel() + " card[" + choice.getIndex() + "] " + choice.getCard().toString();
    }
}
