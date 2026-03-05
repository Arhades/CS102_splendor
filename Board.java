package com.splendor.model;

import java.util.List;
import java.util.Optional;

public final class Board {

    public static final int VISIBLE_PER_LEVEL = 4;

    private final Deck[] decks;
    private final List<Card>[] visible;
    private final List<Noble> nobles;
    private GemCollection bank;

    public Board(Deck level1Deck, Deck level2Deck, Deck level3Deck,
                 List<Noble> nobles, GemCollection initialBank) {}

    public List<Card> getVisibleCards(int level) { return null; }
    public boolean takeVisibleCard(Card card) { return false; }
    public Optional<Card> drawFromDeck(int level) { return null; }
    public int deckSize(int level) { return 0; }

    public List<Noble> getNobles() { return null; }
    public boolean takeNoble(Noble noble) { return false; }

    public GemCollection getBank() { return null; }
    public void withdrawFromBank(GemCollection amount) {}
    public void returnToBank(GemCollection amount) {}
}
