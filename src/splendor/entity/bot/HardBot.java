package splendor.entity.bot;

import java.util.*;
import splendor.rules.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.entity.*;

public class HardBot extends Bot {
    public HardBot(String name, int turnOrder) {
        super(name, turnOrder);
    }

    @Override
    protected String chooseMove(GameState gameState, GameRules gameRules) {
        boolean nobleFocus = gameState.getPlayers().size() >= 3;
        List<CardChoice> affordable = getAffordableChoices(gameState, gameRules);
        if (affordable.size() > 0) {
            CardChoice choice = getBestChoice(affordable, gameState, gameRules, nobleFocus);
            return buyChoice(choice, gameState, gameRules);
        }

        String gemMove = chooseGemMove(gameState, gameRules, nobleFocus);
        if (!gemMove.equals("")) {
            return gemMove;
        }
        
        CardChoice reserveChoice = getReserveChoice(gameState, gameRules, nobleFocus);
        if (reserveChoice != null) {
            return reserveChoice(reserveChoice, gameState, gameRules);
        }

        if (gameRules.canReserveCard(this) && isEarlyGame(gameState)) {
            int level = getHiddenReserveLevel(gameState, nobleFocus);
            if (level > 0) {
                return reserveHidden(level, gameState, gameRules);
            }
        }

        return fallbackRandomGemMove(gameState, gameRules);
    }

    private CardChoice getBestChoice(List<CardChoice> choices, GameState gameState, GameRules gameRules, boolean nobleFocus) {
        int bestScore = Integer.MIN_VALUE;
        List<CardChoice> bestChoices = new ArrayList<>();

        for (CardChoice choice: choices) {
            int score = getCardScore(choice, gameState, gameRules, nobleFocus);
            if (score > bestScore) {
                bestChoices.clear();
                bestChoices.add(choice);
                bestScore = score;
            } else if (score == bestScore) {
                bestChoices.add(choice);
            }
        }

        return chooseRandomChoice(bestChoices);
    }

    private List<CardChoice> getPriorityTargets(GameState gameState, GameRules gameRules, boolean nobleFocus, int limit) {
        List<CardChoice> remaining = new ArrayList<>(getAllChoices(gameState));
        List<CardChoice> targets = new ArrayList<>();

        while (targets.size() < limit && remaining.size() > 0) {
            CardChoice best = remaining.get(0);
            for (CardChoice choice: remaining) {
                if (getCardScore(choice, gameState, gameRules, nobleFocus) > getCardScore(best, gameState, gameRules, nobleFocus)) {
                    best = choice;
                }
            }
            targets.add(best);
            remaining.remove(best);
        }
        return targets;
    }

    private int getCardScore(CardChoice choice, GameState gameState, GameRules gameRules, boolean nobleFocus) {
        Card card = choice.getCard();
        int score = card.getPoints() * 4;
        score += getDiscountUsefulness(card, gameState, nobleFocus);
        score += getNobleUsefulness(card, gameState, nobleFocus);
        score -= getMissingGems(gameRules, card) * 2;
        score -= getWasteCount(gameRules, card);
        score -= getTierPenalty(card, gameState, gameRules);
        score += getDenyBonus(choice, gameState, gameRules);

        if (choice.isReserved()) {
            score += 3;
        }
        if (!nobleFocus) {
            score += card.getPoints() * 2;
        }
        if (isEarlyGame(gameState) && card.getLevel() == 1 && card.getPoints() == 0) {
            score += 8;
        }
        return score;
    }

