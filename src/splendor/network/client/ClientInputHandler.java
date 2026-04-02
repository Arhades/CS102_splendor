package splendor.network.client;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.display.*;

public class ClientInputHandler {
    /**
     * Prompts the player to choose three different gem colors to take.
     *
     * @param scanner the Scanner to read input from
     * @return a list of three color strings, or null if the player backs out
     */
    public static List<String> promptTakeThreeDifferent(Scanner scanner) {
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
    public static String promptTakeTwoSame(Scanner scanner) {
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
    public static String promptPurchase(Scanner scanner) {
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
    public static String promptReserve(Scanner scanner) {
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
    public static void promptAddBot(Scanner scanner, PrintWriter out) {
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

        String defaultName = (botType == 2) ? "EasyBot" : "HardBot";
        System.out.print("Bot name (blank = \"" + defaultName + "\"): ");
        String botName = scanner.nextLine().trim();
        if (botName.isEmpty()) {
            botName = defaultName;
        }
        String botBirthDate = "01/01/1900";
        out.println("JOINED:" + botName + ":" + botBirthDate + ":BOT:" + botType);
        System.out.println(botName + " (" + defaultName + ") has been added to the game!");
    }
}