package valueobjects;

import java.util.*;
import entities.*;


public class CardMarket {
    private List<Card> levelOneVisible;
    private List<Card> levelOneDeck;
    private List<Card> levelTwoVisible;
    private List<Card> levelTwoDeck;
    private List<Card> levelThreeVisible;
    private List<Card> levelThreeDeck;

    public CardMarket(List<Card> levelOneCards, List<Card> levelTwoCards, List<Card> levelThreeCards) {
        levelOneVisible = splitVisible(levelOneCards);
        levelTwoVisible = splitVisible(levelTwoCards);
        levelThreeVisible = splitVisibile(levelThreeCards);
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
            case 1 -> size = levelOneDeck.get(index);
            case 2 -> size = levelTwoDeck.get(index);
            case 3 -> size = levelThreeDeck.get(index);
            default -> throw new InvalidIndexException("Deck level not valid");
        }
        return size;
    }

    public Card drawCard(int level) {
        switch (level) {
            case 1:
                if (levelOneDeck.size() == 0) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                return levelTwoDeck.get(index);
            case 2:
                if (levelTwoDeck.size() == 0) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                return levelTwoDeck.get(index);
            case 3:
                if (levelThreeDeck.size() == 0) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                return levelThreeDeck.get(index);
            default -> throw new InvalidIndexException("Deck level not valid");
        }
    }

    public void removeAndReplaceCard(int level, int index) {
        switch (level) {
            case 1:
                levelOneVisible.set(index, levelOneDeck.drawCard(1));
                break;
            case 2:
                levelTwoVisible.set(index, levelTwoDeck.drawCard(2));
                break;
            case 3:
                levelThreeVisible.set(index, levelThreeDeck.drawCard(3));
                break;
            default -> throw new InvalidIndexException("Card level not valid");
        }

    }
}