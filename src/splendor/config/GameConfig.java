package splendor.config;

import java.io.*;
import java.util.*;
import splendor.exception.*;

/**
 * Stores configuration settings for the game.
 */
public class GameConfig {
    private String cardsFile;
    private String noblesFile;
    private int winningThreshold;
    private int twoPlayerGems;
    private int threePlayerGems;
    private int fourPlayerGems;
    private int goldGems;

    /**
     * Constructs a GameConfig with the specified settings.
     *
     * @param cardsFile        the filename for the cards CSV
     * @param noblesFile       the filename for the nobles CSV
     * @param winningThreshold the points needed to win
     * @param twoPlayerGems    the gem count per color for 2-player games
     * @param threePlayerGems  the gem count per color for 3-player games
     * @param fourPlayerGems   the gem count per color for 4-player games
     * @param goldGems         the number of gold joker gems
     */
    public GameConfig(String cardsFile, String noblesFile, int winningThreshold, int twoPlayerGems, int threePlayerGems, int fourPlayerGems, int goldGems) {
        this.cardsFile = cardsFile;
        this.noblesFile = noblesFile;
        this.winningThreshold = winningThreshold;
        this.twoPlayerGems = twoPlayerGems;
        this.threePlayerGems = threePlayerGems;
        this.fourPlayerGems = fourPlayerGems;
        this.goldGems = goldGems;
    }

    /**
     * Loads a GameConfig from a properties file on the classpath.
     *
     * @param filename the properties file name to locate on the classpath
     * @return a fully initialized GameConfig
     * @throws InvalidFileException if the file cannot be found or parsed
     */
    public static GameConfig load(String filename) throws InvalidFileException {
        Properties properties = new Properties();
        InputStream input = GameConfig.class.getClassLoader().getResourceAsStream(filename);
        if (input == null) {
            throw new InvalidFileException(String.format("Configuration file (%s) not found!", filename));
        }
        try (InputStream in = input) {
            properties.load(in);
        } catch (IOException e) {
            throw new InvalidFileException(String.format("Unable to read configuration file (%s)", filename));
        }

        String cardsFile = require(properties, "cards.file");
        String noblesFile = require(properties, "nobles.file");
        int winningThreshold = parsePositiveInt(properties, "winning.threshold", 15);
        int twoPlayerGems = parsePositiveInt(properties, "gems.two.players", 4);
        int threePlayerGems = parsePositiveInt(properties, "gems.three.players", 5);
        int fourPlayerGems = parsePositiveInt(properties, "gems.four.players", 7);
        int goldGems = parseNonNegativeInt(properties, "gems.gold", 5);

        return new GameConfig(cardsFile, noblesFile, winningThreshold, twoPlayerGems, threePlayerGems, fourPlayerGems, goldGems);
    }

    /**
     * Returns the filename for the cards CSV.
     *
     * @return the cards filename
     */
    public String getCardsFile() {
        return cardsFile;
    }

    /**
     * Returns the filename for the nobles CSV.
     *
     * @return the nobles filename
     */
    public String getNoblesFile() {
        return noblesFile;
    }

    /**
     * Returns the points threshold required to win.
     *
     * @return the winning threshold
     */
    public int getWinningThreshold() {
        return winningThreshold;
    }

    /**
     * Returns the number of gems per color based on the number of players.
     *
     * @param numPlayers the number of players (2, 3, or 4)
     * @return the gem count per color for the given player count
     */
    public int getGemCountPerColor(int numPlayers) {
        if (numPlayers == 2) {
            return twoPlayerGems;
        }
        if (numPlayers == 3) {
            return threePlayerGems;
        }
        return fourPlayerGems;
    }

    /**
     * Returns the number of gold joker gems.
     *
     * @return the gold gem count
     */
    public int getGoldGems() {
        return goldGems;
    }

    /**
     * Retrieves a required property value, throwing if missing or blank.
     *
     * @param properties the properties object to read from
     * @param key        the property key
     * @return the trimmed property value
     * @throws InvalidFileException if the key is missing or blank
     */
    private static String require(Properties properties, String key) throws InvalidFileException {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidFileException(String.format("Missing required configuration key: %s", key));
        }
        return value.trim();
    }

    /**
     * Parses a positive integer from properties, using a default if absent.
     *
     * @param properties   the properties object to read from
     * @param key          the property key
     * @param defaultValue the default value if the key is missing
     * @return the parsed positive integer or the default
     * @throws InvalidFileException if the value is not a positive integer
     */
    private static int parsePositiveInt(Properties properties, String key, int defaultValue) throws InvalidFileException {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new InvalidFileException(String.format("Configuration key (%s) must be greater than zero", key));
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new InvalidFileException(String.format("Configuration key (%s) must be an integer", key));
        }
    }

    /**
     * Parses a non-negative integer from properties, using a default if absent.
     *
     * @param properties   the properties object to read from
     * @param key          the property key
     * @param defaultValue the default value if the key is missing
     * @return the parsed non-negative integer or the default
     * @throws InvalidFileException if the value is not a non-negative integer
     */
    private static int parseNonNegativeInt(Properties properties, String key, int defaultValue) throws InvalidFileException {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new InvalidFileException(String.format("Configuration key (%s) must be zero or greater", key));
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new InvalidFileException(String.format("Configuration key (%s) must be an integer", key));
        }
    }
}
