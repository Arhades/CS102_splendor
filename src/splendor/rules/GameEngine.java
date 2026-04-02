package splendor.rules;

import java.io.*;
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
import splendor.valueobjects.*;

/**
 * Controls the main flow of the Splendor game.
 */
public class GameEngine {

    /**
     * Default constructor
     */
    public GameEngine() {}

    /**
     * Entry point for the local Splendor game.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        runGame();
    }

    /**
     * Creates the list of players by prompting for type, name, and birth date for each.
     *
     * @param sc           the Scanner to read input from
     * @param numPlayers   the number of players to create
     * @param inputHandler the input handler for prompts
     * @return the list of created players (may include bots)
     */
    public static List<Player> createPlayers(Scanner sc, int numPlayers, InputHandler inputHandler) {
        List<Player> players = new ArrayList<>();
        List<LocalDate> birthDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (int i = 0; i < numPlayers; i++) {
            int type = inputHandler.promptPlayerType(sc, i + 1);
            String defaultName = "Player " + (i + 1);
            if (type == 2) {
                defaultName = "EasyBot " + (i + 1);
            } else if (type == 3) {
                defaultName = "HardBot " + (i + 1);
            }

            String name = inputHandler.promptPlayerName(sc, i + 1, defaultName);

            // Collect birth date for player ordering
            LocalDate birthDate = null;
            if (type == 1) {
                // Human players enter their birth date
                while (birthDate == null) {
                    String dateStr = inputHandler.promptBirthDate(sc, i + 1);
                    try {
                        birthDate = LocalDate.parse(dateStr, formatter);
                    } catch (DateTimeParseException e) {
                        System.out.println("  Invalid date. Please try again.\n");
                    }
                }
            } else {
                // Bots get a very old default birth date so they are never youngest
                birthDate = LocalDate.of(1900, 1, 1);
            }
            birthDates.add(birthDate);

            if (type == 1) {
                players.add(new Player(name, i + 1));
            } else if (type == 2) {
                players.add(new EasyBot(name, i + 1));
            } else {
                players.add(new HardBot(name, i + 1));
            }
        }

        System.out.println();

        // Reorder starting from the youngest player
        players = reorderByYoungest(players, birthDates);
        return players;
    }

    /**
     * Reorders the player list so that the youngest player is first,
     * preserving the original clockwise seating order.
     * For example, if players are [A, B, C, D] and C is youngest,
     * the result is [C, D, A, B].
     *
     * @param players    the original list of players
     * @param birthDates a parallel list of birth dates for each player
     * @return a new list starting from the youngest player in clockwise order
     */
    public static List<Player> reorderByYoungest(List<Player> players, List<LocalDate> birthDates) {
        if (players.size() <= 1) {
            return players;
        }

        // Find the index of the youngest player (latest birth date)
        int youngestIndex = 0;
        LocalDate youngestDate = birthDates.get(0);
        for (int i = 1; i < birthDates.size(); i++) {
            if (birthDates.get(i).isAfter(youngestDate)) {
                youngestDate = birthDates.get(i);
                youngestIndex = i;
            }
        }

        // Build new list starting from youngest, wrapping around
        List<Player> reordered = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            int idx = (youngestIndex + i) % players.size();
            reordered.add(players.get(idx));
        }

        System.out.println("  The youngest player is: " + reordered.get(0).getName());
        System.out.println("  " + reordered.get(0).getName() + " will go first!\n");

