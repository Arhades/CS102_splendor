import java.util.*;
import java.io.*;

public class GameEngine {
    public static void main(String[] args) {
        // Scanner sc = new Scanner(System.in);
        // int numOfPlayers = promptNumPlayers(sc);
        // List<Player> players = createPlayers(sc, numOfPlayers);

        List<Noble> levelOneDeck = new ArrayList<>();
        try {
            levelOneDeck = loadNobles("nobles.csv");
            System.out.println(levelOneDeck.toString());
        } catch (InvalidFileException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int promptNumPlayers(Scanner sc) {
        int num = 0;
        while (num < 2 || num > 4) {
            try {
                System.out.print("How many players? (2 - 4): ");
                num = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a digit between 2 and 4\n");
                continue;
            }
            if (num < 2 || num > 4) {
                System.out.println("Please enter a digit between 2 and 4\n");
            }
        }
        return num;
    }

    public static List<Player> createPlayers(Scanner sc, int n) {
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.printf("Player %d name: ", i + 1);
            String name = sc.nextLine();
            players.add(new Player(name, i + 1));
        }

        return players;
    }

    public static List<Card> loadCards(String filename, int level) throws InvalidFileException {
        try (Scanner sc = new Scanner(new File(filename))) {
            sc.nextLine();
            List<Card> cards = new ArrayList<>();
            while (sc.hasNext()) {
                String[] cur = sc.nextLine().split(",");
                if (Integer.parseInt(cur[0]) != level) {
                    continue;
                }
                Map<GemColor, Integer> cost = new HashMap<>();
                cost.put(GemColor.DIAMOND, Integer.parseInt(cur[1]));
                cost.put(GemColor.ONYX, Integer.parseInt(cur[2]));
                cost.put(GemColor.EMERALD, Integer.parseInt(cur[3]));
                cost.put(GemColor.RUBY, Integer.parseInt(cur[4]));
                cost.put(GemColor.SAPPHIRE, Integer.parseInt(cur[5]));

                GemColor color = convertToColor(cur[6]);
                cards.add(new Card(level, Integer.parseInt(cur[7]), color, new Cost(cost)));
            }
            return cards;
        } catch (FileNotFoundException e) {
            throw new InvalidFileException(String.format("File (%s) not found!!", filename));
        }
    } 

    public static GemColor convertToColor(String color) {
        switch (color) {
            case "Diamond":
                return GemColor.DIAMOND;
            case "Onyx":
                return GemColor.ONYX;
            case "Emerald":
                return GemColor.EMERALD;
            case "Ruby":
                return GemColor.RUBY;
            default:
                return GemColor.SAPPHIRE;
        }
    }

    public static List<Noble> loadNobles(String filename) throws InvalidFileException {
        try (Scanner sc = new Scanner(new File(filename))) {
            sc.nextLine();
            List<Noble> nobles = new ArrayList<>();
            while (sc.hasNext()) {
                String[] cur = sc.nextLine().split(",");
                Map<GemColor, Integer> cost = new HashMap<>();
                cost.put(GemColor.DIAMOND, Integer.parseInt(cur[1]));
                cost.put(GemColor.ONYX, Integer.parseInt(cur[2]));
                cost.put(GemColor.EMERALD, Integer.parseInt(cur[3]));
                cost.put(GemColor.RUBY, Integer.parseInt(cur[4]));
                cost.put(GemColor.SAPPHIRE, Integer.parseInt(cur[5]));

                nobles.add(new Noble(cur[0], cost));
            }
            return nobles;
        } catch (FileNotFoundException e) {
            throw new InvalidFileException(String.format("File (%s) not found!!", filename));
        }
    } 

    public static GemCollection buildGemBank(int numPlayers) {
        s
    }
}