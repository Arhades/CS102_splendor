import java.util.*;

public class BotCardMarket extends CardMarket {
    public BotCardMarket(List<Card> levelOneCards, List<Card> levelTwoCards, List<Card> levelThreeCards) {
        super(levelOneCards, levelTwoCards, levelThreeCards);
    }

    @Override
    public void splitVisible(List<Card> deck, List<Card> visible) {
        Random rand = new Random();
        while (visible.size() < 4 && deck.size() > 0) {
            int random = rand.nextInt(deck.size());
            visible.add(deck.get(random));
            deck.remove(random);
        }
    }
}