    private int getDiscountUsefulness(Card card, GameState gameState, boolean nobleFocus) {
        DevelopmentCard card2 = (DevelopmentCard) card;
        int usefulness = 0;
        Map<GemColor, Integer> bonuses = calculateBonuses();

        for (CardChoice choice: getVisibleChoices(gameState)) {
            Card other = choice.getCard();
            if (other.equals(card)) {
                continue;
            }
            if (other.getCost().getRequired(card2.getBonus()) > 0) {
                usefulness += other.getPoints() + 1;
            }
        }

        for (Card reserved: getReservedCards()) {
            DevelopmentCard reserved2 = (DevelopmentCard) reserved;
            if (reserved2.equals(card2)) {
                continue;
            }
            if (reserved.getCost().getRequired(card2.getBonus()) > 0) {
                usefulness += 2;
            }
        }

        if (bonuses.get(card2.getBonus()) < 3) {
            usefulness += (3 - bonuses.get(card2.getBonus())) * 2;
        }
        if (nobleFocus && getOpeningNobleColors(gameState).contains(card2.getBonus())) {
            usefulness += 4;
        }
        return usefulness;
    }

    private int getNobleUsefulness(Card card, GameState gameState, boolean nobleFocus) {
        DevelopmentCard card2 = (DevelopmentCard) card;
        int usefulness = 0;
        Map<GemColor, Integer> bonuses = calculateBonuses();

        for (Noble noble: gameState.getAvailableNobles()) {
            int required = noble.getRequirements().get(card2.getBonus());
            if (required <= bonuses.get(card2.getBonus())) {
                continue;
            }
            usefulness += noble.getPoints();
            if (nobleFocus) {
                usefulness += 6;
            } else {
                usefulness += 2;
            }
            if (countRemainingNobleColors(noble) == 1) {
                usefulness += 6;
            }
        }
        return usefulness;
    }

    private int getTierPenalty(Card card, GameState gameState, GameRules gameRules) {
        DevelopmentCard card2 = (DevelopmentCard) card;
        if (!isEarlyGame(gameState)) {
            return 0;
        }

        int missing = getMissingGems(gameRules, card2);
        if (card2.getLevel() == 3 && missing >= 4) {
            return 8 + missing;
        }
        if (card2.getLevel() == 2 && missing >= 3) {
            return 4 + missing;
        }
        if (card2.getLevel() == 1 && missing >= 4) {
            return 2;
        }
        return 0;
    }

    private int getDenyBonus(CardChoice choice, GameState gameState, GameRules gameRules) {
        if (choice.isReserved()) {
            return 0;
        }

        int deny = 0;
        for (Player player: gameState.getPlayers()) {
            if (player == this) {
                continue;
            }
            int threat = assessThreat(player, gameState, gameRules);
            int missing = gameRules.countMissingGems(player, choice.getCard());
            if (missing == 0) {
                deny += 6 + threat / 8;
            } else if (missing == 1) {
                deny += 4 + threat / 10;
            } else if (missing == 2) {
                deny += 2 + threat / 12;
            }
            if (choice.getCard().getPoints() > 0 && player.getPoints() >= gameState.getWinningThreshold() - 3) {
                deny += 3 + choice.getCard().getPoints() * 2;
            }
        }
        return deny;
    }

    private int assessThreat(Player player, GameState gameState, GameRules gameRules) {
        int threat = player.getPoints() * 3;
        if (player.getPoints() >= gameState.getWinningThreshold() - 3) {
            threat += 10;
        }

        int bestCard = 0;
        List<Card> cards = new ArrayList<>();
        for (CardChoice choice: getVisibleChoices(gameState)) {
            cards.add(choice.getCard());
        }
        cards.addAll(player.getReservedCards());

        for (Card card: cards) {
            int cardThreat = card.getPoints() * 3;
            int missing = gameRules.countMissingGems(player, card);
            if (missing == 0) {
                cardThreat += 8;
            } else if (missing == 1) {
                cardThreat += 5;
            } else if (missing == 2) {
                cardThreat += 3;
            }
            if (cardThreat > bestCard) {
                bestCard = cardThreat;
            }
        }

        threat += bestCard;
        threat += player.getReservedCards().size();
        return threat;
    }

