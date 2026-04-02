package splendor.network.server;

import java.io.*;
import java.net.*;
import java.util.*;
import splendor.rules.*;

/**
* Supports both human clients and server-side bots.
*/
public class SplendorServer {
    /**
     * The IP address of the game server.
     */
    private static final int PORT = 9091;

    /**
     * The list of all connected clients.
     */
    private static final List<ClientHandler> clients = new ArrayList<>();
    
    /** 
     * Maps client player names to their bot type: 
     * 0=human, 2=EasyBot, 3=HardBot 
     */
    private static final Map<String, Integer> botPlayerTypes = new HashMap<>();

    /**
     * The current game state.
     */
    private static GameState gameState;

    /**
     * The game rules used for validating actions.
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
     * Default constructor
     */
    public SplendorServer() {}

    /**
     * Entry point for the Splendor server application.
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
                
                ServerHelper.broadcast("Player " + newPlayerId + " has joined the lobby. (" + clients.size() + "/4)", clients);
            }
            
            System.out.println("The game has started! The server is no longer accepting new connections.");

        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }
    public static GameState getGameState() { 
        return gameState; 
        }
    public static GameRules getGameRules() { 
        return gameRules; 
        }
    public static List<ClientHandler> getClients() { 
        return clients; 
        }
    public static Map<String, Integer> getBotPlayerTypes() { 
        return botPlayerTypes; 
        }
    public static void setGameState(GameState state) { 
        gameState = state; 
        }
    public static void setGameRules(GameRules rules) { 
        gameRules = rules; 
        }
    
}
