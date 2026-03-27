public class CardChoice {
    private Card card;
    private int level;
    private int index;
    private boolean reserved;

    private CardChoice(Card card, int level, int index, boolean reserved) {
        this.card = card;
        this.level = level;
        this.index = index;
        this.reserved = reserved;
    }

    public static CardChoice createVisible(Card card, int level, int index) {
        return new CardChoice(card, level, index, false);
    }

    public static CardChoice createReserved(Card card, int index) {
        return new CardChoice(card, 0, index, true);
    }

    public Card getCard() {
        return card;
    }

    public int getLevel() {
        return level;
    }

    public int getIndex() {
        return index;
    }

    public boolean isReserved() {
        return reserved;
    }
}
