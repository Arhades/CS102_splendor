package splendor.network.client;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.display.*;

/**
 * Listens for messages from the server and processes them on the client side.
 */
public class SplendorClient {
    // use localhost for testing on one machine
    // on the second computer, use the actual server IP

    /**
     * The port number used to connect to the game server.
     */
    private static final String SERVER_IP = "172.20.10.7"; 

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
    protected static volatile boolean myTurn = false;
    static String playerName = "";

    /**
     * Default constructor
     */
    public SplendorClient() {}

    /**
     * Entry point for the Splendor client application.
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
            // first request for input is for player's name
            // second request is for host player to START GAME
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
                    System.out.println("Type 'START GAME' if you are the youngest, or wait for the game to start.");
                    System.out.print("> ");
                    String startGame = scanner.nextLine();
                    if (startGame.equalsIgnoreCase("ADD BOT")) {
                        promptAddBot(scanner, out);
                    } else if (startGame.equalsIgnoreCase("START GAME")) {
                        out.println("START GAME");
                    }
                }
                else {
                    // if (!myTurn) {
                    //     try {
                    //         Thread.sleep(100);
                    //     } catch (InterruptedException e) {}
                    //     continue;
                    // }
                    DisplayUI.printActionMenu();
                    String input = scanner.nextLine();
                    
                    if (input.equalsIgnoreCase("QUIT")) {
                        out.println("QUIT");
                        break;
                    }

                    switch (input) {
                        case "1":
                            List<String> chosenColors = promptTakeThreeDifferent(scanner);
                            if (chosenColors == null) {
                                break; 
                            }
                            waitingForServer = true;
                            out.println("TAKE THREE:" + chosenColors.get(0) + ":" + chosenColors.get(1) + ":" + chosenColors.get(2));
                            break;
                            
                        case "2":
                            String chosenColor = promptTakeTwoSame(scanner);
                            if (chosenColor == null) {
                                break; 
                            }
                            waitingForServer = true;
                            out.println("TAKE TWO:" + chosenColor);
                            break;
                            
                        case "3":
                            String purchaseData = promptPurchase(scanner);
                            if (purchaseData == null) {
                                break;
                            }
                            waitingForServer = true;
                            out.println("PURCHASE:" + purchaseData);
                            break;
                            
                        case "4":
                            String reserveData = promptReserve(scanner);
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
                    // myTurn = false;
                    while (waitingForServer) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("The server was interrupted.");
                        }
                    }
                }
            }

            socket.close();
            scanner.close();

        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    /**
     * Prompts the player to choose three different gem colors to take.
     *
     * @param scanner the Scanner to read input from
     * @return a list of three color strings, or null if the player backs out
     */
    private static List<String> promptTakeThreeDifferent(Scanner scanner) {
        List<String> takenColors = new ArrayList<>();
        List<String> validColors = Arrays.asList("DIAMOND", "SAPPHIRE", "EMERALD", "RUBY", "ONYX");

        while (takenColors.size() < 3) {
            System.out.print("Colour to take (Diamond, Sapphire, Emerald, Ruby, Onyx) [\"back\" to return]: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            String color = input.toUpperCase();

            if (!validColors.contains(color)) {
                System.out.println("Invalid input! Please enter a valid gem color.");
                continue;
            } else if (takenColors.contains(color)) {
                System.out.println("Already taken! You must choose 3 DIFFERENT colors.");
                continue;
            } else {
                System.out.println("Valid option");
            }

            takenColors.add(color);
        }
        
        return takenColors;
    }

    /**
     * Prompts the player to choose a gem color to take two of.
     *
     * @param scanner the Scanner to read input from
     * @return the chosen color string, or null if the player backs out
     */
    private static String promptTakeTwoSame(Scanner scanner) {
        List<String> validColors = Arrays.asList("DIAMOND", "SAPPHIRE", "EMERALD", "RUBY", "ONYX");

        while (true) {
            System.out.print("Colour to take 2 of (Diamond, Sapphire, Emerald, Ruby, Onyx) [\"back\" to return]: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            String color = input.toUpperCase();

            if (!validColors.contains(color)) {
                System.out.println("Invalid input! Please enter a valid gem color.");
                continue;
            }
            return color; 
        }
    }

    /**
     * Prompts the player to select a card to purchase from the table or reserved hand.
     *
     * @param scanner the Scanner to read input from
     * @return a formatted purchase data string, or null if the player backs out
     */
    private static String promptPurchase(Scanner scanner) {
        while (true) {
            System.out.print("From table or from reserved? [\"back\" to return]: ");
            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("back")) {
                return null;
            }

            if (choice.equalsIgnoreCase("reserved")) {
                System.out.print("Which reserved card number (0, 1, or 2)? [\"back\" to return]: ");
                String res = scanner.nextLine().trim();
                if (res.equalsIgnoreCase("back")) return null;
                
                return "RESERVED:" + res; 

            } else if (choice.equalsIgnoreCase("table") || choice.equalsIgnoreCase("board")) {
                System.out.print("Which deck level (1-3)? [\"back\" to return]: ");
                String levelStr = scanner.nextLine().trim();
                if (levelStr.equalsIgnoreCase("back")) return null;

                System.out.print("Which card index (0-3)? [\"back\" to return]: ");
                String indexStr = scanner.nextLine().trim();
                if (indexStr.equalsIgnoreCase("back")) return null;

                return levelStr + ":" + indexStr;
            } else {
                System.out.println("Invalid choice. Please type 'table' or 'reserved'.");
            }
        }
    }


    /**
     * Prompts the player to select a card to reserve from the table.
     *
     * @param scanner the Scanner to read input from
     * @return a formatted reserve data string, or null if the player backs out
     */
    private static String promptReserve(Scanner scanner) {
        while (true) {
            System.out.print("Which deck level to reserve from (1-3)? [\"back\" to return]: ");
            String levelStr = scanner.nextLine().trim();
            if (levelStr.equalsIgnoreCase("back")) return null;

            System.out.print("Which card index (0-3, or 4 for random draw)? [\"back\" to return]: ");
            String indexStr = scanner.nextLine().trim();
            if (indexStr.equalsIgnoreCase("back")) return null;

            return levelStr + ":" + indexStr;
        }
    }

    /**
     * Prompts the player to configure and add a bot to the game.
     * Sends a JOINED message to the server on behalf of the bot with the BOT type flag.
     *
     * @param scanner the Scanner to read input from
     * @param out     the PrintWriter to send messages to the server
     */
    private static void promptAddBot(Scanner scanner, PrintWriter out) {
        // Choose bot type
        int botType = 0;
        while (botType != 2 && botType != 3) {
            System.out.print("Bot type (2 = EasyBot, 3 = HardBot): ");
            String typeStr = scanner.nextLine().trim();
            try {
                botType = Integer.parseInt(typeStr);
                if (botType != 2 && botType != 3) {
                    System.out.println("Please enter 2 or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter 2 or 3.");
            }
        }

        // Choose bot name
        String defaultName = (botType == 2) ? "EasyBot" : "HardBot";
        System.out.print("Bot name (blank = \"" + defaultName + "\"): ");
        String botName = scanner.nextLine().trim();
        if (botName.isEmpty()) {
            botName = defaultName;
        }

        // Bots get a very old birth date so they are never the youngest
        String botBirthDate = "01/01/1900";

        // Send the bot's JOINED message with the BOT type flag
        out.println("JOINED:" + botName + ":" + botBirthDate + ":BOT:" + botType);
        System.out.println(botName + " (" + defaultName + ") has been added to the game!");
    }
}