        return reordered;
    }

    /**
     * Loads development cards of a specific level from a CSV file.
     *
     * @param filename the CSV filename on the classpath
     * @param level    the card level to filter (1, 2, or 3)
     * @return a list of DevelopmentCards at the specified level
     * @throws InvalidFileException if the file cannot be read or parsed
     */
    public static List<DevelopmentCard> loadCards(String filename, int level) throws InvalidFileException {
        InputStream inputStream = DevelopmentCard.class.getClassLoader().getResourceAsStream(filename);

        if (inputStream == null) {
            throw new InvalidFileException(String.format("File (%s) not found in classpath!!", filename));
        }

        try (Scanner sc = new Scanner(inputStream)) {
            sc.nextLine();
            List<DevelopmentCard> cards = new ArrayList<>();
            while (sc.hasNext()) {
                String[] cur = sc.nextLine().split(",");
                if (Integer.parseInt(cur[0]) != level) {
                    continue;
                }
                Map<GemColor, Integer> cost = new HashMap<>();
                cost.put(GemColor.DIAMOND, Integer.parseInt(cur[1]));
                cost.put(GemColor.ONYX, Integer.parseInt(cur[2]));
                cost.put(GemColor.EMERALD, Integer.parseInt(cur[3]));
                cost.put(GemColor.RUBY, Integer.parseInt(cur[4]));
                cost.put(GemColor.SAPPHIRE, Integer.parseInt(cur[5]));

                GemColor color = GemColor.convertToColor(cur[6].toUpperCase());
                cards.add(new DevelopmentCard(level, Integer.parseInt(cur[7]), color, new Cost(cost)));
            }
            return cards;
        } catch (Exception e) {
            throw new InvalidFileException("Failed to load cards from " + filename);
        }
    } 

    /**
     * Loads a random selection of nobles from a CSV file.
     *
     * @param filename the CSV filename on the classpath
     * @param count    the number of nobles to select
     * @return a list of randomly selected Nobles
     * @throws InvalidFileException if the file cannot be read or parsed
     */
    public static List<Noble> loadNobles(String filename, int count) throws InvalidFileException {
        InputStream inputStream = DevelopmentCard.class.getClassLoader().getResourceAsStream(filename);

        if (inputStream == null) {
            throw new InvalidFileException(String.format("File (%s) not found in classpath!!", filename));
        }

        try (Scanner sc = new Scanner(inputStream)) {
            sc.nextLine();
            List<Noble> nobles = new ArrayList<>();
            while (sc.hasNext()) {
                String[] cur = sc.nextLine().split(",");
                Map<GemColor, Integer> cost = new HashMap<>();
                cost.put(GemColor.DIAMOND, Integer.parseInt(cur[1]));
                cost.put(GemColor.ONYX, Integer.parseInt(cur[2]));
                cost.put(GemColor.EMERALD, Integer.parseInt(cur[3]));
                cost.put(GemColor.RUBY, Integer.parseInt(cur[4]));
                cost.put(GemColor.SAPPHIRE, Integer.parseInt(cur[5]));

                nobles.add(new Noble(cur[0], cost));
            }
            List<Noble> noblesUsed = new ArrayList<>();
            Random rand = new Random();
            for (int i = 0; i < count; i++) {
                int random = rand.nextInt(nobles.size());
                noblesUsed.add(nobles.get(random));
                nobles.remove(random);
            }
            return noblesUsed;
        } catch (Exception e) {
            throw new InvalidFileException("Failed to load cards " + filename);
        }
    } 

    /**
     * Builds the initial gem bank based on the number of players and config.
     *
     * @param numPlayers the number of players in the game
     * @param gameConfig the game configuration with gem counts
     * @return a GemCollection representing the initial bank
     */
    public static GemCollection buildGemBank(int numPlayers, GameConfig gameConfig) {
        int numToAdd = gameConfig.getGemCountPerColor(numPlayers);

        Map<GemColor, Integer> map = new HashMap<>();
        map.put(GemColor.DIAMOND, numToAdd);
        map.put(GemColor.ONYX, numToAdd);
        map.put(GemColor.EMERALD, numToAdd);
        map.put(GemColor.RUBY, numToAdd);
        map.put(GemColor.SAPPHIRE, numToAdd);
        map.put(GemColor.GOLD_JOKER, gameConfig.getGoldGems());

        return new GemCollection(map);
    }

    /**
    * Runs the main game loop for the Splendor game.
    */
    public static void runGame() {
        try {
            Scanner sc = new Scanner(System.in);
            GameConfig gameConfig = GameConfig.load("config.properties");
            DisplayUI display = new DisplayUI();
            InputHandler inputHandler = new InputHandler();

            int numOfPlayers = inputHandler.promptNumPlayers(sc, display);
            List<Player> players = createPlayers(sc, numOfPlayers, inputHandler);

            List<DevelopmentCard> levelOneDeck = loadCards(gameConfig.getCardsFile(), 1);
            List<DevelopmentCard> levelTwoDeck = loadCards(gameConfig.getCardsFile(), 2);
            List<DevelopmentCard> levelThreeDeck = loadCards(gameConfig.getCardsFile(), 3);

            CardMarket cardMarket = new CardMarket(levelOneDeck, levelTwoDeck, levelThreeDeck);

            GemCollection initialGems = buildGemBank(numOfPlayers, gameConfig);

            List<Noble> nobles = loadNobles(gameConfig.getNoblesFile(), numOfPlayers + 1);

            GameState gameState = new GameState(players, cardMarket, initialGems, nobles, gameConfig.getWinningThreshold());
            GameRules gameRules = new GameRules(gameState);

            boolean canEnd = false;

            while (!gameState.isGameOver() || !canEnd) {
                canEnd = false;
                System.out.println("--------------------------------------------------------------------------------------------------");
                System.out.println();
                Player curPlayer = gameState.getCurrentPlayer();
                display.printGameState(gameState);
                boolean validAction = false;

                if (curPlayer instanceof Bot) {
                    Bot bot = (Bot) curPlayer;
                    System.out.println(bot.takeTurn(gameState, gameRules));
                } else {
                    while (!validAction) {
                        ActionType action = inputHandler.promptAction(sc, display);
                        validAction = executeAction(sc, action, curPlayer, gameState, gameRules, display, inputHandler);
                        System.out.println();
                    }
                }

                handleGemReturn(sc, curPlayer, gameRules, gameState, inputHandler);
                handleNobleClaims(curPlayer, gameState, gameRules, sc, inputHandler);
                checkEndCondition(gameState, gameRules);
                gameState.advanceToNext();
                System.out.println();
                
                if (gameState.getCurrentPlayerIndex() == 0) {
                    canEnd = true;
                }
            }
            display.printWinner(gameState, gameRules);
        } catch (InvalidFileException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Executes a player's chosen action during local play.
     *
     * @param sc           the Scanner to read input from
     * @param action       the action type chosen
     * @param player       the current player
     * @param gameState    the current game state
     * @param gameRules    the game rules for validation
     * @param display      the display UI
     * @param inputHandler the input handler for prompts
     * @return true if the action was valid and completed, false otherwise
     */
    public static boolean executeAction(Scanner sc, ActionType action, Player player, GameState gameState, GameRules gameRules, DisplayUI display, InputHandler inputHandler) {
        if (action.equals(ActionType.TAKE_THREE_DIFFERENT)) {
            return handleTakeThreeDifferent(sc, player, gameState, gameRules, display, inputHandler);
        } else if (action.equals(ActionType.TAKE_TWO_SAME)) {
            return handleTakeTwoSame(sc, player, gameState, gameRules, display, inputHandler);
        } else if (action.equals(ActionType.PURCHASE_CARD)) {
            return handlePurchaseCard(sc, player, gameState, gameRules, display, inputHandler);
        } else {
            return handleReserveCard(sc, player, gameState, gameRules, display, inputHandler);
        }
    }

    /**
     * Handles the take-three-different-gems action during local play.
     *
     * @param sc           the Scanner to read input from
     * @param player       the current player
     * @param gameState    the current game state
     * @param gameRules    the game rules for validation
     * @param display      the display UI
     * @param inputHandler the input handler for prompts
     * @return true if the action succeeded, false if the player backed out
     */
    public static boolean handleTakeThreeDifferent(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display, InputHandler inputHandler) {
        GemCollection add = inputHandler.promptTakeThreeGems(sc, gameState, gameRules, display);
        if (add == null) {
            return false;
        }

        List<GemColor> colors = add.toList();

        return GameActions.takeThreeDifferent(player, gameState, gameRules, colors);
    }

    /**
     * Handles the take-two-same-gems action during local play.
     *
     * @param sc           the Scanner to read input from
     * @param player       the current player
     * @param gameState    the current game state
     * @param gameRules    the game rules for validation
     * @param display      the display UI
     * @param inputHandler the input handler for prompts
     * @return true if the action succeeded, false if the player backed out
     */
    public static boolean handleTakeTwoSame(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display, InputHandler inputHandler) {
        GemColor col = inputHandler.promptTakeTwoGems(sc, gameState, gameRules, display);
        if (col == null) {
            return false;
        }

        return GameActions.takeTwoSame(player, gameState, gameRules, col);
    }

    /**
     * Handles the purchase-card action during local play.
     *
     * @param sc           the Scanner to read input from
     * @param player       the current player
     * @param gameState    the current game state
     * @param gameRules    the game rules for validation
     * @param display      the display UI
     * @param inputHandler the input handler for prompts
     * @return true if the purchase succeeded, false if the player backed out
     */
    public static boolean handlePurchaseCard(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display, InputHandler inputHandler) {
        DevelopmentCard chosen = inputHandler.promptPurchaseCard(sc, gameState, gameRules, display, player);
        if (chosen == null) {
            return false;
        }
        CardMarket cardMarket = gameState.getCardMarket();
        
        GemCollection cost = gameRules.calculateActualCost(player, chosen);
        player.deductGems(cost);
        player.addCard(chosen);
        
        gameState.addGemsToBank(cost);
        return true;
    }

    /**
     * Handles the reserve-card action during local play.
     *
     * @param sc           the Scanner to read input from
     * @param player       the current player
     * @param gameState    the current game state
     * @param gameRules    the game rules for validation
     * @param display      the display UI
     * @param inputHandler the input handler for prompts
     * @return true if the reserve succeeded, false if the player backed out
     */
    public static boolean handleReserveCard(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display, InputHandler inputHandler) {
        if (!gameRules.canReserveCard(player)) {
            System.out.println("No more reserve slot!");
            return false;
        }

        DevelopmentCard chosen = inputHandler.promptReserveCard(sc, gameState, gameRules, display, player);
        if (chosen == null) {
            return false;
        }

        GameActions.giveGoldJoker(player, gameState);
        player.addReservedCard(chosen);
        return true;
        
    }

    /**
     * Handles gem return when a player exceeds the 10-gem limit during local play.
     *
     * @param sc           the Scanner to read input from
     * @param player       the current player
     * @param gameRules    the game rules for gem limit checks
     * @param gameState    the current game state
     * @param inputHandler the input handler for prompts
     */
    public static void handleGemReturn(Scanner sc, Player player, GameRules gameRules, GameState gameState, InputHandler inputHandler) {
        while (gameRules.mustReturnGems(player)) {
            GemColor col = inputHandler.promptGemReturn(sc, gameRules, player);
            GemCollection gems = new GemCollection();
            gems.add(col, 1);
            player.deductGems(gems);

            gameState.addGemsToBank(gems);
        }
    }


    /**
     * Handles noble claiming after a player's turn during local play.
     *
     * @param player       the current player
     * @param gameState    the current game state
     * @param gameRules    the game rules for noble eligibility
     * @param sc           the Scanner to read input from
     * @param inputHandler the input handler for prompts
     */
    public static void handleNobleClaims(Player player, GameState gameState, GameRules gameRules, Scanner sc, InputHandler inputHandler) {
        Noble chosen = inputHandler.promptNobelClaim(sc, gameState, gameRules, player);
        if (chosen == null) {
            return;
        }
        player.claimNoble(chosen);
        gameState.removeNoble(chosen);
    }

    /**
     * Checks whether any player has reached the winning threshold.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for win condition checks
     * @return true if the game should end, false otherwise
     */
    public static boolean checkEndCondition(GameState gameState, GameRules gameRules) {
        List<Player> players = gameState.getPlayers();

        for (Player player: players) {
            if (gameRules.hasPlayerWon(player, gameState.getWinningThreshold())) {
                gameState.setGameOver(true);
                return true;
            }
        }
        return false;
    }
}

