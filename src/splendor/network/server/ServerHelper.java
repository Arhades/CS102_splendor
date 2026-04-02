package splendor.network.server;

import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.config.*;
import splendor.display.*;
import splendor.entity.*;
import splendor.entity.bot.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.rules.*;
import splendor.valueobjects.*;

/**
 * Centralizes all server-side message handling, broadcasting, and game action processing.
 * All public methods are synchronized to ensure thread safety when multiple
 * ClientHandler threads call them concurrently.
 */
public class ServerHelper {

    /**
     * Default constructor.
     */
    public ServerHelper() {}

    /**
     * Sends a message to a specific player by name.
     *
     * @param targetPlayerName the name of the target player
     * @param message          the message to send
     * @param clients          the list of connected clients
     */
    public static synchronized void sendToSpecificPlayer(String targetPlayerName, String message, List<ClientHandler> clients) {
        for (ClientHandler client : clients) {
            if (client.getPlayerName() != null && client.getPlayerName().equals(targetPlayerName)) {
                client.sendMessage(message);
                break;
            }
        }
    }

    /**
     * Broadcasts the full game board state to all connected clients.
     * Each player receives their own reserved cards privately.
     *
     * @param gameState the current game state
     * @param clients   the list of connected clients
     */
    public static synchronized void broadcastGameState(GameState gameState, List<ClientHandler> clients) {
        if (gameState == null) {
            return;
        }
        String gameBoard = DisplayUI.getGameStateSocketWithoutReserved(gameState);
        
        // need replace \n with @@ cuz the broadcast cant read \n
        String board = gameBoard.replace("\n", "@@");
        broadcast(board, clients);
        for (ClientHandler client : clients) {
            Player clientPlayer = getPlayerByName(client.getPlayerName(), gameState); 
            
            if (clientPlayer != null) {
                String reservedSection = DisplayUI.getReservedCards(clientPlayer).replace("\n", "@@");
                client.sendMessage(reservedSection); 
            }
        }
    }

