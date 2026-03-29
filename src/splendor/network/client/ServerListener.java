package splendor.network.client;

import java.io.*;

public class ServerListener extends Thread {
    
    private BufferedReader in;

    public ServerListener(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.contains("The game has started") || serverMessage.contains("GAME STARTED")) {
                    SplendorClient.gameStarted = true; 
                }
                if (serverMessage.startsWith("BOARD_STATE:")) {
                    SplendorClient.renderBoard(serverMessage); 
                } else {
                    System.out.println("\n[SERVER]: " + serverMessage);
                    System.out.print("> ");
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }
    
}