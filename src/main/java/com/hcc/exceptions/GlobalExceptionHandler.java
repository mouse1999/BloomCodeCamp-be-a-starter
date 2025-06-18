package com.hcc.exceptions;

import com.hcc.exceptions.assignmentexceptions.InvalidAssignmentNumberException;
import com.hcc.exceptions.userexceptions.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false),
                new Date() );


        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        logger.error("User not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false),
                new Date() );


        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
//InvalidAssignmentNumberException
@ExceptionHandler(InvalidAssignmentNumberException.class)
public ResponseEntity handleInvalidAssignmentNumberException(
        UserNotFoundException ex, WebRequest request) {
    logger.error("Invalid Number: {}", ex.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getDescription(false),
            new Date() );


    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        logger.error("User already exist: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false),
                new Date() );


        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


}
