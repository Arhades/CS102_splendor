package splendor.display;

import java.util.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.exception.*;
import splendor.rules.*;
import splendor.valueobjects.*;

/**
 * Handles all display output for the Splendor game.
 * 
 * Every display method has two versions:
 *   - A {@code print} version that outputs directly to System.out (for local/offline play)
 *   - A {@code get} version that returns the same output as a String (for sending over sockets in multiplayer)
 * 
 * Example usage in server code:
 *   {@code broadcast(DisplayUI.getGameState(gameState));}
 *   {@code sendToPlayer(name, DisplayUI.getReservedCards(player));}
 */
public class DisplayUI {

    /** 
     * default constructor
     */
    public DisplayUI() {}

    // ── ANSI codes ───────────────────────────────────────────────────
    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";

    /**
     * Returns the ANSI foreground color code for a gem color.
     *
     * @param c the gem color
     * @return the ANSI escape code string
     */
    private static String gemFg(GemColor c) {
        switch (c) {
            case DIAMOND:    return "\033[97m"; // bright white
            case SAPPHIRE:   return "\033[34m"; // blue
            case EMERALD:    return "\033[32m"; // green
            case RUBY:       return "\033[31m"; // red
            case ONYX:       return "\033[90m"; // gray
            case GOLD_JOKER: return "\033[33m"; // yellow
            default:         return RESET;
        }
    }

    /**
     * Returns the display name for a gem color.
     *
     * @param c the gem color
     * @return the human-readable gem name
     */
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

    /**
     * Returns a colored gem header string for table display.
     *
     * @param c the gem color
     * @return the ANSI-colored gem name string
     */
    private static String coloredGemHeader(GemColor c) {
        return gemFg(c) + gemName(c) + RESET;
    }

    /**
     * Computes the total number of gems held by a player.
     *
     * @param p the player
     * @return the total gem count
     */
    private static int gemTotal(Player p) {
        int total = 0;
        for (int v : p.getGems().getGems().values()) total += v;
        return total;
    }

    /**
     * Safely retrieves a gem count from a map, defaulting to 0.
     *
     * @param m the gem color-to-count map
     * @param c the gem color to look up
     * @return the count, or 0 if absent
     */
    private static int g(Map<GemColor, Integer> m, GemColor c) {
        return m.getOrDefault(c, 0);
    }

    // ── table borders ────────────────────────────────────────────────
    // NOTE: headers with color codes are built dynamically so the
    //       separators (plain ASCII) stay as constants.

    // Players
    private static final String P_LINE =
        "  +--------------+-------+---------+----------+---------+------+------+------+-------+-----+";
    private static final String P_FMT =
        "  | %-12s | %5s | %7d | %8d | %7d | %4d | %4d | %4d | %5d | %3d |%n";

    private static String pHead() {
        return "  | Name         | Score | "
            + coloredGemHeader(GemColor.DIAMOND)  + " | "
            + coloredGemHeader(GemColor.SAPPHIRE) + " | "
            + coloredGemHeader(GemColor.EMERALD)  + " | "
            + coloredGemHeader(GemColor.RUBY)     + " | "
            + coloredGemHeader(GemColor.ONYX)     + " | "
            + coloredGemHeader(GemColor.GOLD_JOKER) + " | Total | Res |";
    }

    // Bonuses
    private static final String BO_LINE =
        "  +--------------+---------+----------+---------+------+------+";
    private static final String BO_FMT =
        "  | %-12s | %7d | %8d | %7d | %4d | %4d |%n";

    private static String boHead() {
        return "  | Name         | "
            + coloredGemHeader(GemColor.DIAMOND)  + " | "
            + coloredGemHeader(GemColor.SAPPHIRE) + " | "
            + coloredGemHeader(GemColor.EMERALD)  + " | "
            + coloredGemHeader(GemColor.RUBY)     + " | "
            + coloredGemHeader(GemColor.ONYX)     + " |";
    }

    // Nobles
    private static final String N_LINE =
        "  +--------------+------+---------+----------+---------+------+------+";
    private static final String N_FMT =
        "  | %-12s | %4d | %7d | %8d | %7d | %4d | %4d |%n";

