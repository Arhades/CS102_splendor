package splendor.network.client;

import java.io.*;

public class ServerListener extends Thread {
    
    private final BufferedReader in;

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
                    String gameBoard = serverMessage.replace("BOARD_STATE:", "");
                    
                    // swap back the @@ to \n
                    String board = gameBoard.replace("@@", "\n");
                    
                    System.out.println(board);
                    SplendorClient.waitingForServer = false;
                } else {
                    System.out.println("\n[SERVER]: " + serverMessage);
                    System.out.print("> ");
                }

                SplendorClient.waitingForServer = false;
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }
    
}