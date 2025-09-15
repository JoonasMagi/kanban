package ee.vikk.kanban.service;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends Exception {
    
    /**
     * Constructor with message
     * @param message Error message
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message Error message
     * @param cause Underlying cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
