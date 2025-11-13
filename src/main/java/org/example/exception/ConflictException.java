package org.example.exception;

/** Бросаем, когда нарушено бизнес-правило (например, дубликат имени). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
    public static ConflictException of(String resource, String reason) {
        return new ConflictException(resource + " conflict: " + reason);
    }
}