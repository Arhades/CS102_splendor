package com.splendor.exception;

public class SplendorException extends RuntimeException {

    public SplendorException(String message) {
        super(message);
    }

    public SplendorException(String message, Throwable cause) {
        super(message, cause);
    }
}
