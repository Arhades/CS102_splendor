import java.util.*;
public class CardMarket {
    private List<Card> levelOneVisible = new ArrayList<>();
    private List<Card> levelOneDeck;
    private List<Card> levelTwoVisible = new ArrayList<>();
    private List<Card> levelTwoDeck;
    private List<Card> levelThreeVisible = new ArrayList<>();
    private List<Card> levelThreeDeck;

    public CardMarket(List<Card> levelOneCards, List<Card> levelTwoCards, List<Card> levelThreeCards) {
        // levelOneVisible = splitVisible(levelOneCards);
        // levelTwoVisible = splitVisible(levelTwoCards);
        // levelThreeVisible = splitVisible(levelThreeCards);
        // levelOneDeck = splitDeck(levelOneCards);
        // levelTwoDeck = splitDeck(levelTwoCards);
        // levelThreeDeck = splitDeck(levelThreeCards);

        levelOneDeck = levelOneCards;
        levelTwoDeck = levelTwoCards;
        levelThreeDeck = levelThreeCards;
        splitVisible(levelOneDeck, levelOneVisible);
        splitVisible(levelTwoDeck, levelTwoVisible);
        splitVisible(levelThreeDeck, levelThreeVisible);
    }
    
    public void splitVisible(List<Card> deck, List<Card> visible) {
        Random rand = new Random();
        while (visible.size() != 4) {
            int random = rand.nextInt(deck.size());

            visible.add(deck.get(random));
            deck.remove(random);
        }
    }

    // private List<Card> splitDeck(List<Card> cards) {
    //     List<Card> cardDeck = new ArrayList<>();
    //     for (Card c : cards) {
    //         cardDeck.add(c);
    //     }
    //     return cardDeck;
    // }

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

    public List<Card> getDeckCards(int level) throws UnavailableCardException {
        List<Card> cards = null;
        switch (level) {
            case 1 -> cards = levelOneDeck;
            case 2 -> cards = levelTwoDeck;
            case 3 -> cards = levelThreeDeck;
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
                Card card = levelOneDeck.get(0);
                levelOneDeck.remove(0);
                return card;
            case 2:
                if (levelTwoDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                card = levelTwoDeck.get(0);
                levelTwoDeck.remove(0);
                return card;
            case 3:
                if (levelThreeDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                card = levelThreeDeck.get(0);
                levelThreeDeck.remove(0);
                return card;
            default:
                throw new InvalidIndexException("Deck level not valid");
        }
    }

    // public void removeAndReplaceCard(int level, int index) {
    //     try {
    //         switch (level) {
    //             case 1:
    //                 levelOneVisible.set(index, drawCard(1));
    //                 break;
    //             case 2:
    //                 levelTwoVisible.set(index, drawCard(2));
    //                 break;
    //             case 3:
    //                 levelThreeVisible.set(index, drawCard(3));
    //                 break;
    //             default:
    //                 throw new InvalidIndexException("Card level not valid");
    //         }
    //     } catch (UnavailableCardException e) {
    //         System.out.println(e.getMessage());
    //     }

    // }

    public void removeCard(int level, int index) {
        List<Card> cards = null;
        switch (level) {
            case 1:
                cards = levelOneVisible;
                break;
            case 2:
                cards = levelTwoVisible;
                break;
            default:
                cards = levelThreeVisible;
        }

        cards.remove(index);
    }
}