package splendor.display;
 
import java.util.*;
import splendor.entity.player.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.valueobjects.*;
import splendor.exception.*;
import splendor.rules.*;
 
public class DisplayUI {
 
    private static String gemName(GemColor c) {
        switch (c) {
            case DIAMOND:    return "Diamond";
            case SAPPHIRE:   return "Sapphire";
            case EMERALD:    return "Emerald";
            case RUBY:       return "Ruby";
            case ONYX:       return "Onyx";
            case GOLD_JOKER: return "Gold";
            default:         return "?";
        }
    }
 
    private static void clear() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }
 
    private static int gemTotal(Player p) {
        int total = 0;
        for (int v : p.getGems().getGems().values()) total += v;
        return total;
    }
 
    private static int g(Map<GemColor, Integer> m, GemColor c) {
        return m.getOrDefault(c, 0);
    }
 
    private static final String P_LINE =
        "  +--------------+-------+---------+----------+---------+------+------+------+-------+-----+";
    private static final String P_HEAD =
        "  | Name         | Score | Diamond | Sapphire | Emerald | Ruby | Onyx | Gold | Total | Res |";
    private static final String P_FMT =
        "  | %-12s | %5s | %7d | %8d | %7d | %4d | %4d | %4d | %5d | %3d |%n";

    private static final String BO_LINE =
        "  +--------------+---------+----------+---------+------+------+";
    private static final String BO_HEAD =
        "  | Name         | Diamond | Sapphire | Emerald | Ruby | Onyx |";
    private static final String BO_FMT =
        "  | %-12s | %7d | %8d | %7d | %4d | %4d |%n";

    private static final String N_LINE =
        "  +--------------+------+---------+----------+---------+------+------+";
    private static final String N_HEAD =
        "  | Name         | Pts  | Diamond | Sapphire | Emerald | Ruby | Onyx |";
    private static final String N_FMT =
        "  | %-12s | %4d | %7d | %8d | %7d | %4d | %4d |%n";

    private static final String C_LINE =
        "  +-----+----------+------+---------+----------+---------+------+------+";
    private static final String C_HEAD =
        "  | No. | Bonus    | Pts  | Diamond | Sapphire | Emerald | Ruby | Onyx |";
    private static final String C_FMT =
        "  | %3d | %-8s | %4s | %7d | %8d | %7d | %4d | %4d |%n";

    private static final String B_LINE =
        "  +---------+----------+---------+------+------+------+";
    private static final String B_HEAD =
        "  | Diamond | Sapphire | Emerald | Ruby | Onyx | Gold |";
    private static final String B_FMT =
        "  | %7d | %8d | %7d | %4d | %4d | %4d |%n";

    private static final String F_LINE =
        "  +--------------+-------+-------+---------+";
    private static final String F_HEAD =
        "  | Name         |  Pts  | Cards | Nobles  |";
    private static final String F_FMT =
        "  | %-12s | %5d | %5d | %7d |%n";
 
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
        System.out.println("  --- S P L E N D O R ---" + "                                                 Turn " + (gameState.getTurnCount() + 1));
        System.out.println();
 
        printPlayers(gameState);
        printAllBonuses(gameState);
        printNobles(gameState);
        printVisibleCards(gameState);
        printGemBank(gameState);
        printReservedCards(gameState.getCurrentPlayer());
 
        System.out.println("  > " + gameState.getCurrentPlayer().getName() + "'s turn");
    }

    private static void printPlayers(GameState gameState) {
        System.out.println("  PLAYERS");
        System.out.println(P_LINE);
        System.out.println(P_HEAD);
        System.out.println(P_LINE);
 
        int threshold = gameState.getWinningThreshold();
        for (Player p : gameState.getPlayers()) {
            boolean cur = (p == gameState.getCurrentPlayer());
            String label = (cur ? "> " : "  ") + p.getName();
            if (label.length() > 12) label = label.substring(0, 12);
            Map<GemColor, Integer> gems = p.getGems().getGems();
 
            System.out.printf(P_FMT,
                label,
                p.getPoints() + "/" + threshold,
                g(gems, GemColor.DIAMOND),
                g(gems, GemColor.SAPPHIRE),
                g(gems, GemColor.EMERALD),
                g(gems, GemColor.RUBY),
                g(gems, GemColor.ONYX),
                g(gems, GemColor.GOLD_JOKER),
                gemTotal(p),
                p.getReservedCards().size());
        }
        System.out.println(P_LINE);
        System.out.println();
    }
 
    public static void printPoints(GameState gameState) {
        printPlayers(gameState);
    }

    private static void printAllBonuses(GameState gameState) {
        System.out.println("  BONUSES (from purchased cards)");
        System.out.println(BO_LINE);
        System.out.println(BO_HEAD);
        System.out.println(BO_LINE);
 
        for (Player p : gameState.getPlayers()) {
            boolean cur = (p == gameState.getCurrentPlayer());
            String label = (cur ? "> " : "  ") + p.getName();
            if (label.length() > 12) label = label.substring(0, 12);
            Map<GemColor, Integer> b = p.calculateBonuses();
 
            System.out.printf(BO_FMT,
                label,
                g(b, GemColor.DIAMOND),
                g(b, GemColor.SAPPHIRE),
                g(b, GemColor.EMERALD),
                g(b, GemColor.RUBY),
                g(b, GemColor.ONYX));
        }
        System.out.println(BO_LINE);
        System.out.println();
    }

    public static void printNobles(GameState gameState) {
        System.out.println("  NOBLES");
        List<Noble> nobles = gameState.getAvailableNobles();
        if (nobles.isEmpty()) {
            System.out.println("  (none remaining)");
        } else {
            System.out.println(N_LINE);
            System.out.println(N_HEAD);
            System.out.println(N_LINE);
            for (Noble n : nobles) {
                Map<GemColor, Integer> r = n.getRequirements();
                String name = n.getName();
                if (name.length() > 12) name = name.substring(0, 12);
                System.out.printf(N_FMT, name, n.getPoints(),
                    g(r, GemColor.DIAMOND), g(r, GemColor.SAPPHIRE),
                    g(r, GemColor.EMERALD), g(r, GemColor.RUBY), g(r, GemColor.ONYX));
            }
            System.out.println(N_LINE);
        }
        System.out.println();
    }
 
    public static void printVisibleCards(GameState gameState) {
        CardMarket market = gameState.getCardMarket();
 
        for (int level = 3; level >= 1; level--) {
            int deckSz;
            try { deckSz = market.getDeckSize(level); }
            catch (Exception e) { deckSz = 0; }
 
            System.out.println("  CARD MARKET - Level " + level + " (" + deckSz + " in deck)");
 
            try {
                List<DevelopmentCard> cards = market.getVisibleCards(level);
                if (cards.isEmpty()) {
                    System.out.println("  (no cards)");
                } else {
                    System.out.println(C_LINE);
                    System.out.println(C_HEAD);
                    System.out.println(C_LINE);
                    for (int i = 0; i < cards.size(); i++) {
                        printCardRow(cards.get(i), i);
                    }
                    System.out.println(C_LINE);
                }
            } catch (UnavailableCardException e) {
                System.out.println("  (no cards)");
            }
            System.out.println();
        }
    }
 
    private static void printCardRow(DevelopmentCard c, int index) {
        Map<GemColor, Integer> cost = c.getCost().getCost().getGems();
        String pts = c.getPoints() > 0 ? String.valueOf(c.getPoints()) : "-";
        System.out.printf(C_FMT, index, gemName(c.getBonus()), pts,
            g(cost, GemColor.DIAMOND), g(cost, GemColor.SAPPHIRE),
            g(cost, GemColor.EMERALD), g(cost, GemColor.RUBY), g(cost, GemColor.ONYX));
    }
 
    public static void printGemBank(GameState gameState) {
        System.out.println("  GEM BANK");
        Map<GemColor, Integer> gems = gameState.getGemBank().getGems();
        System.out.println(B_LINE);
        System.out.println(B_HEAD);
        System.out.println(B_LINE);
        System.out.printf(B_FMT,
            g(gems, GemColor.DIAMOND), g(gems, GemColor.SAPPHIRE),
            g(gems, GemColor.EMERALD), g(gems, GemColor.RUBY),
            g(gems, GemColor.ONYX), g(gems, GemColor.GOLD_JOKER));
        System.out.println(B_LINE);
        System.out.println();
    }
  
    public static void printReservedCards(Player player) {
        int count = player.getReservedCards().size();
        System.out.println("  YOUR RESERVED CARDS (" + count + "/3)");
        if (count == 0) {
            System.out.println("  (none)");
        } else {
            System.out.println(C_LINE);
            System.out.println(C_HEAD);
            System.out.println(C_LINE);
            for (int i = 0; i < count; i++) {
                printCardRow(player.getReservedCards().get(i), i);
            }
            System.out.println(C_LINE);
        }
        System.out.println();
    }
 
    public static void printPlayerGem(Player player) {
        System.out.println("  YOUR GEMS");
        Map<GemColor, Integer> gems = player.getGems().getGems();
        System.out.println(B_LINE);
        System.out.println(B_HEAD);
        System.out.println(B_LINE);
        System.out.printf(B_FMT,
            g(gems, GemColor.DIAMOND), g(gems, GemColor.SAPPHIRE),
            g(gems, GemColor.EMERALD), g(gems, GemColor.RUBY),
            g(gems, GemColor.ONYX), g(gems, GemColor.GOLD_JOKER));
        System.out.println(B_LINE);
        System.out.println("  Total: " + gemTotal(player) + "/10");
        System.out.println();
    }
 
    public static void printWinner(GameState gameState, GameRules gameRules) {
        Player winner = gameRules.getWinner(gameState.getPlayers());
 
        System.out.println();
        System.out.println("  =========================================================");
        System.out.println("  GAME OVER -- " + winner.getName() + " wins with " + winner.getPoints() + " points!");
        System.out.println("  =========================================================");
        System.out.println();
 
        System.out.println("  FINAL STANDINGS");
        System.out.println(F_LINE);
        System.out.println(F_HEAD);
        System.out.println(F_LINE);
        List<Player> sorted = new ArrayList<>(gameState.getPlayers());
        sorted.sort((a, b) -> b.getPoints() - a.getPoints());
        for (Player p : sorted) {
            String name = p.getName();
            if (name.length() > 12) name = name.substring(0, 12);
            System.out.printf(F_FMT, name, p.getPoints(),
                p.getPurchasedCards().size(), p.getClaimedNobles().size());
        }
        System.out.println(F_LINE);
        System.out.println();
    }
}
