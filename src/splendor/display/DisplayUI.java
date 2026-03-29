package splendor.display;

import java.util.*;
import splendor.entity.player.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.valueobjects.*;
import splendor.exception.*;
import splendor.rules.*;

public class DisplayUI {

    private static final GemColor[] REGULAR = {
        GemColor.DIAMOND, GemColor.SAPPHIRE, GemColor.EMERALD, GemColor.RUBY, GemColor.ONYX
    };
    private static final GemColor[] ALL_COLORS = {
        GemColor.DIAMOND, GemColor.SAPPHIRE, GemColor.EMERALD, GemColor.RUBY, GemColor.ONYX, GemColor.GOLD_JOKER
    };

    private static String repeat(char ch, int n) {
        if (n <= 0) return "";
        char[] arr = new char[n];
        Arrays.fill(arr, ch);
        return new String(arr);
    }

    private static String gem(GemColor c) {
        switch (c) {
            case DIAMOND:    return "Dia";
            case SAPPHIRE:   return "Sap";
            case EMERALD:    return "Eme";
            case RUBY:       return "Rub";
            case ONYX:       return "Ony";
            case GOLD_JOKER: return "Gld";
            default:         return "???";
        }
    }

    private static void divider() {
        System.out.println("  " + repeat('-', 56));
    }

    private static void heading(String title) {
        divider();
        System.out.println("  " + title);
        divider();
    }

    private static void clear() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    private static String costStr(Map<GemColor, Integer> cost) {
        StringBuilder sb = new StringBuilder();
        for (GemColor c : REGULAR) {
            int amt = cost.getOrDefault(c, 0);
            if (amt > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(amt).append(gem(c));
            }
        }
        return sb.toString();
    }

    public static int promptNumPlayers(Scanner sc) {
        clear();
        System.out.println();
        System.out.println("  --- S P L E N D O R ---");
        System.out.println();
        int num = 0;
        while (num < 2 || num > 4) {
            try {
                System.out.print("  How many players? (2-4): ");
                num = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("  Please enter 2, 3, or 4.\n");
                continue;
            }
            if (num < 2 || num > 4) {
                System.out.println("  Please enter 2, 3, or 4.\n");
            }
        }
        System.out.println();
        return num;
    }

    public static int promptPlayerType(Scanner sc, int playerNumber) {
        while (true) {
            try {
                System.out.print("  Player " + playerNumber + " type (1=Human  2=EasyBot  3=HardBot): ");
                int type = Integer.parseInt(sc.nextLine());
                if (type >= 1 && type <= 3) return type;
            } catch (NumberFormatException e) { }
            System.out.println("  Invalid input.\n");
        }
    }

    public static String promptPlayerName(Scanner sc, int playerNumber, String defaultName) {
        System.out.print("  Player " + playerNumber + " name (blank = \"" + defaultName + "\"): ");
        String name = sc.nextLine().trim();
        return name.isEmpty() ? defaultName : name;
    }

    public static ActionType promptAction(Scanner sc) {
        System.out.println();
        System.out.println("  1. Take 3 different gems");
        System.out.println("  2. Take 2 same gems");
        System.out.println("  3. Purchase a card");
        System.out.println("  4. Reserve a card");
        System.out.print("  Choose: ");

        while (true) {
            try {
                int action = Integer.parseInt(sc.nextLine());
                if (action >= 1 && action <= 4) {
                    System.out.println();
                    switch (action) {
                        case 1: return ActionType.TAKE_THREE_DIFFERENT;
                        case 2: return ActionType.TAKE_TWO_SAME;
                        case 3: return ActionType.PURCHASE_CARD;
                        default: return ActionType.RESERVE_CARD;
                    }
                }
            } catch (NumberFormatException e) { }
            System.out.print("  Pick 1-4: ");
        }
    }

    public static void printGameState(GameState gameState) {
        clear();
        System.out.println();
        System.out.println("  --- S P L E N D O R ---              Turn " + (gameState.getTurnCount() + 1));
        System.out.println();

        printPoints(gameState);
        printNobles(gameState);
        printVisibleCards(gameState);
        printGemBank(gameState);
        printPlayerGem(gameState.getCurrentPlayer());
        printReservedCards(gameState.getCurrentPlayer());

        System.out.println("  > " + gameState.getCurrentPlayer().getName() + "'s turn");
    }

    public static void printPoints(GameState gameState) {
        heading("SCOREBOARD");
        int threshold = gameState.getWinningThreshold();
        for (Player p : gameState.getPlayers()) {
            boolean cur = (p == gameState.getCurrentPlayer());
            String arrow = cur ? "> " : "  ";
            System.out.printf("  %s%-14s  %d/%d%n", arrow, p.getName(), p.getPoints(), threshold);
        }
        System.out.println();
    }

