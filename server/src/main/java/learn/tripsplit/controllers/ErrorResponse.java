package learn.tripsplit.controllers;

import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.ResultType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private List<String> details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, int status, List<String> details) {
        this(message, status);
        this.details = details;
    }

    public ErrorResponse(String message, int status) {
        this();
        this.message = message;
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public static ResponseEntity<Object> build(Result<?> result) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (result.getType() == ResultType.NOT_FOUND) {
            status = HttpStatus.NOT_FOUND;
        } else if (result.getType() == ResultType.INVALID) {
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed",
                status.value(),
                result.getMessages()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    public static ResponseEntity<Object> build(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(message, status.value());
        return new ResponseEntity<>(errorResponse, status);
    }

}