import java.util.*;

public class GameState {
    private List<Player> players;
    private int currentPlayerIndex;
    private CardMarket cardMarket;
    private GemCollection gemBank;
    private List<Noble> availableNobles;
    private boolean gameOver;
    private int winningThreshold;

    public GameState(List<Player> players, CardMarket cardMarket, GemCollection initialGems, List<Noble> initialNobles) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.cardMarket = cardMarket;
        this.gemBank = initialGems;
        this.availableNobles = initialNobles;
        this.gameOver = false;
        this.winningThreshold = 15;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public CardMarket getCardMarket() {
        return cardMarket;
    }

    public GemCollection getGemBank() {
        return gemBank;
    }

    public List<Noble> getAvailableNobles() {
        return availableNobles;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getWinningThreshold() {
        return winningThreshold;
    }

    public void advanceToNext() {
        currentPlayerIndex++;
        if (currentPlayerIndex == players.size()) {
            currentPlayerIndex = 0;
        }
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void removeNoble(Noble noble) {
        availableNobles.remove(noble);
    }

    public void addGemsToBank(GemCollection gems) {
        gemBank.add(gems);
    }

    public void removeGemsFromBank(GemCollection gems) {
        gemBank.subtract(gems);
    }

}