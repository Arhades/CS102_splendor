package splendor.network.server;

import java.util.*;
import splendor.display.*;
import splendor.entity.bot.*;
import splendor.entity.card.*;
import splendor.entity.player.*;
import splendor.rules.*;

/**
 * Handles automatic execution of bot turns on the server side.
 * When the current player is a Bot, this class runs their turns
 * in sequence until a human player's turn is reached or the game ends.
 */
public class ServerBotHandler {

    /**
     * Default constructor.
     */
    public ServerBotHandler() {}

    /**
     * Automatically executes bot turns in sequence until a human player's turn
     * is reached, or the game ends. Called after every successful human move and
     * after game initialization.
     *
     * @param gameState the current game state
     * @param gameRules the game rules for validation
     * @param clients   the list of connected clients for broadcasting
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

            // Handle noble claims for the bot
            List<Noble> earnedNobles = gameRules.getClaimableNobles(currentPlayer, gameState.getAvailableNobles());
            for (Noble noble : earnedNobles) {
                currentPlayer.claimNoble(noble);
                gameState.removeNoble(noble);
                ServerHelper.broadcast(currentPlayer.getName() + " was visited by a Noble! (+" + noble.getPoints() + " points)", clients);
            }

            // Check winning threshold
            if (currentPlayer.getPoints() >= gameState.getWinningThreshold() && !SplendorServer.isLastRound) {
                SplendorServer.isLastRound = true;
                ServerHelper.broadcast(currentPlayer.getName() + " has reached " + gameState.getWinningThreshold() + " points!", clients);
            }

            // Check if game should end (last round and last player)
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
