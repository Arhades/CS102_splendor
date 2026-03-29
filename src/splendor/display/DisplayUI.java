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

    // ── table borders ────────────────────────────────────────────────

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

    // Lobby table
    private static final String L_LINE =
        "  +-----+--------------+--------+";
    private static final String L_HEAD =
        "  | No. | Name         | Status |";
    private static final String L_FMT =
        "  | %3d | %-12s | %-6s |%n";

    // ══════════════════════════════════════════════════════════════════
    //  HELPER: card row as string
    // ══════════════════════════════════════════════════════════════════

    private static String cardRowStr(DevelopmentCard c, int index) {
        Map<GemColor, Integer> cost = c.getCost().getCost().getGems();
        String pts = c.getPoints() > 0 ? String.valueOf(c.getPoints()) : "-";
        return String.format(C_FMT, index, gemName(c.getBonus()), pts,
            g(cost, GemColor.DIAMOND), g(cost, GemColor.SAPPHIRE),
            g(cost, GemColor.EMERALD), g(cost, GemColor.RUBY), g(cost, GemColor.ONYX));
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOBBY / PRE-GAME METHODS (for multiplayer)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns the splash/title screen shown when a player first connects.
     */
    public static String getBanner() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  =========================================================\n");
        sb.append("              --- S P L E N D O R ---\n");
        sb.append("  =========================================================\n");
        sb.append("\n");
        return sb.toString();
    }

    public static void printBanner() {
        System.out.print(getBanner());
    }

    /**
     * Returns the waiting lobby display showing connected players.
     * @param playerNames  list of player names in join order
     * @param readyStates  list of booleans — true if that player is ready
     */
    public static String getLobby(List<String> playerNames, List<Boolean> readyStates) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  =============== WAITING LOBBY ===============\n");
        sb.append("\n");
        sb.append(L_LINE).append("\n");
        sb.append(L_HEAD).append("\n");
        sb.append(L_LINE).append("\n");
        for (int i = 0; i < playerNames.size(); i++) {
            String name = playerNames.get(i);
            if (name.length() > 12) name = name.substring(0, 12);
            String status = readyStates.get(i) ? "Ready" : "...";
            sb.append(String.format(L_FMT, i + 1, name, status));
        }
        sb.append(L_LINE).append("\n");
        sb.append("  " + playerNames.size() + "/4 players connected\n");
        sb.append("\n");
        return sb.toString();
    }

    public static void printLobby(List<String> playerNames, List<Boolean> readyStates) {
        System.out.print(getLobby(playerNames, readyStates));
    }

    /**
     * Returns the prompt asking for player name.
     */
    public static String getNamePrompt() {
        return "  Enter your player name: ";
    }

    /**
     * Returns the prompt asking for birth date.
     */
    public static String getBirthDatePrompt() {
        return "  Enter your birth date (dd/mm/yyyy): ";
    }

    /**
     * Returns a message telling the player who the youngest is and who starts.
     */
    public static String getStartMessage(String youngestName) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  =========================================================\n");
        sb.append("  The youngest player is: " + youngestName + "\n");
        sb.append("  " + youngestName + " will go first!\n");
        sb.append("  =========================================================\n");
        sb.append("\n");
        return sb.toString();
    }

    public static void printStartMessage(String youngestName) {
        System.out.print(getStartMessage(youngestName));
    }

    /**
     * Returns the action menu shown on a player's turn.
     */
    public static String getActionMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  1. Take 3 different gems\n");
        sb.append("  2. Take 2 same gems\n");
        sb.append("  3. Purchase a card\n");
        sb.append("  4. Reserve a card\n");
        sb.append("  Choose: ");
        return sb.toString();
    }

    public static void printActionMenu() {
        System.out.print(getActionMenu());
    }

    // ══════════════════════════════════════════════════════════════════
    //  GAME STATE — STRING VERSIONS
    // ══════════════════════════════════════════════════════════════════

    public static String getGameState(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  --- S P L E N D O R ---                                                 Turn " + (gameState.getTurnCount() + 1) + "\n");
        sb.append("\n");
        sb.append(getPlayers(gameState));
        sb.append(getAllBonuses(gameState));
        sb.append(getNobles(gameState));
        sb.append(getVisibleCards(gameState));
        sb.append(getGemBank(gameState));
        sb.append(getReservedCards(gameState.getCurrentPlayer()));
        sb.append("  > " + gameState.getCurrentPlayer().getName() + "'s turn\n");
        return sb.toString();
    }

    public static void printGameState(GameState gameState) {
        clear();
        System.out.print(getGameState(gameState));
    }

    // ── players ──────────────────────────────────────────────────────

    public static String getPlayers(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  PLAYERS\n");
        sb.append(P_LINE).append("\n");
        sb.append(P_HEAD).append("\n");
        sb.append(P_LINE).append("\n");

        int threshold = gameState.getWinningThreshold();
        for (Player p : gameState.getPlayers()) {
            boolean cur = (p == gameState.getCurrentPlayer());
            String label = (cur ? "> " : "  ") + p.getName();
            if (label.length() > 12) label = label.substring(0, 12);
            Map<GemColor, Integer> gems = p.getGems().getGems();

            sb.append(String.format(P_FMT,
                label,
                p.getPoints() + "/" + threshold,
                g(gems, GemColor.DIAMOND),
                g(gems, GemColor.SAPPHIRE),
                g(gems, GemColor.EMERALD),
                g(gems, GemColor.RUBY),
                g(gems, GemColor.ONYX),
                g(gems, GemColor.GOLD_JOKER),
                gemTotal(p),
                p.getReservedCards().size()));
        }
        sb.append(P_LINE).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    public static void printPoints(GameState gameState) {
        System.out.print(getPlayers(gameState));
    }

    // ── bonuses ──────────────────────────────────────────────────────

    public static String getAllBonuses(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  BONUSES (from purchased cards)\n");
        sb.append(BO_LINE).append("\n");
        sb.append(BO_HEAD).append("\n");
        sb.append(BO_LINE).append("\n");

        for (Player p : gameState.getPlayers()) {
            boolean cur = (p == gameState.getCurrentPlayer());
            String label = (cur ? "> " : "  ") + p.getName();
            if (label.length() > 12) label = label.substring(0, 12);
            Map<GemColor, Integer> b = p.calculateBonuses();

            sb.append(String.format(BO_FMT,
                label,
                g(b, GemColor.DIAMOND),
                g(b, GemColor.SAPPHIRE),
                g(b, GemColor.EMERALD),
                g(b, GemColor.RUBY),
                g(b, GemColor.ONYX)));
        }
        sb.append(BO_LINE).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    // ── nobles ───────────────────────────────────────────────────────

    public static String getNobles(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  NOBLES\n");
        List<Noble> nobles = gameState.getAvailableNobles();
        if (nobles.isEmpty()) {
            sb.append("  (none remaining)\n");
        } else {
            sb.append(N_LINE).append("\n");
            sb.append(N_HEAD).append("\n");
            sb.append(N_LINE).append("\n");
            for (Noble n : nobles) {
                Map<GemColor, Integer> r = n.getRequirements();
                String name = n.getName();
                if (name.length() > 12) name = name.substring(0, 12);
                sb.append(String.format(N_FMT, name, n.getPoints(),
                    g(r, GemColor.DIAMOND), g(r, GemColor.SAPPHIRE),
                    g(r, GemColor.EMERALD), g(r, GemColor.RUBY), g(r, GemColor.ONYX)));
            }
            sb.append(N_LINE).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public static void printNobles(GameState gameState) {
        System.out.print(getNobles(gameState));
    }

    // ── card market ──────────────────────────────────────────────────

    public static String getVisibleCards(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        CardMarket market = gameState.getCardMarket();

        for (int level = 3; level >= 1; level--) {
            int deckSz;
            try { deckSz = market.getDeckSize(level); }
            catch (Exception e) { deckSz = 0; }

            sb.append("  CARD MARKET - Level " + level + " (" + deckSz + " in deck)\n");

            try {
                List<DevelopmentCard> cards = market.getVisibleCards(level);
                if (cards.isEmpty()) {
                    sb.append("  (no cards)\n");
                } else {
                    sb.append(C_LINE).append("\n");
                    sb.append(C_HEAD).append("\n");
                    sb.append(C_LINE).append("\n");
                    for (int i = 0; i < cards.size(); i++) {
                        sb.append(cardRowStr(cards.get(i), i));
                    }
                    sb.append(C_LINE).append("\n");
                }
            } catch (UnavailableCardException e) {
                sb.append("  (no cards)\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void printVisibleCards(GameState gameState) {
        System.out.print(getVisibleCards(gameState));
    }

    // ── gem bank ─────────────────────────────────────────────────────

    public static String getGemBank(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  GEM BANK\n");
        Map<GemColor, Integer> gems = gameState.getGemBank().getGems();
        sb.append(B_LINE).append("\n");
        sb.append(B_HEAD).append("\n");
        sb.append(B_LINE).append("\n");
        sb.append(String.format(B_FMT,
            g(gems, GemColor.DIAMOND), g(gems, GemColor.SAPPHIRE),
            g(gems, GemColor.EMERALD), g(gems, GemColor.RUBY),
            g(gems, GemColor.ONYX), g(gems, GemColor.GOLD_JOKER)));
        sb.append(B_LINE).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    public static void printGemBank(GameState gameState) {
        System.out.print(getGemBank(gameState));
    }

    // ── reserved cards ───────────────────────────────────────────────

    public static String getReservedCards(Player player) {
        StringBuilder sb = new StringBuilder();
        int count = player.getReservedCards().size();
        sb.append("  YOUR RESERVED CARDS (" + count + "/3)\n");
        if (count == 0) {
            sb.append("  (none)\n");
        } else {
            sb.append(C_LINE).append("\n");
            sb.append(C_HEAD).append("\n");
            sb.append(C_LINE).append("\n");
            for (int i = 0; i < count; i++) {
                sb.append(cardRowStr(player.getReservedCards().get(i), i));
            }
            sb.append(C_LINE).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public static void printReservedCards(Player player) {
        System.out.print(getReservedCards(player));
    }

    // ── player gems (standalone) ─────────────────────────────────────

    public static String getPlayerGem(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("  YOUR GEMS\n");
        Map<GemColor, Integer> gems = player.getGems().getGems();
        sb.append(B_LINE).append("\n");
        sb.append(B_HEAD).append("\n");
        sb.append(B_LINE).append("\n");
        sb.append(String.format(B_FMT,
            g(gems, GemColor.DIAMOND), g(gems, GemColor.SAPPHIRE),
            g(gems, GemColor.EMERALD), g(gems, GemColor.RUBY),
            g(gems, GemColor.ONYX), g(gems, GemColor.GOLD_JOKER)));
        sb.append(B_LINE).append("\n");
        sb.append("  Total: " + gemTotal(player) + "/10\n");
        sb.append("\n");
        return sb.toString();
    }

    public static void printPlayerGem(Player player) {
        System.out.print(getPlayerGem(player));
    }

    // ── winner ───────────────────────────────────────────────────────

    public static String getWinner(GameState gameState, GameRules gameRules) {
        StringBuilder sb = new StringBuilder();
        Player winner = gameRules.getWinner(gameState.getPlayers());

        sb.append("\n");
        sb.append("  =========================================================\n");
        sb.append("  GAME OVER -- " + winner.getName() + " wins with " + winner.getPoints() + " points!\n");
        sb.append("  =========================================================\n");
        sb.append("\n");

        sb.append("  FINAL STANDINGS\n");
        sb.append(F_LINE).append("\n");
        sb.append(F_HEAD).append("\n");
        sb.append(F_LINE).append("\n");
        List<Player> sorted = new ArrayList<>(gameState.getPlayers());
        sorted.sort((a, b) -> b.getPoints() - a.getPoints());
        for (Player p : sorted) {
            String name = p.getName();
            if (name.length() > 12) name = name.substring(0, 12);
            sb.append(String.format(F_FMT, name, p.getPoints(),
                p.getPurchasedCards().size(), p.getClaimedNobles().size()));
        }
        sb.append(F_LINE).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    public static void printWinner(GameState gameState, GameRules gameRules) {
        System.out.print(getWinner(gameState, gameRules));
    }

    // ══════════════════════════════════════════════════════════════════
    //  PROMPTS (offline/local only — these read from Scanner)
    // ══════════════════════════════════════════════════════════════════

    public static int promptNumPlayers(Scanner sc) {
        clear();
        printBanner();
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
        System.out.print(getActionMenu());
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
}
