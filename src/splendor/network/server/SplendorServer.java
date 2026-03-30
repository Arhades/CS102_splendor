package splendor.network.server;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.config.*;
import splendor.display.*;
import splendor.entity.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.exception.*;
import splendor.rules.*;
import splendor.valueobjects.*;

public class SplendorServer {
    private static final int PORT = 9090;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static GameState gameState;
    private static GameRules gameRules;
    public static volatile boolean gameStarted = false;
    public static volatile boolean isLastRound = false;
    public final CardMarket market = gameState.getCardMarket();
    public final List<Noble> nobles = gameState.getAvailableNobles();

    public static void main(String[] args) {
        System.out.println("Splendor Server is starting...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (!gameStarted) {
                if (clients.size() >= 4) {
                    System.out.println("Lobby is full. Waiting for the host to start game");
                    // wait a bit so we don't spam the CPU
                    try { 
                        Thread.sleep(2000); 
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                    continue; 
                }
                Socket socket = serverSocket.accept(); 
                int newPlayerId = clients.size() + 1;
                System.out.println("A new player has connected! Assigning as Player " + newPlayerId);

                ClientHandler clientThread = new ClientHandler(socket);
                clients.add(clientThread);
                
                Thread thread = new Thread(clientThread);
                thread.start();
                
                broadcast("Player " + newPlayerId + " has joined the lobby. (" + clients.size() + "/4)");
            }
            
            System.out.println("The game has started! The server is no longer accepting new connections.");

        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }

    // the synchronized keyword ensures that only one thread can be doing an action at any given time
    public static synchronized void processPlayerAction(String actionCommand, ClientHandler player) {
        String playerName = player.getPlayerName();
        System.out.println("Received from " + playerName + ": " + actionCommand);
        String[] parts = actionCommand.split(":");
        if (parts.length < 1 || parts[0].trim().isEmpty()) {
            return;
        }


        String actionType = parts[0].trim().toUpperCase();

        if (gameState == null && !actionType.equals("START GAME")) {
            sendToSpecificPlayer(playerName, "The game has not started yet.");
            return;
        }

        Player expectedPlayer = null;
        if (gameState != null && !actionType.equals("START GAME")) {
            expectedPlayer = gameState.getCurrentPlayer();
            if (!playerName.equals(expectedPlayer.getName())) {
                sendToSpecificPlayer(playerName, "It is not your turn yet.");
                return;
            }
        }


        boolean moveSuccessful = false;

        try {
            switch (actionType) {
                case "START GAME":
                    int numOfPlayers = clients.size();
                    if (gameStarted) {
                        sendToSpecificPlayer(playerName, "The game has already started!");
                        return; 
                    }
                    if (clients.size() < 2) {
                        sendToSpecificPlayer(playerName, "You need at least 2 players to start!");
                        return;
                    } 
                    // wrong way to sort
                    // clients.sort((c1, c2) -> {
                    //     if (c1.getBirthDate() == null && c2.getBirthDate() == null) return 0;
                    //     if (c1.getBirthDate() == null) return 1;
                    //     if (c2.getBirthDate() == null) return -1;
                    //     return c2.getBirthDate().compareTo(c1.getBirthDate());
                    // });
                    String youngestName = clients.get(0).getPlayerName();

                    //===============================
                    //GUYS FIND A WAY TO DO THE CLOCKWISE THING
                    //===============================

                    if (!playerName.equals(youngestName)) {
                        sendToSpecificPlayer(playerName, "Only the youngest player (" + youngestName + ") can start the game.");
                        sendToSpecificPlayer(playerName, "AND YOU ARE OLD");
                        return;
                    }
                    DisplayUI.printStartMessage(youngestName);
                    List<Player> players = new ArrayList<>();
                    int id = 1;
                    for (ClientHandler client : clients) {
                        players.add(new Player(client.getPlayerName(), id));
                        id++;
                    }
                    try {
                        GameConfig config = GameConfig.load("config.properties");

                        List<DevelopmentCard> levelOneDeck = GameEngine.loadCards(config.getCardsFile(), 1);
                        List<DevelopmentCard> levelTwoDeck = GameEngine.loadCards(config.getCardsFile(), 2);
                        List<DevelopmentCard> levelThreeDeck = GameEngine.loadCards(config.getCardsFile(), 3);
                        CardMarket cardMarket = new CardMarket(levelOneDeck, levelTwoDeck, levelThreeDeck);

                        List<Noble> nobles = GameEngine.loadNobles(config.getNoblesFile(), numOfPlayers + 1);

                        int standardGemCount = config.getGemCountPerColor(numOfPlayers);
                        
                        GemCollection initialGems = GameEngine.buildGemBank(numOfPlayers, config);
                        gameState = new GameState(players, cardMarket, initialGems, nobles, config.getWinningThreshold());
                        gameRules = new GameRules(gameState);

                        gameStarted = true;
                        System.out.println("Game board successfully initialized!");
                        
                        broadcast("The game has started with " + numOfPlayers + " players with " + youngestName + " being the youngest player!");
                        broadcast("It is now " + gameState.getCurrentPlayer().getName() + "'s turn.");
                        broadcastGameState();

                    } catch (NullPointerException e) {
                        System.out.println("Client does not exist");
                        e.printStackTrace();
                        broadcast("Player missing");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid Argument");
                        e.printStackTrace();
                        broadcast("Wrong input by player.");
                    }
                    catch (Exception e) {
                        System.out.println("Failed to initialize game.");
                        e.printStackTrace();
                        broadcast("The game crashed");
                    }
                    return;
                
                case "TAKE TWO":
                    String resultTwo = handleTakeTwoSame(
                        expectedPlayer, gameState, gameRules, parts[1]
                    );

                    if (resultTwo.equals("SUCCESS")) {
                        moveSuccessful = true;
                        broadcast(expectedPlayer.getName() + " took 2 " + parts[1].toUpperCase() + " gems.");
                    } else {
                        sendToSpecificPlayer(playerName, resultTwo.replace("ERROR: ", ""));
                    }
                    break;
                    
                case "TAKE THREE":
                        String resultThree = handleTakeThreeDifferent(
                        expectedPlayer, gameState, gameRules, parts[1], parts[2], parts[3]
                    );

                    if (resultThree.equals("SUCCESS")) {
                        moveSuccessful = true; 
                        broadcast(expectedPlayer.getName() + " took 1 " + parts[1].toUpperCase() + ", 1 " + parts[2].toUpperCase() + ", and 1 " + parts[3].toUpperCase() + ".");
                    } else {
                        sendToSpecificPlayer(playerName, resultThree.replace("ERROR: ", ""));
                    }
                    break;
                    
                case "PURCHASE":
                    if (parts.length < 3) {
                        sendToSpecificPlayer(playerName, "Invalid purchase command format.");
                        break;
                    }

                    boolean isReserved = parts[1].equals("RESERVED");
                    int level = 0;
                    int index = 0;

                    try {
                        if (isReserved) {
                            index = Integer.parseInt(parts[2]); 
                        } else {
                            level = Integer.parseInt(parts[1]);
                            index = Integer.parseInt(parts[2]);
                        }
                    } catch (NumberFormatException e) {
                        sendToSpecificPlayer(playerName, "Card numbers must be integers.");
                        break;
                    }

                    String resultPurchase = handlePurchaseCard(
                        expectedPlayer, gameState, gameRules, isReserved, level, index
                    );

                    if (resultPurchase.equals("SUCCESS")) {
                        moveSuccessful = true;
                        String location = isReserved ? "their reserved hand" : "the board";
                        broadcast(expectedPlayer.getName() + " purchased a card from " + location + ".");
                        
                        List<Noble> earnedNobles = gameRules.getClaimableNobles(expectedPlayer, gameState.getAvailableNobles());
                        for (Noble noble : earnedNobles) {
                            expectedPlayer.claimNoble(noble);
                            gameState.removeNoble(noble);
                            broadcast(expectedPlayer.getName() + " was visited by a Noble! (+" + noble.getPoints() + " points)");
                        }

                    } else {
                        sendToSpecificPlayer(playerName, resultPurchase.replace("ERROR: ", ""));
                    }
                    break;
                    
                case "RESERVE":
                    if (parts.length < 3) {
                        sendToSpecificPlayer(playerName, "Invalid reserve command format.");
                        break;
                    }

                    int reserveLevel;
                    int reserveIndex;
                    try {
                        reserveLevel = Integer.parseInt(parts[1]);
                        reserveIndex = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        sendToSpecificPlayer(playerName, "Level and Index must be numbers.");
                        break;
                    }

                    String resultReserve = handleReserveCard(
                        expectedPlayer, gameState, gameRules, reserveLevel, reserveIndex
                    );

                    if (resultReserve.startsWith("SUCCESS")) {
                        moveSuccessful = true;
                        
                        String type = (reserveIndex == 4) ? "the top of the deck" : "the board";
                        String message = expectedPlayer.getName() + " reserved a Level " + reserveLevel + " card from " + type + ".";
                        
                        if (resultReserve.equals("SUCCESS_JOKER")) {
                            message += " They also received 1 Gold Joker.";
                        }
                        
                        broadcast(message);
                    } else {
                        sendToSpecificPlayer(playerName, resultReserve.replace("ERROR: ", ""));
                    }
                    break;
                    
                default:
                    sendToSpecificPlayer(playerName, "Unknown command.");
                    break;
            }
    

            if (moveSuccessful) {
                broadcast(playerName + " did action: " + actionType);
                // check if they hit the winning threshold and if so, this will be the last round
                if (expectedPlayer.getPoints() >= gameState.getWinningThreshold() && !isLastRound) { 
                    isLastRound = true;
                    broadcast(expectedPlayer.getName() + " has reached " + gameState.getWinningThreshold() + " points!");
                    broadcastGameState();
                }
                
                // check if is last player
                List<Player> allPlayers = gameState.getPlayers();
                boolean isLastPlayer = allPlayers.indexOf(expectedPlayer) == (allPlayers.size() - 1);
                
                // end game if both true
                if (isLastRound && isLastPlayer) {
                    String winnerScreen = DisplayUI.getWinner(gameState, gameRules).replace("\n", "@@");
                    broadcast("BOARD_STATE:" + winnerScreen);
                    broadcast("The game has ended! Thanks for playing.");
                    System.exit(0);
                    return;
                }
                gameState.advanceToNext();
                broadcastGameState();
                broadcast("It is now " + gameState.getCurrentPlayer().getName() + "'s turn.");
            } else {
                sendToSpecificPlayer(playerName, "Illegal move. Try again.");
            }

        } catch (Exception e) {
            System.out.println("CRASH DURING ACTION: " + actionType);
            e.printStackTrace(); 
            sendToSpecificPlayer(playerName, "Invalid Input");
        }
    }

    public static synchronized void sendToSpecificPlayer(String targetPlayerName, String message) {
        for (ClientHandler client : clients) {
            if (client.getPlayerName().equals(targetPlayerName)) {
                client.sendMessage(message);
                break;
            }
        }
    }

    public static synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static synchronized void processClientMessage(ClientHandler sender, String message) {
        if (message.startsWith("JOINED:")) {
            String[] parts = message.split(":");
            sender.setPlayerName(parts[1]);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(parts[2], formatter);
            sender.setBirthDate(date);
            System.out.println(parts[1] + " successfully joined");
        } else {
            processPlayerAction(message, sender);
        }
    }

    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(client.getPlayerName() + " left the game.");
        
        if (gameStarted) {
            broadcast(client.getPlayerName() + " disconnected. The game is over.");
            System.exit(0);
        }
    }

