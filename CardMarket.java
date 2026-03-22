import java.util.*;
public class CardMarket {
    private final List<Card> levelOneVisible;
    private final List<Card> levelOneDeck;
    private final List<Card> levelTwoVisible;
    private final List<Card> levelTwoDeck;
    private final List<Card> levelThreeVisible;
    private final List<Card> levelThreeDeck;

    public CardMarket(List<Card> levelOneCards, List<Card> levelTwoCards, List<Card> levelThreeCards) {
        levelOneVisible = splitVisible(levelOneCards);
        levelTwoVisible = splitVisible(levelTwoCards);
        levelThreeVisible = splitVisible(levelThreeCards);
        levelOneDeck = splitDeck(levelOneCards);
        levelTwoDeck = splitDeck(levelTwoCards);
        levelThreeDeck = splitDeck(levelThreeCards);
    }
    
    private List<Card> splitVisible(List<Card> cards) {
        List<Card> cardVisible = new ArrayList<>();
        for (Card c : cards) {
            if (cardVisible.size() == 4) {
                break;
            }
            cardVisible.add(c);
        }
        return cardVisible; 
    }

    private List<Card> splitDeck(List<Card> cards) {
        List<Card> cardDeck = new ArrayList<>();
        for (Card c : cards) {
            cardDeck.add(c);
        }
        return cardDeck;
    }

    public List<Card> getVisibleCards(int level) throws UnavailableCardException {
        List<Card> cards = null;
        switch (level) {
            case 1 -> cards = levelOneVisible;
            case 2 -> cards = levelTwoVisible;
            case 3 -> cards = levelThreeVisible;
            default -> throw new InvalidIndexException("Card level not valid");
        }
        return cards;
    }

    public Card getVisibleCard(int level, int index) {
        Card card = null;
        switch (level) {
            case 1 -> card = levelOneVisible.get(index);
            case 2 -> card = levelTwoVisible.get(index);
            case 3 -> card = levelThreeVisible.get(index);
            default -> throw new InvalidIndexException("Card level not valid");
        }
        return card;
    }

    public int getDeckSize(int level) {
        int size = 0;
        switch (level) {
            case 1 -> size = levelOneDeck.size();
            case 2 -> size = levelTwoDeck.size();
            case 3 -> size = levelThreeDeck.size();
            default -> throw new InvalidIndexException("Deck level not valid");
        }
        return size;
    }

    public Card drawCard(int level) throws UnavailableCardException {
        switch (level) {
            case 1:
                if (levelOneDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                return levelOneDeck.get(0);
            case 2:
                if (levelTwoDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                return levelTwoDeck.get(0);
            case 3:
                if (levelThreeDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                return levelThreeDeck.get(0);
            default:
                throw new InvalidIndexException("Deck level not valid");
        }
    }

    public void removeAndReplaceCard(int level, int index) {
        try {
            switch (level) {
                case 1:
                    levelOneVisible.set(index, drawCard(1));
                    break;
                case 2:
                    levelTwoVisible.set(index, drawCard(2));
                    break;
                case 3:
                    levelThreeVisible.set(index, drawCard(3));
                    break;
                default:
                    throw new InvalidIndexException("Card level not valid");
            }
        } catch (UnavailableCardException e) {
            System.out.println(e.getMessage());
        }

    }
}