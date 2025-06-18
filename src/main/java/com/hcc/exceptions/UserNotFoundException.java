package com.hcc.exceptions;

import com.hcc.exceptions.userexceptions.UserException;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