    public static void broadcastGameState() {
        if (gameState == null) {
            return;
        }
        String gameBoard = DisplayUI.getGameState(gameState);
        
        // need replace \n with @@ cuz the broadcast cant read \n
        String board = gameBoard.replace("\n", "@@");
        broadcast("BOARD_STATE:" + board);
    }

    public static String handlePurchaseCard(Player player, GameState gameState, GameRules gameRules, boolean isReserved, int level, int index) {
        CardMarket cardMarket = gameState.getCardMarket();

        try {
            DevelopmentCard chosen = null;

            if (isReserved) {
                List<DevelopmentCard> reservedCards = player.getReservedCards();
                if (reservedCards.size() == 0) {
                    return "ERROR: You do not have any reserved cards.";
                }
                if (index < 0 || index >= reservedCards.size()) {
                    return "ERROR: Invalid reserved card index.";
                }
                chosen = reservedCards.get(index);

                if (!gameRules.canAffordCard(player, chosen)) {
                    return "ERROR: You cannot afford this reserved card.";
                }

                player.addCard(chosen);
                GemCollection cost = gameRules.calculateActualCost(player, chosen);
                player.deductGems(cost);
                player.removeReservedCard(chosen);
                gameState.addGemsToBank(cost);

                return "SUCCESS";

            } else {
                if (level < 1 || level > 3 || index < 0 || index > 3) {
                    return "ERROR: Invalid card level or index.";
                }
                chosen = cardMarket.getVisibleCard(level, index);

                if (!gameRules.canAffordCard(player, chosen)) {
                    return "ERROR: You cannot afford this card.";
                }
                player.addCard(chosen);
                GemCollection cost = gameRules.calculateActualCost(player, chosen);
                player.deductGems(cost);
                
                cardMarket.removeCard(level, index);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));
                gameState.addGemsToBank(cost);

                return "SUCCESS";
            }
        } catch (UnavailableCardException e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    public static String handleReserveCard(Player player, GameState gameState, GameRules gameRules, int level, int number) {
        if (!gameRules.canReserveCard(player)) {
            return "ERROR: You cannot reserve more than 3 cards.";
        }

        if (level < 1 || level > 3 || number < 0 || number > 4) {
            return "ERROR: Invalid level or card index.";
        }

        CardMarket cardMarket = gameState.getCardMarket();
        GemCollection gemBank = gameState.getGemBank();
        boolean gotJoker = false;

        try {
            DevelopmentCard chosen = null;

            if (number == 4) {
                chosen = cardMarket.drawCard(level);
                player.addReservedCard(chosen);
            } else {
                chosen = cardMarket.getVisibleCard(level, number);
                player.addReservedCard(chosen);
                cardMarket.removeCard(level, number);
                cardMarket.splitVisible(cardMarket.getDeckCards(level), cardMarket.getVisibleCards(level));
            }

            GemColor jokerColor = GemColor.GOLD_JOKER;
            if (gemBank.getCount(jokerColor) > 0) {
                GemCollection gems = new GemCollection();
                gems.add(jokerColor, 1);
                gemBank.subtract(gems);
                player.addGems(gems);
                gotJoker = true;
            }

            return gotJoker ? "SUCCESS_JOKER" : "SUCCESS_NO_JOKER";

        } catch (UnavailableCardException e) {
            return "ERROR: That card or deck is no longer available.";
        }
    }
    public static String handleTakeTwoSame(Player player, GameState gameState, GameRules gameRules, String colorStr) {
        GemCollection gems = gameState.getGemBank();
        colorStr = colorStr.toUpperCase();

        if (!gameRules.validColor(colorStr) || colorStr.equals("GOLD_JOKER")) {
            return "ERROR: Invalid gem color.";
        }

        GemColor col = GameEngine.convertToColor(colorStr);

        if (gameRules.canTakeTwoSameGems(col, gems)) {
            GemCollection add = new GemCollection();
            add.add(col, 2);
            player.addGems(add);
            gems.subtract(add);
            return "SUCCESS";
        } else {
            return "ERROR: Not enough " + colorStr + " gems in the bank to take two.";
        }
    }

    public static String handleTakeThreeDifferent(Player player, GameState gameState, GameRules gameRules, String c1, String c2, String c3) {
        GemCollection gems = gameState.getGemBank();
        c1 = c1.toUpperCase();
        c2 = c2.toUpperCase();
        c3 = c3.toUpperCase();

        if (c1.equals(c2) || c1.equals(c3) || c2.equals(c3)) {
            return "ERROR: You must choose 3 different gem colors.";
        }

        if (!gameRules.validColor(c1) || c1.equals("GOLD_JOKER") ||
            !gameRules.validColor(c2) || c2.equals("GOLD_JOKER") ||
            !gameRules.validColor(c3) || c3.equals("GOLD_JOKER")) {
            return "ERROR: Invalid gem colors selected.";
        }

        GemCollection add = new GemCollection();
        add.add(GameEngine.convertToColor(c1), 1);
        add.add(GameEngine.convertToColor(c2), 1);
        add.add(GameEngine.convertToColor(c3), 1);

        if (gameRules.canTakeThreeDifferentGems(add, gems)) {
            player.addGems(add);
            gems.subtract(add);
            return "SUCCESS";
        } else {
            return "ERROR: The bank does not have enough of those gems.";
        }
    }
}