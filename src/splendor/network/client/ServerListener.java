package splendor.network.client;

import java.io.*;

public class ServerListener extends Thread {
    
    private final BufferedReader in;

    /**
     * Constructs a ServerListener that reads messages from the server.
     *
     * @param in the BufferedReader connected to the server's output stream
     */
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
                // swap back the @@ to \n
                String board = serverMessage.replace("@@", "\n"); 
                System.out.println(board);
            //  System.out.println("Press ENTER for the option menu.");
                // if (serverMessage.contains("It is now " + SplendorClient.playerName + "'s turn.")) {
                //     SplendorClient.myTurn = true;
                // }
                // // still my turn
                // if (serverMessage.contains("Invalid") || serverMessage.contains("ERROR") || serverMessage.contains("Not enough") || serverMessage.contains("You must")) {
                //     SplendorClient.myTurn = true;
                // }

                SplendorClient.waitingForServer = false;
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }
    
}
