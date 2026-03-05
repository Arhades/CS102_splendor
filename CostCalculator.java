package com.splendor.engine;

import com.splendor.model.Card;
import com.splendor.model.GemCollection;
import com.splendor.model.Player;

public final class CostCalculator {

    private CostCalculator() {}

    public static boolean canAfford(Card card, Player player) { return false; }
    public static GemCollection computePayment(Card card, Player player) { return null; }
    public static int computeGoldRequired(Card card, Player player) { return 0; }
    public static GemCollection computeShortfall(Card card, Player player) { return null; }
}
