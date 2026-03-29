package splendor.exception;

/**
 * Exception thrown when an invalid index is used.
 * This may occur when accessing elements outside the valid range
 * of a data structure such as a list or array.
 */
public class InvalidIndexException extends RuntimeException {

    /**
     * Constructs a new InvalidIndexException with a specified error message.
     *
     * @param msg the detail message explaining the cause of the exception
     */
    public InvalidIndexException(String msg) {
        super(msg);
    }
}
