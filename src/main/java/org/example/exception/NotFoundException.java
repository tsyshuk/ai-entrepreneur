package org.example.exception;

/**
 * Бросаем, когда ресурс (Project/User/и т.п.) не найден.
 * Сообщение делай человекочитаемым: "Project 123 not found".
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " " + id + " not found");
    }
}