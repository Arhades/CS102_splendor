package splendor.entity;
/**
 * Enumeration of all possible player actions in the game.
 
 *   TAKE_THREE_DIFFERENT – take three gems of different colors
 *   TAKE_TWO_SAME – take two gems of the same color
 *   PURCHASE_CARD – buy a card using available gems
 *   RESERVE_CARD – reserve a card for future purchase
 * 
*/
public enum ActionType {
    /**
     * Take three gems of different colors from the gem bank.
     */
    TAKE_THREE_DIFFERENT,

    /**
     * Take two gems of the same color from the gem bank.
     */
    TAKE_TWO_SAME,
    
    /**
     * Purchase a development card using available gems.
     */
    PURCHASE_CARD,

    /**
     * Reserve a development card for future purchase.
     */
    RESERVE_CARD

    
}
