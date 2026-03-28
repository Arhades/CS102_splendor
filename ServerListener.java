import java.io.*;

public class ServerListener extends Thread {
    
    private BufferedReader in;

    // Constructor so the Thread knows where to read the messages from
    public ServerListener(BufferedReader in) {
        this.in = in;
    }

    // This is the specialized instructions for this specific worker
    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.startsWith("BOARD_STATE:")) {
                    // Note: renderBoard would need to be accessible here!
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