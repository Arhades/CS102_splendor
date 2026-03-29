package splendor.network.server;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
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
    public static synchronized void processPlayerAction(String actionCommand, String playerName) {
        System.out.println("Received from " + playerName + ": " + actionCommand);

        // action command example: 
        // TAKE THREE:RUBY:ONYX:EMERALD
        // ACTION:PURCHASE:RESERVED:0
        // ACTION:PURCHASE:1:2
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
                    if (gameStarted) {
                        sendToSpecificPlayer(playerName, "The game has already started!");
                        return; 
                    }
                    if (clients.size() < 2) {
                        sendToSpecificPlayer(playerName, "You need at least 2 players to start!");
                        return;
                    } 
                    clients.sort((c1, c2) -> {
                        if (c1.getBirthDate() == null && c2.getBirthDate() == null) return 0;
                        if (c1.getBirthDate() == null) return 1;
                        if (c2.getBirthDate() == null) return -1;
                        return c2.getBirthDate().compareTo(c1.getBirthDate());
                    });

                    String youngestName = clients.get(0).getPlayerName();
                    if (!playerName.equals(youngestName)) {
                        sendToSpecificPlayer(playerName, "Only the youngest player (" + youngestName + ") can start the game.");
                        sendToSpecificPlayer(playerName, "AND YOU ARE OLD");
                        return; 
                    }
                    try {
                        System.out.println("Loading cards and nobles...");
                        int numOfPlayers = clients.size();
                        List<Player> players = new ArrayList<>();
                        int id = 1;
                        for (ClientHandler client : clients) {
                            players.add(new Player(client.getPlayerName(), id)); 
                            id++;
                        }

                        GameConfig config = GameConfig.load("config.properties");

                        List<DevelopmentCard> levelOneDeck = GameEngine.loadCards(config.getCardsFile(), 1);
                        List<DevelopmentCard> levelTwoDeck = GameEngine.loadCards(config.getCardsFile(), 2);
                        List<DevelopmentCard> levelThreeDeck = GameEngine.loadCards(config.getCardsFile(), 3);
                        CardMarket cardMarket = new CardMarket(levelOneDeck, levelTwoDeck, levelThreeDeck);

                        List<Noble> nobles = GameEngine.loadNobles(config.getNoblesFile(), numOfPlayers + 1);

                        int standardGemCount = config.getGemCountPerColor(numOfPlayers);
                        
                        GemCollection initialGems = GameEngine.buildGemBank(standardGemCount, config);

                        // Initialize GameState with the winning threshold from config
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
                    System.out.println("Taking two gems: " + parts[1]);
                    
                    // get the color they requested and convert it
                    String colorStr = parts[1].toUpperCase();
                    GemColor col = GameEngine.convertToColor(colorStr);
                    
                    if (!gameRules.validColor(colorStr) || colorStr.equals("GOLD_JOKER")) {
                        sendToSpecificPlayer(playerName, "Invalid gem color.");
                        break;
                    }
                    
                    if (gameRules.canTakeTwoSameGems(col, gameState.getGemBank())) {
                        GemCollection gemsToTake = new GemCollection();
                        gemsToTake.add(col, 2);
                        
                        expectedPlayer.addGems(gemsToTake);
                        gameState.getGemBank().subtract(gemsToTake);
                        
                        moveSuccessful = true;
                        broadcast(expectedPlayer.getName() + " took 2 " + colorStr + " gems.");
                        
                    } else {
                        sendToSpecificPlayer(playerName, "Not enough " + colorStr + " gems in the bank to take two.");
                    }
                    break;
                    
                case "TAKE THREE":
                    if (parts.length < 4) {
                        sendToSpecificPlayer(playerName, "You must select exactly 3 gem colors.");
                        break; 
                    }

                    String c1 = parts[1].toUpperCase();
                    String c2 = parts[2].toUpperCase();
                    String c3 = parts[3].toUpperCase();

                    System.out.println("Taking three gems: " + c1 + ", " + c2 + ", " + c3);

                    if (c1.equals(c2) || c1.equals(c3) || c2.equals(c3)) {
                        sendToSpecificPlayer(playerName, "You must choose 3 different gem colors.");
                        break;
                    }

                    if (!gameRules.validColor(c1) || c1.equals("GOLD_JOKER") ||
                        !gameRules.validColor(c2) || c2.equals("GOLD_JOKER") ||
                        !gameRules.validColor(c3) || c3.equals("GOLD_JOKER")) {
                        sendToSpecificPlayer(playerName, "Invalid gem colors selected.");
                        break;
                    }

                    GemCollection gemsToAdd = new GemCollection();
                    gemsToAdd.add(GameEngine.convertToColor(c1), 1);
                    gemsToAdd.add(GameEngine.convertToColor(c2), 1);
                    gemsToAdd.add(GameEngine.convertToColor(c3), 1);

                    if (gameRules.canTakeThreeDifferentGems(gemsToAdd, gameState.getGemBank())) {
                        expectedPlayer.addGems(gemsToAdd);
                        gameState.getGemBank().subtract(gemsToAdd);
                        // if true, then the turn can go on
                        moveSuccessful = true; 
                        broadcast(expectedPlayer.getName() + " took 1 " + c1 + ", 1 " + c2 + ", and 1 " + c3 + ".");
                        
                    } else {
                        sendToSpecificPlayer(playerName, "The bank does not have enough of those gems.");
                    }
                    break;
                    
                case "PURCHASE":
                    if (parts.length < 3) {
                        sendToSpecificPlayer(playerName, "Invalid purchase command format.");
                        break;
                    }
                    
                    // this is the "1", "2", "3", or "RESERVED"
                    String sourceOrLevel = parts[1].toUpperCase();
                    int index = 0;
                    int index2 = 0;
                    if (!(sourceOrLevel.equals("RESERVED"))) {
                        try {
                            index = Integer.parseInt(parts[2]);
                            index2 = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            sendToSpecificPlayer(playerName, "Card index must be a number.");
                            break;
                        }

                        System.out.println("Processing purchase for Card level " + sourceOrLevel + " and Index " + index);
                    }
                    DevelopmentCard cardToBuy = null;
                    try {
                        if (sourceOrLevel.equals("RESERVED")) {
                            List<DevelopmentCard> reservedCards = expectedPlayer.getReservedCards();
                            if (index < 0 || index >= reservedCards.size()) {
                                sendToSpecificPlayer(playerName, "Invalid reserved card index.");
                                break;
                            }
                            cardToBuy = reservedCards.get(index);
                            
                        } else if ("1".equals(sourceOrLevel) || 
                                   "2".equals(sourceOrLevel) || 
                                   "3".equals(sourceOrLevel)) {
                            int level = Integer.parseInt(sourceOrLevel);
                            if (level < 1 || level > 3 || index < 0 || index > 3) {
                                sendToSpecificPlayer(playerName, "Invalid level or card index.");
                                break;
                            }
                            cardToBuy = gameState.getCardMarket().getVisibleCard(level, index);
                            gameState.getCardMarket().removeCard(level, index);
                            // remove and replace
                            gameState.getCardMarket().splitVisible(
                            gameState.getCardMarket().getDeckCards(level), 
                            gameState.getCardMarket().getVisibleCards(level)
                        );
                        } else {
                            expectedPlayer.removeReservedCard(cardToBuy);
                        }
                    } catch (NumberFormatException e) {
                         sendToSpecificPlayer(playerName, "Level must be 1, 2, or 3, or 'RESERVED'.");
                         break;
                    } catch (UnavailableCardException e) {
                        sendToSpecificPlayer(playerName, "That card is no longer available to be bought.");
                        break;
                    } catch (NullPointerException e) {
                        sendToSpecificPlayer(playerName, "You do not have such a reserved card.");
                    }

                    if (!gameRules.canAffordCard(expectedPlayer, cardToBuy)) {
                        sendToSpecificPlayer(playerName, "You cannot afford this card.");
                        break;
                    }
                    
                    GemCollection actualCost = gameRules.calculateActualCost(expectedPlayer, cardToBuy);
                    
                    expectedPlayer.deductGems(actualCost);
                    expectedPlayer.addCard(cardToBuy);
                    gameState.addGemsToBank(actualCost);

                    moveSuccessful = true;
                    broadcast(expectedPlayer.getName() + " purchased a card.");
                    break;
                    
                case "RESERVE":
                    if (parts.length < 3) {
                        sendToSpecificPlayer(playerName, "Invalid reserve command format.");
                        break;
                    }

                    int reserveLevel;
                    int reserveIndex;

                    try {
                        reserveLevel = Integer.parseInt(parts[2]);
                        reserveIndex = Integer.parseInt(parts[3]);
                    } catch (NumberFormatException e) {
                        sendToSpecificPlayer(playerName, "Level and Index must be numbers.");
                        break;
                    }

                    System.out.println("Reserving card level " + reserveLevel + ", index " + reserveIndex);
                    if (!gameRules.canReserveCard(expectedPlayer)) {
                        sendToSpecificPlayer(playerName, "You cannot reserve more than 3 cards.");
                        break;
                    }

                    DevelopmentCard cardToReserve = null;

                    try {
                        if (reserveLevel < 1 || reserveLevel > 3 || reserveIndex < 0 || reserveIndex > 3) {
                            sendToSpecificPlayer(playerName, "Invalid level or card index.");
                        }
                        cardToReserve = gameState.getCardMarket().getVisibleCard(reserveLevel, reserveIndex);

                        // remove and replace
                        gameState.getCardMarket().removeCard(reserveLevel, reserveIndex);
                        gameState.getCardMarket().splitVisible(
                        gameState.getCardMarket().getDeckCards(reserveLevel), 
                        gameState.getCardMarket().getVisibleCards(reserveLevel)
                        );
                        
                    } catch (NumberFormatException e) {
                         sendToSpecificPlayer(playerName, "Level or Card index must be 1, 2, or 3 and 1, 2, 3, 4 respectively.");
                    } catch (UnavailableCardException e) {
                        sendToSpecificPlayer(playerName, "That card is no longer available to reserve.");
                    }

                    expectedPlayer.addReservedCard(cardToReserve);

                    GemColor jokerColor = GameEngine.convertToColor("GOLD_JOKER");
                    boolean gotJoker = false;
                    
                    // check joker's availablility
                    if (gameState.getGemBank().getCount(jokerColor) > 0) {
                        GemCollection jokerReward = new GemCollection();
                        jokerReward.add(jokerColor, 1);
                        
                        expectedPlayer.addGems(jokerReward);
                        gameState.getGemBank().subtract(jokerReward);
                        gotJoker = true;
                    }

                    moveSuccessful = true;
                    
                    String message = expectedPlayer.getName() + " reserved a Level " + reserveLevel + " card.";
                    if (gotJoker) {
                        message += " They also received 1 Gold Joker.";
                    }
                    broadcast(message);
                    
                    break;
                    
                default:
                    sendToSpecificPlayer(playerName, "Unknown command.");
            }

            if (moveSuccessful) {
                broadcast(playerName + " did action: " + actionType);
                gameState.advanceToNext();
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
            processPlayerAction(message, sender.getPlayerName());
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

        StringBuilder stateStr = new StringBuilder();
        stateStr.append("BOARD_STATE:");

        stateStr.append("BANK=");
        stateStr.append(gameState.getGemBank().getBankAsString()); 
        stateStr.append("|");

        stateStr.append("MARKET=");
        stateStr.append(gameState.getCardMarket().getMarketAsString());
        stateStr.append("|");

        for (Player p : gameState.getPlayers()) {
            stateStr.append("PLAYER=").append(p.getName()).append(",");
            stateStr.append(p.getPlayerStateAsString());
            stateStr.append("|");
        }

        // send this massive string to every connected client
        broadcast(stateStr.toString());
    }
}