    public static void printNobles(GameState gameState) {
        heading("NOBLES");
        List<Noble> nobles = gameState.getAvailableNobles();
        if (nobles.isEmpty()) {
            System.out.println("  (none remaining)");
        } else {
            for (Noble n : nobles) {
                Map<GemColor, Integer> reqs = n.getRequirements();
                StringBuilder reqStr = new StringBuilder();
                for (GemColor c : REGULAR) {
                    int amt = reqs.getOrDefault(c, 0);
                    if (amt > 0) {
                        if (reqStr.length() > 0) reqStr.append(" ");
                        reqStr.append(amt).append(gem(c));
                    }
                }
                System.out.println("  " + n.getName() + " (" + n.getPoints() + "pts)  needs: " + reqStr);
            }
        }
        System.out.println();
    }

    public static void printVisibleCards(GameState gameState) {
        heading("CARD MARKET");
        CardMarket market = gameState.getCardMarket();

        for (int level = 3; level >= 1; level--) {
            int deckSz;
            try { deckSz = market.getDeckSize(level); }
            catch (Exception e) { deckSz = 0; }

            System.out.println("  Lv." + level + "  (" + deckSz + " in deck)");

            try {
                List<DevelopmentCard> cards = market.getVisibleCards(level);
                for (int i = 0; i < cards.size(); i++) {
                    DevelopmentCard card = cards.get(i);
                    String pts = card.getPoints() > 0 ? card.getPoints() + "pt" : " -";
                    System.out.println("    [" + i + "]  " + gem(card.getBonus())
                            + "  " + pts + "   cost: " + costStr(card.getCost().getCost().getGems()));
                }
            } catch (UnavailableCardException e) {
                System.out.println("    (no cards)");
            }

            if (level > 1) System.out.println();
        }
        System.out.println();
    }

    public static void printGemBank(GameState gameState) {
        heading("GEM BANK");
        Map<GemColor, Integer> gems = gameState.getGemBank().getGems();
        StringBuilder sb = new StringBuilder("  ");
        for (GemColor c : ALL_COLORS) {
            sb.append(gem(c)).append(":").append(gems.getOrDefault(c, 0)).append("  ");
        }
        System.out.println(sb);
        System.out.println();
    }

    public static void printPlayerGem(Player player) {
        heading("YOUR GEMS & BONUSES");
        Map<GemColor, Integer> gems = player.getGems().getGems();
        Map<GemColor, Integer> bonus = player.calculateBonuses();

        StringBuilder sb = new StringBuilder("  ");
        int total = 0;
        for (GemColor c : ALL_COLORS) {
            int count = gems.getOrDefault(c, 0);
            total += count;
            int bon = bonus.getOrDefault(c, 0);
            sb.append(gem(c)).append(":").append(count);
            if (!c.equals(GemColor.GOLD_JOKER) && bon > 0) {
                sb.append("+").append(bon);
            }
            sb.append("  ");
        }
        System.out.println(sb);
        System.out.println("  Total: " + total + "/10");
        System.out.println();
    }

    public static void printReservedCards(Player player) {
        int count = player.getReservedCards().size();
        heading("RESERVED CARDS (" + count + "/3)");
        if (count == 0) {
            System.out.println("  (none)");
        } else {
            for (int i = 0; i < count; i++) {
                DevelopmentCard dc = player.getReservedCards().get(i);
                String pts = dc.getPoints() > 0 ? dc.getPoints() + "pt" : " -";
                System.out.println("    [" + i + "]  " + gem(dc.getBonus())
                        + "  " + pts + "   cost: " + costStr(dc.getCost().getCost().getGems()));
            }
        }
        System.out.println();
    }

    public static void printWinner(GameState gameState, GameRules gameRules) {
        Player winner = gameRules.getWinner(gameState.getPlayers());

        System.out.println();
        divider();
        System.out.println("  GAME OVER — " + winner.getName() + " wins with " + winner.getPoints() + " points!");
        divider();
        System.out.println();

        System.out.println("  FINAL STANDINGS");
        List<Player> sorted = new ArrayList<>(gameState.getPlayers());
        sorted.sort((a, b) -> b.getPoints() - a.getPoints());
        int rank = 1;
        for (Player p : sorted) {
            System.out.printf("  %d. %-14s  %dpts  %d cards  %d nobles%n",
                    rank, p.getName(), p.getPoints(),
                    p.getPurchasedCards().size(), p.getClaimedNobles().size());
            rank++;
        }
        System.out.println();
    }
}