    private CardChoice getReserveChoice(GameState gameState, GameRules gameRules, boolean nobleFocus) {
        if (!gameRules.canReserveCard(this)) {
            return null;
        }

        List<CardChoice> visible = getVisibleChoices(gameState);
        if (visible.size() == 0) {
            return null;
        }

        CardChoice best = getBestChoice(visible, gameState, gameRules, nobleFocus);
        int score = getCardScore(best, gameState, gameRules, nobleFocus);
        int missing = getMissingGems(gameRules, best.getCard());
        int deny = getDenyBonus(best, gameState, gameRules);

        if (isEarlyGame(gameState) && getReservedCards().size() < 2) {
            if (score >= 12 || missing <= 1 || nobleFocus && getOpeningNobleColors(gameState).contains(best.getCard().getBonus())) {
                return best;
            }
        }
        if (deny >= 6) {
            return best;
        }
        if (best.getCard().getPoints() >= 3 && missing <= 4) {
            return best;
        }
        return null;
    }

    private String chooseGemMove(GameState gameState, GameRules gameRules, boolean nobleFocus) {
        Map<GemColor, Integer> weights = new HashMap<>();
        List<CardChoice> targets = getPriorityTargets(gameState, gameRules, nobleFocus, 4);

        for (int i = 0; i < targets.size(); i++) {
            int base = 3;
            if (i == 0) {
                base = 8;
            } else if (i == 1) {
                base = 5;
            }

            CardChoice choice = targets.get(i);
            List<GemColor> needed = getNeededColorsForCard(gameRules, choice.getCard());
            for (GemColor color: needed) {
                Map<GemColor, Integer> cost = gameRules.getDiscountedCost(this, choice.getCard());
                int required = cost.get(color);
                int owned = getSpecificGem(color);
                if (required > owned) {
                    addWeight(weights, color, (required - owned) * base);
                } else {
                    addWeight(weights, color, base);
                }
            }
            if (choice.getCard() instanceof DevelopmentCard) {
                DevelopmentCard card = (DevelopmentCard) choice.getCard();
                addWeight(weights, card.getBonus(), base - 2);
            }
        }

        if (nobleFocus) {
            List<GemColor> nobleColors = getOpeningNobleColors(gameState);
            for (GemColor color: nobleColors) {
                addWeight(weights, color, 3);
            }
        }

        Map<GemColor, Integer> bonus = calculateBonuses();
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            if (bonus.get(color) >= 3) {
                addWeight(weights, color, -(bonus.get(color) - 2) * 2);
            }
            if (getSpecificGem(color) > 2) {
                addWeight(weights, color, -(getSpecificGem(color) - 2) * 2);
            }
        }

        String move = takeWeightedGemMove(gameState, gameRules, weights);
        if (move.endsWith("had no useful move.")) {
            return "";
        }
        return move;
    }

    private List<GemColor> getOpeningNobleColors(GameState gameState) {
        Map<GemColor, Integer> counts = new HashMap<>();
        for (GemColor color: GemColor.values()) {
            counts.put(color, 0);
        }

        for (Noble noble: gameState.getAvailableNobles()) {
            for (Map.Entry<GemColor, Integer> entry: noble.getRequirements().entrySet()) {
                counts.put(entry.getKey(), counts.get(entry.getKey()) + entry.getValue());
            }
        }

        List<GemColor> chosen = new ArrayList<>();
        while (chosen.size() < 3) {
            GemColor best = null;
            for (GemColor color: GemColor.values()) {
                if (color.equals(GemColor.GOLD_JOKER) || chosen.contains(color)) {
                    continue;
                }
                if (best == null || counts.get(color) > counts.get(best)) {
                    best = color;
                }
            }
            if (best == null) {
                break;
            }
            chosen.add(best);
        }
        return chosen;
    }

    private int getHiddenReserveLevel(GameState gameState, boolean nobleFocus) {
        int preferred = nobleFocus ? 2 : 3;
        if (gameState.getCardMarket().getDeckSize(preferred) > 0) {
            return preferred;
        }
        if (gameState.getCardMarket().getDeckSize(2) > 0) {
            return 2;
        }
        if (gameState.getCardMarket().getDeckSize(1) > 0) {
            return 1;
        }
        if (gameState.getCardMarket().getDeckSize(3) > 0) {
            return 3;
        }
        return 0;
    }
}
