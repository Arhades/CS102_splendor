import java.util.*;
/**
 * Represents the card market in the game.
 * 
 * Manages visible cards and decks for three different levels.
 * Each level has a deck of cards and a set of visible cards available to players.
 */
public class CardMarket {
    private List<Card> levelOneVisible = new ArrayList<>();
    private List<Card> levelOneDeck;
    private List<Card> levelTwoVisible = new ArrayList<>();
    private List<Card> levelTwoDeck;
    private List<Card> levelThreeVisible = new ArrayList<>();
    private List<Card> levelThreeDeck;
    /**
     * Constructs a CardMarket with decks for each level.
     * Initializes the visible cards by randomly selecting 4 cards from each deck.
     *
     * @param levelOneCards the list of level 1 cards
     * @param levelTwoCards the list of level 2 cards
     * @param levelThreeCards the list of level 3 cards
     */
    public CardMarket(List<Card> levelOneCards, List<Card> levelTwoCards, List<Card> levelThreeCards) {
        levelOneDeck = levelOneCards;
        levelTwoDeck = levelTwoCards;
        levelThreeDeck = levelThreeCards;
        // levelOneVisible.add(levelOneDeck.get(0));
        // levelOneVisible.add(levelOneDeck.get(1));
        // levelOneVisible.add(levelOneDeck.get(2));
        splitVisible(levelOneDeck, levelOneVisible);
        splitVisible(levelTwoDeck, levelTwoVisible);
        splitVisible(levelThreeDeck, levelThreeVisible);
    }
    
    /**
     * Randomly selects 4 cards from the given deck and moves them into the visible list.
     * @param deck the deck of cards to draw from
     * @param visible the list to store visible cards
     */
    
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
    /**
     * Returns the list of visible cards for the specified level.
     *
     * @param level the card level (1, 2, or 3)
     * @return the list of visible cards at the specified level
     * @throws InvalidIndexException if the level is not valid
     */
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

    /**
     * Returns the deck of cards for the specified level.
     *
     * @param level the card level (1, 2, or 3)
     * @return the list of cards in the deck
     * @throws InvalidIndexException if the level is not valid
     */

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

    /**
     * Returns a specific visible card from the given level and index.
     *
     * @param level the card level (1, 2, or 3)
     * @param index the index of the card in the visible list
     * @return the selected card
     * @throws InvalidIndexException if the level is not valid
     */

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
    /**
     * Returns the number of cards remaining in the deck for the specified level.
     *
     * @param level the card level (1, 2, or 3)
     * @return the number of cards in the deck
     * @throws InvalidIndexException if the level is not valid
     */
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
    /**
     * Draws a random card from the specified deck level.
     * The card is removed from the deck.
     *
     * @param level the card level (1, 2, or 3)
     * @return the drawn card
     * @throws UnavailableCardException if the deck is empty
     * @throws InvalidIndexException if the level is not valid
     */
    public Card drawCard(int level) throws UnavailableCardException {
        Random rand = new Random();
        switch (level) {
            case 1:
                if (levelOneDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                int random = rand.nextInt(levelOneDeck.size());
                Card card = levelOneDeck.get(random);
                levelOneDeck.remove(random);
                return card;
            case 2:
                if (levelTwoDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                random = rand.nextInt(levelTwoDeck.size());
                card = levelTwoDeck.get(random);
                levelTwoDeck.remove(random);
                return card;
            case 3:
                if (levelThreeDeck.isEmpty()) {
                    throw new UnavailableCardException("No more card left in deck");
                }
                random = rand.nextInt(levelThreeDeck.size());
                card = levelThreeDeck.get(random);
                levelThreeDeck.remove(random);
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
    /**
     * Removes a visible card at the specified level and index.
     *
     * @param level the card level (1, 2, or 3)
     * @param index the index of the card to remove
     */
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

    public String getMarketAsString() {
        StringBuilder sb = new StringBuilder();
        try {
            for (int level = 1; level <= 3; level++) {
                sb.append("L").append(level).append(":");
                
                // Get visible cards for this level
                List<Card> visible = getVisibleCards(level);
                for (Card c : visible) {
                    if (c != null) {
                        // Send minimal data: Color and Points (e.g., "RUBY-2")
                        sb.append(c.getBonus().name()).append("-").append(c.getPoints()).append(",");
                    } else {
                        sb.append("EMPTY,");
                    }
                }
                // Remove trailing comma, add a semicolon to separate levels
                if (sb.length() > 0 && sb.charAt(sb.length()-1) == ',') {
                    sb.setLength(sb.length() - 1);
                }
                sb.append(";");
            }
        } catch (UnavailableCardException e) {
            System.out.println("Invalid Level");
        }
        
        
        return sb.toString();
    }
}
