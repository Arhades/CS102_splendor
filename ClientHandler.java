import java.io.*;
import java.net.*;
import java.time.*;

public class ClientHandler implements Runnable {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private LocalDate playerBirthDate;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error setting up client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String temp =  in.readLine();
            String[] parts = temp.split(":");
            playerName = parts[1];
            System.out.println(playerName + " has joined the server!");
            
            SplendorServer.broadcast("SERVER: " + playerName + " joined the lobby.");

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("[" + playerName + "]: " + clientMessage);
                
                if (clientMessage.equalsIgnoreCase("QUIT")) {
                    break;
                }
                
                // We pass 'this' so the server knows exactly sent the command
                SplendorServer.processClientMessage(this, clientMessage);
            }
            
        } catch (IOException e) {
            System.out.println(playerName + " suddenly disconnected.");
        } finally {
            disconnect();
        }
    }

    public void setPlayerName(String name) {
        playerName = name;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setBirthDate(LocalDate date) {
        playerBirthDate = date;
    }

    public LocalDate getBirthDate() {
        return playerBirthDate;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void disconnect() {
        try {
            SplendorServer.removeClient(this);
            SplendorServer.broadcast("SERVER: " + playerName + " has left the game.");
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}