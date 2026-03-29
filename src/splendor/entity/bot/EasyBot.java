package splendor.entity.bot;

import java.util.*;
import splendor.rules.*;
import splendor.entity.*;

public class EasyBot extends Bot {
    public EasyBot(String name, int turnOrder) {
        super(name, turnOrder);
    }

    @Override
    protected String chooseMove(GameState gameState, GameRules gameRules) {
        List<GemColor> preferredColors = getPreferredBonusColors(gameState);
        List<CardChoice> affordable = getAffordableChoices(gameState, gameRules);

        if (affordable.size() > 0) {
            List<CardChoice> preferredAffordable = new ArrayList<>();
            for (CardChoice choice: affordable) {
                if (preferredColors.contains(choice.getCard().getBonus())) {
                    preferredAffordable.add(choice);
                }
            }
            if (preferredAffordable.size() > 0) {
                affordable = preferredAffordable;
            }
            if (affordable.size() == 1) {
                return buyChoice(affordable.get(0), gameState, gameRules);
            }
            CardChoice chosen = getCheapestAffordableCard(affordable, gameRules);
            return buyChoice(chosen, gameState, gameRules);
        }

        String gemMove = chooseGemMove(gameState, gameRules, preferredColors);
        if (!gemMove.equals("")) {
            return gemMove;
        }

        if (gameRules.canReserveCard(this)) {
            CardChoice reserveChoice = getReserveChoice(gameState, gameRules, preferredColors);
            if (reserveChoice != null) {
                return reserveChoice(reserveChoice, gameState, gameRules);
            }
        }

        return getName() + " had no useful move.";
    }

    private List<GemColor> getPreferredBonusColors(GameState gameState) {
        List<GemColor> colors = getNeededColorsForNoble(getClosestNoble(gameState));
        if (colors.size() > 0) {
            return colors;
        }

        List<GemColor> fallback = new ArrayList<>();
        List<CardChoice> visible = getVisibleChoices(gameState);
        for (CardChoice choice: visible) {
            addColor(fallback, choice.getCard().getBonus());
            if (fallback.size() == 3) {
                break;
            }
        }
        return fallback;
    }

    private CardChoice getCheapestAffordableCard(List<CardChoice> choices, GameRules gameRules) {
        int bestCost = Integer.MAX_VALUE;
        int bestWaste = Integer.MAX_VALUE;
        List<CardChoice> bestChoices = new ArrayList<>();

        for (CardChoice choice: choices) {
            int cost = getDiscountedCostTotal(gameRules, choice.getCard());
            int waste = getWasteCount(gameRules, choice.getCard());
            if (cost < bestCost) {
                bestChoices.clear();
                bestChoices.add(choice);
                bestCost = cost;
                bestWaste = waste;
            } else if (cost == bestCost) {
                if (waste < bestWaste) {
                    bestChoices.clear();
                    bestChoices.add(choice);
                    bestWaste = waste;
                } else if (waste == bestWaste) {
                    bestChoices.add(choice);
                }
            }
        }

        return chooseRandomChoice(bestChoices);
    }

    private CardChoice getReserveChoice(GameState gameState, GameRules gameRules, List<GemColor> preferredColors) {
        List<CardChoice> visible = getVisibleChoices(gameState);
        List<CardChoice> matching = new ArrayList<>();

        for (CardChoice choice: visible) {
            if (preferredColors.contains(choice.getCard().getBonus())) {
                matching.add(choice);
            }
        }
        if (matching.size() > 0) {
            visible = matching;
        }

        CardChoice chosen = null;
        int leastMissing = Integer.MAX_VALUE;
        for (CardChoice choice: visible) {
            int missing = getMissingGems(gameRules, choice.getCard());
            if (chosen == null || missing < leastMissing) {
                chosen = choice;
                leastMissing = missing;
            }
        }
        return chosen;
    }

    private String chooseGemMove(GameState gameState, GameRules gameRules, List<GemColor> preferredColors) {
        Map<GemColor, Integer> weights = new HashMap<>();
        List<CardChoice> visible = getVisibleChoices(gameState);
        List<CardChoice> targets = new ArrayList<>();

        for (CardChoice choice: visible) {
            if (preferredColors.contains(choice.getCard().getBonus())) {
                targets.add(choice);
            }
        }
        if (targets.size() == 0) {
            targets = visible;
        }

        for (CardChoice choice: targets) {
            List<GemColor> colors = getNeededColorsForCard(gameRules, choice.getCard());
            for (GemColor color: colors) {
                addWeight(weights, color, 2);
            }
            addWeight(weights, choice.getCard().getBonus(), 1);
        }

        List<GemColor> best = getBestColors(weights, gameState, 4);
        if (best.size() >= 3 && gameRules.canTakeThreeDifferentGems(gameState.getGemBank())) {
            Collections.shuffle(best, getRandom());
            List<GemColor> choice = new ArrayList<>();
            choice.add(best.get(0));
            choice.add(best.get(1));
            choice.add(best.get(2));
            if (GameActions.takeThreeDifferent(this, gameState, gameRules, choice)) {
                return getName() + " took " + formatColors(choice) + ".";
            }
        }

        GemColor doubleColor = getBestDoubleColor(weights, gameState, gameRules);
        if (doubleColor != null && getRandom().nextBoolean()) {
            if (GameActions.takeTwoSame(this, gameState, gameRules, doubleColor)) {
                return getName() + " took 2 " + doubleColor.name() + ".";
            }
        }

        String fallback = fallbackRandomGemMove(gameState, gameRules);
        if (fallback.endsWith("had no useful move.")) {
            return "";
        }
        return fallback;
    }
}
