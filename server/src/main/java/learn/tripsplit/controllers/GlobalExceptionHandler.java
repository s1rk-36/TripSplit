package learn.tripsplit.controllers;

import learn.tripsplit.controllers.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity violation occurred";

        // figure out the specific exception
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof SQLIntegrityConstraintViolationException) {
            SQLIntegrityConstraintViolationException sqlEx = (SQLIntegrityConstraintViolationException) rootCause;
            String sqlMessage = sqlEx.getMessage();

            if (sqlMessage != null) {
                if (sqlMessage.contains("Duplicate entry")) {
                    message = "A record with this information already exists";
                } else if (sqlMessage.contains("foreign key constraint")) {
                    message = "Cannot perform this operation due to related data dependencies";
                } else if (sqlMessage.contains("cannot be null")) {
                    message = "Required field cannot be empty";
                } else if (sqlMessage.contains("Data too long")) {
                    message = "Data exceeds maximum allowed length";
                }
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                HttpStatus.BAD_REQUEST.value(),
                Arrays.asList("Please check your data and try again")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getReason() != null ? ex.getReason() : "An error occurred",
                ex.getStatus().value()
        );

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid request parameters",
                HttpStatus.BAD_REQUEST.value(),
                Arrays.asList(ex.getMessage())
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        System.err.println("Unexpected error occurred: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        ex.printStackTrace();

        ErrorResponse errorResponse = new ErrorResponse(
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Required request body is missing or malformed";

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                HttpStatus.BAD_REQUEST.value(),
                Arrays.asList("Please provide a valid request body")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMediaTypeNotSupportedException ex) {
        String message = "Media type not supported";

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                Arrays.asList("Can not support the media type.")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

}