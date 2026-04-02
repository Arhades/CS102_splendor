package splendor.display;

import java.util.*;
import splendor.entity.*;
import splendor.entity.bot.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.exception.*;
import splendor.rules.*;
import splendor.valueobjects.*;

/**
 * Handles all user input prompts for the Splendor game.
 */
public class InputHandler {

    /**
     * Default constructor
     */
    public InputHandler() {}

    /**
     * Prompts the player to pick three different gem colors from the bank.
     *
     * @param sc        the Scanner to read input from
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param display   the display UI for rendering the gem bank
     * @return a GemCollection of the three chosen gems, or null if the player backs out
     */
    public GemCollection promptTakeThreeGems(Scanner sc, GameState gameState, GameRules gameRules, DisplayUI display) {
        GemCollection gems = gameState.getGemBank();
        
        if (!gameRules.canTakeThreeDifferentGems(gems)) {
            System.out.println("Not enough gems");
            return null;
        }

        display.printGemBank(gameState);
        boolean valid = false;
        GemCollection add = new GemCollection();

        while (!valid) {
            List<String> taken = new ArrayList<>();
            add = new GemCollection();
            while (!gems.isEmptyWithoutJoker() && taken.size() != 3) {
                System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx)  [\"back\" to return]: ");
                String color = sc.nextLine();
                if (color.equalsIgnoreCase("back")) {
                    return null;
                }
                color = color.toUpperCase();
                if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                    System.out.println("Invalid input!");
                    continue;
                }
                if (taken.contains(color)) {
                    System.out.println("Already taken!");
                    continue;
                }
                if (gems.getCount(GemColor.convertToColor(color)) == 0) {
                    System.out.println("No more!");
                    continue;
                }
                taken.add(color);
                add.add(GemColor.convertToColor(color), 1);
                System.out.println("Valid option\n");
            }
            if (gameRules.canTakeThreeDifferentGems(add, gameState.getGemBank())) {
                valid = true;
            }
        }
        return add;
    }

    /**
     * Prompts the player to pick a gem color to take two of from the bank.
     *
     * @param sc        the Scanner to read input from
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param display   the display UI for rendering the gem bank
     * @return the chosen GemColor, or null if the player backs out
     */
    public GemColor promptTakeTwoGems(Scanner sc, GameState gameState, GameRules gameRules, DisplayUI display) {
        GemCollection gems = gameState.getGemBank();

        if (!gameRules.canTakeTwoSameGems(gems)) {
            System.out.println("Not enough gems");
            return null;
        }
        display.printGemBank(gameState);

        while (true) {
            System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx)  [\"back\" to return]: ");
            String color = sc.nextLine();
            if (color.equalsIgnoreCase("back")) {
                return null;
            }
            color = color.toUpperCase();
            if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                System.out.println("Invalid input!");
                continue;
            }
            GemColor col = GemColor.convertToColor(color);

            if (gameRules.canTakeTwoSameGems(col, gems)) {
                return col;
            }

            System.out.println("Insufficient\n");
        }
    }

    /**
     * Prompts the player to select and purchase a card from the table or reserved hand.
     *
     * @param sc        the Scanner to read input from
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param display   the display UI for rendering cards
     * @param player    the player attempting the purchase
     * @return the chosen DevelopmentCard, or null if the player backs out
     */
    public DevelopmentCard promptPurchaseCard(Scanner sc, GameState gameState, GameRules gameRules, DisplayUI display, Player player) {
        CardMarket cardMarket = gameState.getCardMarket();

        while (true) {
            try {
                System.out.print("From table or from reserved?  [\"back\" to return]: ");
                String str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return null;
                }

                if (str.equalsIgnoreCase("reserved")) {
                    List<DevelopmentCard> reservedCards = player.getReservedCards();
                    if (reservedCards.size() == 0) {
                        System.out.println("No reserved cards\n");
                        return null;
                    }
                    display.printReservedCards(player);
                    System.out.print("Number to purchase  [\"back\" to return]:");
                    str = sc.nextLine();
                    if (str.equalsIgnoreCase("back")) {
                        return null;
                    }
                    int num = Integer.parseInt(str);
                    if (num < 0 || num >= reservedCards.size()) {
                        System.out.println("Invalid input");
                        continue;
                    }
                    DevelopmentCard chosen = reservedCards.get(num);

                    if (!gameRules.canAffordCard(player, chosen)) {
                        System.out.println("Cannot afford!\n");
                        continue;
                    }

                    player.removeReservedCard(chosen);

                    return chosen;
                }

                display.printVisibleCards(gameState);
                System.out.print("Level  [\"back\" to return]: ");
                str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return null;
                }
                int level = Integer.parseInt(str);
                if (level < 1 || level > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                System.out.print("Number (0 to 3)  [\"back\" to return]: ");
                str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return null;
                }
                int number = Integer.parseInt(str);

                if (number < 0 || number > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                DevelopmentCard chosen = cardMarket.getVisibleCard(level, number);

                if (!gameRules.canAffordCard(player, chosen)) {
                    System.out.println("Cannot afford!\n");
                    continue;
                }

                cardMarket.removeCard(level, number);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));
                return chosen;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            } catch (InvalidIndexException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Prompts the player to reserve a card from the visible market or the deck.
     *
     * @param sc        the Scanner to read input from
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param display   the display UI for rendering cards
     * @param player    the player attempting the reserve
     * @return the chosen DevelopmentCard, or null if the player backs out
     */
    public DevelopmentCard promptReserveCard(Scanner sc, GameState gameState, GameRules gameRules, DisplayUI display, Player player) {
        CardMarket cardMarket = gameState.getCardMarket();
        GemCollection gemBank = gameState.getGemBank();

        while (true) {
            try {
                display.printVisibleCards(gameState);

                System.out.print("Level  [\"back\" to return]: ");
                String str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return null;
                }
                int level = Integer.parseInt(str);
                if (level < 1 || level > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                System.out.print("Number (0 to 4 -> 4 = random)  [\"back\" to return]: ");
                str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return null;
                }
                int number = Integer.parseInt(str);

                if (number < 0 || number > 4) {
                    System.out.println("Invalid input");
                    continue;
                }

                if (number == 4) {
                    DevelopmentCard chosen = cardMarket.drawCard(level);
                    return chosen;
                }
                DevelopmentCard chosen = cardMarket.getVisibleCard(level, number);

                cardMarket.removeCard(level, number);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));

                return chosen;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            } catch (UnavailableCardException e) {
                System.out.println(e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                System.out.println("No more card at that spot!");
            }
        }
    }

    /**
     * Prompts the player to return a gem when they have more than 10.
     *
     * @param sc        the Scanner to read input from
     * @param gameRules the game rules for validation
     * @param player    the player who must return a gem
     * @return the GemColor the player chose to return
     */
    public GemColor promptGemReturn(Scanner sc, GameRules gameRules, Player player) {
        while (true) {
            try {
                System.out.println("More than 10 gems!!");
                System.out.print("Colour to return (Diamond, Sapphire, Emerald, Ruby, Onyx): ");
                String color = sc.nextLine();
                color = color.toUpperCase();

                if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                    System.out.println("Invalid input!");
                    continue;
                }

                GemColor col = GemColor.convertToColor(color);
                if (player.getSpecificGem(col) < 1) {
                    System.out.println(String.format("No more %s gem\n", color));
                    continue;
                }

                return col;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }
    }

    /**
     * Prompts the player to pick a noble to claim, if eligible for more than one.
     *
     * @param sc        the Scanner to read input from
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param player    the player attempting to claim a noble
     * @return the chosen Noble, or null if none are claimable
     */
    public Noble promptNobelClaim(Scanner sc, GameState gameState, GameRules gameRules, Player player) {
        List<Noble> nobles = gameRules.getClaimableNobles(player, gameState.getAvailableNobles());
        if (nobles.size() == 0) {
            return null;
        }
        if (nobles.size() == 1 || player instanceof Bot) {
            return nobles.get(0);
        }
        int i = 0;

        for (Noble noble: nobles) {
            System.out.println("Number: " + i + "| " + noble);
            i++;
        }
        System.out.println();

        while (true) {
            try {
                System.out.printf("Which noble to pick? (Number 0 to %d)", i - 1);
                int option = Integer.parseInt(sc.nextLine());
                if (option < 0 || option >= nobles.size()) {
                    System.out.println("Invalid option!\n");
                    continue;
                }
                return nobles.get(option);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }
    }

    /**
     * Prompts the user for the number of players (2-4) and returns it.
     * Only used in local/offline mode.
     *
     * @param sc      the Scanner to read input from
     * @param display the display UI for rendering the banner
     * @return the number of players (2, 3, or 4)
     */
    public static int promptNumPlayers(Scanner sc, DisplayUI display) {
        display.printBanner();
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

    /**
     * Prompts the user to select a player type and returns it.
     * Only used in local/offline mode.
     *
     * @param sc            the Scanner to read input from
     * @param playerNumber  the player number (1-based) being configured
     * @return the player type (1=Human, 2=EasyBot, 3=HardBot)
     */
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

    /**
     * Prompts the user to enter a player name and returns it.
     * If the user enters nothing, the default name is returned.
     * Only used in local/offline mode.
     *
     * @param sc            the Scanner to read input from
     * @param playerNumber  the player number (1-based) being configured
     * @param defaultName   the default name if the user presses Enter
     * @return the player name entered, or defaultName if blank
     */
    public static String promptPlayerName(Scanner sc, int playerNumber, String defaultName) {
        System.out.print("  Player " + playerNumber + " name (blank = \"" + defaultName + "\"): ");
        String name = sc.nextLine().trim();
        return name.isEmpty() ? defaultName : name;
    }

    /**
     * Prompts the user to enter a birth date string (dd/mm/yyyy) for player ordering.
     * Only used in local/offline mode.
     *
     * @param sc            the Scanner to read input from
     * @param playerNumber  the player number (1-based) being configured
     * @return the birth date string entered by the player
     */
    public static String promptBirthDate(Scanner sc, int playerNumber) {
        while (true) {
            System.out.print("  Player " + playerNumber + " birth date (dd/mm/yyyy): ");
            String date = sc.nextLine().trim();
            if (date.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return date;
            }
            System.out.println("  Invalid format. Please use dd/mm/yyyy.\n");
        }
    }

    /**
     * Displays the action menu and prompts the user to pick an action.
     * Only used in local/offline mode.
     *
     * @param sc      the Scanner to read input from
     * @param display the display UI for rendering the action menu
     * @return the ActionType chosen by the player
     */
    public static ActionType promptAction(Scanner sc, DisplayUI display) {
        System.out.print(display.getActionMenu());
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
