package splendor.config;

import java.io.*;
import java.util.*;
import splendor.exception.*;

public class GameConfig {
    private String cardsFile;
    private String noblesFile;
    private int winningThreshold;
    private int twoPlayerGems;
    private int threePlayerGems;
    private int fourPlayerGems;
    private int goldGems;

    public GameConfig(String cardsFile, String noblesFile, int winningThreshold, int twoPlayerGems, int threePlayerGems, int fourPlayerGems, int goldGems) {
        this.cardsFile = cardsFile;
        this.noblesFile = noblesFile;
        this.winningThreshold = winningThreshold;
        this.twoPlayerGems = twoPlayerGems;
        this.threePlayerGems = threePlayerGems;
        this.fourPlayerGems = fourPlayerGems;
        this.goldGems = goldGems;
    }

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

    public String getCardsFile() {
        return cardsFile;
    }

    public String getNoblesFile() {
        return noblesFile;
    }

    public int getWinningThreshold() {
        return winningThreshold;
    }

    public int getGemCountPerColor(int numPlayers) {
        if (numPlayers == 2) {
            return twoPlayerGems;
        }
        if (numPlayers == 3) {
            return threePlayerGems;
        }
        return fourPlayerGems;
    }

    public int getGoldGems() {
        return goldGems;
    }

    private static String require(Properties properties, String key) throws InvalidFileException {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidFileException(String.format("Missing required configuration key: %s", key));
        }
        return value.trim();
    }

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