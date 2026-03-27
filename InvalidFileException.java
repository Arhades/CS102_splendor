import java.io.*;

/**
 * Exception thrown when there is an issue with reading or processing a file.
 * This could occur if the file format is invalid, corrupted, or does not meet
 * the expected structure.
 */
public class InvalidFileException extends Exception {

    /**
     * Constructs a new InvalidFileException with a specified error message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public InvalidFileException(String message) {
        super(message);
    }
}
