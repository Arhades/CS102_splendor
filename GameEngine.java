package com.splendor.engine;

import com.splendor.model.Board;
import com.splendor.model.Card;
import com.splendor.model.GameState;
import com.splendor.model.Noble;
import com.splendor.model.Player;

import java.util.List;

public final class GameEngine {

    public static final int PRESTIGE_TO_WIN = 15;

    private final List<Player> players;
    private final Board board;
    private GameState state;
    private int currentPlayerIndex;
    private int lastRoundTriggerIndex;
    private GameResult result;

    public GameEngine(List<Player> players, Board board) {}

    public void startGame() {}
    public boolean isFinished() { return false; }
    public GameState getState() { return null; }

    public Player getCurrentPlayer() { return null; }
    public String getCurrentPlayerId() { return null; }
    public List<Player> getPlayers() { return null; }
    public Board getBoard() { return null; }

    public void applyAction(String playerId, TurnAction action) {}

    public GameResult getResult() { return null; }

    public List<Card> getAffordableCards(Player player) { return null; }
    public List<Noble> getEligibleNobles(Player player) { return null; }
}
