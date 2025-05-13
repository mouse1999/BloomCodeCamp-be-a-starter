package com.hcc.exceptions.userexceptions;

public class InvalidUserAttributeException extends UserException{
    public InvalidUserAttributeException(String message) {
        super(message);
    }

    public InvalidUserAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}
