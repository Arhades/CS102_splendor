package splendor.network.client;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class SplendorClient {
    // use localhost for testing on one machine
    // on the second computer, use the actual server IP
    private static final String SERVER_IP = "localhost"; 
    private static final int SERVER_PORT = 9090;
    protected static volatile boolean gameStarted = false;
    protected static volatile boolean waitingForServer = false;

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
                    if (clientInput == null) {
                        // for if the client disconnects
                        break;
                    }
                    try {
                        playerBirthDate = LocalDate.parse(clientInput, formatter);
                        validDateEntered = true;
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid input.");  
                    }

                }
                out.println("JOINED:" + playerName + ":" + playerBirthDate);
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
                    System.out.println("\n=============== YOUR TURN ===============");
                    System.out.println("1. Take 3 different gems");
                    System.out.println("2. Take 2 gems of the same color");
                    System.out.println("3. Purchase a card");
                    System.out.println("4. Reserve a card");
                    System.out.println("Type a number (1-4) or QUIT:");
                    System.out.print("> ");
                    
                    String input = scanner.nextLine();
                    
                    if (input.equalsIgnoreCase("QUIT")) {
                        out.println("QUIT");
                        break;
                    }

                    switch (input) {
                        case "1":
                            System.out.print("Enter first color: ");
                            String color1 = scanner.nextLine().toUpperCase();
                            
                            System.out.print("Enter second color: ");
                            String color2 = scanner.nextLine().toUpperCase();
                            
                            System.out.print("Enter third color: ");
                            String color3 = scanner.nextLine().toUpperCase();
                            
                            waitingForServer = true;
                            out.println("TAKE THREE:" + color1 + ":" + color2 + ":" + color3);
                            break;
                            
                        case "2":
                            System.out.print("Enter the color you want 2 of: ");
                            String color = scanner.nextLine().toUpperCase();
                            waitingForServer = true;
                            out.println("TAKE TWO:" + color);
                            break;
                            
                        case "3":
                            System.out.println("From Reserved or Board? : ");
                            String choice = scanner.nextLine();
                            if (choice.equalsIgnoreCase("reserved")) {
                                System.out.println("Which card? (1 for the first card and so on):");
                                String res = scanner.nextLine();
                                waitingForServer = true;
                                out.println("PURCHASE:RESERVED:" + res);
                            } else {
                                System.out.print("Which deck level (1-3)? ");
                                String level = scanner.nextLine();
                                System.out.print("Which card index (0-3)? ");
                                String index = scanner.nextLine();
                                waitingForServer = true;
                                out.println("PURCHASE:" + level + ":" + index);
                            }
                            break;
                            
                        case "4":
                            System.out.print("Which deck level to reserve from (1-3)? ");
                            String resLevel = scanner.nextLine();
                            System.out.print("Which card index (0-3)? ");
                            String resIndex = scanner.nextLine();
                            waitingForServer = true;
                            out.println("RESERVE:" + resLevel + ":" + resIndex);
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

    // public static void renderBoard(String rawState) {
    //     String cleanState = rawState.replace("BOARD_STATE:", "");
    //     String[] sections = cleanState.split("\\|");

    //     System.out.println("\n========================================");
    //     System.out.println("           SPLENDOR GAME BOARD          ");
    //     System.out.println("========================================");

    //     for (String section : sections) {
            
    //         if (section.startsWith("BANK=")) {
    //             System.out.println("\n--- GEM BANK ---");
    //             String bankData = section.replace("BANK=", "").replace(",", " | ");
    //             System.out.println(bankData);
    //         } 
            
    //         else if (section.startsWith("MARKET=")) {
    //             System.out.println("\n--- CARD MARKET ---");
    //             String marketData = section.replace("MARKET=", "");
    //             String[] levels = marketData.split(";");
    //             for (String level : levels) {
    //                 System.out.println(level.replace(",", "   "));
    //             }
    //         } 
            
    //         else if (section.startsWith("PLAYER=")) {
    //             System.out.println("\n--- PLAYER STATS ---");
    //             String[] playerParts = section.replace("PLAYER=", "").split(",", 2);
                
    //             String playerName = playerParts[0];
    //             String stats = playerParts.length > 1 ? playerParts[1] : "No stats";
                
    //             System.out.println(playerName.toUpperCase() + ":");
    //             stats = stats.replace(",TOKENS:", "\n  Tokens: ")
    //                          .replace(",BONUSES:", "\n  Bonuses: ")
    //                          .replace(",RESERVED:", "\n  Reserved: ");
    //             System.out.println("  " + stats);
    //         }
    //     }
        
    //     System.out.println("========================================");
    //     System.out.print("Your move > ");
    // }
}