    /**
     * Finds a Player object by name from the game state's player list.
     *
     * @param name      the player name to search for
     * @param gameState the current game state
     * @return the matching Player, or null if not found
     */
    private static Player getPlayerByName(String name, GameState gameState) {
        for (Player p : gameState.getPlayers()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Removes a client from the server and handles disconnection cleanup.
     * If the game has already started, ends the game for all players.
     *
     * @param client  the ClientHandler to remove
     * @param clients the list of connected clients
     */
    public static synchronized void removeClient(ClientHandler client, List<ClientHandler> clients) {
        clients.remove(client);
        System.out.println(client.getPlayerName() + " left the game.");
        
        if (SplendorServer.gameStarted) {
            broadcast(client.getPlayerName() + " disconnected. The game is over.", clients);
            System.exit(0);
        }
    }
    
    /**
     * Processes a raw message from a client, handling JOINED messages, CHAT messages,
     * and routing game actions.
     * JOINED format: "JOINED:name:dd/MM/yyyy" for humans,
     * or "JOINED:name:dd/MM/yyyy:BOT:2" / "JOINED:name:dd/MM/yyyy:BOT:3" for bots.
     * CHAT format: "CHAT:message text" — broadcasts to all players.
     *
     * @param sender         the ClientHandler that sent the message
     * @param message        the raw message string
     * @param gameState      the current game state (may be null before game starts)
     * @param gameRules      the current game rules (may be null before game starts)
     * @param clients        the list of connected clients
     * @param botPlayerTypes map of player names to bot types
     */
    public static synchronized void processClientMessage(ClientHandler sender, String message, GameState gameState, GameRules gameRules, List<ClientHandler> clients, Map<String, Integer> botPlayerTypes) {
        if (message.startsWith("JOINED:")) {
            String[] parts = message.split(":");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(parts[2], formatter);

            // check if its a bot or human
            if (parts.length >= 5 && parts[3].equals("BOT")) {
                if (clients.size() >= 4) {
                    sender.sendMessage("The lobby is full (4/4). Cannot add more bots.");
                    return;
                }

                String botName = parts[1];
                int botType = Integer.parseInt(parts[4]);
                botPlayerTypes.put(botName, botType);

                // create a fake client handler (does nth)
                ClientHandler botHandler = new ClientHandler(null);
                botHandler.setPlayerName(botName);
                botHandler.setBirthDate(date);

                // Insert after the sender to preserve seating order
                int senderIndex = clients.indexOf(sender);
                if (senderIndex >= 0 && senderIndex < clients.size() - 1) {
                    clients.add(senderIndex + 1, botHandler);
                } else {
                    clients.add(botHandler);
                }

                broadcast("[SERVER]: " + botName + " (Bot) joined the lobby. (" + clients.size() + "/4)", clients);
                System.out.println(botName + " (Bot) successfully joined");
            } else {
                sender.setPlayerName(parts[1]);
                sender.setBirthDate(date);
                botPlayerTypes.put(parts[1], 0);
                System.out.println(parts[1] + " successfully joined");
            }
        } else if (message.startsWith("CHAT:")) {
            // Chat message — broadcast to all players with sender's name
            String chatContent = message.substring(5).trim();
            String senderName = sender.getPlayerName();
            if (senderName == null) senderName = "Unknown";
            broadcast("[CHAT] " + senderName + ": " + chatContent, clients);
        } else {
            processPlayerAction(message, sender, gameState, gameRules, clients, botPlayerTypes);
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message the message to broadcast
     * @param clients the list of connected clients
     */
    public static synchronized void broadcast(String message, List<ClientHandler> clients) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Processes a player action command received from a client.
     * Handles START GAME, TAKE TWO, TAKE THREE, PURCHASE, RESERVE,
     * RETURN GEM, and CHAT commands.
     *
     * @param actionCommand  the raw action string from the client
     * @param player         the ClientHandler that sent the command
     * @param gameState      the current game state
     * @param gameRules      the current game rules
     * @param clients        the list of connected clients
     * @param botPlayerTypes map of player names to bot types
     */
    public static synchronized void processPlayerAction(String actionCommand, ClientHandler player, GameState gameState, GameRules gameRules, List<ClientHandler> clients, Map<String, Integer> botPlayerTypes) {
        String playerName = player.getPlayerName();
        System.out.println("Received from " + playerName + ": " + actionCommand);
        String[] parts = actionCommand.split(":");
        if (parts.length < 1 || parts[0].trim().isEmpty()) {
            return;
        }


        String actionType = parts[0].trim().toUpperCase();

        if (gameState == null && !actionType.equals("START GAME")) {
            sendToSpecificPlayer(playerName, "The game has not started yet.", clients);
            return;
        }

        Player expectedPlayer = null;
        if (gameState != null && !actionType.equals("START GAME") && !actionType.equals("RETURN GEM")) {
            expectedPlayer = gameState.getCurrentPlayer();
            if (!playerName.equals(expectedPlayer.getName())) {
                sendToSpecificPlayer(playerName, "It is not your turn yet.", clients);
                return;
            }
        }


        boolean moveSuccessful = false;
        String message = "";
        boolean gotNoble = false;
        String messageNoble = "";

        try {
            switch (actionType) {
                case "START GAME":
                    int numOfPlayers = clients.size();
                    if (SplendorServer.gameStarted) {
                        sendToSpecificPlayer(playerName, "The game has already started!", clients);
                        return; 
                    }
                    if (clients.size() < 2) {
                        sendToSpecificPlayer(playerName, "You need at least 2 players to start!", clients);
                        return;
                    } 

                    // ensure all connected players have entered their name and birth date
                    for (ClientHandler client : clients) {
                        if (client.getPlayerName() == null || client.getPlayerName().isEmpty()
                                || client.getBirthDate() == null) {
                            sendToSpecificPlayer(playerName, "Not all players are ready! Everyone must enter their name and birth date first.", clients);
                            return;
                        }
                    }

                    // find youngest player by birthdate
                    int youngestIdx = 0;
                    LocalDate youngestBirth = clients.get(0).getBirthDate();
                    for (int i = 1; i < clients.size(); i++) {
                        LocalDate bd = clients.get(i).getBirthDate();
                        if (bd != null && (youngestBirth == null || bd.isAfter(youngestBirth))) {
                            youngestBirth = bd;
                            youngestIdx = i;
                        }
                    }
                    String youngestName = clients.get(youngestIdx).getPlayerName();

                    // reorder
                    List<ClientHandler> reorderedClients = new ArrayList<>();
                    for (int i = 0; i < clients.size(); i++) {
                        int idx = (youngestIdx + i) % clients.size();
                        reorderedClients.add(clients.get(idx));
                    }
                    clients.clear();
                    clients.addAll(reorderedClients);

                    if (!playerName.equals(youngestName)) {
                        sendToSpecificPlayer(playerName, "Only the youngest player (" + youngestName + ") can start the game.", clients);
                        sendToSpecificPlayer(playerName, "AND YOU ARE OLD", clients);
                        return;
                    }
                    DisplayUI.printStartMessage(youngestName);
                    List<Player> players = new ArrayList<>();
                    int id = 1;
                    for (ClientHandler client : clients) {
                        String cName = client.getPlayerName();
                        int botType = botPlayerTypes.getOrDefault(cName, 0);
                        if (botType == 2) {
                            players.add(new EasyBot(cName, id));
                        } else if (botType == 3) {
                            players.add(new HardBot(cName, id));
                        } else {
                            players.add(new Player(cName, id));
                        }
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

                        SplendorServer.setGameState(gameState);
                        SplendorServer.setGameRules(gameRules);

                        SplendorServer.gameStarted = true;
                        System.out.println("Game board successfully initialized!");
                        
                        broadcast("The game has started with " + numOfPlayers + " players with " + youngestName + " being the youngest player!", clients);
                        broadcastGameState(gameState, clients);
                        broadcast("It is now " + gameState.getCurrentPlayer().getName() + "'s turn.\n", clients);
                        ServerBotHandler.runBotTurns(gameState, gameRules, clients);

                    } catch (NullPointerException e) {
                        System.out.println("Client does not exist");
                        e.printStackTrace();
                        broadcast("Player missing", clients);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid Argument");
                        e.printStackTrace();
                        broadcast("Wrong input by player.", clients);
                    }
                    catch (Exception e) {
                        System.out.println("Failed to initialize game.");
                        e.printStackTrace();
                        broadcast("The game crashed", clients);
                    }
                    return;
                
                case "TAKE TWO":
                    String resultTwo = ServerInputHandler.handleTakeTwoSame(
                        expectedPlayer, gameState, gameRules, parts[1]
                    );

                    if (resultTwo.equals("SUCCESS")) {
                        moveSuccessful = true;
                        message = expectedPlayer.getName() + " took 2 " + parts[1].toUpperCase() + " gems.";
                    } else {
                        sendToSpecificPlayer(playerName, resultTwo.replace("ERROR: ", ""), clients);
                    }
                    break;
                    
                case "TAKE THREE":
                        String resultThree = ServerInputHandler.handleTakeThreeDifferent(
                        expectedPlayer, gameState, gameRules, parts[1], parts[2], parts[3]
                    );

                    if (resultThree.equals("SUCCESS")) {
                        moveSuccessful = true; 
                        message = expectedPlayer.getName() + " took 1 " + parts[1].toUpperCase() + ", 1 " + parts[2].toUpperCase() + ", and 1 " + parts[3].toUpperCase() + ".";
                    } else {
                        sendToSpecificPlayer(playerName, resultThree.replace("ERROR: ", ""), clients);
                    }
                    break;
                    
                case "PURCHASE":
                    if (parts.length < 3) {
                        sendToSpecificPlayer(playerName, "Invalid purchase command format.", clients);
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
                        sendToSpecificPlayer(playerName, "Card numbers must be integers.", clients);
                        break;
                    }

                    String resultPurchase = ServerInputHandler.handlePurchaseCard(
                        expectedPlayer, gameState, gameRules, isReserved, level, index
                    );

                    if (resultPurchase.equals("SUCCESS")) {
                        moveSuccessful = true;
                        String location = isReserved ? "their reserved hand" : "the board";
                        message = expectedPlayer.getName() + " purchased a card from " + location + ".";
                        
                        List<Noble> earnedNobles = gameRules.getClaimableNobles(expectedPlayer, gameState.getAvailableNobles());
                        for (Noble noble : earnedNobles) {
                            expectedPlayer.claimNoble(noble);
                            gameState.removeNoble(noble);
                            gotNoble = true;
                            messageNoble = expectedPlayer.getName() + " was visited by a Noble! (+" + noble.getPoints() + " points)";
                        }

                    } else {
                        sendToSpecificPlayer(playerName, resultPurchase.replace("ERROR: ", ""), clients);
                    }
                    break;
                    
                case "RESERVE":
                    if (parts.length < 3) {
                        sendToSpecificPlayer(playerName, "Invalid reserve command format.", clients);
                        break;
                    }

                    int reserveLevel;
                    int reserveIndex;
                    try {
                        reserveLevel = Integer.parseInt(parts[1]);
                        reserveIndex = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        sendToSpecificPlayer(playerName, "Level and Index must be numbers.", clients);
                        break;
                    }

                    String resultReserve = ServerInputHandler.handleReserveCard(
                        expectedPlayer, gameState, gameRules, reserveLevel, reserveIndex
                    );

                    if (resultReserve.startsWith("SUCCESS")) {
                        moveSuccessful = true;
                        
                        String type = (reserveIndex == 4) ? "the top of the deck" : "the board";
                        message = expectedPlayer.getName() + " reserved a Level " + reserveLevel + " card from " + type + ".";
                        
                        if (resultReserve.equals("SUCCESS_JOKER")) {
                            message += " They also received 1 Gold Joker.";
                        }
                    } else {
                        sendToSpecificPlayer(playerName, resultReserve.replace("ERROR: ", ""), clients);
                    }
                    break;

                case "RETURN GEM":
                    // Get the current player who needs to return gems
                    expectedPlayer = gameState.getCurrentPlayer();
                    String colorStr = parts[1].trim().toUpperCase();
                    GemColor gemColor = GemColor.convertToColor(colorStr);

                    if (expectedPlayer.getSpecificGem(gemColor) < 1) {
                        sendToSpecificPlayer(playerName, "You don't have any " + colorStr + " gems to return.", clients);
                        sendToSpecificPlayer(playerName, "PROMPT_RETURN_GEM", clients);
                        return;
                    }

                    GemCollection gemsToReturn = new GemCollection();
                    gemsToReturn.add(gemColor, 1);
                    expectedPlayer.deductGems(gemsToReturn);
                    gameState.addGemsToBank(gemsToReturn);
                    broadcast(playerName + " returned 1 " + colorStr + " gem.", clients);

                    // Check if they still need to return more
                    if (gameRules.mustReturnGems(expectedPlayer)) {
                        broadcastGameState(gameState, clients);
                        sendToSpecificPlayer(playerName, "PROMPT_RETURN_GEM", clients);
                        return;
                    }
                    
                    // Done returning — check win condition and advance turn
                    if (expectedPlayer.getPoints() >= gameState.getWinningThreshold() && !SplendorServer.isLastRound) {
                        SplendorServer.isLastRound = true;
                        broadcast(expectedPlayer.getName() + " has reached " + gameState.getWinningThreshold() + " points!", clients);
                    }

                    List<Player> allPlayersReturn = gameState.getPlayers();
                    boolean isLastPlayerReturn = allPlayersReturn.indexOf(expectedPlayer) == (allPlayersReturn.size() - 1);
                    if (SplendorServer.isLastRound && isLastPlayerReturn) {
                        String winnerScreen = DisplayUI.getWinner(gameState, gameRules).replace("\n", "@@");
                        broadcast(winnerScreen, clients);
                        broadcast("The game has ended! Thanks for playing.", clients);
                        System.exit(0);
                        return;
                    }

                    gameState.advanceToNext();
                    broadcastGameState(gameState, clients);
                    broadcast("It is now " + gameState.getCurrentPlayer().getName() + "'s turn.", clients);
                    ServerBotHandler.runBotTurns(gameState, gameRules, clients);
                    return;
                    
                default:
                    sendToSpecificPlayer(playerName, "Unknown command.", clients);
                    break;
            }
    
            message += '\n';
            if (moveSuccessful) {
                if (gameRules.mustReturnGems(expectedPlayer)) {
                    broadcastGameState(gameState, clients);
                    broadcast(message, clients);
                    // Send the prompt signal to this player
                    sendToSpecificPlayer(playerName, "PROMPT_RETURN_GEM", clients);
                    return;
                }
                // check if they hit the winning threshold and if so, this will be the last round
                if (expectedPlayer.getPoints() >= gameState.getWinningThreshold() && !SplendorServer.isLastRound) { 
                    SplendorServer.isLastRound = true;
                    broadcastGameState(gameState, clients);
                    broadcast(message, clients);
                    if (gotNoble) {
                        broadcast(messageNoble, clients);
                    }
                    broadcast(expectedPlayer.getName() + " has reached " + gameState.getWinningThreshold() + " points!", clients);
                }
                
                // check if is last player
                List<Player> allPlayers = gameState.getPlayers();
                boolean isLastPlayer = allPlayers.indexOf(expectedPlayer) == (allPlayers.size() - 1);
                
                // end game if both true
                if (SplendorServer.isLastRound && isLastPlayer) {
                    String winnerScreen = DisplayUI.getWinner(gameState, gameRules).replace("\n", "@@");
                    broadcast(message, clients);
                    if (gotNoble) {
                        broadcast(messageNoble, clients);
                    }
                    broadcast(winnerScreen, clients);
                    broadcast("The game has ended! Thanks for playing.", clients);
                    System.exit(0);
                    return;
                }
                gameState.advanceToNext();
                broadcastGameState(gameState, clients);
                broadcast(message, clients);
                    if (gotNoble) {
                        broadcast(messageNoble, clients);
                    }
                broadcast("It is now " + gameState.getCurrentPlayer().getName() + "'s turn.", clients);
                // auto execute bot's turns
                ServerBotHandler.runBotTurns(gameState, gameRules, clients);
            } else {
                sendToSpecificPlayer(playerName, "Illegal move. Try again.", clients);
            }

        } catch (Exception e) {
            System.out.println("CRASH DURING ACTION: " + actionType);
            e.printStackTrace(); 
            sendToSpecificPlayer(playerName, "Invalid Input", clients);
        }
    }
    


}
