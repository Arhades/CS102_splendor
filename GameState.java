import java.util.*;

/**
 * The GameState class represents the current state of the game.
 * It keeps track of players, turn order, available resources,
 * nobles, and whether the game has ended.
 */
public class GameState {
    private List<Player> players;
    private int currentPlayerIndex;
    private CardMarket market;
    private GemCollection gemBank;
    private List<Noble> availableNobles;
    private boolean gameOver;
    private int winningThreshold;

    /**
     * Constructs a GameState with the given players, market,
     * initial gems, and nobles.
     *
     * @param players the list of players in the game
     * @param market the card market
     * @param initialGems the starting gems in the bank
     * @param initialNobles the starting available nobles
     */
    public GameState(List<Player> players, CardMarket market, GemCollection initialGems, List<Noble> initialNobles) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.market = market;
        this.gemBank = initialGems;
        this.availableNobles = initialNobles;
        this.gameOver = false;
        this.winningThreshold = 15;
    }

    /**
     * Returns the player whose turn it currently is.
     *
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Returns the index of the current player.
     *
     * @return the current player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Returns the list of all players.
     *
     * @return the list of players
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Returns the card market.
     *
     * @return the card market
     */
    public CardMarket getCardMarket() {
        return market;
    }

    /**
     * Returns the current gem bank.
     *
     * @return the gem bank
     */
    public GemCollection getGemBank() {
        return gemBank;
    }

    /**
     * Returns the list of available nobles.
     *
     * @return the available nobles
     */
    public List<Noble> getAvailableNobles() {
        return availableNobles;
    }

    /**
     * Checks whether the game has ended.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the number of points required to win the game.
     *
     * @return the winning threshold
     */
    public int getWinningThreshold() {
        return winningThreshold;
    }

    /**
     * Advances the game to the next player's turn.
     * Wraps around to the first player after the last.
     */
    public void advanceToNext() {
        currentPlayerIndex++;
        if (currentPlayerIndex == players.size()) {
            currentPlayerIndex = 0;
        }
    }

    /**
     * Sets whether the game is over.
     *
     * @param gameOver true if the game has ended, false otherwise
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Removes a noble from the list of available nobles.
     *
     * @param noble the noble to remove
     */
    public void removeNoble(Noble noble) {
        availableNobles.remove(noble);
    }

    /**
     * Adds gems back into the gem bank.
     *
     * @param gems the gems to add
     */
    public void addGemsToBank(GemCollection gems) {
        gemBank.add(gems);
    }

    /**
     * Removes gems from the gem bank.
     *
     * @param gems the gems to remove
     */
    public void removeGemsFromBank(GemCollection gems) {
        gemBank.subtract(gems);
    }
}
