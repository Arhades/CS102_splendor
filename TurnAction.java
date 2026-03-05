package com.splendor.engine;

import com.splendor.model.Card;
import com.splendor.model.GemCollection;

public abstract sealed class TurnAction
        permits TurnAction.TakeGems, TurnAction.ReserveCard, TurnAction.PurchaseCard {

    private TurnAction() {}

    // ── Static factories ──────────────────────────────────────────────────────

    public static TakeGems takeGems(GemCollection gemsToTake) { return null; }
    public static TakeGems takeGems(GemCollection gemsToTake, GemCollection gemsToDiscard) { return null; }
    public static ReserveCard reserveVisibleCard(Card card) { return null; }
    public static ReserveCard reserveFromDeck(int level) { return null; }
    public static PurchaseCard purchaseBoardCard(Card card) { return null; }
    public static PurchaseCard purchaseReservedCard(Card card) { return null; }

    // ── TakeGems ──────────────────────────────────────────────────────────────

    public static final class TakeGems extends TurnAction {
        private final GemCollection gemsToTake;
        private final GemCollection gemsToDiscard;

        private TakeGems(GemCollection gemsToTake, GemCollection gemsToDiscard) {}

        public GemCollection getGemsToTake() { return null; }
        public GemCollection getGemsToDiscard() { return null; }
    }

    // ── ReserveCard ───────────────────────────────────────────────────────────

    public static final class ReserveCard extends TurnAction {
        private final Card card;
        private final boolean fromDeck;
        private final int deckLevel;

        private ReserveCard(Card card, boolean fromDeck, int deckLevel) {}

        public Card getCard() { return null; }
        public boolean isFromDeck() { return false; }
        public int getDeckLevel() { return 0; }
    }

    // ── PurchaseCard ──────────────────────────────────────────────────────────

    public static final class PurchaseCard extends TurnAction {
        private final Card card;
        private final boolean fromReserve;

        private PurchaseCard(Card card, boolean fromReserve) {}

        public Card getCard() { return null; }
        public boolean isFromReserve() { return false; }
    }
}
