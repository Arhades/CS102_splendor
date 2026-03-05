package com.splendor.engine;

import com.splendor.model.Board;
import com.splendor.model.Player;

public final class ActionValidator {

    private ActionValidator() {}

    public static void validateTakeGems(TurnAction.TakeGems action, Player player, Board board) {}
    public static void validateReserveCard(TurnAction.ReserveCard action, Player player, Board board) {}
    public static void validatePurchaseCard(TurnAction.PurchaseCard action, Player player, Board board) {}
}
