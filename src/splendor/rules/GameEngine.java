package splendor.rules;

import java.io.*;
import java.util.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.entity.bot.*;
import splendor.exception.*;
import splendor.valueobjects.*;
import splendor.config.*;
import splendor.display.*;

public class GameEngine {
    public static void main(String[] args) {
        runGame();
    }

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

    public static List<Player> createPlayers(Scanner sc, int numPlayers) {
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < numPlayers; i++) {
            int type = promptPlayerType(sc, i + 1);
            String defaultName = "Player " + (i + 1);
            if (type == 2) {
                defaultName = "EasyBot " + (i + 1);
            } else if (type == 3) {
                defaultName = "HardBot " + (i + 1);
            }

            String name = promptPlayerName(sc, i + 1, defaultName);
            if (type == 1) {
                players.add(new Player(name, i + 1));
            } else if (type == 2) {
                players.add(new EasyBot(name, i + 1));
            } else {
                players.add(new HardBot(name, i + 1));
            }
        }

        System.out.println();
        return players;
    }

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

                GemColor color = convertToColor(cur[6].toUpperCase());
                cards.add(new DevelopmentCard(level, Integer.parseInt(cur[7]), color, new Cost(cost)));
            }
            return cards;
        } catch (Exception e) {
            throw new InvalidFileException("Failed to load cards from " + filename);
        }
    } 

    public static GemColor convertToColor(String color) {
        switch (color) {
            case "DIAMOND":
                return GemColor.DIAMOND;
            case "ONYX":
                return GemColor.ONYX;
            case "EMERALD":
                return GemColor.EMERALD;
            case "RUBY":
                return GemColor.RUBY;
            default:
                return GemColor.SAPPHIRE;
        }
    }

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

    public static void runGame() {
        try {
            Scanner sc = new Scanner(System.in);
            GameConfig gameConfig = GameConfig.load("config.properties");
            DisplayUI display = new DisplayUI();

            int numOfPlayers = promptNumPlayers(sc);
            List<Player> players = createPlayers(sc, numOfPlayers);

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
                        ActionType action = promptAction(sc);
                        validAction = executeAction(sc, action, curPlayer, gameState, gameRules, display);
                        System.out.println();
                    }
                }

                handleGemReturn(sc, curPlayer, gameRules, gameState);
                handleNobleClaims(curPlayer, gameState, gameRules, sc);
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

    public static boolean executeAction(Scanner sc, ActionType action, Player player, GameState gameState, GameRules gameRules, DisplayUI display) {
        if (action.equals(ActionType.TAKE_THREE_DIFFERENT)) {
            return handleTakeThreeDifferent(sc, player, gameState, gameRules, display);
        } else if (action.equals(ActionType.TAKE_TWO_SAME)) {
            return handleTakeTwoSame(sc, player, gameState, gameRules, display);
        } else if (action.equals(ActionType.PURCHASE_CARD)) {
            return handlePurchaseCard(sc, player, gameState, gameRules, display);
        } else {
            return handleReserveCard(sc, player, gameState, gameRules, display);
        }
    }

    public static boolean handleTakeThreeDifferent(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display) {
        GemCollection gems = gameState.getGemBank();
        
        if (!gameRules.canTakeThreeDifferentGems(gems)) {
            System.out.println("Not enough gems");
            return false;
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
                    return false;
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
                if (gems.getCount(convertToColor(color)) == 0) {
                    System.out.println("No more!");
                    continue;
                }
                taken.add(color);
                add.add(convertToColor(color), 1);
            }
            if (gameRules.canTakeThreeDifferentGems(add, gameState.getGemBank())) {
                valid = true;
            }
        }
        player.addGems(add);
        gems.subtract(add);
        return true;
    }

    public static boolean handleTakeTwoSame(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display) {
        GemCollection gems = gameState.getGemBank();

        if (!gameRules.canTakeTwoSameGems(gems)) {
            System.out.println("Not enough gems");
            return false;
        }

        display.printGemBank(gameState);
        while (true) {
            System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx)  [\"back\" to return]: ");
            String color = sc.nextLine();
            if (color.equalsIgnoreCase("back")) {
                return false;
            }
            color = color.toUpperCase();
            if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                System.out.println("Invalid input!");
                continue;
            }
            GemColor col = convertToColor(color);

            if (gameRules.canTakeTwoSameGems(col, gems)) {
                GemCollection add = new GemCollection();
                add.add(col, 2);
                player.addGems(add);
                gems.subtract(add);
                return true;
            }
        }
    }

    public static boolean handlePurchaseCard(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display) {
        CardMarket cardMarket = gameState.getCardMarket();

        while (true) {
            try {
                System.out.print("From table or from reserved?  [\"back\" to return]: ");
                String str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return false;
                }

                if (str.equalsIgnoreCase("reserved")) {
                    List<DevelopmentCard> reservedCards = player.getReservedCards();
                    if (reservedCards.size() == 0) {
                        System.out.println("No reserved cards\n");
                        return false;
                    }
                    display.printReservedCards(player);
                    System.out.print("Number to purchase  [\"back\" to return]:");
                    str = sc.nextLine();
                    if (str.equalsIgnoreCase("back")) {
                        return false;
                    }
                    int num = Integer.parseInt(str);
                    if (num < 0 || num >= reservedCards.size()) {
                        System.out.println("Invalid input");
                        continue;
                    }
                    DevelopmentCard chosen = reservedCards.get(num);
                    player.addCard(chosen);
                
                    GemCollection cost = gameRules.calculateActualCost(player, chosen);
                    player.deductGems(cost);
                    player.removeReservedCard(chosen);

                    gameState.addGemsToBank(cost);
                    return true;
                }

                display.printVisibleCards(gameState);
                System.out.print("Level  [\"back\" to return]: ");
                str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return false;
                }
                int level = Integer.parseInt(str);
                if (level < 1 || level > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                System.out.print("Number (0 to 3)  [\"back\" to return]: ");
                str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return false;
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

                player.addCard(chosen);
                
                GemCollection cost = gameRules.calculateActualCost(player, chosen);
                player.deductGems(cost);
                cardMarket.removeCard(level, number);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));

                gameState.addGemsToBank(cost);
                return true;
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            } catch (UnavailableCardException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static boolean handleReserveCard(Scanner sc, Player player, GameState gameState, GameRules gameRules, DisplayUI display) {
        if (!gameRules.canReserveCard(player)) {
            System.out.println("No more reserve slot!");
            return false;
        }
        CardMarket cardMarket = gameState.getCardMarket();
        GemCollection gemBank = gameState.getGemBank();

        while (true) {
            try {
                display.printVisibleCards(gameState);

                System.out.print("Level  [\"back\" to return]: ");
                String str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return false;
                }
                int level = Integer.parseInt(str);
                if (level < 1 || level > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                System.out.print("Number (0 to 4 -> 4 = random)  [\"back\" to return]: ");
                str = sc.nextLine();
                if (str.equalsIgnoreCase("back")) {
                    return false;
                }
                int number = Integer.parseInt(str);

                if (number < 0 || number > 4) {
                    System.out.println("Invalid input");
                    continue;
                }

                if (number == 4) {
                    DevelopmentCard chosen = cardMarket.drawCard(level);
                    player.addReservedCard(chosen);
                    if (gemBank.getCount(GemColor.GOLD_JOKER) > 0) {
                        GemCollection gems = new GemCollection();
                        gems.add(GemColor.GOLD_JOKER, 1);
                        gemBank.subtract(gems);
                        player.addGems(gems);
                    }
                    return true;
                }
                DevelopmentCard chosen = cardMarket.getVisibleCard(level, number);

                player.addReservedCard(chosen);

                cardMarket.removeCard(level, number);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));

                if (gemBank.getCount(GemColor.GOLD_JOKER) > 0) {
                    GemCollection gems = new GemCollection();
                    gems.add(GemColor.GOLD_JOKER, 1);
                    gemBank.subtract(gems);
                    player.addGems(gems);
                }
                return true;
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            } catch (UnavailableCardException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void handleGemReturn(Scanner sc, Player player, GameRules gameRules, GameState gameState) {
        while (gameRules.mustReturnGems(player)) {
            try {
                System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx): ");
                String color = sc.nextLine();
                color = color.toUpperCase();

                if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                    System.out.println("Invalid input!");
                    continue;
                }

                GemColor col = convertToColor(color);
                if (player.getSpecificGem(col) < 1) {
                    System.out.println(String.format("No more %s gem", color));
                    continue;
                }

                GemCollection gems = new GemCollection();
                gems.add(col, 1);
                player.deductGems(gems);

                gameState.addGemsToBank(gems);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }
    }


    public static void handleNobleClaims(Player player, GameState gameState, GameRules gameRules, Scanner sc) {
        List<Noble> nobles = gameRules.getClaimableNobles(player, gameState.getAvailableNobles());
        if (nobles.size() == 0) {
            return;
        }
        if (nobles.size() == 1 || player instanceof Bot) {
            player.claimNoble(nobles.get(0));
            gameState.removeNoble(nobles.get(0));
            return;
        }
        int i = 0;

        for (Noble noble: nobles) {
            System.out.println("Number: " + i + "| " + noble);
            i++;
        }
        System.out.println();

        System.out.printf("Which noble to pick? (Number 0 to %d)", i - 1);
        while (true) {
            try {
                int option = Integer.parseInt(sc.nextLine());
                if (option < 0 || option >= nobles.size()) {
                    continue;
                }
                player.claimNoble(nobles.get(option));
                gameState.removeNoble(nobles.get(option));
                return;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
        }

        
    }

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
