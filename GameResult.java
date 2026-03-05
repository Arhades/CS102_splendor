package com.splendor.engine;

import com.splendor.model.Player;

import java.util.List;

public final class GameResult {

    private List<Player> winners;
    private List<Player> allPlayers;
    private int winningScore;
    private int winningCardCount;

    public GameResult(List<Player> winners, List<Player> allPlayers) {
        this.winners = winners;
        this.allPlayers = allPlayers;
    }

    public List<Player> getWinners() {
        return this.winners;
    }

    public List<Player> getAllPlayers() {
        return this.allPlayers;
    }

    public int getWinningScore() {
        int max = 0;
        for (Player player: winners) {
            
        }
    }
    public int getWinningCardCount() { return 0; }

    public boolean hasUniqueWinner() {
        if (this.winner.size() == 1) {
            return true;
        }
        return false;
    }
}
