package splendor.rules;

import java.util.*;
import splendor.entity.player.*;
import splendor.entity.*;
import splendor.valueobjects.*;
import splendor.exception.*;
import splendor.entity.card.*;

public final class GameActions {
    private GameActions() {
    }

    public static boolean takeThreeDifferent(Player player, GameState gameState, GameRules gameRules, List<GemColor> colors) {
        if (colors == null || colors.size() != 3) {
            return false;
        }

        List<GemColor> unique = new ArrayList<>();
        for (GemColor color: colors) {
            if (color == null || color.equals(GemColor.GOLD_JOKER)) {
                return false;
            }
            if (unique.contains(color)) {
                return false;
            }
            unique.add(color);
        }

        GemCollection add = new GemCollection();
        for (GemColor color: colors) {
            add.add(color, 1);
        }

        if (!gameRules.canTakeThreeDifferentGems(add, gameState.getGemBank())) {
            return false;
        }

        player.addGems(add);
        gameState.removeGemsFromBank(add);
        return true;
    }

    public static boolean takeTwoSame(Player player, GameState gameState, GameRules gameRules, GemColor color) {
        if (color == null || color.equals(GemColor.GOLD_JOKER)) {
            return false;
        }
        if (!gameRules.canTakeTwoSameGems(color, gameState.getGemBank())) {
            return false;
        }

        GemCollection add = new GemCollection();
        add.add(color, 1);
        add.add(color, 1);
        player.addGems(add);
        gameState.removeGemsFromBank(add);
        return true;
    }

    public static boolean purchaseVisibleCard(Player player, GameState gameState, GameRules gameRules, int level, int index) {
        CardMarket cardMarket = gameState.getCardMarket();
        try {
            DevelopmentCard chosen = cardMarket.getVisibleCard(level, index);
            if (!gameRules.canAffordCard(player, chosen)) {
                return false;
            }

            GemCollection cost = gameRules.calculateActualCost(player, chosen);
            player.deductGems(cost);
            player.addCard(chosen);
            cardMarket.removeCard(level, index);
            cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));
            gameState.addGemsToBank(cost);
            return true;
        } catch (UnavailableCardException e) {
            return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean purchaseReservedCard(Player player, GameState gameState, GameRules gameRules, int index) {
        if (index < 0 || index >= player.getReservedCards().size()) {
            return false;
        }

        DevelopmentCard chosen = player.getReservedCards().get(index);
        if (!gameRules.canAffordCard(player, chosen)) {
            return false;
        }

        GemCollection cost = gameRules.calculateActualCost(player, chosen);
        player.deductGems(cost);
        player.addCard(chosen);
        player.removeReservedCard(chosen);
        gameState.addGemsToBank(cost);
        return true;
    }

    public static boolean reserveVisibleCard(Player player, GameState gameState, GameRules gameRules, int level, int index) {
        if (!gameRules.canReserveCard(player)) {
            return false;
        }

        CardMarket cardMarket = gameState.getCardMarket();
        try {
            DevelopmentCard chosen = cardMarket.getVisibleCard(level, index);
            player.addReservedCard(chosen);
            cardMarket.removeCard(level, index);
            cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));
            giveGoldJoker(player, gameState);
            return true;
        } catch (UnavailableCardException e) {
            return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean reserveHiddenCard(Player player, GameState gameState, GameRules gameRules, int level) {
        if (!gameRules.canReserveCard(player)) {
            return false;
        }

        try {
            DevelopmentCard chosen = gameState.getCardMarket().drawCard(level);
            player.addReservedCard(chosen);
            giveGoldJoker(player, gameState);
            return true;
        } catch (UnavailableCardException e) {
            return false;
        }
    }

    private static void giveGoldJoker(Player player, GameState gameState) {
        if (gameState.getGemBank().getCount(GemColor.GOLD_JOKER) < 1) {
            return;
        }

        GemCollection gold = new GemCollection();
        gold.add(GemColor.GOLD_JOKER, 1);
        player.addGems(gold);
        gameState.removeGemsFromBank(gold);
    }
}
