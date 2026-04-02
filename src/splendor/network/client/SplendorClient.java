package splendor.network.client;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.display.*;

/**
 * The Splendor game client that connects to a server over sockets.
 * Handles player input, sends commands to the server, and displays
 * server responses. Supports chat messages at any time by typing
 * "CHAT: message" during the game.
 */
public class SplendorClient {
    // use localhost for testing on one machine
    // on the second computer, use the actual server IP

    /**
     * The IP address of the game server.
     */
    private static final String SERVER_IP = "localhost"; 

    /**
     * The port number used to connect to the game server.
     */
    private static final int SERVER_PORT = 9091;

    /**
     * The display UI used to print game information.
     */
    private DisplayUI displayUI = new DisplayUI();

    /**
     * Indicates whether the game has started.
     */
    protected static volatile boolean gameStarted = false;

    /**
     * Indicates whether the client is waiting for a server response.
     */
    protected static volatile boolean waitingForServer = false;

    /**
     * Indicates whether the player needs to return a gem (over 10 limit).
     */
    protected static volatile boolean needsToReturnGem = false;

    /**
     * The local player's name.
     */
    static String playerName = "";

    /**
     * Default constructor.
     */
    public SplendorClient() {}

    /**
     * Entry point for the Splendor client application.
     * Connects to the server, prompts for name and birth date, then enters
     * the lobby/game loop. Players can type "CHAT: message" at any input
     * prompt to send a chat message to all players.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Connected to the Splendor Server!");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            ServerListener listenerThread = new ServerListener(in);
            listenerThread.start();
            LocalDate playerBirthDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            System.out.println("ENTER YOUR INFO");
            System.out.println("Enter your player name: ");
            playerName = scanner.nextLine();
            while (true) {
                boolean validDateEntered = false;
                while (!validDateEntered) {
                    System.out.println("Please enter your birth date in the formal dd/mm/yyyy: ");
                    String clientInput = scanner.nextLine();
                    try {
                        playerBirthDate = LocalDate.parse(clientInput, formatter);
                        validDateEntered = true;
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid input.");  
                    }

                }
                out.println("JOINED:" + playerName + ":" + playerBirthDate.format(formatter));
                break;
            }

  
            while (true) {
                if (!gameStarted) {
                    System.out.println("\n=============== WAITING LOBBY ===============");
                    System.out.println("Type 'ADD BOT' to add a bot to the game.");
                    System.out.println("Type 'CHAT: <message>' to send a chat message.");
                    System.out.println("Type 'START GAME' if you are the youngest, or wait for the game to start.");
                    System.out.print("> ");
                    String startGame = scanner.nextLine();
                    if (startGame.toUpperCase().startsWith("CHAT:")) {
                        String chatMsg = startGame.substring(5).trim();
                        out.println("CHAT:" + chatMsg);
                    } else if (startGame.equalsIgnoreCase("ADD BOT")) {
                        ClientInputHandler.promptAddBot(scanner, out);
                    } else if (startGame.equalsIgnoreCase("START GAME")) {
                        out.println("START GAME");
                    }
                }
                else {
                    DisplayUI.printActionMenu();
                    System.out.println("  (Type 'CHAT: <message>' to chat anytime)");
                    String input = scanner.nextLine();

                    // Allow chat at any time during the game
                    if (input.toUpperCase().startsWith("CHAT:")) {
                        String chatMsg = input.substring(5).trim();
                        out.println("CHAT:" + chatMsg);
                        continue;
                    }
                    
                    if (input.equalsIgnoreCase("QUIT")) {
                        out.println("QUIT");
                        break;
                    }

                    switch (input) {
                        case "1":
                            List<String> chosenColors = ClientInputHandler.promptTakeThreeDifferent(scanner);
                            if (chosenColors == null) {
                                break; 
                            }
                            waitingForServer = true;
                            out.println("TAKE THREE:" + chosenColors.get(0) + ":" + chosenColors.get(1) + ":" + chosenColors.get(2));
                            break;
                            
                        case "2":
                            String chosenColor = ClientInputHandler.promptTakeTwoSame(scanner);
                            if (chosenColor == null) {
                                break; 
                            }
                            waitingForServer = true;
                            out.println("TAKE TWO:" + chosenColor);
                            break;
                            
                        case "3":
                            String purchaseData = ClientInputHandler.promptPurchase(scanner);
                            if (purchaseData == null) {
                                break;
                            }
                            waitingForServer = true;
                            out.println("PURCHASE:" + purchaseData);
                            break;
                            
                        case "4":
                            String reserveData = ClientInputHandler.promptReserve(scanner);
                            if (reserveData == null) {
                                break;
                            }
                            waitingForServer = true;
                            out.println("RESERVE:" + reserveData);
                            break;
                            
                        default:
                            System.out.println("Invalid choice. Please type 1, 2, 3, or 4.");
                            break;
                    }

                    while (waitingForServer) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("The server was interrupted.");
                        }
                    }

                    if (needsToReturnGem) {
                        System.out.println("You have exceeded the 10 gem limit!");
                        System.out.print("Enter a gem color to return: ");
                        
                        if (!scanner.hasNextLine()) {
                            break;
                        }
                        String colorToReturn = scanner.nextLine();
                        
                        waitingForServer = true;
                        needsToReturnGem = false;
                        out.println("RETURN GEM:" + colorToReturn);
                        continue;
                    }
                }
            }
            socket.close();
            scanner.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    } 
}
