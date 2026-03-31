package splendor.network.client;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.display.*;

public class SplendorClient {
    // use localhost for testing on one machine
    // on the second computer, use the actual server IP
    private static final String SERVER_IP = "localhost"; 
    private static final int SERVER_PORT = 9090;
    private DisplayUI displayUI = new DisplayUI();
    protected static volatile boolean gameStarted = false;
    protected static volatile boolean waitingForServer = false;

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
            String playerName = null;
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
                    System.out.println("Type 'START GAME' if you are the youngest, or wait for the game to start. (I'll know if you are not the youngest...)");
                    System.out.print("> ");
                    String startGame = scanner.nextLine();
                    if (startGame.equalsIgnoreCase("START GAME")) {
                        out.println("START GAME");
                    }
                }
                else {
                    // System.out.println("\n=============== YOUR TURN ===============");
                    // System.out.println("1. Take 3 different gems");
                    // System.out.println("2. Take 2 gems of the same color");
                    // System.out.println("3. Purchase a card");
                    // System.out.println("4. Reserve a card");
                    // System.out.println("Type a number (1-4) or QUIT:");
                    // System.out.print("> ");
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
                    while (waitingForServer) {
                        try {
                            Thread.sleep(50);
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
            }

            if (takenColors.contains(color)) {
                System.out.println("Already taken! You must choose 3 DIFFERENT colors.");
                continue;
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
}
