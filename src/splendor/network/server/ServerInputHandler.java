package splendor.network.server;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.config.*;
import splendor.display.*;
import splendor.entity.*;
import splendor.entity.bot.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.exception.*;
import splendor.rules.*;
import splendor.valueobjects.*;


public class ServerInputHandler {
    public ServerInputHandler() {}
    /**
     * Handles a take-three-different-gems action on the server side.
     *
     * @param player    the player taking gems
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param c1        the first gem color string
     * @param c2        the second gem color string
     * @param c3        the third gem color string
     * @return "SUCCESS" or an error message
     */
    public static String handleTakeThreeDifferent(Player player, GameState gameState, GameRules gameRules, String c1, String c2, String c3) {
        GemCollection gems = gameState.getGemBank();
        c1 = c1.toUpperCase();
        c2 = c2.toUpperCase();
        c3 = c3.toUpperCase();

        if (c1.equals(c2) || c1.equals(c3) || c2.equals(c3)) {
            return "ERROR: You must choose 3 different gem colors.";
        }

        if (!gameRules.validColor(c1) || c1.equals("GOLD_JOKER") ||
            !gameRules.validColor(c2) || c2.equals("GOLD_JOKER") ||
            !gameRules.validColor(c3) || c3.equals("GOLD_JOKER")) {
            return "ERROR: Invalid gem colors selected.";
        }

        GemCollection add = new GemCollection();
        add.add(GemColor.convertToColor(c1), 1);
        add.add(GemColor.convertToColor(c2), 1);
        add.add(GemColor.convertToColor(c3), 1);

        if (gameRules.canTakeThreeDifferentGems(add, gems)) {
            player.addGems(add);
            gems.subtract(add);
            return "SUCCESS";
        } else {
            return "ERROR: The bank does not have enough of those gems.";
        }
    }

    /**
     * Handles a take-two-same-gems action on the server side.
     *
     * @param player    the player taking gems
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param colorStr  the gem color string
     * @return "SUCCESS" or an error message
     */
    public static String handleTakeTwoSame(Player player, GameState gameState, GameRules gameRules, String colorStr) {
        GemCollection gems = gameState.getGemBank();
        colorStr = colorStr.toUpperCase();

        if (!gameRules.validColor(colorStr) || colorStr.equals("GOLD_JOKER")) {
            return "ERROR: Invalid gem color.";
        }

        GemColor col = GemColor.convertToColor(colorStr);

        if (gameRules.canTakeTwoSameGems(col, gems)) {
            GemCollection add = new GemCollection();
            add.add(col, 2);
            player.addGems(add);
            gems.subtract(add);
            return "SUCCESS";
        } else {
            return "ERROR: Not enough " + colorStr + " gems in the bank to take two.";
        }
    }

    /**
     * Handles a card reserve action on the server side.
     *
     * @param player    the player reserving the card
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param level     the card level (1, 2, or 3)
     * @param number    the card index (0-3 for visible, 4 for hidden draw)
     * @return "SUCCESS_JOKER", "SUCCESS_NO_JOKER", or an error message
     */
    public static String handleReserveCard(Player player, GameState gameState, GameRules gameRules, int level, int number) {
        if (!gameRules.canReserveCard(player)) {
            return "ERROR: You cannot reserve more than 3 cards.";
        }

        if (level < 1 || level > 3 || number < 0 || number > 4) {
            return "ERROR: Invalid level or card index.";
        }

        CardMarket cardMarket = gameState.getCardMarket();
        GemCollection gemBank = gameState.getGemBank();
        boolean gotJoker = false;

        try {
            DevelopmentCard chosen = null;

            if (number == 4) {
                chosen = cardMarket.drawCard(level);
                player.addReservedCard(chosen);
            } else {
                chosen = cardMarket.getVisibleCard(level, number);
                player.addReservedCard(chosen);
                cardMarket.removeCard(level, number);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));
            }

            GemColor jokerColor = GemColor.GOLD_JOKER;
            if (gemBank.getCount(jokerColor) > 0) {
                GemCollection gems = new GemCollection();
                gems.add(jokerColor, 1);
                gemBank.subtract(gems);
                player.addGems(gems);
                gotJoker = true;
            }

            return gotJoker ? "SUCCESS_JOKER" : "SUCCESS_NO_JOKER";

        } catch (UnavailableCardException e) {
            return "ERROR: That card or deck is no longer available.";
        }
    }
    /**
     * Handles a card purchase action on the server side.
     *
     * @param player     the player purchasing the card
     * @param gameState  the current game state
     * @param gameRules  the game rules for validation
     * @param isReserved true if purchasing from reserved hand, false if from table
     * @param level      the card level (ignored if isReserved)
     * @param index      the card index
     * @return "SUCCESS" if the purchase succeeded, or an error message
     */
    public static String handlePurchaseCard(Player player, GameState gameState, GameRules gameRules, boolean isReserved, int level, int index) {
        CardMarket cardMarket = gameState.getCardMarket();

        try {
            DevelopmentCard chosen = null;

            if (isReserved) {
                List<DevelopmentCard> reservedCards = player.getReservedCards();
                if (reservedCards.size() == 0) {
                    return "ERROR: You do not have any reserved cards.";
                }
                if (index < 0 || index >= reservedCards.size()) {
                    return "ERROR: Invalid reserved card index.";
                }
                chosen = reservedCards.get(index);

                if (!gameRules.canAffordCard(player, chosen)) {
                    return "ERROR: You cannot afford this reserved card.";
                }

                player.addCard(chosen);
                GemCollection cost = gameRules.calculateActualCost(player, chosen);
                player.deductGems(cost);
                player.removeReservedCard(chosen);
                gameState.addGemsToBank(cost);

                return "SUCCESS";

            } else {
                if (level < 1 || level > 3 || index < 0 || index > 3) {
                    return "ERROR: Invalid card level or index.";
                }
                chosen = cardMarket.getVisibleCard(level, index);

                if (!gameRules.canAffordCard(player, chosen)) {
                    return "ERROR: You cannot afford this card.";
                }
                player.addCard(chosen);
                GemCollection cost = gameRules.calculateActualCost(player, chosen);
                player.deductGems(cost);
                
                cardMarket.removeCard(level, index);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));
                gameState.addGemsToBank(cost);

                return "SUCCESS";
            }
        } catch (InvalidIndexException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}