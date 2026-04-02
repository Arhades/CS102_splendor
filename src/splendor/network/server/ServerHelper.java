package splendor.network.server;

import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.config.*;
import splendor.display.*;
import splendor.entity.bot.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.rules.*;
import splendor.valueobjects.*;

public class ServerHelper {
    public ServerHelper() {}
    /**
     * Sends a message to a specific player by name.
     *
     * @param targetPlayerName the name of the target player
     * @param message          the message to send
     */
    public static synchronized void sendToSpecificPlayer(String targetPlayerName, String message, List<ClientHandler> clients) {
        for (ClientHandler client : clients) {
            if (client.getPlayerName().equals(targetPlayerName)) {
                client.sendMessage(message);
                break;
            }
        }
    }

    /**
     * Broadcasts the full game board state to all connected clients.
     */
    public static void broadcastGameState(GameState gameState, List<ClientHandler> clients) {
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
     *
     * @param client the ClientHandler to remove
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
     * Processes a raw message from a client, handling JOINED messages and routing actions.
     * JOINED format: "JOINED:name:dd/MM/yyyy" for humans, or "JOINED:name:dd/MM/yyyy:BOT:2" or "JOINED:name:dd/MM/yyyy:BOT:3" for bots.
     * Bot JOINED messages are sent by a human client on behalf of the bot, so the server
     * creates a separate virtual ClientHandler for the bot and inserts it right after the
     * sender to preserve clockwise seating order.
     *
     * @param sender  the ClientHandler that sent the message
     * @param message the raw message string
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

                clients.add(botHandler);

                broadcast("[SERVER]: " + botName + " (Bot) joined the lobby. (" + clients.size() + "/4)", clients);
                System.out.println(botName + " (Bot) successfully joined");
            } else {
                sender.setPlayerName(parts[1]);
                sender.setBirthDate(date);
                botPlayerTypes.put(parts[1], 0);
                System.out.println(parts[1] + " successfully joined");
            }
        } else {
            processPlayerAction(message, sender, gameState, gameRules, clients, botPlayerTypes);
        }
    }
    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message the message to broadcast
     */
    public static synchronized void broadcast(String message, List<ClientHandler> clients) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    /**
     * Processes a player action command received from a client.
     *
     * @param actionCommand the raw action string from the client
     * @param player        the ClientHandler that sent the command
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
        if (gameState != null && !actionType.equals("START GAME")) {
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
                        gameRules = new GameRules(SplendorServer.getGameState());

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
                    String colorStr = parts[1].toUpperCase();
                    GemCollection gemsToReturn = new GemCollection();
                    expectedPlayer.deductGems(gemsToReturn);
                    gameState.addGemsToBank(gemsToReturn);
                    if (gameRules.mustReturnGems(expectedPlayer)) {
                        ServerHelper.broadcastGameState(gameState, clients);
                        ServerHelper.sendToSpecificPlayer(playerName, "PROMPT RETURN GEM", clients);
                        return;
                    }
                    
                    gameState.advanceToNext();
                    ServerHelper.broadcastGameState(gameState, clients);
                    ServerHelper.broadcast("It is now " + gameState.getCurrentPlayer().getName() + "'s turn.", clients);
                    ServerBotHandler.runBotTurns(gameState, gameRules, clients);
                    break;
                    
                default:
                    sendToSpecificPlayer(playerName, "Unknown command.", clients);
                    break;
            }
    
            message += '\n';
            if (moveSuccessful) {
                if (gameRules.mustReturnGems(expectedPlayer)) {
                    ServerHelper.broadcastGameState(gameState, clients);
                    ServerHelper.broadcast(message, clients);
                    // Send a secret code just to this player
                    ServerHelper.sendToSpecificPlayer(playerName, "PROMPT_RETURN_GEM", clients);
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