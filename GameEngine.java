import java.io.*;
import java.util.*;

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

    public static List<Player> createPlayers(Scanner sc, int n) {
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.printf("Player %d name: ", i + 1);
            String name = sc.nextLine();
            players.add(new Player(name, i + 1));
        }

        System.out.println();
        return players;
    }

    public static List<Card> loadCards(String filename, int level) throws InvalidFileException {
        try (Scanner sc = new Scanner(new File(filename))) {
            sc.nextLine();
            List<Card> cards = new ArrayList<>();
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
                cards.add(new Card(level, Integer.parseInt(cur[7]), color, new Cost(cost)));
            }
            return cards;
        } catch (FileNotFoundException e) {
            throw new InvalidFileException(String.format("File (%s) not found!!", filename));
        }
    } 

    public static GemColor convertToColor(String color) {
        switch (color) {
            case "DIAMOND":
                return GemColor.DIAMOND;
            case "ONYX":
                return GemColor.ONYX;
            case "EMERAlD":
                return GemColor.EMERALD;
            case "RUBY":
                return GemColor.RUBY;
            default:
                return GemColor.SAPPHIRE;
        }
    }

    public static List<Noble> loadNobles(String filename) throws InvalidFileException {
        try (Scanner sc = new Scanner(new File(filename))) {
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
            return nobles;
        } catch (FileNotFoundException e) {
            throw new InvalidFileException(String.format("File (%s) not found!!", filename));
        }
    } 

    public static GemCollection buildGemBank(int numPlayers) {
        int numToAdd = 0;
        switch (numPlayers) {
            case 4:
                numToAdd = 7;
                break;
            case 3:
                numToAdd = 5;
                break;
            default:
                numToAdd = 4;
        }

        Map<GemColor, Integer> map = new HashMap<>();
        map.put(GemColor.DIAMOND, numToAdd);
        map.put(GemColor.ONYX, numToAdd);
        map.put(GemColor.EMERALD, numToAdd);
        map.put(GemColor.RUBY, numToAdd);
        map.put(GemColor.SAPPHIRE, numToAdd);
        map.put(GemColor.GOLD_JOKER, 5);

        return new GemCollection(map);
    }

    public static void runGame() {
        try {
            Scanner sc = new Scanner(System.in);

            int numOfPlayers = promptNumPlayers(sc);
            List<Player> players = createPlayers(sc, numOfPlayers);

            List<Card> levelOneDeck = loadCards("cards.csv", 1);
            List<Card> levelTwoDeck = loadCards("cards.csv", 2);
            List<Card> levelThreeDeck = loadCards("cards.csv", 3);

            CardMarket cardMarket = new CardMarket(levelOneDeck, levelTwoDeck, levelThreeDeck);

            GemCollection initialGems = buildGemBank(numOfPlayers);

            List<Noble> nobles = loadNobles("nobles.csv");

            GameState gameState = new GameState(players, cardMarket, initialGems, nobles);
            GameRules gameRules = new GameRules(gameState);

            while (!gameState.isGameOver()) {
                System.out.println("--------------------------------------------------------------------------------------------------");
                System.out.println();
                Player curPlayer = gameState.getCurrentPlayer();
                printGameState(gameState);
                boolean validAction = false;
                    
                while (!validAction) {
                    ActionType action = promptAction(sc);
                    validAction = executeAction(sc, action, curPlayer, gameState, gameRules);
                }

                handleGemReturn(sc, curPlayer, gameRules, gameState);
                handleNobleClaims(curPlayer, gameState, gameRules);
                checkEndCondition(gameState, gameRules);
                gameState.advanceToNext();
                System.out.println();
            }
            printWinner(gameState, gameRules);
        } catch (InvalidFileException e) {
            System.out.println(e.getMessage());
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

        for (Card card: cards) {
            System.out.println("-> " + card);
        }
        System.out.println();
    }

    public static void printReservedCards(Player player) {
        System.out.println("PLAYER'S RESERVED CARDS");
        if (player.getReservedCards().size() == 0) {
            System.out.println("-> EMPTY\n");
            return;
        }

        for (Card card: player.getReservedCards()) {
            System.out.println("-> " + card);
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
            System.out.println(String.format("-> name: %s: %d", player.getName(), player.getPoints()));
        }
        System.out.println();
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

    public static boolean executeAction(Scanner sc, ActionType action, Player player, GameState gameState, GameRules gameRules) {
        if (action.equals(ActionType.TAKE_THREE_DIFFERENT)) {
            return handleTakeThreeDifferent(sc, player, gameState, gameRules);
        } else if (action.equals(ActionType.TAKE_TWO_SAME)) {
            return handleTakeTwoSame(sc, player, gameState, gameRules);
        } else if (action.equals(ActionType.PURCHASE_CARD)) {
            return handlePurchaseCard(sc, player, gameState, gameRules);
        } else {
            return handleReserveCard(sc, player, gameState, gameRules);
        }
    }

    public static boolean handleTakeThreeDifferent(Scanner sc, Player player, GameState gameState, GameRules gameRules) {
        GemCollection gems = gameState.getGemBank();
        
        if (!gameRules.canTakeThreeDifferentGems(gems)) {
            System.out.println("Not enough gems");
            return false;
        }

        boolean valid = false;
        GemCollection add = new GemCollection();

        while (!valid) {
            List<String> taken = new ArrayList<>();
            add = new GemCollection();
            while (!gems.isEmptyWithouJoker() && taken.size() != 3) {
                System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx): ");
                String color = sc.nextLine();
                color = color.toUpperCase();
                if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                    System.out.println("Invalid input!");
                    continue;
                }
                if (taken.contains(color)) {
                    System.out.println("Already taken!");
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

    public static boolean handleTakeTwoSame(Scanner sc, Player player, GameState gameState, GameRules gameRules) {
        GemCollection gems = gameState.getGemBank();

        if (!gameRules.canTakeTwoSameGems(gems)) {
            System.out.println("Not enough gems");
            return false;
        }


        while (true) {
            System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx, Gold_Joker): ");
            String color = sc.nextLine();
            color = color.toUpperCase();
            if (!gameRules.validColor(color)) {
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

    public static boolean handlePurchaseCard(Scanner sc, Player player, GameState gameState, GameRules gameRules) {
        CardMarket cardMarket = gameState.getCardMarket();

        while (true) {
            try {
                System.out.print("Level: ");
                int level = Integer.parseInt(sc.nextLine());
                if (level < 1 || level > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                System.out.print("Number (0 to 3): ");
                int number = Integer.parseInt(sc.nextLine());

                if (number < 0 || number > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                Card chosen = cardMarket.getVisibleCard(level, number);

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

    public static boolean handleReserveCard(Scanner sc, Player player, GameState gameState, GameRules gameRules) {
        CardMarket cardMarket = gameState.getCardMarket();
        GemCollection gemBank = gameState.getGemBank();

        while (true) {
            try {
                System.out.print("Level: ");
                int level = Integer.parseInt(sc.nextLine());
                if (level < 1 || level > 3) {
                    System.out.println("Invalid input");
                    continue;
                }

                System.out.print("Number (0 to 4 -> 4 = random): ");
                int number = Integer.parseInt(sc.nextLine());

                if (number < 0 || number > 4) {
                    System.out.println("Invalid input");
                    continue;
                }

                if (number == 4) {
                    Card chosen = cardMarket.drawCard(level);
                    player.addReservedCard(chosen);
                    return true;
                }
                Card chosen = cardMarket.getVisibleCard(level, number);

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
                System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx, Gold_Joker): ");
                String color = sc.nextLine();
                color = color.toUpperCase();

                if (!gameRules.validColor(color)) {
                    continue;
                }

                GemColor col = convertToColor(color);
                if (player.getSpecificGem(col) < 1) {
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


    public static void handleNobleClaims(Player player, GameState gameState, GameRules gameRules) {
        List<Noble> nobles = gameRules.getClaimableNobles(player, gameState.getAvailableNobles());
        if (nobles.size() == 0) {
            return;
        }
        for (Noble noble: nobles) {
            player.claimNoble(noble);
            gameState.removeNoble(noble);
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

    public static void printWinner(GameState gameState, GameRules gameRules) {
        List<Player> players = gameState.getPlayers();
        System.out.println(gameRules.getWinner(players));
    }
}