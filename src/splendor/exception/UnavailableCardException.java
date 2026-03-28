package splendor.exception;

/**
 * Exception thrown when a requested card is not available.
 * This may occur when the card is not present in the market
 * or has already been taken by another player.
 */
public class UnavailableCardException extends Exception {

    /**
     * Constructs a new UnavailableCardException with a specified error message.
     *
     * @param msg the detail message explaining why the card is unavailable
     */
    public UnavailableCardException(String msg) {
        super(msg);
    }
}
