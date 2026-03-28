package splendor.display;

import java.util.*;
import splendor.entity.player.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.valueobjects.*;
import splendor.exception.*;
import splendor.rules.*;

public class DisplayUI {

    public static int promptNumPlayers(Scanner sc) {
        int num = 0;
        while (num < 2 || num > 4) {
            try {
                System.out.print("How many players? (2 - 4): ");
                num = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a digit between 2 and 4\n");
                continue;
            }
            if (num < 2 || num > 4) {
                System.out.println("Please enter a digit between 2 and 4\n");
            }
        }
        System.out.println();
        return num;
    }

        public static int promptPlayerType(Scanner sc, int playerNumber) {
        while (true) {
            try {
                System.out.printf("Player %d type (1 = Human, 2 = Easy Bot, 3 = Hard Bot): ", playerNumber);
                int type = Integer.parseInt(sc.nextLine());
                if (type >= 1 && type <= 3) {
                    return type;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Invalid input\n");
        }
    }

    public static String promptPlayerName(Scanner sc, int playerNumber, String defaultName) {
        System.out.printf("Player %d name (blank for \"%s\"): ", playerNumber, defaultName);
        String name = sc.nextLine().trim();
        if (name.equals("")) {
            return defaultName;
        }
        return name;
    }

    public static ActionType promptAction(Scanner sc) {
        System.out.println("Pick an action");
        System.out.println("-> 1 - TAKE_THREE_DIFFERENT");
        System.out.println("-> 2 - TAKE_TWO_SAME");
        System.out.println("-> 3 - PURCHASE_CARD");
        System.out.println("-> 4 - RESERVE_CARD");
        System.out.print("Pick a number: ");
    
        boolean validAction = false;
        int action = 0;
        while (!validAction) {
            try {
                action = Integer.parseInt(sc.nextLine());
                if (action < 1 || action > 4) {
                    System.out.println("Invalid input");
                    System.out.print("\nPick a number: ");
                    continue;
                }
                validAction = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                System.out.print("\nPick a number: ");
            }
        }

        switch (action) {
            case 1:
                System.out.println();
                return ActionType.TAKE_THREE_DIFFERENT;
            case 2:
                System.out.println();
                return ActionType.TAKE_TWO_SAME;
            case 3:
                System.out.println();
                return ActionType.PURCHASE_CARD;
            default:
                System.out.println();
                return ActionType.RESERVE_CARD;
        }
    }
    
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