import java.util.*;

public class BotGameState extends GameState {
    private int turnCount;

    public BotGameState(List<Player> players, CardMarket cardMarket, GemCollection initialGems, List<Noble> initialNobles) {
        super(players, cardMarket, initialGems, initialNobles);
        this.turnCount = 0;
    }

    public int getTurnCount() {
        return turnCount;
    }

    @Override
    public void advanceToNext() {
        turnCount++;
        super.advanceToNext();
    }
}
