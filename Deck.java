package com.splendor.model;

import java.util.List;
import java.util.Optional;

public final class Deck {

    private final int level;

    public Deck(int level, List<Card> cards) {}

    public int getLevel() { return 0; }
    public int size() { return 0; }
    public boolean isEmpty() { return false; }
    public Optional<Card> draw() { return null; }
    public Optional<Card> peek() { return null; }
}
