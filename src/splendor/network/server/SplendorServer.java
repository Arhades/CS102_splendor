package splendor.network.server;

import java.io.*;
import java.net.*;
import java.util.*;
import splendor.rules.*;

/**
 * The main Splendor game server. Listens for client connections on the configured port,
 * manages the lobby, and holds the shared game state. Supports both human clients
 * and server-side bots. Chat messages are routed through ServerHelper.
 */
public class SplendorServer {

    /**
     * The port number the server listens on.
     */
    private static final int PORT = 9091;

    /**
     * The list of all connected clients (including virtual bot handlers).
     */
    private static final List<ClientHandler> clients = new ArrayList<>();
    
    /** 
     * Maps client player names to their bot type: 
     * 0=human, 2=EasyBot, 3=HardBot.
     */
    private static final Map<String, Integer> botPlayerTypes = new HashMap<>();

    /**
     * The current game state. Null before the game starts.
     */
    private static GameState gameState;

    /**
     * The game rules used for validating actions. Null before the game starts.
     */
    private static GameRules gameRules;

    /**
     * Indicates whether the game has started.
     */
    public static volatile boolean gameStarted = false;

    /**
     * Indicates whether the game is in the final round.
     */
    public static volatile boolean isLastRound = false;

    /**
     * Default constructor.
     */
    public SplendorServer() {}

    /**
     * Entry point for the Splendor server application.
     * Listens for connections, adds clients to the lobby, and blocks
     * until the game is started by the youngest player.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Splendor Server is starting...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Server is listening on port " + PORT);
            while (!gameStarted) {
                if (clients.size() >= 4) {
                    System.out.println("Lobby is full. Waiting for the host to start game");
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
                
                ServerHelper.broadcast("Player " + newPlayerId + " has joined the lobby. (" + clients.size() + "/4)", clients);
            }
            
            System.out.println("The game has started! The server is no longer accepting new connections.");

        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }

    /**
     * Returns the current game state.
     *
     * @return the game state, or null if the game hasn't started
     */
    public static GameState getGameState() { 
        return gameState; 
    }

    /**
     * Returns the current game rules.
     *
     * @return the game rules, or null if the game hasn't started
     */
    public static GameRules getGameRules() { 
        return gameRules; 
    }

    /**
     * Returns the list of all connected client handlers.
     *
     * @return the clients list
     */
    public static List<ClientHandler> getClients() { 
        return clients; 
    }

    /**
     * Returns the map of player names to bot types.
     *
     * @return the bot player types map
     */
    public static Map<String, Integer> getBotPlayerTypes() { 
        return botPlayerTypes; 
    }

    /**
     * Sets the game state. Called when the game is initialized.
     *
     * @param state the new game state
     */
    public static void setGameState(GameState state) { 
        gameState = state; 
    }

    /**
     * Sets the game rules. Called when the game is initialized.
     *
     * @param rules the new game rules
     */
    public static void setGameRules(GameRules rules) { 
        gameRules = rules; 
    }
    
}
