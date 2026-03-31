package splendor.rules;

import splendor.entity.card.*;

/**
 * Represents a card choice, either from the visible market or from a player's reserved hand.
 */
public class CardChoice {
    private DevelopmentCard card;
    private int level;
    private int index;
    private boolean reserved;

    /**
     * Constructs a CardChoice.
     *
     * @param card     the development card being chosen
     * @param level    the market level (0 if reserved)
     * @param index    the index of the card in the visible row or reserved list
     * @param reserved true if the card comes from the player's reserved hand
     */
    private CardChoice(DevelopmentCard card, int level, int index, boolean reserved) {
        this.card = card;
        this.level = level;
        this.index = index;
        this.reserved = reserved;
    }

    /**
     * Creates a CardChoice representing a visible market card.
     *
     * @param card  the development card
     * @param level the market level (1, 2, or 3)
     * @param index the index of the card in the visible row
     * @return a new CardChoice for a visible card
     */
    public static CardChoice createVisible(DevelopmentCard card, int level, int index) {
        return new CardChoice(card, level, index, false);
    }

    /**
     * Creates a CardChoice representing a reserved card.
     *
     * @param card  the development card
     * @param index the index of the card in the player's reserved list
     * @return a new CardChoice for a reserved card
     */
    public static CardChoice createReserved(DevelopmentCard card, int index) {
        return new CardChoice(card, 0, index, true);
    }

    /**
     * Returns the development card.
     *
     * @return the card
     */
    public DevelopmentCard getCard() {
        return card;
    }

    /**
     * Returns the market level of this choice.
     *
     * @return the level (0 if reserved)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the index of the card.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns whether this card choice is from the reserved hand.
     *
     * @return true if reserved, false if from the visible market
     */
    public boolean isReserved() {
        return reserved;
    }
}
