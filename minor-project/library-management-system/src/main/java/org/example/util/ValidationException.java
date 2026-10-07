package org.example.util;

/**
 * Custom runtime exception thrown when business validation fails.
 * Holds user-friendly messages suitable for display in GUI dialogs.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
