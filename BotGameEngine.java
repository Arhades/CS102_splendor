import java.util.*;
import java.io.*;

public class BotGameEngine {
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
        System.out.printf("Player %d name (blank for %s): ", playerNumber, defaultName);
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
            case "EMERALD":
                return GemColor.EMERALD;
            case "RUBY":
                return GemColor.RUBY;
            case "GOLD_JOKER":
                return GemColor.GOLD_JOKER;
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

        GemCollection bank = new GemCollection();
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                continue;
            }
            for (int i = 0; i < numToAdd; i++) {
                bank.add(color, 1);
            }
        }
        for (int i = 0; i < 5; i++) {
            bank.add(GemColor.GOLD_JOKER, 1);
        }
        return bank;
    }

    public static void runGame() {
        try {
            Scanner sc = new Scanner(System.in);

            int numPlayers = promptNumPlayers(sc);
            List<Player> players = createPlayers(sc, numPlayers);

            List<Card> levelOneDeck = loadCards("cards.csv", 1);
            List<Card> levelTwoDeck = loadCards("cards.csv", 2);
            List<Card> levelThreeDeck = loadCards("cards.csv", 3);

            BotCardMarket cardMarket = new BotCardMarket(levelOneDeck, levelTwoDeck, levelThreeDeck);
            GemCollection initialGems = buildGemBank(numPlayers);
            List<Noble> nobles = loadNobles("nobles.csv");

            BotGameState gameState = new BotGameState(players, cardMarket, initialGems, nobles);
            BotGameRules gameRules = new BotGameRules(gameState);

            while (!gameState.isGameOver()) {
                System.out.println("--------------------------------------------------------------------------------------------------");
                System.out.println();
                Player currentPlayer = gameState.getCurrentPlayer();
                printGameState(gameState);

                if (currentPlayer instanceof Bot) {
                    Bot bot = (Bot) currentPlayer;
                    System.out.println(bot.takeTurn(gameState, gameRules));
                } else {
                    boolean validAction = false;
                    while (!validAction) {
                        ActionType action = promptAction(sc);
                        validAction = executeAction(sc, action, currentPlayer, gameState, gameRules);
                    }
                    handleGemReturn(sc, currentPlayer, gameRules, gameState);
                }

                handleNobleClaims(currentPlayer, gameState, gameRules);
                checkEndCondition(gameState, gameRules);
                gameState.advanceToNext();
                System.out.println();
            }

            printWinner(gameState, gameRules);
        } catch (InvalidFileException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void printGameState(BotGameState gameState) {
        printPlayer(gameState);
        printNobles(gameState);
        printVisibleCards(gameState);
        printGemBank(gameState);
        printPoints(gameState);
        printReservedCards(gameState.getCurrentPlayer());
        printPlayerGem(gameState.getCurrentPlayer());
    }

    public static void printPlayer(BotGameState gameState) {
        System.out.println(String.format("%s's turn", gameState.getCurrentPlayer().getName()));
        System.out.println();
    }

    public static void printNobles(BotGameState gameState) {
        System.out.println("NOBLES AVAILABLE");
        if (gameState.getAvailableNobles().size() == 0) {
            System.out.println("No more nobles");
        }
        for (Noble noble: gameState.getAvailableNobles()) {
            System.out.println("-> " + noble);
        }
        System.out.println();
    }

    public static void printVisibleCards(BotGameState gameState) {
        System.out.println("AVAILABLE CARDS FOR PURCHASE");
        for (int level = 1; level <= 3; level++) {
            System.out.println("Level " + level + ":");
            try {
                List<Card> cards = gameState.getCardMarket().getVisibleCards(level);
                if (cards.size() == 0) {
                    System.out.println("-> EMPTY");
                }
                for (int i = 0; i < cards.size(); i++) {
                    System.out.println("-> [" + i + "] " + cards.get(i));
                }
            } catch (UnavailableCardException e) {
                System.out.println("-> EMPTY");
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
        for (int i = 0; i < player.getReservedCards().size(); i++) {
            System.out.println("-> [" + i + "] " + player.getReservedCards().get(i));
        }
        System.out.println();
    }

    public static void printGemBank(BotGameState gameState) {
        System.out.println("GEMBANK");
        for (GemColor color: GemColor.values()) {
            System.out.println(String.format("-> %s: %d", color.name(), gameState.getGemBank().getCount(color)));
        }
        System.out.println();
    }

    public static void printPlayerGem(Player player) {
        System.out.println("PLAYER'S GEMS");
        Map<GemColor, Integer> bonus = player.calculateBonuses();
        for (GemColor color: GemColor.values()) {
            if (color.equals(GemColor.GOLD_JOKER)) {
                System.out.println(String.format("-> %s: %d", color.name(), player.getSpecificGem(color)));
            } else {
                System.out.println(String.format("-> %s: %d / bonus = %d", color.name(), player.getSpecificGem(color), bonus.get(color)));
            }
        }
        System.out.println();
    }

    public static void printPoints(BotGameState gameState) {
        System.out.println("POINTS");
        for (Player player: gameState.getPlayers()) {
            System.out.println(String.format("-> %s: %d", player.getName(), player.getPoints()));
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

        while (true) {
            try {
                int action = Integer.parseInt(sc.nextLine());
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
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                System.out.print("\nPick a number: ");
            }
        }
    }

    public static boolean executeAction(Scanner sc, ActionType action, Player player, BotGameState gameState, BotGameRules gameRules) {
        if (action.equals(ActionType.TAKE_THREE_DIFFERENT)) {
            return handleTakeThreeDifferent(sc, player, gameState, gameRules);
        } else if (action.equals(ActionType.TAKE_TWO_SAME)) {
            return handleTakeTwoSame(sc, player, gameState, gameRules);
        } else if (action.equals(ActionType.PURCHASE_CARD)) {
            return handlePurchaseCard(sc, player, gameState, gameRules);
        }
        return handleReserveCard(sc, player, gameState, gameRules);
    }

    public static boolean handleTakeThreeDifferent(Scanner sc, Player player, BotGameState gameState, BotGameRules gameRules) {
        if (!gameRules.canTakeThreeDifferentGems(gameState.getGemBank())) {
            System.out.println("Not enough gems");
            return false;
        }

        while (true) {
            List<GemColor> colors = new ArrayList<>();
            while (colors.size() < 3) {
                System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx): ");
                String color = sc.nextLine().trim().toUpperCase();
                if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                    System.out.println("Invalid input!");
                    continue;
                }
                GemColor chosen = convertToColor(color);
                if (colors.contains(chosen)) {
                    System.out.println("Already taken!");
                    continue;
                }
                colors.add(chosen);
            }
            if (GameActions.takeThreeDifferent(player, gameState, gameRules, colors)) {
                return true;
            }
            System.out.println("Those gems are not available.");
        }
    }

    public static boolean handleTakeTwoSame(Scanner sc, Player player, BotGameState gameState, BotGameRules gameRules) {
        if (!gameRules.canTakeTwoSameGems(gameState.getGemBank())) {
            System.out.println("Not enough gems");
            return false;
        }

        while (true) {
            System.out.print("Colour to take twice (Diamond, Sapphire, Emerald, Ruby, Onyx): ");
            String color = sc.nextLine().trim().toUpperCase();
            if (!gameRules.validColor(color) || color.equals("GOLD_JOKER")) {
                System.out.println("Invalid input!");
                continue;
            }
            GemColor chosen = convertToColor(color);
            if (GameActions.takeTwoSame(player, gameState, gameRules, chosen)) {
                return true;
            }
            System.out.println("Cannot take two of that color.");
        }
    }

    public static boolean handlePurchaseCard(Scanner sc, Player player, BotGameState gameState, BotGameRules gameRules) {
        while (true) {
            System.out.print("Buy visible or reserved? (v/r): ");
            String source = sc.nextLine().trim().toLowerCase();
            if (source.equals("r")) {
                if (player.getReservedCards().size() == 0) {
                    System.out.println("No reserved cards to buy.\n");
                    continue;
                }
                int index = promptBoundedInt(sc, "Reserved number: ", 0, player.getReservedCards().size() - 1);
                if (GameActions.purchaseReservedCard(player, gameState, gameRules, index)) {
                    return true;
                }
                System.out.println("Cannot afford that reserved card!\n");
            } else if (source.equals("v")) {
                int level = promptBoundedInt(sc, "Level: ", 1, 3);
                try {
                    List<Card> cards = gameState.getCardMarket().getVisibleCards(level);
                    if (cards.size() == 0) {
                        System.out.println("No visible cards left at that level.\n");
                        continue;
                    }
                    int index = promptBoundedInt(sc, "Number: ", 0, cards.size() - 1);
                    if (GameActions.purchaseVisibleCard(player, gameState, gameRules, level, index)) {
                        return true;
                    }
                    System.out.println("Cannot afford that card!\n");
                } catch (UnavailableCardException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("Invalid input\n");
            }
        }
    }

    public static boolean handleReserveCard(Scanner sc, Player player, BotGameState gameState, BotGameRules gameRules) {
        if (!gameRules.canReserveCard(player)) {
            System.out.println("You already have 3 reserved cards.");
            return false;
        }

        while (true) {
            System.out.print("Reserve visible or hidden? (v/h): ");
            String source = sc.nextLine().trim().toLowerCase();
            if (source.equals("h")) {
                int level = promptBoundedInt(sc, "Level: ", 1, 3);
                if (GameActions.reserveHiddenCard(player, gameState, gameRules, level)) {
                    return true;
                }
                System.out.println("No cards left in that deck.\n");
            } else if (source.equals("v")) {
                int level = promptBoundedInt(sc, "Level: ", 1, 3);
                try {
                    List<Card> cards = gameState.getCardMarket().getVisibleCards(level);
                    if (cards.size() == 0) {
                        System.out.println("No visible cards left at that level.\n");
                        continue;
                    }
                    int index = promptBoundedInt(sc, "Number: ", 0, cards.size() - 1);
                    if (GameActions.reserveVisibleCard(player, gameState, gameRules, level, index)) {
                        return true;
                    }
                } catch (UnavailableCardException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("Invalid input\n");
            }
        }
    }

    public static void handleGemReturn(Scanner sc, Player player, BotGameRules gameRules, BotGameState gameState) {
        while (gameRules.mustReturnGems(player)) {
            System.out.print("Colour to return (Diamond, Sapphire, Emerald, Ruby, Onyx, Gold_Joker): ");
            String color = sc.nextLine().trim().toUpperCase();
            if (!gameRules.validColor(color)) {
                System.out.println("Invalid input!");
                continue;
            }
            GemColor chosen = convertToColor(color);
            if (player.getSpecificGem(chosen) < 1) {
                System.out.println("You do not have that gem.");
                continue;
            }
            GemCollection gems = new GemCollection();
            gems.add(chosen, 1);
            player.deductGems(gems);
            gameState.addGemsToBank(gems);
        }
    }

    public static void handleNobleClaims(Player player, BotGameState gameState, BotGameRules gameRules) {
        List<Noble> nobles = gameRules.getClaimableNobles(player, gameState.getAvailableNobles());
        for (Noble noble: nobles) {
            player.claimNoble(noble);
            gameState.removeNoble(noble);
        }
    }

    public static boolean checkEndCondition(BotGameState gameState, BotGameRules gameRules) {
        for (Player player: gameState.getPlayers()) {
            if (gameRules.hasPlayerWon(player, gameState.getWinningThreshold())) {
                gameState.setGameOver(true);
                return true;
            }
        }
        return false;
    }

    public static void printWinner(BotGameState gameState, BotGameRules gameRules) {
        Player winner = gameRules.getWinner(gameState.getPlayers());
        if (winner == null) {
            System.out.println("No winner.");
            return;
        }
        System.out.println("Winner: " + winner.getName() + " with " + winner.getPoints() + " points.");
    }

    public static int promptBoundedInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(sc.nextLine());
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Invalid input");
        }
    }
}
