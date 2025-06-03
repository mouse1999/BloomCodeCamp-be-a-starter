package com.hcc.exceptions.userexceptions;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    public UserAlreadyExistsException(Throwable cause, String message) {
        super(message, cause);
    }

}
