package learn.tripsplit.controllers;

import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.ResultType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    // Static method to build a response from a single message
    public static ResponseEntity<Object> build(String message) {
        return new ResponseEntity<>(
                new ErrorResponse(message),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    public static <T> ResponseEntity<Object> build(Result<T> result) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500

        if (result.getType() == null || result.getType() == ResultType.INVALID) {
            status = HttpStatus.BAD_REQUEST; // 400
        } else if (result.getType() == ResultType.NOT_FOUND) {
            status = HttpStatus.NOT_FOUND; //404
        } else if (result.getType() == ResultType.UNSUPPORTED) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE; // 415
        }

        return new ResponseEntity<>(result.getMessages(), status);
    }

}