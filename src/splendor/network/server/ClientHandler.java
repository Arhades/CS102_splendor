package splendor.network.server;

import java.io.*;
import java.net.*;
import java.time.*;

/**
 * Handles communication between the server and a single client.
 * 
 * This class manages the socket connection, receives messages
 * from the client, and sends messages back to the client through
 * the server. Each client connection runs on its own thread.
 */
public class ClientHandler implements Runnable {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private LocalDate playerBirthDate;
    
    /**
     * Constructs a ClientHandler for the given client socket.
     * Pass null for bot players that have no real socket connection.
     *
     * @param socket the client's socket connection, or null for bot players
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
        if (socket != null) {
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Error setting up client handler: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        try {
            String temp =  in.readLine();
            String[] parts = temp.split(":");
            playerName = parts[1];
            System.out.println(playerName + " has joined the server!");
            
            ServerHelper.broadcast("[SERVER]: " + playerName + " joined the lobby.", SplendorServer.getClients());
            ServerHelper.processClientMessage(this, temp, SplendorServer.getGameState(), SplendorServer.getGameRules(), SplendorServer.getClients(), SplendorServer.getBotPlayerTypes());

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("[" + playerName + "]: " + clientMessage);
                
                if (clientMessage.equalsIgnoreCase("QUIT")) {
                    break;
                }
                
                ServerHelper.processClientMessage(this, clientMessage, SplendorServer.getGameState(), SplendorServer.getGameRules(), SplendorServer.getClients(), SplendorServer.getBotPlayerTypes());
            }
            
        } catch (IOException e) {
            System.out.println(playerName + " suddenly disconnected.");
        } finally {
            disconnect();
        }
    }

    /**
     * Sets the player's display name.
     *
     * @param name the player name
     */
    public void setPlayerName(String name) {
        playerName = name;
    }

    /**
     * Returns the player's display name.
     *
     * @return the player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the player's birth date.
     *
     * @param date the player's birth date
     */
    public void setBirthDate(LocalDate date) {
        playerBirthDate = date;
    }

    /**
     * Returns the player's birth date.
     *
     * @return the player's birth date
     */
    public LocalDate getBirthDate() {
        return playerBirthDate;
    }

    /**
     * Sends a message to this client.
     *
     * @param message the message string to send
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void disconnect() {
        try {
            ServerHelper.removeClient(this, SplendorServer.getClients());
            ServerHelper.broadcast("SERVER: " + playerName + " has left the game.", SplendorServer.getClients());
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
