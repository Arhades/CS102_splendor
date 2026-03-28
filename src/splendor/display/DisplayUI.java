package splendor.display;

import java.util.*;
import splendor.entity.player.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.valueobjects.*;
import splendor.exception.*;
import splendor.rules.*;

public class DisplayUI {
    
    public static void printGameState(GameState gameState) {
        printPlayer(gameState);
        printNobles(gameState);
        printVisibleCards(gameState);
        printGemBank(gameState);
        printPoints(gameState);
        printReservedCards(gameState.getCurrentPlayer());
        printPlayerGem(gameState.getCurrentPlayer());
    }

    public static void printPlayer(GameState gameState) {
        System.out.println(String.format("%s's turn", gameState.getCurrentPlayer().getName()));
        System.out.println();
    }

    public static void printNobles(GameState gameState) {
        System.out.println("NOBLES AVAILABLE");
        List<Noble> nobles = gameState.getAvailableNobles();
        if (nobles.size() == 0) {
            System.out.println("No more nobles");
        }
        for (Noble noble: nobles) {
            System.out.println("-> " + noble);
        }
        System.out.println();
    }

    public static void printVisibleCards(GameState gameState) {
        System.out.println("AVAILABLE CARDS FOR PURCHASE");
        List<Card> cards = new ArrayList<>();
        CardMarket cardMarket = gameState.getCardMarket();

        for (int i = 1; i <= 3; i++) {
            try {
                cards.addAll(cardMarket.getVisibleCards(i));
            } catch (UnavailableCardException e) {}
        }
        
        int i = 0;
        for (Card card: cards) {
            System.out.println(String.format("-> Number: %d | %s", i++, card));
            if (i == 4) {
                i = 0;
            }
        }
        System.out.println();
    }

    public static void printReservedCards(Player player) {
        System.out.println("PLAYER'S RESERVED CARDS");
        if (player.getReservedCards().size() == 0) {
            System.out.println("-> EMPTY\n");
            return;
        }

        int i = 0;
        for (Card card: player.getReservedCards()) {
            System.out.println(String.format("-> Number: %d | %s", i++, card));
        }
        System.out.println();
    }

    public static void printGemBank(GameState gameState) {
        System.out.println("GEMBANK");
        Map<GemColor, Integer> gems = gameState.getGemBank().getGems();

        for (GemColor color: gems.keySet()) {
            System.out.println(String.format("-> %s: %d", color.name(), gems.get(color)));
        }
        System.out.println();
    }

    public static void printPlayerGem(Player player) {
        System.out.println("PLAYER'S GEMS");
        Map<GemColor, Integer> gems = player.getGems().getGems();
        Map<GemColor, Integer> bonus = player.calculateBonuses();

        for (GemColor color: gems.keySet()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                System.out.println(String.format("-> %s: %d", color.name(), gems.get(color)));
                continue;
            }
            System.out.println(String.format("-> %s: %d / bonus = %d", color.name(), gems.get(color), bonus.get(color)));
        }
        System.out.println();
    }

    public static void printPoints(GameState gameState) {
        System.out.println("POINTS");
        for (Player player: gameState.getPlayers()) {
            System.out.println(String.format("-> name: %s = %d", player.getName(), player.getPoints()));
        }
        System.out.println();
    }

    public static void printWinner(GameState gameState, GameRules gameRules) {
        List<Player> players = gameState.getPlayers();
        System.out.println(String.format("Winner: %s!!", gameRules.getWinner(players).getName()));
    }
}