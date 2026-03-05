package com.splendor.model;

import java.util.List;

public final class Player {

    public static final int MAX_GEMS = 10;
    public static final int MAX_RESERVED = 3;

    private final String id;
    private final String name;
    private GemCollection gems;
    private final List<Card> purchased;
    private final List<Card> reserved;
    private final List<Noble> nobles;

    public Player(String id, String name) {}

    public String getId() { return null; }
    public String getName() { return null; }

    public GemCollection getGems() { return null; }
    public void addGems(GemCollection toAdd) {}
    public void removeGems(GemCollection toRemove) {}
    public int totalGems() { return 0; }

    public GemCollection getBonuses() { return null; }

    public List<Card> getPurchased() { return null; }
    public void addPurchased(Card card) {}

    public List<Card> getReserved() { return null; }
    public boolean canReserve() { return false; }
    public void addReserved(Card card) {}
    public boolean removeReserved(Card card) { return false; }

    public List<Noble> getNobles() { return null; }
    public void addNoble(Noble noble) {}

    public int getPrestigePoints() { return 0; }
}
