package splendor.network.server;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import splendor.config.*;
import splendor.display.*;
import splendor.entity.*;
import splendor.entity.bot.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.exception.*;
import splendor.rules.*;
import splendor.valueobjects.*;

public class ServerBotHandler {
    /**
     * Automatically executes bot turns in sequence until a human player's turn
     * is reached, or the game ends. Called after every successful move and
     * after game initialization.
     */
    public static void runBotTurns(GameState gameState, GameRules gameRules, List<ClientHandler> clients) {
        while (gameState != null && !gameState.isGameOver()) {
            Player currentPlayer = gameState.getCurrentPlayer();
            if (!(currentPlayer instanceof Bot)) {
                break;
            }

            Bot bot = (Bot) currentPlayer;
            String moveDescription = bot.takeTurn(gameState, gameRules);
            ServerHelper.broadcast("[BOT] " + moveDescription, clients);

            List<Noble> earnedNobles = gameRules.getClaimableNobles(currentPlayer, gameState.getAvailableNobles());
            for (Noble noble : earnedNobles) {
                currentPlayer.claimNoble(noble);
                gameState.removeNoble(noble);
                ServerHelper.broadcast(currentPlayer.getName() + " was visited by a Noble! (+" + noble.getPoints() + " points)", clients);
            }

            if (currentPlayer.getPoints() >= gameState.getWinningThreshold() && !SplendorServer.isLastRound) {
                SplendorServer.isLastRound = true;
                ServerHelper.broadcast(currentPlayer.getName() + " has reached " + gameState.getWinningThreshold() + " points!", clients);
            }

            List<Player> allPlayers = gameState.getPlayers();
            boolean isLastPlayer = allPlayers.indexOf(currentPlayer) == (allPlayers.size() - 1);

            if (SplendorServer.isLastRound && isLastPlayer) {
                String winnerScreen = DisplayUI.getWinner(gameState, gameRules).replace("\n", "@@");
                ServerHelper.broadcast(winnerScreen, clients);
                ServerHelper.broadcast("The game has ended! Thanks for playing.", clients);
                System.exit(0);
                return;
            }

            gameState.advanceToNext();
            ServerHelper.broadcastGameState(gameState, clients);
            ServerHelper.broadcast("It is now " + gameState.getCurrentPlayer().getName() + "'s turn.", clients);
        }
    }
}