    private static String nHead() {
        return "  | Name         | Pts  | "
            + coloredGemHeader(GemColor.DIAMOND)  + " | "
            + coloredGemHeader(GemColor.SAPPHIRE) + " | "
            + coloredGemHeader(GemColor.EMERALD)  + " | "
            + coloredGemHeader(GemColor.RUBY)     + " | "
            + coloredGemHeader(GemColor.ONYX)     + " |";
    }

    // Cards
    private static final String C_LINE =
        "  +-----+----------+------+---------+----------+---------+------+------+";
    private static final String C_FMT =
        "  | %3d | %-8s | %4s | %7d | %8d | %7d | %4d | %4d |%n";

    private static String cHead() {
        return "  | No. | Bonus    | Pts  | "
            + coloredGemHeader(GemColor.DIAMOND)  + " | "
            + coloredGemHeader(GemColor.SAPPHIRE) + " | "
            + coloredGemHeader(GemColor.EMERALD)  + " | "
            + coloredGemHeader(GemColor.RUBY)     + " | "
            + coloredGemHeader(GemColor.ONYX)     + " |";
    }

    // Gem bank
    private static final String B_LINE =
        "  +---------+----------+---------+------+------+------+";
    private static final String B_FMT =
        "  | %7d | %8d | %7d | %4d | %4d | %4d |%n";

    private static String bHead() {
        return "  | "
            + coloredGemHeader(GemColor.DIAMOND)  + " | "
            + coloredGemHeader(GemColor.SAPPHIRE) + " | "
            + coloredGemHeader(GemColor.EMERALD)  + " | "
            + coloredGemHeader(GemColor.RUBY)     + " | "
            + coloredGemHeader(GemColor.ONYX)     + " | "
            + coloredGemHeader(GemColor.GOLD_JOKER) + " |";
    }

    // Final standings
    private static final String F_LINE =
        "  +--------------+-------+-------+---------+";
    private static final String F_HEAD =
        "  | Name         |  Pts  | Cards | Nobles  |";
    private static final String F_FMT =
        "  | %-12s | %5d | %5d | %7d |%n";

    // Lobby
    private static final String L_LINE =
        "  +-----+--------------+--------+";
    private static final String L_HEAD =
        "  | No. | Name         | Status |";
    private static final String L_FMT =
        "  | %3d | %-12s | %-6s |%n";

    // ── card row helper ──────────────────────────────────────────────

    /**
     * Formats a single card row for table display.
     *
     * @param c     the development card to display
     * @param index the display index of the card
     * @return the formatted row string
     */
    private static String cardRowStr(DevelopmentCard c, int index) {
        Map<GemColor, Integer> cost = c.getCost().getCost().getGems();
        String pts = c.getPoints() > 0 ? String.valueOf(c.getPoints()) : "-";
        String bonus = gemFg(c.getBonus()) + gemName(c.getBonus()) + RESET;
        // pad colored bonus to 8 visible chars
        int visLen = gemName(c.getBonus()).length();
        String bonusPadded = bonus + spaces(8 - visLen);
        return String.format("  | %3d | %s | %4s | %7d | %8d | %7d | %4d | %4d |%n",
            index, bonusPadded, pts,
            g(cost, GemColor.DIAMOND), g(cost, GemColor.SAPPHIRE),
            g(cost, GemColor.EMERALD), g(cost, GemColor.RUBY), g(cost, GemColor.ONYX));
    }

