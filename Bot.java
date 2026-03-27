import java.util.*;

public abstract class Bot extends Player {
    private Random random;

    public Bot(String name, int turnOrder) {
        super(name, turnOrder);
        this.random = new Random();
    }

    public String takeTurn(GameState gameState, GameRules gameRules) {
        String move = chooseMove(gameState, gameRules);
        String returned = returnExtraGems(gameState, gameRules);
        if (!returned.equals("")) {
            move += " " + returned;
        }
        return move;
    }

    protected abstract String chooseMove(GameState gameState, GameRules gameRules);

    protected Random getRandom() {
        return random;
    }

    protected boolean isEarlyGame(GameState gameState) {
        return gameState.getTurnCount() < gameState.getPlayers().size() * 2;
    }

    protected List<CardChoice> getVisibleChoices(GameState gameState) {
        List<CardChoice> choices = new ArrayList<>();
        for (int level = 1; level <= 3; level++) {
            try {
                List<Card> cards = gameState.getCardMarket().getVisibleCards(level);
                for (int i = 0; i < cards.size(); i++) {
                    choices.add(CardChoice.createVisible(cards.get(i), level, i));
                }
            } catch (UnavailableCardException e) {
            }
        }
        return choices;
    }

    protected List<CardChoice> getReservedChoices() {
        List<CardChoice> choices = new ArrayList<>();
        for (int i = 0; i < getReservedCards().size(); i++) {
            choices.add(CardChoice.createReserved(getReservedCards().get(i), i));
        }
        return choices;
    }

    protected List<CardChoice> getAllChoices(GameState gameState) {
        List<CardChoice> choices = getVisibleChoices(gameState);
        choices.addAll(getReservedChoices());
        return choices;
    }

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

    protected CardChoice chooseRandomChoice(List<CardChoice> choices) {
        if (choices.size() == 0) {
            return null;
        }
        return choices.get(random.nextInt(choices.size()));
    }

    protected int getMissingGems(GameRules gameRules, Card card) {
        return gameRules.countMissingGems(this, card);
    }

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

    protected void addColor(List<GemColor> colors, GemColor color) {
        if (!colors.contains(color) && !color.equals(GemColor.GOLD_JOKER)) {
            colors.add(color);
        }
    }

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

    protected String reserveChoice(CardChoice choice, GameState gameState, GameRules gameRules) {
        boolean success = GameActions.reserveVisibleCard(this, gameState, gameRules, choice.getLevel(), choice.getIndex());
        if (!success) {
            return getName() + " could not reserve the target card.";
        }
        return getName() + " reserved " + describeChoice(choice) + ".";
    }

    protected String reserveHidden(int level, GameState gameState, GameRules gameRules) {
        boolean success = GameActions.reserveHiddenCard(this, gameState, gameRules, level);
        if (!success) {
            return getName() + " could not reserve a hidden card.";
        }
        return getName() + " reserved a hidden level " + level + " card.";
    }

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

    protected int getWeight(Map<GemColor, Integer> weights, GemColor color) {
        if (!weights.containsKey(color)) {
            return 0;
        }
        return weights.get(color);
    }

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

    protected String describeChoice(CardChoice choice) {
        if (choice.isReserved()) {
            return "reserved[" + choice.getIndex() + "] " + choice.getCard().toString();
        }
        return "level " + choice.getLevel() + " card[" + choice.getIndex() + "] " + choice.getCard().toString();
    }
}
