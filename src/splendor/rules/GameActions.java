package splendor.rules;

import java.util.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.exception.*;
import splendor.valueobjects.*;

/**
 * Utility class containing static methods that execute player actions
 * (taking gems, purchasing cards, reserving cards) and modify game state.
 */
public final class GameActions {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GameActions() {
    }

    /**
     * Takes three different-colored gems from the bank for a player.
     *
     * @param player    the player taking the gems
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param colors    the list of three distinct gem colors to take
     * @return true if the action succeeded, false otherwise
     */
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

    /**
     * Takes two gems of the same color from the bank for a player.
     *
     * @param player    the player taking the gems
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param color     the gem color to take two of
     * @return true if the action succeeded, false otherwise
     */
    public static boolean takeTwoSame(Player player, GameState gameState, GameRules gameRules, GemColor color) {
        if (color == null || color.equals(GemColor.GOLD_JOKER)) {
            return false;
        }
        if (!gameRules.canTakeTwoSameGems(color, gameState.getGemBank())) {
            return false;
        }

        GemCollection add = new GemCollection();
        add.add(color, 2);
        player.addGems(add);
        gameState.removeGemsFromBank(add);
        return true;
    }

    /**
     * Purchases a visible card from the market for a player.
     *
     * @param player    the player purchasing the card
     * @param gameState the current game state
     * @param gameRules the game rules for affordability checks
     * @param level     the card market level (1, 2, or 3)
     * @param index     the index of the card in the visible row
     * @return true if the purchase succeeded, false otherwise
     */
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
        } catch (InvalidIndexException e) {
            return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Purchases a reserved card from the player's hand.
     *
     * @param player    the player purchasing the card
     * @param gameState the current game state
     * @param gameRules the game rules for affordability checks
     * @param index     the index of the card in the player's reserved list
     * @return true if the purchase succeeded, false otherwise
     */
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

    /**
     * Reserves a visible card from the market for a player, giving a gold joker if available.
     *
     * @param player    the player reserving the card
     * @param gameState the current game state
     * @param gameRules the game rules for reserve-slot validation
     * @param level     the card market level (1, 2, or 3)
     * @param index     the index of the card in the visible row
     * @return true if the reserve succeeded, false otherwise
     */
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
        } catch (InvalidIndexException e) {
            return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Reserves a hidden card drawn from the top of a deck at the given level.
     *
     * @param player    the player reserving the card
     * @param gameState the current game state
     * @param gameRules the game rules for reserve-slot validation
     * @param level     the deck level (1, 2, or 3)
     * @return true if the reserve succeeded, false otherwise
     */
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

    /**
     * Gives a gold joker gem to a player from the bank, if any are available.
     *
     * @param player    the player to receive the joker
     * @param gameState the current game state
     */
    public static void giveGoldJoker(Player player, GameState gameState) {
        if (gameState.getGemBank().getCount(GemColor.GOLD_JOKER) < 1) {
            return;
        }

        GemCollection gold = new GemCollection();
        gold.add(GemColor.GOLD_JOKER, 1);
        player.addGems(gold);
        gameState.removeGemsFromBank(gold);
    }
}