    /**
     * Returns a string of n space characters.
     *
     * @param n the number of spaces
     * @return a string containing n spaces
     */
    private static String spaces(int n) {
        if (n <= 0) return "";
        char[] arr = new char[n];
        java.util.Arrays.fill(arr, ' ');
        return new String(arr);
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOBBY / PRE-GAME METHODS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns the title/splash screen shown when a player first connects.
     *
     * @return the banner as a String
     */
    public static String getBanner() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  " + BOLD + "=========================================================" + RESET + "\n");
        sb.append("              " + BOLD + "--- S P L E N D O R ---" + RESET + "\n");
        sb.append("  " + BOLD + "=========================================================" + RESET + "\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Prints the title/splash screen to System.out.
     */
    public static void printBanner() {
        System.out.print(getBanner());
    }

    /**
     * Returns the waiting lobby display showing all connected players.
     *
     * @param playerNames  list of player names in the order they joined
     * @param readyStates  list of booleans, true if that player is ready
     * @return the formatted lobby as a String
     */
    public static String getLobby(List<String> playerNames, List<Boolean> readyStates) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  " + BOLD + "=============== WAITING LOBBY ===============" + RESET + "\n");
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

    /**
     * Prints the waiting lobby to System.out.
     *
     * @param playerNames  list of player names in the order they joined
     * @param readyStates  list of booleans, true if that player is ready
     */
    public static void printLobby(List<String> playerNames, List<Boolean> readyStates) {
        System.out.print(getLobby(playerNames, readyStates));
    }

    /**
     * Returns the prompt asking for the player's name.
     *
     * @return the name prompt as a String
     */
    public static String getNamePrompt() {
        return "  Enter your player name: ";
    }

    /**
     * Returns the prompt asking for the player's birth date.
     *
     * @return the birth date prompt as a String
     */
    public static String getBirthDatePrompt() {
        return "  Enter your birth date (dd/mm/yyyy): ";
    }

    /**
     * Returns a message announcing who the youngest player is and who goes first.
     *
     * @param youngestName  the name of the youngest player
     * @return the start message as a String
     */
    public static String getStartMessage(String youngestName) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  " + BOLD + "=========================================================" + RESET + "\n");
        sb.append("  The youngest player is: " + BOLD + youngestName + RESET + "\n");
        sb.append("  " + youngestName + " will go first!\n");
        sb.append("  " + BOLD + "=========================================================" + RESET + "\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Prints the start message to System.out.
     *
     * @param youngestName  the name of the youngest player
     */
    public static void printStartMessage(String youngestName) {
        System.out.print(getStartMessage(youngestName));
    }

    /**
     * Returns the action menu shown on a player's turn.
     *
     * @return the action menu as a String
     */
    public static String getActionMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  " + BOLD + "1." + RESET + " Take 3 different gems\n");
        sb.append("  " + BOLD + "2." + RESET + " Take 2 same gems\n");
        sb.append("  " + BOLD + "3." + RESET + " Purchase a card\n");
        sb.append("  " + BOLD + "4." + RESET + " Reserve a card\n");
        sb.append("  Choose: ");
        return sb.toString();
    }

    /**
     * Prints the action menu to System.out.
     */
    public static void printActionMenu() {
        System.out.print(getActionMenu());
    }

    // ══════════════════════════════════════════════════════════════════
    //  GAME STATE
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns the full game board as a String, including players, bonuses,
     * nobles, card market, gem bank, reserved cards, and turn indicator.
     *
     * @param gameState  the current game state
     * @return the full game board as a String
     */
    public static String getGameState(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  " + BOLD + "--- S P L E N D O R ---" + RESET + spaces(45) + DIM + "Turn " + (gameState.getTurnCount() + 1) + RESET + "\n");
        sb.append("\n");
        sb.append(getNobles(gameState));
        sb.append(getVisibleCards(gameState));
        sb.append(getGemBank(gameState));
        sb.append(getPlayers(gameState));
        sb.append(getAllBonuses(gameState));
        sb.append(getReservedCards(gameState.getCurrentPlayer()));
        sb.append("  " + BOLD + "> " + gameState.getCurrentPlayer().getName() + "'s turn" + RESET + "\n");
        return sb.toString();
    }

    /**
     * Returns the full game board as a String, including players, bonuses,
     * nobles, card market, gem bank, reserved cards, and turn indicator.
     *
     * @param gameState  the current game state
     * @return the full game board as a String
     */
    public static String getGameStateSocketWithoutReserved(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  " + BOLD + "--- S P L E N D O R ---" + RESET + spaces(45) + DIM + "Turn " + (gameState.getTurnCount() + 1) + RESET + "\n");
        sb.append("\n");
        sb.append(getNobles(gameState));
        sb.append(getVisibleCards(gameState));
        sb.append(getGemBank(gameState));
        sb.append(getPlayers(gameState));
        sb.append(getAllBonuses(gameState));
        return sb.toString();
    }

    /**
     * Prints the full game board to System.out.
     *
     * @param gameState  the current game state
     */
    public static void printGameState(GameState gameState) {
        System.out.print(getGameState(gameState));
    }

    // ── players ──────────────────────────────────────────────────────

    /**
     * Returns the players table showing each player's score, gem counts,
     * total gems, and number of reserved cards.
     *
     * @param gameState  the current game state
     * @return the players table as a String
     */
    public static String getPlayers(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  " + BOLD + "PLAYERS" + RESET + "\n");
        sb.append(P_LINE).append("\n");
        sb.append(pHead()).append("\n");
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
        sb.append(P_LINE).append("\n\n");
        return sb.toString();
    }

    /**
     * Prints the players table to System.out.
     *
     * @param gameState  the current game state
     */
    public static void printPoints(GameState gameState) {
        System.out.print(getPlayers(gameState));
    }

    // ── bonuses ──────────────────────────────────────────────────────

    /**
     * Returns the bonuses table showing each player's permanent gem
     * bonuses from purchased development cards.
     *
     * @param gameState  the current game state
     * @return the bonuses table as a String
     */
    public static String getAllBonuses(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  " + BOLD + "BONUSES" + RESET + DIM + " (from purchased cards)" + RESET + "\n");
        sb.append(BO_LINE).append("\n");
        sb.append(boHead()).append("\n");
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
        sb.append(BO_LINE).append("\n\n");
        return sb.toString();
    }

    /**
     * Returns the nobles table showing all available nobles and their
     * bonus card requirements.
     *
     * @param gameState  the current game state
     * @return the nobles table as a String
     */
    public static String getNobles(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  " + BOLD + "NOBLES" + RESET + "\n");
        List<Noble> nobles = gameState.getAvailableNobles();
        if (nobles.isEmpty()) {
            sb.append("  (none remaining)\n");
        } else {
            sb.append(N_LINE).append("\n");
            sb.append(nHead()).append("\n");
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

    /**
     * Prints the nobles table to System.out.
     *
     * @param gameState  the current game state
     */
    public static void printNobles(GameState gameState) {
        System.out.print(getNobles(gameState));
    }

    /**
     * Returns the card market showing all visible development cards
     * for levels 3, 2, and 1, along with remaining deck sizes.
     *
     * @param gameState  the current game state
     * @return the card market as a String
     */
    public static String getVisibleCards(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        CardMarket market = gameState.getCardMarket();

        for (int level = 3; level >= 1; level--) {
            int deckSz;
            try { deckSz = market.getDeckSize(level); }
            catch (Exception e) { deckSz = 0; }

            sb.append("  " + BOLD + "CARD MARKET - Level " + level + RESET + DIM + " (" + deckSz + " in deck)" + RESET + "\n");

            try {
                List<DevelopmentCard> cards = market.getVisibleCards(level);
                if (cards.isEmpty()) {
                    sb.append("  (no cards)\n");
                } else {
                    sb.append(C_LINE).append("\n");
                    sb.append(cHead()).append("\n");
                    sb.append(C_LINE).append("\n");
                    for (int i = 0; i < cards.size(); i++) {
                        sb.append(cardRowStr(cards.get(i), i));
                    }
                    sb.append(C_LINE).append("\n");
                }
            } catch (InvalidIndexException e) {
                sb.append("  (no cards)\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Prints the card market to System.out.
     *
     * @param gameState  the current game state
     */
    public static void printVisibleCards(GameState gameState) {
        System.out.print(getVisibleCards(gameState));
    }

    /**
     * Returns the gem bank table showing how many gems of each color
     * are available in the bank.
     *
     * @param gameState  the current game state
     * @return the gem bank table as a String
     */
    public static String getGemBank(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        sb.append("  " + BOLD + "GEM BANK" + RESET + "\n");
        Map<GemColor, Integer> gems = gameState.getGemBank().getGems();
        sb.append(B_LINE).append("\n");
        sb.append(bHead()).append("\n");
        sb.append(B_LINE).append("\n");
        sb.append(String.format(B_FMT,
            g(gems, GemColor.DIAMOND), g(gems, GemColor.SAPPHIRE),
            g(gems, GemColor.EMERALD), g(gems, GemColor.RUBY),
            g(gems, GemColor.ONYX), g(gems, GemColor.GOLD_JOKER)));
        sb.append(B_LINE).append("\n\n");
        return sb.toString();
    }

    /**
     * Prints the gem bank table to System.out.
     *
     * @param gameState  the current game state
     */
    public static void printGemBank(GameState gameState) {
        System.out.print(getGemBank(gameState));
    }

    /**
     * Returns the reserved cards table for a specific player, showing
     * each card's bonus, points, and cost. Per Splendor rules, only
     * the owning player should see their reserved card details.
     *
     * @param player  the player whose reserved cards to display
     * @return the reserved cards table as a String
     */
    public static String getReservedCards(Player player) {
        StringBuilder sb = new StringBuilder();
        int count = player.getReservedCards().size();
        sb.append("  " + BOLD + "YOUR RESERVED CARDS" + RESET + " (" + count + "/3)\n");
        if (count == 0) {
            sb.append("  (none)\n");
        } else {
            sb.append(C_LINE).append("\n");
            sb.append(cHead()).append("\n");
            sb.append(C_LINE).append("\n");
            for (int i = 0; i < count; i++) {
                sb.append(cardRowStr(player.getReservedCards().get(i), i));
            }
            sb.append(C_LINE).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Prints the reserved cards table to System.out.
     *
     * @param player  the player whose reserved cards to display
     */
    public static void printReservedCards(Player player) {
        System.out.print(getReservedCards(player));
    }

    /**
     * Returns the current player's gem counts in a table, with total.
     * Used standalone during gem-taking and gem-returning actions.
     *
     * @param player  the player whose gems to display
     * @return the player gems table as a String
     */
    public static String getPlayerGem(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("  " + BOLD + "YOUR GEMS" + RESET + "\n");
        Map<GemColor, Integer> gems = player.getGems().getGems();
        sb.append(B_LINE).append("\n");
        sb.append(bHead()).append("\n");
        sb.append(B_LINE).append("\n");
        sb.append(String.format(B_FMT,
            g(gems, GemColor.DIAMOND), g(gems, GemColor.SAPPHIRE),
            g(gems, GemColor.EMERALD), g(gems, GemColor.RUBY),
            g(gems, GemColor.ONYX), g(gems, GemColor.GOLD_JOKER)));
        sb.append(B_LINE).append("\n");
        sb.append("  Total: " + gemTotal(player) + "/10\n\n");
        return sb.toString();
    }

    /**
     * Prints the player gems table to System.out.
     *
     * @param player  the player whose gems to display
     */
    public static void printPlayerGem(Player player) {
        System.out.print(getPlayerGem(player));
    }

    /**
     * Returns the game over screen showing the winner and final standings
     * for all players, sorted by points.
     *
     * @param gameState  the current game state
     * @param gameRules  the game rules (used to determine the winner)
     * @return the game over screen as a String
     */
    public static String getWinner(GameState gameState, GameRules gameRules) {
        StringBuilder sb = new StringBuilder();
        Player winner = gameRules.getWinner(gameState.getPlayers());

        sb.append("\n");
        sb.append("  " + BOLD + "=========================================================" + RESET + "\n");
        sb.append("  " + BOLD + "GAME OVER" + RESET + " -- " + BOLD + winner.getName() + RESET + " wins with " + BOLD + winner.getPoints() + RESET + " points!\n");
        sb.append("  " + BOLD + "=========================================================" + RESET + "\n");
        sb.append("\n");

        sb.append("  " + BOLD + "FINAL STANDINGS" + RESET + "\n");
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
        sb.append(F_LINE).append("\n\n");
        return sb.toString();
    }

    /**
     * Prints the game over screen to System.out.
     *
     * @param gameState  the current game state
     * @param gameRules  the game rules (used to determine the winner)
     */
    public static void printWinner(GameState gameState, GameRules gameRules) {
        System.out.print(getWinner(gameState, gameRules));
    }
}
